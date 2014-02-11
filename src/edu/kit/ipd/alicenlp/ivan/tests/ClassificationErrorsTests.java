package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.annotateClassifications;
import static edu.stanford.nlp.util.logging.Redwood.log;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.SentenceClassificationAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.DocumentErrorAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SpanAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.PrettyLogger;
import edu.stanford.nlp.util.logging.Redwood;

/**
 * Tests the sentence type classifier with working tests which are absolutely
 * neccessary
 * 
 * @author Jonny
 * 
 */
public class ClassificationErrorsTests {

	/**
	 * These tests are cases where the sentence should be classified as ERROR
	 * because it does not have a verb. If these tests fail because Stanford
	 * CoreNLP suddenly recognises them better, please remove them.
	 */
	@Test
	public void testNoVerb() {
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("The bunny says: “Come on! What time is it?\"");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("Alice answers to the bunny.");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("Alice Liddell says, \"Oh...\" and then moves to the bunny.");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
	}

	/**
	 * There are problems with these sentences and they produce weird outputs,
	 * but I don't know why or how to fix them.
	 * 
	 */
	@Test
	public void testUnknownProblem()
	{
		String words = "Alice Liddell says: \"Oh...\" and then moves to the bunny.";
		System.out.println("This is what the whole document looks like:");
		Annotation doc = TestUtilities.annotateClassifications(words);
		for (CoreMap sentence  : doc.get(SentencesAnnotation.class)) {
			System.out.println(sentence);
			System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}
		System.err.println("---");
		System.out.println("And this is what the single sentence looks like:");
		{
			CoreMap sentence = TestUtilities.annotateSingleClassification(words);
			System.out.println(sentence);
			System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}
	}
	
	@Test
	public void testBadRoot() {
		{
			// the problem with this sentence is manyfold. For one, its root
			// points to "going" instead of "says". Then the coref is wrong.
			// Then there is an appos annotation which should be clausal
			// complement (or something like that) instead.
			String words = "While going to the bunny, Alice says: \"Okay.\"";
			CoreMap tags = TestUtilities.annotateSingleClassification(words);
			assertThat("", tags.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}

	}
}