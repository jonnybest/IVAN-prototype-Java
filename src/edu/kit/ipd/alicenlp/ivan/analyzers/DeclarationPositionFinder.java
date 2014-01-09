package edu.kit.ipd.alicenlp.ivan.analyzers;

import static edu.stanford.nlp.util.logging.Redwood.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.jcraft.jsch.Logger;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.rules.AliasByCorefRule;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule;
import edu.kit.ipd.alicenlp.ivan.rules.PrepositionalRule;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
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
import edu.stanford.nlp.util.logging.Redwood;

/** This analyzer finds Declarations, Locations and Direction in sentences.
 * It also creates a state object, which saves that information for future processing. 
 * 
 * @author Jonny
 *
 */
public class DeclarationPositionFinder extends IvanAnalyzer
{
	
	/** This field contains the names and entities. The rules are: only one state per analyzer. */ 
	private InitialState mystate = new InitialState();
	static private DeclarationPositionFinder myinstance = null;
	private Dictionary mydictionary;
	private StanfordCoreNLP mypipeline = null;

	/** The default constructor for creating a new instance.
	 * 
	 */
	public DeclarationPositionFinder() 
	{
		// this creates a wordnet dictionary
		setupWordNet();
		// this creates the corenlp pipeline
		setupCoreNLP();
		
		// static reference for no real reason
		if (myinstance == null) {
			myinstance = this;
		}
		
		// this class is also in charge of keeping a state, but I really only want one state per analyzer.
	}

	/** This is the constructor for usage with the Stanford CoreNLP pipeline. 
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

	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#getDictionary()
	 */
	
	public Dictionary getDictionary() {
		return mydictionary;
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#getPipeline()
	 */	
	public StanfordCoreNLP getPipeline() {
		return mypipeline;
	}


	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#setDictionary(net.sf.extjwnl.dictionary.Dictionary)
	 */
	
	public void setDictionary(Dictionary dictionary) {
		this.mydictionary = dictionary;
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#setPipeline(edu.stanford.nlp.pipeline.StanfordCoreNLP)
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
		    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		    Properties props = new Properties();
		    // alternativ: wsj-bidirectional 
		    try {
				props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger"); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		    // konfiguriere pipeline
		    props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); //$NON-NLS-1$ //$NON-NLS-2$
		    pipeline = new StanfordCoreNLP(props);	    
		    mypipeline = pipeline;
		}
		else {
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
				//new style, instance dictionary
				mydictionary = Dictionary.getInstance(properties);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
	}
	
	/** Set the instance for this class
	 * @param myinstance the myinstance to set
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

	/** This method attempts to get all the available information out of a single sentence. 
	 * @param sentence
	 * @return
	 */
	public static List<EntityInfo> findAll(CoreMap sentence) 
	{
		ArrayList<EntityInfo> infos = new ArrayList<EntityInfo>();
		/* If this was perfect, this is how'd you find all infos:
		 *   1. learn all the names
		 *   2. learn all the locations
		 *   3. learn all the directions
		 *   4. for all names, see if there is an associated location and direction and if yes
		 *   5. build a new EntityInfo with the available information and return it
		 *   
		 * But instead, we only find out all the names and for all the names we add location and direction and return.		
		 */
		try {
			// get ALL the names
			List<String> names = recogniseEntities(sentence);
			// get THE location for this sentence. yes, this assumes they're all standing in the same spot
			EntityInfo locs = getLocation(sentence);
			// get THE direction for all the entities in this sentence. yes, this assumes they're all facing the same way
			EntityInfo dirs = getDirection(sentence);
			// put the loc/dir info into new entityinfos
			for (String n : names) {
				EntityInfo info = new EntityInfo(n);
				if(hasLocation(sentence))
					info.setLocation(locs.getLocation());
				if(hasDirection(sentence))
					info.setDirection(dirs.getDirection());
				// set name range
				Span range = locateRange(n, sentence);
				info.setEntitySpan(range);
				// set NNP flag
				CoreLabel lbl = locateLastToken(n, sentence);
				info.setIsProperName(BaseRule.isPOSFamily(lbl, "NNP"));
				// add things
				infos.add(info);
			}			
		} catch (IvanException e) {
			// not sure what to do now?
			e.printStackTrace();
		}
		
		return infos;
	}
	
	/** This method finds the first occurence of a string in a document. 
	 * Basically it works like String.indexOf(String), but it returns an index
	 * for the whole document, instead of only this sentence.
	 *  
	 * @param n The search string
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
				if(s.equals(originalToken.originalText()))
				{
					begin = originalToken.get(CharacterOffsetBeginAnnotation.class);
					return Span.fromValues(begin, begin + n.length());
				}
			}
		}
		// the search string is not present in the given sentence
		log(Redwood.WARN, "the search string is not present in the given sentence");
		return null;
	}
	
	private static CoreLabel locateLastToken(String n, CoreMap sentence) {
		String[] tokens = n.split(" ");
		List<String> tokenslist = Arrays.asList(tokens);
		Collections.reverse(tokenslist);
		for (String s : tokenslist) {
			for (CoreLabel originalToken : sentence.get(TokensAnnotation.class)) {
				if(s.equals(originalToken.originalText()))
				{
					return originalToken;
				}
			}
		}
		// the search string is not present in the given sentence
		log(Redwood.WARN, "the search string is not present in the given sentence");
		return null;
	}

	private static boolean hasDirection(CoreMap sentence) {
		return getDirection(sentence) != null;
	}

	/**
	 * Indicates whether this sentence contains a location.
	 * @param sentence
	 * @return True, if a location has been recognised.
	 */
	public static boolean hasLocation(CoreMap sentence)
	{
		return getLocation(sentence) != null; 
	}

	/**
	 * Attempts to find a location in the given sentence. 
	 * @param sentence A CoreMap to look inside
	 * @return An EntityInfo containing the location and the word it refers to. Or {@code null} if none was found.
	 */
	public static EntityInfo getLocation(CoreMap sentence) 
	{
		String entity, location = null;
		// the entity is most likely the subject(s) of the sentence
		entity = BaseRule.getSubject(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class)).word();
		
		PrepositionalRule inRule = new PrepositionalRule();
		if (inRule.apply(sentence)) {
//			entity = inRule.getWord().originalText(); // the entity is most likely not the word, but the subject(s) of the sentence
			location = inRule.printModifiers();
		}
		
		return new EntityInfo(entity, location);
	}
	
