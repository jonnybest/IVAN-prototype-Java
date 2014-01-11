/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.rules.AliasByCorefRule;
import edu.kit.ipd.alicenlp.ivan.rules.EntitiesSynonymsErrorRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * This test case simply applies all the rules to a given test document. The point is to achieve maximum code coverage on all the library methods.
 * 
 * @author Jonny
 * 
 */
public class RulesTest {

	Annotation doc;

	/**
	 * Create an annotation for the test cases
	 */
	@Before
	public void annotate() {
		doc = annotate("The ground is covered with grass, the sky is blue. \n"
				+ "In the background on the left hand side there is a PalmTree. \n"
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. \n"
				+ "Right to the mailbox there is a Frog facing east. \n"
				+ "In front of the Bunny there is a Broccoli. \n"
				+ "In the foreground on the right hand side there is a Bunny facing southwest. \n"
				+ "The Bunny turns to face the Broccoli. \n"
				+ "The Bunny hops three times to the Broccoli. \n"
				+ "The Bunny eats the Broccoli. \n"
				+ "The Bunny turns to face the Frog. \n"
				+ "The Bunny taps his foot twice. \n"
				+ "The Frog ribbits. The Frog turns to face northeast. \n"
				+ "The frog hops three times to northeast. \n"
				+ "The Bunny turns to face the Mailbox. \n"
				+ "The Bunny hops three times to the Mailbox. \n"
				+ "The Bunny opens the Mailbox. \n"
				+ "The Bunny looks in the Mailbox and at the same time the Frog turns to face the Bunny. \n"
				+ "The Frog hops two times to the Bunny. \n"
				+ "The Frog disappears. A short time passes.");
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testAliasByCorefRule() throws JWNLException {
		new AliasByCorefRule().apply(doc);
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public final void testEntities() throws JWNLException {
		new EntitiesSynonymsErrorRule(doc.get(IvanAnnotations.IvanEntitiesAnnotation.class));
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testAliasHearstRule() throws JWNLException {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.AliasHearstRule().apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testDirection() {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule()
					.apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testError() {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.ErrorRule().apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testPrepositional() {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.PrepositionalRule().apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testTime() {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.TimeRule().apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testTowards() {
		for (CoreMap sen : sentences(doc))
			new edu.kit.ipd.alicenlp.ivan.rules.TowardsPresentRule().apply(sen);
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testLikelihoodAndEvent() throws JWNLException {
		for (CoreMap sen : sentences(doc)) {
			new edu.kit.ipd.alicenlp.ivan.rules.EventRule().apply(sen);
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
