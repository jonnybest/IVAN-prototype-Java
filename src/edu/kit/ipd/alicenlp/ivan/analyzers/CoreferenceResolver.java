/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/** This class resolves coreferences; that means it tries to find a name to a given pronoun.
 * @author Jonny
 *
 */
public class CoreferenceResolver {
	
	private static CoreferenceResolver instance;
	private StanfordCoreNLP mypipeline = null;
	private Annotation doc;

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
	 * @param name The word to resolve to a name (e.g. "she")
	 * @param startIndex The index where this word occurs in the text (e.g. indexOf(word))
	 * @param text The reference text which (hopefully) contains the name
	 * @return The name for this given pronoun
	 */
	public static String findName(String name, int startIndex, String text)
	{	
		if(startIndex < 0)
			return null;
		
		/** note: if you get a multi-word name, you can indexOf the whole thing to get a start index and the just split
		 *        at the space to get the head. the head is always the rightmost word. 
		 *        the new index is then = indexOf(name) + length(name) - length(head) */
		
		// create the annotation document
		Annotation doc = new Annotation(text);
		// run my pipeline on it
		getInstance().mypipeline.annotate(doc);
		/* okay, how to go about finding the other mentions for this exact word?
		*  suppose I want to use getMentionsWithSameHead(sentenceNumber, headIndex) -- headIndex means the position of the word which is the head of the NP (if the name has more than one word)
		*  first, I need to find out where this word occurs: in which sentence and at which index
		*    to go about that, I will iterate the sentences to find out if the startIndex is within the sentences bounds, then get the indexword at that position, and then note the word's index
		*    after that I can iterate the CorefChains and for each chain invoke getMentionsWithSameHead
		*    when I have found the right chain, I return the representative phrase
		*/ 
		CoreLabel labelForMyWord = null;
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for(CoreMap s : sentences)
		{
			if(startIndex >= s.get(CharacterOffsetBeginAnnotation.class)){
				if(startIndex < s.get(CharacterOffsetEndAnnotation.class)){
					List<CoreLabel> tokens = s.get(TokensAnnotation.class);
					//tokens.
					for (CoreLabel lbl : tokens) {
						if(lbl.word().equals(name)) // this words for single-word names only!
						{
							labelForMyWord = lbl;
							nop();
							break;
						}
					}
				}
			}
			nop();
		}
		
		return resolve(labelForMyWord, doc);
	}

	/**
	 * @param doc
	 * @param labelForMyWord
	 * @return
	 */
	public static String resolve(CoreLabel name, Annotation doc) {
		if(doc == null)
			return null;
		if(name == null)
			return null;
		
		// coref-part
		int sentence = -1;
		Map<Integer, CorefChain> coref = doc.get(CorefChainAnnotation.class);
		for (CoreMap s : doc.get(SentencesAnnotation.class)) {
			sentence = s.get(SentenceIndexAnnotation.class);
			if(s.get(TokensAnnotation.class).contains(name))
				break;
			nop();
		}
		for (CorefChain entry : coref.values()) {
			Set<CorefMention> mention = entry.getMentionsWithSameHead(sentence  + 1 /* starts at 1 */, name.index());
			if(mention != null)
				return entry.getRepresentativeMention().mentionSpan;
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
	
	public String resolve(CoreLabel name) throws IvanException {
		if(this.doc != null)
			return resolve(name, doc);
		else {
			throw new IvanException("Document for this instance has not been set. Call setDocument() first, please.");			
		}
	}
	
	/** Set an annotated document for this resolver
	 * @param document
	 */
	public void setDocument(Annotation document){
		doc = document;
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
