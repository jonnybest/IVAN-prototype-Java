package edu.kit.ipd.alicenlp.ivan.analyzers;

import static edu.stanford.nlp.util.logging.Redwood.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.data.DocumentErrorAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.EntitiesSynonymsErrorRule;
import edu.kit.ipd.alicenlp.ivan.rules.ErrorRule;
import edu.kit.ipd.alicenlp.ivan.rules.EventRule;
import edu.kit.ipd.alicenlp.ivan.rules.TimeRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.AgentGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

public class StaticDynamicClassifier extends IvanAnalyzer  
{
	static private StaticDynamicClassifier myinstance = null;
	private Dictionary dictionary;
	
	public Classification classifySentence(IndexedWord root, CoreMap sentence) throws JWNLException
	{
		// this is the default result, if nothing else matches
		Classification defaultclass = Classification.ActionDescription;
		
		/** This is a new style of classifying. Simply create a rule to check and call "apply".
		 *  The apply method is responsible for producing the result and all we have to do
		 *  is to decide what to do with the result.
		 *  In most cases, we simply want to annotate.
		 */
		// does this sentence container an error?
		ErrorRule checkError = new ErrorRule();
		if(checkError.apply(sentence))
		{
			System.out.println("bad sentence found");
			sentence.set(ErrorMessageAnnotation.class, checkError.getErrorMessage());
			return Classification.ErrorDescription;
		}
		
		// does this sentence contain an event?
		EventRule checkevent = new EventRule();
		// yes!
		if(checkevent.apply(sentence))
		{
			System.out.println("Event found");
			// since we only support one classification, return the classification instantly
			return Classification.EventDescription;
			//sentence.set(Classification.class, Classification.EventDescription);
		}
		
		// does this sentence explicitly reference time or duration?
		TimeRule checkTime = new TimeRule();
		// yes!
		if(checkTime.apply(sentence))
		{
			System.out.print("Time reference found");
			return Classification.TimeDescription;
		}
		
		/** Old style classification follows. 
		 * 
		 */
		// short classification fix for broken sentences (wrong copula)
		// hint1: root is no verb
		if (!BaseRule.isPOSFamily(root, "VB")) {
			// hint 2: there is only one verb
			List<CoreLabel> verbs1 = new ArrayList<CoreLabel>();
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (CoreLabel word1 : labels) {
				if (BaseRule.isPOSFamily(word1, "VB")) {
					verbs1.add(word1);
				}
			}
			List<CoreLabel> verbs = verbs1;
			if (verbs.size() == 1) {
				String word = verbs.get(0).toString();
				// hint 3: the only verb is "to be"
				IndexWord wnetlemma = dictionary.lookupIndexWord(POS.VERB, word);
				IndexWord tobe = dictionary.getIndexWord(POS.VERB, "be");
				if (tobe.equals(wnetlemma)) {
					// ex: "Henry, Liv and Paddy are dogs."
					return Classification.SetupDescription;
				}
			}
		}
		
		// normal classification rules follow:
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//		System.out.println(graph.toString());
		String word = expandVerb(root, graph);
		// classify by grammatical construction
		boolean passive = BaseRule.isPassive(root, graph);
		if(passive)
		{
			System.out.println("This sentence is passive.");
			//return Classification.SetupDescription; // probably a bad idea?
		}
		
		// classify by lexical file num
		IndexWord wnetw = dictionary.lookupIndexWord(POS.VERB, word);
		if (wnetw == null) {
			return Classification.ErrorDescription;
		}
		wnetw.sortSenses();
		List<Synset> senses = wnetw.getSenses();
		Synset mcs = senses.get(0); // most common sense
		long lexnum = mcs.getLexFileNum();

		if (lexnum == 42)
		{
			// stative
			// TODO: make sure this actually refers to a state; not a changing
			// state
			List<Pointer> pointers = mcs.getPointers(PointerType.HYPERNYM);
			if (pointers.size() > 0) {
				printHypernymfeedback(word, pointers);
//				System.out.print("Hypernym lexname: ");
//				System.out.println(pointers.get(0).getTargetSynset().getLexFileName());			
			}
			return Classification.SetupDescription;
		}
		else if (senses.size() > 1 && senses.get(1).getLexFileNum() == 42) {			
			System.out.println("Second synset:");
			List<Pointer> pointers = senses.get(1).getPointers(PointerType.HYPERNYM);
			if (pointers.size() > 0) {
				printHypernymfeedback(word, pointers);
//				System.out.print("Hypernym lexname: ");
//				System.out.println(pointers.get(0).getTargetSynset().getLexFileName());			
			}
			return Classification.SetupDescription;
		}
		else if (lexnum == 36) // verb.creation			
		{
			// ex: "The roof of the shed is painted blue, like the sky." 
			if(passive)
			{
				if (!StaticDynamicClassifier.hasAgent(root, graph)) {
					// ex: The roof is painted by the father.
					return Classification.ActionDescription;
				}
				return Classification.SetupDescription;
			}
		}

		return defaultclass;
	}

	/**
	 * @param word
	 * @param pointers
	 */
	private void printHypernymfeedback(String word, List<Pointer> pointers) {		
		System.out.println("To " + word + " is one way to " + 
				pointers.get(0).getTargetSynset().getWords().get(0).getLemma() + ".");
	}

	public StaticDynamicClassifier() 
	{
		// this creates a wordnet dictionary
		setupWordNet();
		
		if (myinstance == null) {
			myinstance = this;
		}
	}

