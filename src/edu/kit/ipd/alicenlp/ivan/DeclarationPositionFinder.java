package edu.kit.ipd.alicenlp.ivan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.AgentGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.ClausalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

public class DeclarationPositionFinder {
	public class EntityLocationPair {

		public EntityLocationPair(String entity, String location) {
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * @author Jonny
	 *
	 */
	public class DeclarationQuadruple 
	{
		public String Entity;
		public String Name;
		public String Location;
		public String Direction;		
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			else if (obj.getClass().equals(DeclarationQuadruple.class)) {
				DeclarationQuadruple other = (DeclarationQuadruple) obj;
				if (Name != null) {
					return Entity.equalsIgnoreCase(other.Entity);
				}
				else {
					return Name.equalsIgnoreCase(other.Name);
				}
			}
			else {				
				return false;
			}
		}
	}

	static private DeclarationPositionFinder myinstance = null;
	private Dictionary mydictionary;
	private StanfordCoreNLP mypipeline = null;
	
	private boolean hasAgent(IndexedWord root, SemanticGraph graph) {
		// implement a check for agent(root, nounphrase)
		GrammaticalRelation agentrel = GrammaticalRelation.getRelation(AgentGRAnnotation.class);
		// TODO: verify this 
		return graph.hasChildWithReln(root, agentrel);
	}

	/**
	 * @param word
	 * @param pointers
	 */
	private void printHypernymfeedback(String word, List<Pointer> pointers) {		
		System.out.println("To " + word + " is one way to " + 
				pointers.get(0).getTargetSynset().getWords().get(0).getLemma() + ".");
	}
	
	private boolean isPassive(IndexedWord root, SemanticGraph graph) {
		// Examples: 
		// “Dole was defeated by Clinton” nsubjpass(defeated, Dole)
		GrammaticalRelation nsubjpass = GrammaticalRelation.getRelation(NominalPassiveSubjectGRAnnotation.class);
		// “That she lied was suspected by everyone” csubjpass(suspected, lied)
		GrammaticalRelation csubjpass = GrammaticalRelation.getRelation(ClausalPassiveSubjectGRAnnotation.class);
		// “Kennedy was killed” auxpass(killed, was)
		GrammaticalRelation auxrel = GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AuxPassiveGRAnnotation.class);
		Boolean passive = false;
		passive = passive || graph.hasChildWithReln(root, nsubjpass);
		passive = passive || graph.hasChildWithReln(root, csubjpass);
		passive = passive || graph.hasChildWithReln(root, auxrel);
		return passive;
	}

	public DeclarationPositionFinder() 
	{
		// this creates a wordnet dictionary
		setupWordNet();
		// this creates the corenlp pipeline
		setupCoreNLP();
		
		if (myinstance == null) {
			myinstance = this;
		}
	}

	public DeclarationPositionFinder(StanfordCoreNLP pipeline, Dictionary wordnet)
	{
		mypipeline = pipeline;
		mydictionary = wordnet;
		if (myinstance == null) {
			myinstance = this;
		}
	}

