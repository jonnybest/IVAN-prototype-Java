package edu.kit.ipd.alicenlp.ivan.rules;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

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
				
		List<CorefMention> mentions = rule.getMentions();
		
		assertThat("Too few results", mentions.size(), is(1));
		
		CorefMention m = mentions.get(0);
		
		assertThat("Name does not match", m.mentionSpan, is("Spanky"));
		
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
