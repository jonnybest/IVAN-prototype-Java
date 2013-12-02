package edu.kit.ipd.alicenlp.ivan.analyzers;

import static edu.stanford.nlp.util.logging.Redwood.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.lang.model.type.ErrorType;

import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.data.CoreferenceSpan;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule;
import edu.kit.ipd.alicenlp.ivan.rules.PrepositionalRule;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.Dictionaries.Animacy;
import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.logging.Redwood;

/**
 * This analyzer finds Declarations, Locations and Direction in sentences. It
 * also creates a state object, which saves that information for future
 * processing.
 * 
 * @author Jonny
 * 
 */
public class DeclarationPositionFinder extends IvanAnalyzer {

	/**
	 * This field contains the names and entities. The rules are: only one state
	 * per analyzer.
	 */
	final private InitialState mystate = new InitialState();
	static private DeclarationPositionFinder myinstance = null;
	private Dictionary mydictionary;
	private StanfordCoreNLP mypipeline = null;

	/**
	 * The default constructor for creating a new instance.
	 * 
	 */
	public DeclarationPositionFinder() {
		// this creates a wordnet dictionary
		setupWordNet();
		// this creates the corenlp pipeline
		setupCoreNLP();

		// static reference for no real reason
		if (myinstance == null) {
			myinstance = this;
		}

		// this class is also in charge of keeping a state, but I really only
		// want one state per analyzer.
	}

	/**
	 * This is the constructor for usage with the Stanford CoreNLP pipeline.
	 * Annotations can be assumed. WordNet must be provided.
	 * 
	 * @param string
	 * @param properties
	 */
	public DeclarationPositionFinder(String string, Properties properties) {
		// empty
	}

