/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.AliasHearstRule;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 * 
 */
public class AliasHearstRuleTest {

	/**
	 * Test method for
	 * {@link edu.kit.ipd.alicenlp.ivan.rules.AliasHearstRule#apply(edu.stanford.nlp.util.CoreMap)}
	 * .
	 * @throws JWNLException 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testApply() throws JWNLException {

		{
			String samplesentence = "The dog is called Spanky.";
			Annotation doc = annotate(samplesentence);

			CoreMap thing = doc.get(SentencesAnnotation.class).get(0);

			AliasHearstRule rule = new AliasHearstRule();

			assertTrue("Nothing recognised", rule.apply(thing)); // runs rule

			List<CorefMention> mentions = rule.getAliasMentions();

			assertThat("Too few results", mentions.size(), is(1));

			CorefMention m = mentions.get(0);

			assertThat("Name does not match", m.mentionSpan, is("Spanky"));			
			assertThat("Entity does not match", rule.getEntity(m).mentionSpan, is("dog"));
			
			assertThat("alias index is wrong", m.headIndex, is(5));			
			assertThat("entity index wrong", rule.getEntity(m).headIndex, is(2));
		}
		
		{
			String samplesentence = "The cat is named Fluffy.";
			Annotation doc = annotate(samplesentence);

			CoreMap thing = doc.get(SentencesAnnotation.class).get(0);

			AliasHearstRule rule = new AliasHearstRule();

			assertTrue("Nothing recognised", rule.apply(thing)); // runs rule

			List<CorefMention> mentions = rule.getAliasMentions();

			assertThat("Too few results", mentions.size(), is(1));

			CorefMention m = mentions.get(0);

			assertThat("Name does not match", m.mentionSpan, is("Fluffy"));
			assertThat("Entity does not match", rule.getEntity(m).mentionSpan, is("cat"));
			
			assertThat("alias index is wrong", m.headIndex, is(5));			
			assertThat("entity index wrong", rule.getEntity(m).headIndex, is(2));
		}

	}

	private static Annotation annotate(String text) {
		Annotation doc = new Annotation(text);
		StanfordCoreNLP pipeline;

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		// alternativ: wsj-bidirectional
		try {
			props.put(
					"pos.model",
					"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
		} catch (Exception e) {
			e.printStackTrace();
		}
		props.put("customAnnotatorClass.decl",
				"edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");

		// konfiguriere pipeline
		props.put(
				"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, decl"); //$NON-NLS-1$ //$NON-NLS-2$
		pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(doc);
		return doc;
	}

}