	public StaticDynamicClassifier(StanfordCoreNLP pipeline, Dictionary wordnet)
	{
		dictionary = wordnet;
		if (myinstance == null) {
			myinstance = this;
		}
	}


	/** This is the constructor for the Stanford Pipeline Interface
	 * 
	 * @param string
	 * @param properties
	 */
	public StaticDynamicClassifier(String name, Properties properties) {
		// this creates a wordnet dictionary
		setupWordNet();

		// do not assign any instace, because I fear multiple annotators may be interfering with each other if the references escape pipeline context		
	}

	public Dictionary getDictionary() {
		return dictionary;
	}
	
	protected Boolean hasWordNetEntry(String verb) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, verb);
		if (word == null) {
			word = dictionary.lookupIndexWord(POS.VERB, verb);
			if (word == null || !word.getLemma().equals(verb)) {
				// skip
				//System.err.println("-- Cannot find word \"" + verb + "\" in WordNet dictionary."); //$NON-NLS-1$ //$NON-NLS-2$
				//System.err.println();
				return false;
			}
			else 
				return true;
		}
		else
			return true;
	}
	
	/** Finds whole word to multi-word verbs like phrasal verbs
	 * @param graph The sentence this word occurs in
	 * @param word The word to find parts for
	 * @return The whole verb (in base form) as it exists in WordNet 
	 * @throws JWNLException
	 */
	protected String expandVerb(IndexedWord word, SemanticGraph graph)
			throws JWNLException {
		String lemma = word.lemma();
		if (BaseRule.hasParticle(word, graph)) {
			String particle = null;
			particle = BaseRule.getParticle(word, graph).word();
			//System.err.println(particle);
			String combinedword = lemma + " " + particle;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(BaseRule.hasPrepMod(word, graph)) {
			String prepmod = null;
			prepmod = BaseRule.getPrepMod(word, graph).word();
			//System.err.println(prepmod);
			String combinedword = lemma + " " + prepmod;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(BaseRule.hasDirectObjectNP(word, graph)) {
			String dirobstr = null;
			IndexedWord direObj = null;
			direObj = BaseRule.getDirectObject(word, graph);
			CoreLabel det = BaseRule.getDeterminer(direObj, graph);
			if (det != null) {
				dirobstr = det.word() + " " + direObj.word();
			}
			else {
				dirobstr = direObj.word();
			}
			//System.err.println(direObj);
			String combinedword = lemma + " " + dirobstr;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;
			}
		}
		return lemma;
	}

	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#setDictionary(net.sf.extjwnl.dictionary.Dictionary)
	 */
	
	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
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
	    	if (dictionary == null) {
				//new style, instance dictionary
				dictionary = Dictionary.getInstance(properties);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
	}
	
	/** Set the instance for this class
	 * @param myinstance the myinstance to set
	 */
	public static void setInstance(StaticDynamicClassifier myinstance) {
		StaticDynamicClassifier.myinstance = myinstance;
	}
	public static StaticDynamicClassifier getInstance() {
		if (myinstance == null) {
			StaticDynamicClassifier.myinstance = new StaticDynamicClassifier();
		}
		return myinstance;
	}

	@Override
	public void annotate(Annotation annotation) 
	{
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			// process 
			try {
				Classification sentenceclass = classifySentence(BaseRule.getRoot(sentence), sentence);
				sentence.set(Classification.class, sentenceclass);
			} catch (JWNLException e) {
				// no classification for this sentence then :(
				log(Redwood.ERR, "Error while classifying sentences.", e);
			}
		}
		try {
			classifyDocument(annotation);
		} catch (JWNLException e) {
			log(Redwood.ERR, e);
		}
	}

	private static void classifyDocument(Annotation annotation) throws JWNLException {
		// TODO: implement document-wide error checking
		List<ErrorMessageAnnotation> errors = annotation.get(DocumentErrorAnnotation.class);
		if(errors == null)
			errors = new ArrayList<ErrorMessageAnnotation>();

		// lets check the entities for consistency. 
		InitialState entities = annotation.get(IvanEntitiesAnnotation.class);
		if(entities == null)
			return; // no entities - nothing to do
		
		// 1. distinct entities should not be synonymous to each other 
		EntitiesSynonymsErrorRule rule = new EntitiesSynonymsErrorRule(entities);
		if(rule.apply(annotation, true))
		{
			errors.add(rule.getErrorMessage());			
			annotation.set(DocumentErrorAnnotation.class, errors);
		}
	}

	@Override
	public Set<Requirement> requirementsSatisfied() {
		Set<Requirement> isatisfy = new HashSet<Annotator.Requirement>();
		isatisfy.add(SETUP_CLASSIFICATION);
		isatisfy.add(ACTION_CLASSIFICATION);
		return isatisfy;
	}

	@Override
	public Set<Requirement> requires() {
		Set<Requirement> myreqs = new HashSet<Annotator.Requirement>();
		myreqs.addAll(TOKENIZE_SSPLIT_POS_LEMMA);
		//myreqs.add(PARSE_REQUIREMENT);
		return myreqs;
	}

	/** This method decides whether a given <code>word</code> has an agent.
	 * 	Ex: "The man has been killed by the police"
	 *  
	 * @param word
	 * @param graph
	 * @return
	 */
	public static boolean hasAgent(IndexedWord word, SemanticGraph graph) {
		// implement a check for agent(root, nounphrase)
		GrammaticalRelation agentrel = GrammaticalRelation.getRelation(AgentGRAnnotation.class); 
		return graph.hasChildWithReln(word, agentrel);
	}

}
