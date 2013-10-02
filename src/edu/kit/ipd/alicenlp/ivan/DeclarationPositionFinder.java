package edu.kit.ipd.alicenlp.ivan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule;
import edu.kit.ipd.alicenlp.ivan.rules.NounRootRule;
import edu.kit.ipd.alicenlp.ivan.rules.WordPrepInDetRule;
import edu.kit.ipd.alicenlp.ivan.rules.WordPrepOnDetRule;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.AgentGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.ClausalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NounCompoundModifierGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

public class DeclarationPositionFinder {
	

	private InitialState mystate = null;
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
		
		// this class is also in charge of keeping a state, so here it is:
		this.mystate = new InitialState();
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
		// this class is also in charge of keeping a state, so here it is:
		this.mystate = new InitialState();
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
	protected void setupCoreNLP() {
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

	public EntityInfo findAll(String name, IndexedWord root, CoreMap sentence) {
		// TODO Auto-generated method stub
		//  
		return new EntityInfo(name);
	}
	
	/**
	 * Indicates whether this sentence contains a location.
	 * @param sentence
	 * @return True, if a location has been recognised.
	 */
	public boolean hasLocation(CoreMap sentence)
	{
		return getLocation(sentence) != null; 
	}

	/**
	 * Attempts to find a location in the given sentence. 
	 * @param sentence A CoreMap to look inside
	 * @return An EntityInfo containing the location and the word it refers to. Or {@code null} if none was found.
	 */
	public EntityInfo getLocation(CoreMap sentence) 
	{
		String entity, location = null;
		
		WordPrepInDetRule inRule = new WordPrepInDetRule();
		if (inRule.apply(sentence)) {
			entity = inRule.getWord().originalText();
			location = inRule.getPrepositionalModifier().toString();
		} else {
			WordPrepOnDetRule onRule = new WordPrepOnDetRule();
			if(onRule.apply(sentence))
			{
				entity = onRule.getWord().originalText();
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
	public EntityInfo getDirection(CoreMap sentence) {
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
	

	/** Returns entityinfos which make up the subject of {@code sentence}
	 * @param sentence
	 * @return
	 */
	public List<EntityInfo> getDeclarations(CoreMap sentence) {
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

	public List<String> recogniseNames(CoreMap sentence) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord subj = BaseRule.getSubject(graph);
		ArrayList<String> names = resolveCc(subj, graph);
		return names;
	}

	/** This method attempts to resolve noun phrases and conjunction. 
	 * More precisely, it looks for nn and con_and dependencies below {@code head} and creates a list of entities.
	 * @param head The head of the noun phrase
	 * @param graph The sentence to look in.
	 * @return A list of distinct words or names, grouped by "and"
	 */
	protected ArrayList<String> resolveCc(IndexedWord head,
			SemanticGraph graph) {
		// list of names
		ArrayList<String> names = new ArrayList<String>();
		// adding this subject
		names.add(resolveNN(head, graph));
		// check for more!
		// more names can be reached with "and". Get us an "and":
		GrammaticalRelation andrel = EnglishGrammaticalRelations.getConj("and");
		// ask the graph for everything that is connected by "and":
		List<IndexedWord> ands = graph.getChildrenWithReln(head, andrel);
		for (IndexedWord w : ands) {
			// add 'em
			names.add(resolveNN(w, graph));
		}
		// hope those are all
		return names;
	}

	/** This method attempts to resolve noun phrases which consist of more than one word.
	 * More precisely, it looks for nn dependencies below {@code head} and creates an entity.
	 * @param head The head of the noun phrase
	 * @param graph The sentence to look in.
	 * @return A distinct word
	 */
	private String resolveNN(IndexedWord head, SemanticGraph graph) {
		List<IndexedWord> nns = graph.getChildrenWithReln(head, EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER);
		String name = "";
		// check for nulls. if there is nothing here, we have nothing to do.
		if (nns != null) {
			for (IndexedWord part : nns) {
				name += part.word();
				name += " ";
			}
		}
		name += head.word();
		return name;
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

}
