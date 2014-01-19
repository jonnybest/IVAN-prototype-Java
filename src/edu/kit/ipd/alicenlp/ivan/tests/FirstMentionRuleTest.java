package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
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
	public void test() throws JWNLException, IvanException {
		FirstMentionRule rule = new FirstMentionRule();
		Annotation doc = TestUtilities.annotateDeclarations("In the background on the left hand side there is a palm tree. "
				+ "In the foreground on the left hand side there is a closed mailbox facing southeast. "
				+ "On the right of the mailbox there is a frog facing east. "
				+ "In the foreground on the right hand side there is a bunny facing southwest. "
				+ "In front of the bunny there is a broccoli.");
		boolean apply = rule.apply(doc);
		assertTrue(apply);

		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (int index : rule.getResults()) {
			Classification kind = sentences.get(index).get(IvanAnnotations.SentenceClassificationAnnotation.class);
			assertThat(kind, is(Classification.SetupDescription));
		}
	}

}