	private Boolean printLexicographerFileNamesForVerbs(String token) throws JWNLException {
		IndexWord word = mydictionary.getIndexWord(POS.VERB, token);
		if (word == null) {
			word = mydictionary.lookupIndexWord(POS.VERB, token);
			if (word == null) {
				// skip
				System.err.println("-- Cannot find word \"" + token + "\" in WordNet dictionary."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println();
				return false;
			}
		}
		printLexicographerFileNames(word);
		return true;
	}

	protected Annotation annotate(String text) {
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    mypipeline.annotate(document);
	    System.out.println("{annotation is now done}"); //$NON-NLS-1$
		return document;
	}

	private void printLexicographerFileNames(IndexWord word) {
		word.sortSenses();
		List<Synset> senses = word.getSenses();
		System.out.print("to " + word.getLemma() + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		for (Synset synset : senses) {
			if (senses.indexOf(synset) > 2) {
				break;
			}
			System.out.print(synset.getLexFileName() + "(" + synset.getLexFileNum() + ") "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println();
	}

	private IndexedWord getDeterminer(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}

	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#getDictionary()
	 */
	
	public Dictionary getDictionary() {
		return mydictionary;
	}
	
	private IndexedWord getDirectObject(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#getPipeline()
	 */	
	public StanfordCoreNLP getPipeline() {
		return mypipeline;
	}

	private CoreLabel getPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	protected Boolean hasWordNetEntry(String verb) throws JWNLException {
		IndexWord word = mydictionary.getIndexWord(POS.VERB, verb);
		if (word == null) {
			word = mydictionary.lookupIndexWord(POS.VERB, verb);
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
		if (hasParticle(word, graph)) {
			String particle = null;
			particle = getParticle(word, graph).word();
			//System.err.println(particle);
			String combinedword = lemma + " " + particle;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(hasPrepMod(word, graph)) {
			String prepmod = null;
			prepmod = getPrepMod(word, graph).word();
			//System.err.println(prepmod);
			String combinedword = lemma + " " + prepmod;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(hasDirectObjectNP(word, graph)) {
			String dirobstr = null;
			IndexedWord direObj = null;
			direObj = getDirectObject(word, graph);
			CoreLabel det = getDeterminer(direObj, graph);
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


	protected static Boolean is1stPerson(IndexedWord root, SemanticGraph graph)
	{
		// not actually always first person, but for our corpus, it's good enough 
//		if ("VBP".equalsIgnoreCase(root.get(CoreAnnotations.PartOfSpeechAnnotation.class))) {
//			return true;
//		}
		GrammaticalRelation subjclass = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		IndexedWord subject = graph.getChildWithReln(root, subjclass);
		return subject == null || subject.word().equalsIgnoreCase("I");
	}
	
	protected static IndexedWord getParticle(IndexedWord word, SemanticGraph graph)
	{
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	private static boolean hasAdverbMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.AdverbialModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}

	private static boolean hasDirectObjectNP(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		if (graph.hasChildWithReln(word, reln)) {
			String pos = graph.getChildWithReln(word, reln).get(PartOfSpeechAnnotation.class);
			if (pos.equalsIgnoreCase("NN")) {
				return true;
			}
		}
		return false;
	}

	protected static Boolean hasParticle(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}
	
    private static boolean hasPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}
    
    private static void nop() {
		// nop		
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

	public DeclarationQuadruple findAll(IndexedWord root, CoreMap sentence) {
		// TODO Auto-generated method stub
		//  
		return null;
	}
	
	/***
	 * This function implements a check for rule "in_foreground".
	 * It checks for these ternary relations:  root->prep_in->det and nsubj->prep_in->det
	 * @param sentence
	 * @return
	 */
	public boolean hasLocation(CoreMap sentence)
	{
		GrammaticalRelation nsubjreln = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if (root == null) {
			// no root - no grammar!
			return false;
		}
		// look for relations: first root->prep_in->det
		if(hasPrepinDetRelation(graph, root))
		{
			// we've found it! done!
			return true;
		}
		else // look for relations: second root->prep_in->det 
		{
			// let's try to find a subject:
			IndexedWord subject = graph.getChildWithReln(root, nsubjreln);
			if (subject == null) {
				// no subject - nothing to attribute anything to it!
				return false;
			}
			// we already have our starting point njsubj. now let's find out if there are
			// any prep_in->det attached to it:
			return hasPrepinDetRelation(graph, subject);
			// done
		}		
	}

	/**
	 * @param graph
	 * @param startingWord
	 * @return 
	 */
	private boolean hasPrepinDetRelation(SemanticGraph graph,
			IndexedWord startingWord) {
		//GrammaticalRelation prepreln = GrammaticalRelation.getRelation(PrepositionalModifierGRAnnotation.class);
		GrammaticalRelation prepreln = EnglishGrammaticalRelations.getPrep("in");		
		GrammaticalRelation detreln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
		// checking rule: root->prep_in->det
		List<IndexedWord> listpreps = graph.getChildrenWithReln(startingWord, prepreln);
		for (IndexedWord indexedWord : listpreps) {			
			// we have found a relation: root->in
			// now check for in->det
			IndexedWord det = graph.getChildWithReln(indexedWord, detreln);
			if (det != null) {
				// success! this graph contains the relation root->prep_in->det
				return true;
			}			
		}
		// found nothing, sorry!
		return false;
	}

	public EntityLocationPair getLocation(CoreMap sentence) throws LocationNotFoundException {
		String entity, location = null;
		IndexedWord startingword;
		GrammaticalRelation nsubjreln = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if (root == null) {
			// no root - no grammar!
			return null;
		}
		if(hasLocation(sentence))
		{
			// entity is the subject			
			// let's try to find a subject:
			IndexedWord subject = graph.getChildWithReln(root, nsubjreln);
			if (subject == null) {
				// no subject - nothing to attribute anything to it!
				return null;
			}
			entity = subject.getString(TextAnnotation.class);
			
			// which part of the sentence has the prep_in->det relation?
			if (hasPrepinDetRelation(graph, root)) {
				startingword = root;
			}
			else {
				startingword = subject;
			}
			
			// we already have our starting point njsubj. now let's find out if there are
			// any prep_in->det attached to it:
			GrammaticalRelation prepreln = EnglishGrammaticalRelations.getPrep("in");		
			GrammaticalRelation detreln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
			// checking rule: root->prep_in->det
			List<IndexedWord> listpreps = graph.getChildrenWithReln(startingword, prepreln);			
			for (IndexedWord indexedWord : listpreps) {			
				// we have found a relation: root->in
				// now check for in->det
				IndexedWord det = graph.getChildWithReln(indexedWord, detreln);
				if (det != null) {
					// success! this graph contains the relation root->prep_in->det
					// this means, it's probably our location:
					location = indexedWord.getString(TextAnnotation.class);
					break;
				}	
			}			
			// found nothing, sorry!
			// done
		}
		else {
			// there is no location in this sentence
			return null;
		}
		
		if (location == null) {
			// I tried to identify a location but failed
			throw new LocationNotFoundException("Could not identify location in sentence \"" + sentence.toString());
		}
		else {
			return new EntityLocationPair(entity, location);		
		}
	}
}
