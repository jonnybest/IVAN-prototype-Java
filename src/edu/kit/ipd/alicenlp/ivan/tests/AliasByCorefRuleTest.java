package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.AliasByCorefRule;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

@SuppressWarnings("javadoc")
public class AliasByCorefRuleTest 
{

	/**
	 * Asser that the rule can find a name in a sample sentence.
	 * @throws JWNLException 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testApplyAnnotation() throws JWNLException {
		String samplesentence = "Spanky is a dog."; 
		Annotation thing = annotate(samplesentence);

		AliasByCorefRule rule = new AliasByCorefRule();
		
		assertTrue("Nothing recognised", rule.apply(thing)); // runs rule
				
		List<CorefMention> mentions = rule.getAliasMentions();
		
		assertThat("Too few results", mentions.size(), is(1));
		
		CorefMention m = mentions.get(0);
		
		assertThat("Name does not match", m.mentionSpan, is("Spanky"));
		
		assertThat("Description does not match", rule.getEntity(m).mentionSpan, is("a dog"));
	}
	
	@Test
	public void indexTest() throws JWNLException
	{
		String samplesentence = "Terror in the nights outlasted Batman's second cousin. Spanky is a dog."; 
		Annotation annotation = annotate(samplesentence);

		AliasByCorefRule aliasRule = new AliasByCorefRule();		
		assertTrue("Nothing recognised", aliasRule.apply(annotation)); // runs rule				
		List<CorefMention> mentions = aliasRule.getAliasMentions();
		
		CorefMention ms = mentions.get(3);		
		
		assertThat(ms.mentionSpan, is("Spanky"));
		assertThat(aliasRule.getEntity(ms).mentionSpan, is("a dog"));
		
		int entityHeadIndex = aliasRule.getEntity(ms).headIndex - 1;
		int sentenceNum = aliasRule.getEntity(ms).sentNum - 1;
		CoreMap sen = annotation.get(SentencesAnnotation.class).get(sentenceNum);
		CoreLabel head = sen.get(TokensAnnotation.class).get(entityHeadIndex);
		String headstring = head.lemma();
		
		assertThat(headstring, is("dog"));
	}

	
	private static Annotation annotate(String text) {
		Annotation doc = new Annotation(text);
		StanfordCoreNLP pipeline;
			
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    // alternativ: wsj-bidirectional 
	    try {
			props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	    props.put("customAnnotatorClass.decl", "edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
	    
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, decl"); //$NON-NLS-1$ //$NON-NLS-2$
	    pipeline = new StanfordCoreNLP(props);	
	    
	    pipeline.annotate(doc);
	    return doc;
	}
}
