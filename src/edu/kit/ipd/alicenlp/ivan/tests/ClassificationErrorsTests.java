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
	 * These tests are cases where the sentence should be classified as ERROR because it does not have a verb.
	 * If these tests fail because Stanford CoreNLP suddenly recognises them better, please remove them.
	 */
	@Test
	public void testNoVerb(){
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities.annotateSingleClassification("The bunny says: “Come on! What time is it?\"");
			assertThat("This sentence should not have any verb", thing.get(SentenceClassificationAnnotation.class), is(Classification.ErrorDescription));
		}
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities.annotateSingleClassification("Alice answers to the bunny.");
			assertThat("This sentence should not have any verb", thing.get(SentenceClassificationAnnotation.class), is(Classification.ErrorDescription));
		}
	}
}