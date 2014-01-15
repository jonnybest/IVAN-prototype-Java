package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.ErrorRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 * 
 */
public class ErrorRuleTest extends ErrorRule {

	/**
	 * Checks if the tags SBAR, UCP and FRAG are marked as errors.
	 */
	@Test
	public final void testBadPennTags() {
		// Annotation doc =
		// annotate("In Japan, during the last war and just before the armistice am hav5ing a great time, 6a5s4dAAF swimmed :-D towards, I can be; Speakeasy. "
		// +
		// "Harrison Ford has said he would be more than willing to take on another Indiana Jones project. "
		// + "In a New York minute.");
		Annotation doc = annotate("In a New York minute.");

		List<CoreMap> sentences = sentences(doc);
		for (CoreMap sen : sentences) {
			boolean result = applyBadPennTags(sen);
			assertTrue(result);
		}
	}

	private static List<CoreMap> sentences(Annotation doc2) {
		return doc2.get(SentencesAnnotation.class);
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
		props.put("customAnnotatorClass.classi",
				"edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");

		// konfiguriere pipeline
		props.put(
				"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, decl, classi"); //$NON-NLS-1$ //$NON-NLS-2$
		pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(doc);
		return doc;
	}
}
