/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefMentionFinder;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/** This class resolves coreferences; that means it tries to find a name to a given pronoun.
 * @author Jonny
 *
 */
public class CoreferenceResolver implements CorefMentionFinder {
	
	private static CoreferenceResolver instance;
	private StanfordCoreNLP mypipeline = null;

	public static CoreferenceResolver getInstance()
	{
		if(instance == null)
		{
			instance = new CoreferenceResolver();
		}
		return instance;
	}
	
	public CoreferenceResolver()
	{
		instance = this;
		setupCoreNLP();
	}
	
	public CoreferenceResolver(StanfordCoreNLP pipeline) {
		instance = this;
		mypipeline = pipeline;
		setupCoreNLP();
	}
	
	/** This method finds the name to a given word (e.g. a pronoun)
	 * 
	 * @param word The word to resolve to a name (e.g. "she")
	 * @param startIndex The index where this word occurs in the text (e.g. indexOf(word))
	 * @param text The reference text which (hopefully) contains the name
	 * @return The name for this given pronoun
	 */
	public static String findName(String word, int startIndex, String text)
	{		
		startIndex = text.indexOf(word);
		Annotation doc = new Annotation(text);
		getInstance().mypipeline.annotate(doc);
		Map<Integer, CorefChain> coref = doc.get(CorefChainAnnotation.class);
		for (CorefChain entry : coref.values()) {
			Object bla = entry.getMentionsWithSameHead(2, startIndex);
			nop();
		}
//		for(CoreMap sentence : doc.get(SentencesAnnotation.class))
//		{
//			@SuppressWarnings("unused")
//			Map<Integer, CorefChain> coref = sentence.get(CorefChainAnnotation.class);
//			nop();
//		}
		return null;
	}
	
	@Override
	public List<List<Mention>> extractPredictedMentions(Annotation doc, int maxGoldID, Dictionaries dict) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String resolve(IndexedWord n) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static void nop() {
		// nop nop nop nop nop		
	}

	/**
	 * 
	 */
	private void setupCoreNLP() {
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
		    props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, dcoref"); //$NON-NLS-1$ //$NON-NLS-2$
		    pipeline = new StanfordCoreNLP(props);	    
		    mypipeline = pipeline;
		}
		else {
			pipeline = mypipeline;
		}
	}

}