	protected Annotation annotate(String text) {
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		mypipeline.annotate(document);
		System.out.println("{annotation is now done}"); //$NON-NLS-1$
		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#getDictionary()
	 */

	public Dictionary getDictionary() {
		return mydictionary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#getPipeline()
	 */
	public StanfordCoreNLP getPipeline() {
		return mypipeline;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#setDictionary(net.sf.extjwnl
	 * .dictionary.Dictionary)
	 */

	public void setDictionary(Dictionary dictionary) {
		this.mydictionary = dictionary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#setPipeline(edu.stanford
	 * .nlp.pipeline.StanfordCoreNLP)
	 */

	public void setPipeline(StanfordCoreNLP mypipeline) {
		this.mypipeline = mypipeline;
	}

	/**
	 * 
	 */
	protected StanfordCoreNLP setupCoreNLP() {
		StanfordCoreNLP pipeline;
		if (mypipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternativ: wsj-bidirectional
			try {
				props.put(
						"pos.model",
						"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// konfiguriere pipeline
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); //$NON-NLS-1$ //$NON-NLS-2$
			pipeline = new StanfordCoreNLP(props);
			mypipeline = pipeline;
		} else {
			pipeline = mypipeline;
		}
		return pipeline;
	}

	/**
	 * 
	 */
	protected void setupWordNet() {
		// set up properties file
		String propsFile = "file_properties.xml";
		FileInputStream properties = null;
		try {
			properties = new FileInputStream(propsFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// create a dictionary and run the analytics
		try {

			// run
			if (mydictionary == null) {
				// new style, instance dictionary
				mydictionary = Dictionary.getInstance(properties);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Set the instance for this class
	 * 
	 * @param myinstance
	 *            the myinstance to set
	 */
	public static void setInstance(DeclarationPositionFinder myinstance) {
		DeclarationPositionFinder.myinstance = myinstance;
	}

	public static DeclarationPositionFinder getInstance() {
		if (myinstance == null) {
			DeclarationPositionFinder.myinstance = new DeclarationPositionFinder();
		}
		return myinstance;
	}

	/**
	 * This method attempts to get all the available information out of a single
	 * sentence.
	 * 
	 * @param sentence
	 * @return
	 */
	public static List<EntityInfo> findAll(CoreMap sentence) {
		ArrayList<EntityInfo> infos = new ArrayList<EntityInfo>();
		/*
		 * If this was perfect, this is how'd you find all infos: 1. learn all
		 * the names 2. learn all the locations 3. learn all the directions 4.
		 * for all names, see if there is an associated location and direction
		 * and if yes 5. build a new EntityInfo with the available information
		 * and return it
		 * 
		 * But instead, we only find out all the names and for all the names we
		 * add location and direction and return.
		 */
		try {
			// get ALL the names
			List<String> names = recogniseNames(sentence);
			// get THE location for this sentence. yes, this assumes they're all
			// standing in the same spot
			EntityInfo locs = getLocation(sentence);
			// get THE direction for all the entities in this sentence. yes,
			// this assumes they're all facing the same way
			EntityInfo dirs = getDirection(sentence);
			// put the loc/dir info into new entityinfos
			for (String n : names) {
				EntityInfo info = new EntityInfo(n);
				if (hasLocation(sentence))
					info.setLocation(locs.getLocation());
				if (hasDirection(sentence))
					info.setDirection(dirs.getDirection());
				// set name range
				Span range = locateRange(n, sentence);
				info.setEntitySpan(range);
				// set name range relative to the sentence (for coreference)
				IntPair tuple;
				info.setCoreferenceSpan(new CoreferenceSpan(sentence.get(CoreAnnotations.SentenceIndexAnnotation.class), tuple));
				// set named flag (NNP)
				CoreLabel lbl = locateLastToken(n, sentence);
				info.setIsProperName(BaseRule.isPOSFamily(lbl, "NNP"));
				// set Pronoun flag (PRP)
				info.setIsPronoun(BaseRule.isPOSFamily(lbl, "PRP"));
				// add things
				infos.add(info);
			}
		} catch (IvanException e) {
			// not sure what to do now?
			e.printStackTrace();
		}

		return infos;
	}

	/**
	 * This method finds the first occurence of a string in a document.
	 * Basically it works like String.indexOf(String), but it returns an index
	 * for the whole document, instead of only this sentence.
	 * 
	 * @param n
	 *            The search string
	 * @param sentence
	 * @return The "absolute" position of the search string in this sentence.
	 */
	private static Span locateRange(String n, CoreMap sentence) {
		String[] tokens = n.split(" ");
		List<String> tokenslist = Arrays.asList(tokens);
		Collections.reverse(tokenslist);
		for (String s : tokenslist) {
			int begin;
			for (CoreLabel originalToken : sentence.get(TokensAnnotation.class)) {
				if (s.equals(originalToken.originalText())) {
					begin = originalToken
							.get(CharacterOffsetBeginAnnotation.class);
					return Span.fromValues(begin, begin + n.length());
				}
			}
		}
		// the search string is not present in the given sentence
		log(Redwood.WARN,
				"the search string is not present in the given sentence");
		return null;
	}
	
	/**
	 * This method finds the first occurence of a string in a document.
	 * It returns the token-based index for the whole document.
	 * If you need a character-based index, use <code>locateRange()</code> instead.
	 * 
	 * @param n
	 *            The search string
	 * @param sentence
	 * @return The "absolute" position of the search string in this sentence.
	 */
	private static Span locateIndex(String n, CoreMap sentence) {
		
		String[] tokens = n.split(" "); // this is wrong. I should have used the Stanford tokenizer to do that
		
		List<String> tokenslist = Arrays.asList(tokens);
		Collections.reverse(tokenslist);
		for (String s : tokenslist) {
			for (CoreLabel originalToken : sentence.get(TokensAnnotation.class)) {
				if (s.equals(originalToken.originalText())) {
					// we have found the proper token
					// now calculate how big our original span must be
					int countTokensBackwards = tokens.length - 1;
					int index = originalToken.get(CoreAnnotations.TokenBeginAnnotation.class);
					int starttokenindex = index - countTokensBackwards;
					int endtokenindex = index;
					// create a span for our numbers
					return Span.fromValues(starttokenindex, /*non-inclusive!*/ endtokenindex + 1);
				}
			}
		}
		// the search string is not present in the given sentence
		log(Redwood.WARN,
				"the search string is not present in the given sentence");
		return null;
	}

	private static CoreLabel locateLastToken(String n, CoreMap sentence) {
		String[] tokens = n.split(" "); // this is wrong. I should have used the Stanford tokenizer to do that
		List<String> tokenslist = Arrays.asList(tokens);
		Collections.reverse(tokenslist);
		for (String s : tokenslist) {
			for (CoreLabel originalToken : sentence.get(TokensAnnotation.class)) {
				if (s.equals(originalToken.originalText())) {
					return originalToken;
				}
			}
		}
		// the search string is not present in the given sentence
		log(Redwood.WARN,
				"the search string is not present in the given sentence");
		return null;
	}

	private static boolean hasDirection(CoreMap sentence) {
		return getDirection(sentence) != null;
	}

	/**
	 * Indicates whether this sentence contains a location.
	 * 
	 * @param sentence
	 * @return True, if a location has been recognised.
	 */
	public static boolean hasLocation(CoreMap sentence) {
		return getLocation(sentence) != null;
	}

	/**
	 * Attempts to find a location in the given sentence.
	 * 
	 * @param sentence
	 *            A CoreMap to look inside
	 * @return An EntityInfo containing the location and the word it refers to.
	 *         Or {@code null} if none was found.
	 */
	public static EntityInfo getLocation(CoreMap sentence) {
		String entity, location = null;
		// the entity is most likely the subject(s) of the sentence
		entity = BaseRule.getSubject(
				sentence.get(CollapsedCCProcessedDependenciesAnnotation.class))
				.word();

		PrepositionalRule inRule = new PrepositionalRule();
		if (inRule.apply(sentence)) {
			// entity = inRule.getWord().originalText(); // the entity is most
			// likely not the word, but the subject(s) of the sentence
			location = inRule.printModifiers();
		}

		return new EntityInfo(entity, location);
	}

	/**
	 * Attempts to find a direction in the given sentence.
	 * 
	 * @param sentence
	 *            A CoreMap to look inside
	 * @return An EntityInfo containing the direction and the word it refers to.
	 *         Or {@code null} if none was found.
	 */
	public static EntityInfo getDirection(CoreMap sentence) {
		String entity, direction = null;

		DirectionKeywordRule dRule = new DirectionKeywordRule();
		if (dRule.apply(sentence)) {
			entity = dRule.getSubject().word();
			direction = dRule.getDirection();
			return new EntityInfo(entity, null, direction);
		} else {
			return null;
		}
	}

	/**
	 * Finds out which entites are declared in this {@code sentence}. Right now,
	 * it simply recognises names.
	 * 
	 * @param sentence
	 * @return
	 * @throws IvanException
	 */
	public List<EntityInfo> getDeclarations(CoreMap sentence)
			throws IvanException {
		// NounRootRule nrrule = new NounRootRule();
		// if (nrrule.apply(sentence)) {
		// // TODO : get declared names and maybe map names to entities if
		// possible or maybe even more
		// }
		// assuming that this method only gets called when no previous
		// declarations were found,
		// it's probably safe to simply get everything that's in the subject
		// TODO: name -> entity mapping
		List<EntityInfo> infos = new ArrayList<EntityInfo>();
		List<String> names = recogniseNames(sentence);
		for (String n : names) {
			if (!mystate.containsName(n)) {
				infos.add(new EntityInfo(n));
			} else {
				infos.add(mystate.getSingle(n));
			}
		}
		return infos;
	}

	/**
	 * Searches the head of the sentence (subject and root) for nouns
	 * 
	 * @param sentence
	 * @return
	 * @throws IvanException
	 */
	public static List<String> recogniseNames(CoreMap sentence)
			throws IvanException {
		SemanticGraph graph = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord head = BaseRule.getSubject(graph);

		// if the nominal subject is a plural word (like "characters" or
		// "people"),
		// try extracting the stuff elsewhere
		if (head != null && head.tag().equals("NNS")) {
			// we start looking for other mentions with the root (unless it
			// already is the subject
			if (!head.equals(graph.getFirstRoot())) {
				head = graph.getFirstRoot();
			} else {
				// if not, try the direct objects instead
				head = graph.getChildWithReln(head,
						EnglishGrammaticalRelations.DIRECT_OBJECT);
			}
		}

		// try to do something with the sentence and hope it is meaningful:
		if (head == null) {
			// second try: use the root in subject-less sentences
			head = graph.getFirstRoot();
			// check again:
			if (head == null) {
				// I'm out of ideas
				return null;
			}
		}

		// extract names:
		// make space for names
		ArrayList<IndexedWord> namesIW = new ArrayList<IndexedWord>();
		// follow "and" and "or" conjunctions starting from the head
		ArrayList<String> names = BaseRule.resolveCc(head, sentence, namesIW);
		// resolve personal pronouns
		for (IndexedWord n : namesIW) {
			if (n.tag().equals("PRP")) {
				// this part doesn't even work:
				// CoreferenceResolver cresolver =
				// CoreferenceResolver.getInstance();
				// n.setValue(cresolver.resolve(n)); // this is probably a bad
				// idea
			}
		}
		return names;
	}

	/***
	 * This method decides whether the given names are already declared in the
	 * state.
	 * 
	 * @param names
	 * @return False, if at least one of the names is not declared yet
	 */
	public boolean isDeclared(List<String> names) {
		return mystate.containsAllNames(names);
	}

	public boolean isDeclared(String name) {
		return mystate.containsName(name);
	}

	/**
	 * Returns the internal state of the declarations
	 * 
	 * @return
	 */
	public InitialState getCurrentState() {
		return mystate;
	}

	/**
	 * Resets this classes' state to starting conditions.
	 */
	public void reset() {
		// FIXME: I really hope the entities don't leak
		mystate.clear();
	}

	/**
	 * Analyzes the given sentences and persists the result in the interal state
	 * 
	 * @throws IvanException
	 */
	public void learnDeclarations(CoreMap sentence) //throws IvanException 
	{
		// TODO implement learnDecl
		// learn names
		List<EntityInfo> things = findAll(sentence);
		for (EntityInfo n : things) {
			this.mystate.add(n);
			nop();
		}
	}

	private void nop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void annotate(Annotation annotation) {
		// Map<Integer, CorefChain> coref =
		// annotation.get(CorefChainAnnotation.class);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			// TODO: do stuff
			LocationListAnnotation list = findLocationAsTrees(sentence);
			if (!list.isEmpty())
				sentence.set(LocationListAnnotation.class, list);
			
			//learnDeclarations(sentence);
			learnAndCheckDeclarations(sentence, annotation);
			
		}
		annotation.set(IvanEntitiesAnnotation.class, getCurrentState());
	}

	/**
	 * This method learns declarations but prior to learning, it checks the
	 * found declaration against the following criteria: 1. is it a named
	 * entity? if yes, find its entity and create a new named entity object. 2.
	 * is it a pronoun? if yes, find its entity and merge information. 3. is it
	 * an unnamed entity? if yes, proceed as usual.
	 * 
	 * @param sentence
	 *            the sentence under analysis
	 * @param coref
	 *            the coreference information which is used to find the other
	 *            mentions
	 */
	private void learnAndCheckDeclarations(CoreMap sentence, Annotation document) {
		Map<Integer, CorefChain> coref = document
				.get(CorefChainAnnotation.class);
		// this is the implementation of "learn declarations"
		// List<EntityInfo> things = findAll(sentence);
		// for (EntityInfo n : things) {
		// this.mystate.add(n);
		// }

		// TODO implement name checking and recognition
		List<EntityInfo> things = findAll(sentence);
		for (EntityInfo info : things) {
			if (info.isProperName()) {
				// 1. find entity in created entities

				// 2. if entity not present, create named entity

			} else if (info.isPronoun()) {
				// resolve reference
				CorefChain reference = findMentionsForInfo(info, coref);

				// let's see if we found anything
				if (reference != null) {
					/**
					 * yupp. we can work with this. if this pronoun is gendered,
					 * we have a good chance that the coref annotation may be
					 * correct. so check the gender first. If there is no
					 * gender, the recognition may as well be wrong.
					 * 
					 * In this part, we need to find out which mention is the
					 * best one (it's the one with the name) and retrieve its
					 * associated EntiyInfo. If this info does not exist
					 * (because it has not yet been processed), we can create
					 * and add it, because the subsequent analysis will stumble
					 * upon our info and merge it then.
					 */

					// get the best mention
					CorefMention bestmention = findBestMention(reference);

					Gender g = bestmention.gender;
					if (g == Gender.FEMALE || g == Gender.MALE) {
						// everything is shiny						
					} 
					else {
						// this one is of neutral or unknown gender. the
						// previous analysis may be wrong.
						// TODO: fix the coreference analysis and replace it
						// with your own.

					}
					// work
					// 1. find the info for the best mention
					String alias = bestmention.mentionSpan;
					EntityInfo retrieved = mystate.getSingle(alias);
					if (retrieved == null) {
						// not present; create it
						retrieved = new EntityInfo(alias);
						// retrieved.setEntitySpan(Span.fromValues(0,1)); //
						// FIXME: this is lazy and wrong
					}

					// 2. merge our information with the information for the
					// best mention
					// that means, add our position or set the direction or
					// skip, if the direction is already set
					if (!retrieved.hasDirection())
						retrieved.setDirection(info.getDirection());

					if (!retrieved.hasLocation()) {
						retrieved.setLocation(info.getLocation());
					}
					// else: retrieved.addLocation(info.getLocation());
					// that function is not implemented (or neccessary)

					// 3. add again. if this call fails, we have already
					// modified the correct info
					mystate.add(retrieved);
					// done

				} else {
					// nope. this pronoun is utterly inexplicable
					// ERROR something something
					// TODO: try to wiggle your way out by finding a fitting
					// reference on your own. maybe try the subject of the
					// sentence?
					sentence.set(
							IvanAnnotations.SentenceClassificationAnnotation.class,
							Classification.ErrorDescription);
					new IvanErrorMessage(IvanErrorType.COREFERENCE,
							document.get(IDAnnotation.class),
							info.getEntitySpan(),
							"This pronoun does not refer to anything. "
									+ "Please use a name instead.");
				}
			} else {
				// this is the default case where we just add whatever name we
				// found to our list of known entities
				this.mystate.add(info);
			}
		}
	}

	/**
	 * find the best mention (according to our own standards)
	 * 
	 * @param reference
	 * @return getRepresentativeMention() or better
	 */
	private static CorefMention findBestMention(CorefChain reference) {
		//
		CorefMention bestmention = reference.getRepresentativeMention();

		for (CorefMention newmention : reference.getMentionsInTextualOrder()) {
			// compare (rank) two mentions
			if (bestmention.mentionType == MentionType.PROPER)
				break; // this is the best mention (we also found it last
						// round...)

			if (newmention.mentionType == MentionType.PRONOMINAL)
				continue; // this is the worst candidate; skip

			bestmention = newmention; // try again next round with a new mention
		}
		return bestmention;
	}

	/**
	 * this is the EntityInfo -> CorefChain search algorithm (in case you want
	 * to reuse it)
	 * 
	 * @param info
	 * @param coref
	 * @return NULL if none was found
	 */
	private static CorefChain findMentionsForInfo(EntityInfo info,
			Map<Integer, CorefChain> coref) {
		// our value
		CorefChain reference = null;
		// retrieve our own start and end
		CoreferenceSpan mypos = null;
		mypos = info.getCoreferenceSpan();

		for (Entry<Integer, CorefChain> entry : coref.entrySet()) {
			
			CorefChain chain = entry.getValue();
			// check each mention if it contains our declaration (our
			// pronoun)
			for (CorefMention mention : chain.getMentionsInTextualOrder()) {
				// retrieve the mention's start and end.
				// also convert it to the span format
				if(mention.sentNum == mypos.Sentence)
				{
					mention.position.equals(mypos.getTuple());
				}
				
//				Span candidatepos = Span.fromValues(mention.position.get(0),
//						mention.position.get(1));
//				// if one contains the other, the mentions overlap and
//				// we have a winner
//				if (mypos.contains(candidatepos)
//						|| candidatepos.contains(mypos)) {
//					// we have found it! this coref chain contains our
//					// mention
//					reference = chain;
//					break;
//				}
			}
		}
		return reference;
	}

	/**
	 * Returns the locations which are mentioned in this sentence or an empty
	 * list if none
	 * 
	 * @param sentence
	 * @return
	 */
	private LocationListAnnotation findLocationAsTrees(CoreMap sentence) {

		LocationListAnnotation ourlocs = new LocationListAnnotation();

		PrepositionalRule prepRule = new PrepositionalRule();
		if (prepRule.apply(sentence)) {
			for (Tree t : prepRule.getPrepositionalModifiers()) {
				LocationAnnotation someloc = new LocationAnnotation();
				someloc.setReferent(prepRule.getReferent());
				someloc.setLocation(t);
				ourlocs.add(someloc);
			}
		}

		return ourlocs;
	}

	@Override
	public Set<Requirement> requirementsSatisfied() {
		Set<Requirement> isatisfy = new HashSet<Annotator.Requirement>();
		isatisfy.add(DECLARATION_REQUIREMENT);
		isatisfy.add(LOCATION_REQUIREMENT);
		isatisfy.add(DIRECTION_REQUIREMENT);
		return isatisfy;
	}

	@Override
	public Set<Requirement> requires() {
		Set<Requirement> myreqs = new HashSet<Annotator.Requirement>();
		myreqs.addAll(TOKENIZE_SSPLIT_POS_LEMMA);
		myreqs.add(PARSE_REQUIREMENT);
		myreqs.add(DETERMINISTIC_COREF_REQUIREMENT);
		return myreqs;
	}

}
