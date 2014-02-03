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
		{
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
	}
	
	/**
	 * A negative test for ERROR annotations. This test mitigates a regression error.
	 */
	@Test
	public void negative1stPersonErrorTest() {
		{
			/*
			 * rule: no first person reason: verb does not pertain to things
			 * happening in the scene
			 */
			Annotation doc = annotateClassifications("There is a hare facing north. The hare is in the foreground to the left. "
					+ "There is a rabbit facing west. The rabbit is in the background to the right. The rabbit is called Fluffy.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(4);
			System.out.println(sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class));
			if(sentence.get(IvanAnnotations.ErrorMessageAnnotation.class) != null)
				PrettyLogger.log(sentence.get(IvanAnnotations.ErrorMessageAnnotation.class));
			assertThat("false positive on error recognition",
					sentence.get(Classification.class),
					is(not(Classification.ErrorDescription)));
		}
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
		//Annotation doc2 = annotateClassifications("A very short time passes.");
		Annotation doc2 = annotateClassifications("A very short time is passing.");
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

		/** 
		 * How to fix this:
		 * The rule for classifying time is as follows:
		 * A) if the noun "time" occurs, the sentence mentions time
		 * B) if the verb "elapse" or its synonyms (pass, go by) occur, it mentions passing
		 * C) if the NER tag for "duration" is present, the sentence has a duration
		 * A sentence classifies as TimeDescription, if (A && B || C ) holds.
		 */
		
		// bad test moved to hard
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

	/**
	 * A positive test for ERROR annotations. If this test passes, the analyzer
	 * identified an undesirable sentence.
	 */
	@Test
	public void positiveNoSynonymousAgentsErrorTest() {
		{
			/*
			 * rule: different agents cannot be synonyms of each other reason:
			 * we resolve synonyms to the same Alice entity. They need a name at
			 * least.
			 */
			Annotation doc = annotateClassifications("On the left side in the background, there is a rabbit. "
					+ "On the right side, in the foreground, there is a hare.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(1);

			CoreLabel bunny = findWord("hare",
					sentence.get(TokensAnnotation.class));
			log("span: " + bunny.get(SpanAnnotation.class));
			log(Redwood.DBG, bunny.get(CharacterOffsetBeginAnnotation.class)
					+ " " + bunny.get(CharacterOffsetEndAnnotation.class)
					+ " offsets");
			Span bunnyspan = makeSpan(bunny);
			log(bunnyspan);

			List<IvanErrorMessage> myerrors = doc.get(DocumentErrorAnnotation.class);
			assertNotNull("Error tag is missing.",
					myerrors);

			assertThat("hare/bunny error classified wrong",
					sentence.get(Classification.class),
					is(Classification.ErrorDescription));

			assertThat(myerrors.get(0).getSpan(), is(bunnyspan));
		}
	}
	
	/**
	 * A negative test for ERROR annotations. If this test passes, the analyzer
	 * correctly ignored a desirable sentence.
	 */
	@Test
	public void negativeNoSynonymousAgentsErrorTest() {
		{
			/*
			 * rule: different agents cannot be synonyms of each other reason:
			 * we resolve synonyms to the same Alice entity. They need a name at
			 * least.
			 */
			{
				/** Make sure that names are alright.
				 * 
				 */
				Annotation doc = annotateClassifications("On the left side in the background, there is a rabbit."
						+ " The rabbit is called Harry."
						+ " On the right side, in the foreground, there is a hare."
						+ " The hare is called Lucas.");
	
				List<IvanErrorMessage> errors = doc
						.get(DocumentErrorAnnotation.class);
				if(errors != null)
				{
					for (IvanErrorMessage err : errors) {
						assertThat("Error tag should not be present if proper names are used!",
								err.getType(), is(not(IvanErrorType.SYNONYMS)));
					}
				}
			}
			/*
			 * rule: no synonyms, no errors.
			 */
			{
				/** Make sure that non-synonymous usages are alright.
				 * 
				 */
				Annotation doc = annotateClassifications("On the left side in the background, there is a bunny."
						+ " On the right side, in the foreground, there is a hare."
						);
	
				List<IvanErrorMessage> errors = doc
						.get(DocumentErrorAnnotation.class);
				if(errors != null)
				{
					for (IvanErrorMessage err : errors) {	
						assertThat("Error tag should not be present for non-synonyms!",
								err.getType(), is(not(IvanErrorType.SYNONYMS)));
					}
				}
			}
		}
	}

	private static Span makeSpan(CoreLabel label) {
		return Span.fromValues(label.get(CharacterOffsetBeginAnnotation.class),
				label.get(CharacterOffsetEndAnnotation.class));
	}

	private static CoreLabel findWord(String name, List<CoreLabel> list) {
		for (CoreLabel c : list) {
			if (c.originalText().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return null;
	}

	@Test
	public void testDirectionsInSaloon(){
		String text = "A saloon is in the left background. A barn is in the background right. A horse is in the foreground left. A monkey is in the foreground right. The saloon is facing south. The barn is facing southwest. The horse is facing the monkey. The monkey is facing the horse.";
		Annotation doc = TestUtilities.annotateClassifications(text);
		System.out.println(doc.get(IvanAnnotations.IvanEntitiesAnnotation.class));
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertThat("Bad tag: " + sentence,sentence.get(IvanAnnotations.SentenceClassificationAnnotation.class),is(Classification.SetupDescription));
		}
//		fail("stub");
	}
}