	/**
	 * Attempts to find a direction in the given sentence. 
	 * @param sentence A CoreMap to look inside
	 * @return An EntityInfo containing the direction and the word it refers to. Or {@code null} if none was found.
	 */
	public static EntityInfo getDirection(CoreMap sentence) {
		String entity, direction = null;
		
		DirectionKeywordRule dRule = new DirectionKeywordRule();
		if (dRule.apply(sentence)) {
			entity = dRule.getSubject().word();
			direction = dRule.getDirection();
			return new EntityInfo(entity, null, direction);
		}
		else {
			return null;			
		}
	}
	

	/** Finds out which entites are declared in this {@code sentence}. 
	 * Right now, it simply recognises names. 
	 * @param sentence
	 * @return
	 * @throws IvanException 
	 */
	public List<EntityInfo> getDeclarations(CoreMap sentence) throws IvanException {
//		NounRootRule nrrule = new NounRootRule();
//		if (nrrule.apply(sentence)) {
//			// TODO : get declared names and maybe map names to entities if possible or maybe even more
//		}
		// assuming that this method only gets called when no previous declarations were found, 
		// it's probably safe to simply get everything that's in the subject
		// TODO: name -> entity mapping
		List<EntityInfo> infos = new ArrayList<EntityInfo>();
		List<String> names = recogniseEntities(sentence);
		for (String n : names) {
			if (!mystate.containsName(n)) {
				infos.add(new EntityInfo(n));				
			}
			else {
				infos.add(mystate.getSingle(n));
			}
		}
		return infos;
	}

	/** Searches the head of the sentence (subject and root) for nouns
	 * @param sentence
	 * @return
	 * @throws IvanException 
	 */
	public static List<String> recogniseEntities(CoreMap sentence) throws IvanException {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord head = BaseRule.getSubject(graph);
		
		// if the nominal subject is a plural word (like "characters" or "people"),
		// try extracting the stuff elsewhere
		if(head != null && head.tag().equals("NNS"))
		{
			// we start looking for other mentions with the root (unless it already is the subject
			if(!head.equals(graph.getFirstRoot()))
			{
				head = graph.getFirstRoot();
				log(Redwood.DBG, "Name recognition selected head: " + head);
			}
			else 
			{
				// if not, try the direct objects instead
				head = graph.getChildWithReln(head, EnglishGrammaticalRelations.DIRECT_OBJECT);
				
				log(Redwood.DBG, "Name recognition is falling back on direct object: " + head);
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
			if(n.tag().equals("PRP"))
			{
				log(Redwood.DBG, "Name is a preposition.");
				// this part doesn't even work:
//				CoreferenceResolver cresolver = CoreferenceResolver.getInstance();
//				n.setValue(cresolver.resolve(n)); // this is probably a bad idea
			}
			else {
				//log(Redwood.DBG, "Name is not a preposition.");
			}
		}
		return names;
	}

	/***
	 * This method decides whether the given names are already declared in the state.
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
	 * @return
	 */
	public InitialState getCurrentState()
	{
		return mystate;
	}

	/** Resets this classes' state to starting conditions.
	 */
	public void reset() {	
		// FIXME: I really hope the entities don't leak
		mystate.clear();
	}

	/** Analyzes the given sentences and persists the result in the interal state 
	 * @throws IvanException 
	 */
	public void learnDeclarations(CoreMap sentence) throws IvanException {
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
		this.mystate = new InitialState();
		
		AliasByCorefRule aliasRule = new AliasByCorefRule();
		try {
			// extract names from the text
			if(aliasRule.apply(annotation))
			{
				// add each found name to the state
				for (CorefMention ms : aliasRule.getMentions()) {
					// create alias
					// create and add entity
					// add alias
				}				
			}			
			
		} catch (JWNLException e1) {
			// does not occur
		}
		
		
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			// TODO: do stuff
			LocationListAnnotation list = findLocationAsTrees(sentence);
			if(!list.isEmpty())
				sentence.set(LocationListAnnotation.class, list);
			try {
				learnDeclarations(sentence);
			} catch (IvanException e) {
				log(Redwood.ERR, e);
			}
		}		
		annotation.set(IvanEntitiesAnnotation.class, getCurrentState());
	}

	/** Returns the locations which are mentioned in this sentence or an empty list if none 
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
		return myreqs;
	}

}
