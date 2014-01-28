package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.extjwnl.JWNLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.rules.FirstMentionRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class FirstMentionRuleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testPositive() throws JWNLException, IvanException {
		FirstMentionRule rule = new FirstMentionRule();
		Annotation doc = TestUtilities
				.annotateDeclarations("In the background on the left hand side there is a palm tree. "
						+ "In the foreground on the left hand side there is a closed mailbox facing southeast. "
						+ "On the right of the mailbox there is a frog facing east. "
						+ "In the foreground on the right hand side there is a bunny facing southwest. "
						+ "In front of the bunny there is a broccoli.");
		boolean apply = rule.apply(doc);
		assertTrue(apply);

		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (int index : rule.getResults()) {
			Classification kind = sentences.get(index).get(
					IvanAnnotations.SentenceClassificationAnnotation.class);
			assertThat(kind, is(Classification.SetupDescription));
		}
	}

	@Test
	public void testNegative() throws JWNLException, IvanException {
		FirstMentionRule rule = new FirstMentionRule();
		Annotation doc = TestUtilities
				.annotateDeclarations("The ground is covered with grass, the sky is blue. In the background on the left hand side there is a palm tree. In the foreground on the left hand side there is a closed mailbox facing southeast. Right to the mailbox there is a frog facing east. In the foreground on the right hand side there is a bunny facing southwest. In front of the bunny there is a broccoli. The bunny turns to face the broccoli. The bunny hops three times to the broccoli. The bunny eats the broccoli. The bunny turns to face the frog. The bunny is tapping its foot twice. The frog ribbits. The frog turns to face northeast. The frog hops three times to northeast. The bunny turns to face the mailbox. The bunny hops three times to the mailbox. The bunny opens the mailbox. The bunny looks in the mailbox and at the same time the frog turns to face the bunny. The frog hops two times to the bunny. The frog ribbits.");
		boolean apply = rule.apply(doc);
		assertTrue(apply);

		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (int i = 6; i < doc.get(SentencesAnnotation.class).size(); i++) {
			Classification kind = sentences.get(i).get(
					IvanAnnotations.SentenceClassificationAnnotation.class);
			assertThat(kind, is(not(Classification.SetupDescription)));
		}
	}

	@Test
	public void testFindDirection() throws JWNLException, IvanException {
		Annotation doc = TestUtilities
				.annotateClassifications("There is a bunny on the left hand side. The bunny jumps up into the air. The bunny looks north.");
		assertTrue(new FirstMentionRule().apply(doc));
		CoreMap thirdSentence = doc.get(SentencesAnnotation.class).get(2);
		assertThat(
				thirdSentence
						.get(IvanAnnotations.SentenceClassificationAnnotation.class),
				is(Classification.SetupDescription));
	}

	/** Test a more complicated example
	 * @throws JWNLException
	 * @throws IvanException
	 */
	@Test
	public void testLeaveSecondDirectionAlone() throws JWNLException,
			IvanException {
		Annotation doc = TestUtilities
				.annotateClassifications("There is a bunny on the left hand side. The bunny is looking south. "
						+ "The bunny looks at the camera. The bunny faces the camera. The bunny is looking at the camera. The bunny turns to the camera."
//						+ " The bunny orients itself towards the camera." // "orient" has lexname verb.stative (42) and yields a wrong classification
						+ " The bunny looks towards the camera.");
		assertTrue(new FirstMentionRule().apply(doc));
		CoreMap secondSentence = doc.get(SentencesAnnotation.class).get(1);
		assertThat("Rule missed the first direction in this sentence",
				secondSentence
						.get(IvanAnnotations.SentenceClassificationAnnotation.class),
				is(Classification.SetupDescription));
		
		System.out.println(doc.get(IvanAnnotations.IvanEntitiesAnnotation.class));
		for (int i = 2; i < doc.get(SentencesAnnotation.class).size(); i++) {
			CoreMap followingSentence = doc.get(SentencesAnnotation.class).get(i);
			assertThat("Sentence is falsely classified as Setup: " + followingSentence,
					followingSentence
							.get(IvanAnnotations.SentenceClassificationAnnotation.class),
					is(Classification.ActionDescription));
		}
	}
}
