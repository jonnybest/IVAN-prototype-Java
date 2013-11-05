package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule;
import edu.kit.ipd.alicenlp.ivan.rules.ILocationRule;
import edu.kit.ipd.alicenlp.ivan.rules.WordPrepInDetRule;
import edu.kit.ipd.alicenlp.ivan.rules.WordPrepOnDetRule;
import edu.stanford.nlp.ling.CoreAnnotations.LabelAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotator.Requirement;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.TypesafeMap.Key;

public class DeclarationPositionFinder extends IvanAnalyzer
{
	
	/** This field contains the names and entities. The rules are: only one state per analyzer. */ 
	final private InitialState mystate = new InitialState();
	static private DeclarationPositionFinder myinstance = null;
	private Dictionary mydictionary;
	private StanfordCoreNLP mypipeline = null;

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

	public DeclarationPositionFinder(StanfordCoreNLP pipeline, Dictionary wordnet)
	{
		// this creates the corenlp pipeline
		mypipeline = pipeline;
		// this creates a wordnet dictionary
		mydictionary = wordnet;
		// static reference for no real reason
		if (myinstance == null) {
			myinstance = this;
		}
	}

	public DeclarationPositionFinder(String string, Properties properties) {
		// TODO Auto-generated constructor stub
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
			List<String> names = recogniseNames(sentence);
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
				infos.add(info);
			}			
		} catch (IvanException e) {
			// not sure what to do now?
			e.printStackTrace();
		}
		
		return infos;
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
		
		ILocationRule inRule = new WordPrepInDetRule();
		if (inRule.apply(sentence)) {
//			entity = inRule.getWord().originalText(); // the entity is most likely not the word, but the subject(s) of the sentence
			location = inRule.getPrepositionalModifier().toString();
		} else {
			WordPrepOnDetRule onRule = new WordPrepOnDetRule();
			if(onRule.apply(sentence))
			{
//				entity = onRule.getWord().originalText(); // the entity is most likely not the word, but the subject(s) of the sentence
				location = onRule.getPrepositionalModifier().toString();
			}
			else {
				return null;
			}
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
		List<String> names = recogniseNames(sentence);
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
	public static List<String> recogniseNames(CoreMap sentence) throws IvanException {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord head = BaseRule.getSubject(graph);
		if (head == null) {
			// second try: use the root in subject-less sentences
			head = graph.getFirstRoot();
			// check again:
			if (head == null) {
				// I'm out of ideas
				return null;
			}
		}
		ArrayList<IndexedWord> namesIW = new ArrayList<IndexedWord>();
		ArrayList<String> names = BaseRule.resolveCc(head, graph, namesIW);
		for (IndexedWord n : namesIW) {
			if(n.tag().equals("PRP"))
			{
				CoreferenceResolver cresolver = CoreferenceResolver.getInstance();
				cresolver.resolve(n);
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
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			// TODO: do stuff
			LocationListAnnotation list = findLocationAsTrees(sentence);
			if(!list.isEmpty())
				sentence.set(LocationListAnnotation.class, list);
		}		
	}

	/** Returns the locations which are mentioned in this sentence or an empty list if none 
	 * 
	 * @param sentence
	 * @return
	 */
	private LocationListAnnotation findLocationAsTrees(CoreMap sentence) {
		// FIXME
		
		LocationListAnnotation ourlocs = new LocationListAnnotation();

		ILocationRule inRule = new WordPrepInDetRule();
		if (inRule.apply(sentence)) {
			LocationAnnotation someloc = new LocationAnnotation();
			someloc.setReferent(inRule.getWordAsTree());
			someloc.setLocation(inRule.getPrepositionalModifierAsTree());
			ourlocs.add(someloc);
		} 
		
		WordPrepOnDetRule onRule = new WordPrepOnDetRule();
		if(onRule.apply(sentence))
		{
			LocationAnnotation someloc = new LocationAnnotation();
			someloc.setReferent(onRule.getWordAsTree());
			someloc.setLocation(onRule.getPrepositionalModifierAsTree());
			ourlocs.add(someloc);
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
