package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.annotateClassifications;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Tests the sentence type classifier with working tests which are absolutely
 * neccessary
 * 
 * @author Jonny
 * 
 */
public class ImportantClassificationTests {
	
	/**
	 * A positive test for ACTION annotations. If this test passes, the analyzer
	 * has correctly identified an action.
	 */
	@Test
	public void positiveActionTest() {
		Annotation doc = annotateClassifications("The penguin jumps once.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertThat("jumps sentence classified wrong",
				sentence.get(Classification.class),
				is(Classification.ActionDescription));

		Annotation doc2 = annotateClassifications("The boy and the girl lift their left arms simulteanously as well.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("lift sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.ActionDescription));

		Annotation doc3 = annotateClassifications("After a short pause, the penguin turns around towards the back of the bucket behind it, jumps onto its stomach and slides towards the bucket, flapping its wings again.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("turns around sentence classified wrong",
				sentence3.get(Classification.class),
				is(Classification.ActionDescription));

	}


	/**
	 * A negative test for TIME annotations. If this test passes, the analyzer
	 * has correctly identified something that is not a description of a time
	 * period.
	 */
	@Test
	public void negativeTimeTest() {
		Annotation doc = annotateClassifications("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("flame sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.TimeDescription)));

		Annotation doc2 = annotateClassifications("The scene takes place in the desert.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("desert sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.TimeDescription)));

		Annotation doc3 = annotateClassifications("Ground is covered with green grass.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("Ground sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.TimeDescription)));

	}

	/**
	 * A positive test for EVENT annotations. If this test passes, the analyzer
	 * has correctly identified an event.
	 */
	@Test
	public void positiveEventTest() {
		Annotation doc = annotateClassifications("The grinning cat appears in the branches of the tree.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertThat("appears sentence classified wrong",
				sentence.get(Classification.class),
				is(Classification.EventDescription));

		Annotation doc2 = annotateClassifications("The bunny jumps into the hole and disappears.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("jumps, disappears sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.EventDescription));

		Annotation doc4 = annotateClassifications("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence4 = doc4.get(SentencesAnnotation.class).get(0);
		assertThat("flame sentence classified wrong",
				sentence4.get(Classification.class),
				is(Classification.EventDescription));

	}
	

	/**
	 * A negative test for ERROR annotations. If this test passes, the analyzer
	 * has found a valid sentence.
	 */
	@Test
	public void negativeErrorTest() {
		Annotation doc = annotateClassifications("The rabbit screams \"Ahhhhhhhh!\" and turns around towards the hole.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("flame sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.ErrorDescription)));
	}
	

	/**
	 * This is a test for Stanford Pipeline compliance.
	 * 
	 * Here are the steps: (from the FAQ) 1. extend the class
	 * edu.stanford.nlp.pipeline.Annotator 2. I assume you're writing your own
	 * code to do the processing. Whatever code you write, you want to call it
	 * from a class that is a subclass of Annotator. Look at any of the existing
	 * Annotator classes, such as POSTaggerAnnotator, and try to emulate what it
	 * does. 3. Have a constructor with the signature (String, Properties) 4. If
	 * your new annotator is FilterAnnotator, for example, it must have a
	 * constructor FilterAnnotator(String name, Properties props) in order to
	 * work. 5. Add the property customAnnotatorClass.FOO=BAR 6. Using the same
	 * example, suppose your full class name is com.foo.FilterAnnotator, and you
	 * want the new annotator to have the name "filter". When creating the
	 * CoreNLP properties, you need to add the flag
	 * customAnnotatorClass.filter=com.foo.FilterAnnotator 7. You can then add
	 * "filter" to the list of annotators in the annotators property. When you
	 * do that, the constructor FilterAnnotator(String, Properties) will be
	 * called with the name "filter" and the properties files you run CoreNLP
	 * with. This lets you define any property flag you want. For example, you
	 * could name a flag filter.verbose and then extract that flag from the
	 * properties to determine the verbosity of your new annotator.
	 */
	@Test
	public void StanfordPipelineTest() {

		// Construction test
		@SuppressWarnings("unused")
		StaticDynamicClassifier myannotator = new StaticDynamicClassifier("hi",
				new Properties());

		// functional test
		String text = "In the background on the left hand side there is a PalmTree. "
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. "
				+ "Right to the mailbox there is a Frog facing east. "
				+ "In the foreground on the right hand side there is a Bunny facing southwest. "
				+ "In front of the Bunny there is a Broccoli.";
		Annotation doc = annotateClassifications(text);

		// lets see if there are any annotations at all
		assertEquals("Sentences are missing", 5,
				doc.get(SentencesAnnotation.class).size());

		// the sentences in this test should all have some annotation or another
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertTrue("Sentences was not classified: " + sentence.toString(),
					sentence.containsKey(Classification.class));
			assertNotNull(sentence.get(Classification.class));
			System.out.println(sentence.get(Classification.class) + ": "
					+ sentence.toString());
		}

	}

	/**
	 * A negative test for SETUP annotations. If this test passes, the analyzer
	 * has found something other then a setup description.
	 */
	@Test
	public void negativeSetupTest() {
		{
			Annotation doc = annotateClassifications("The rabbit screams \"You cannot stand on the right side!\" and turns around towards the hole.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertNotNull("class is missing",
					sentence.get(Classification.class));
			assertThat("utterance sentence classified wrong",
					sentence.get(Classification.class),
					is(not(Classification.SetupDescription)));
		}
	}


	/**
	 * A positive test for ERROR annotations. If this test passes, the analyzer
	 * identified an undesirable sentence.
	 */
	@Test
	public void positive1stPersonErrorTest() {
		/*
		 * rule: no first person reason: verb does not pertain to things
		 * happening in the scene
		 */
		Annotation doc = annotateClassifications("I see a palm tree on the left of the screen, a mailbox in front of it.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		System.out.println(sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class));
		assertThat("I see sentence classified wrong",
				sentence.get(Classification.class),
				is(Classification.ErrorDescription));
	}


	// Time examples:
	// The fire lasts for around 10 seconds.

	/**
	 * A positive test for TIME annotations. If this test passes, the analyzer
	 * has correctly identified time.
	 */
	@Test
	public void positiveTimeTest() {
		/*
		 * A very short time passes. The fire lasts for around 10 seconds. The
		 * flames continue to burn.
		 */
		// explicitly references passing time
		Annotation doc2 = annotateClassifications("A very short time passes.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("passes sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.TimeDescription));

		// contains duration
		Annotation doc3 = annotateClassifications("The fire lasts for around 10 seconds.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("fire around sentence classified wrong",
				sentence3.get(Classification.class),
				is(Classification.TimeDescription));

		// there is no time in this sentence. just a state. the duration is
		// implied, not explicit.
		// Annotation doc = annotateText("The flames continue to burn.");
		// CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		// assertThat("flames sentence classified wrong",
		// sentence.get(Classification.class),
		// is(Classification.TimeDescription));

	}

	/**
	 * A negative test for EVENT annotations. If this test passes, the analyzer
	 * has correctly identified something that is not an event.
	 */
	@Test
	public void negativeEventTest() {
		Annotation doc = annotateClassifications("A very short time passes.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("time passes sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.EventDescription)));

		Annotation doc2 = annotateClassifications("A frog sits left of the Brokkoli facing it.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("frog sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.EventDescription)));

		// The start depicts a boy facing to the right of the screen, and a
		// woman facing to the front.
		Annotation doc3 = annotateClassifications("The start depicts a boy facing to the right of the screen, and a woman facing to the front.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("depicts sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.EventDescription)));

	}


	/**
	 * A negative test for ACTION annotations. If this test passes, the analyzer
	 * has correctly identified an non-action.
	 */
	@Test
	public void negativeActionTest() {
		Annotation doc = annotateClassifications("A very short time passes.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("time passes sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.ActionDescription)));

		Annotation doc2 = annotateClassifications("A frog sits left of the Brokkoli facing it.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("sits sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.ActionDescription)));

		// The start depicts a boy facing to the right of the screen, and a
		// woman facing to the front.
		Annotation doc3 = annotateClassifications("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("appears sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.ActionDescription)));

	}

}