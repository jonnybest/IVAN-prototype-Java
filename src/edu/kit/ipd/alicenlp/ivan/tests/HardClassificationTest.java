/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.annotateClassifications;
import static edu.stanford.nlp.util.logging.Redwood.log;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.extjwnl.JWNLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.*;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SpanAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

/** Tests the sentence type classifier
 * @author Jonny
 * 
 */
public class HardClassificationTest {

	static List<CoreMap> setuplist = new ArrayList<CoreMap>();
	static List<CoreMap> negativeslist = new ArrayList<CoreMap>();
	private static String inputlocs;
	private static String inputdirs;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// load corpus examples for locations
		inputlocs = loadTestFile("setup.txt");

		// load corpus examples for directions
		inputdirs = loadTestFile("actions.txt");
	}

	/** Tests classification with the setup list.
	 * .
	 */
	@Test
	public void classifyFilesSetupTest() {
		// get instance
		StaticDynamicClassifier proto = StaticDynamicClassifier.getInstance();

		// annotate corpus examples
		annotateSentence(inputlocs, setuplist);

		for (CoreMap sentence : setuplist) {
			
			Classification result = null;
			try {
				result = proto.classifySentence(sentence.get(CoreAnnotations.TextAnnotation.class));
			} catch (JWNLException e) {
				fail("Classifying \"" + sentence + "\" caused an exception.");
			}
			if (result != Classification.SetupDescription) {
				fail("Wrong classification for setup sentence \"" + sentence
						+ "\"");
				// System.out.println("fails: " + sentence);
			}
		}

		// annotate corpus examples
		annotateSentence(inputdirs, negativeslist);

		for (CoreMap sentence : negativeslist) {
			Classification result = null;
			try {
				result = proto.classifySentence(sentence.get(OriginalTextAnnotation.class));
			} catch (JWNLException e) {
				fail("Classifying \"" + sentence + "\" caused an exception.");
			}
			if (result == Classification.SetupDescription) {
				fail("Wrong classification for action sentence \"" + sentence
						+ "\"");
			}
		}
	}

	/**
	 * @param location
	 * @param sentencelist
	 */
	protected static void annotateSentence(String location,
			List<CoreMap> sentencelist) {
		DeclarationPositionFinder proto = DeclarationPositionFinder
				.getInstance();
		Annotation anno = new Annotation(location);
		proto.getPipeline().annotate(anno);
		List<CoreMap> maps = anno.get(SentencesAnnotation.class);
		for (CoreMap coreMap : maps) {
			sentencelist.add(coreMap);
		}
	}

	private static String loadTestFile(String testfilename) {
		/**
		 * this stuff is mostly c&p'd from edu.kit.alicenlp.konkordanz
		 */
		// texts go here:
		List<String> texts = new LinkedList<String>();

		/*
		 * Load text from individual files
		 */
		File infile = new File("test/files/" + testfilename);
		// File[] listOfFiles = folder.listFiles();
		File[] listOfFiles = { infile };
		// this is our buffer to reading. we read in 1024 byte chunks
		CharBuffer buffer = CharBuffer.allocate(1024);

		// read the files into a list
		System.out.println("{Reading files: ");
		for (File item : listOfFiles) {
			// skip things that are not files
			if (item.isFile()) {
				System.out.println(item.getName() + ", ");
				/*
				 * actual file loading
				 */
				// this stringbuffer will contain a single textfile
				StringBuffer strbuffer = new StringBuffer();
				// reset the character buffer
				buffer.clear();
				try {
					// create input stream
					FileReader stream = new FileReader(item);
					// count the bytes
					int readbytes = 0;
					do {
						// read 1024 bytes from the file into the charbuffer
						readbytes = stream.read(buffer);
						if (readbytes < 0) {
							// read nothing
							break;
						}
						// copy the read bytes to the buffer
						strbuffer.append(buffer.array(), 0, readbytes);
						// reset the charbuffer
						buffer.clear();
					} while (readbytes > 0);
					// close stream
					stream.close();
					// save the text in the list of texts
					texts.add(strbuffer.toString());
					// System.out.println("{read: " +strbuffer.toString() +
					// " }");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (item.isDirectory()) {
				System.out.println("Skipped directory " + item.getName());
			}
		}
		return texts.get(0);
	}




	/**
	 * The test for "stop" events.
	 * 
	 */
	@Test
	public void positiveHardEventTest() {
		Annotation doc3 = annotateClassifications("She stops in front of the rabbit.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("flame sentence classified wrong",
				sentence3.get(SentenceClassificationAnnotation.class),
				is(Classification.EventDescription));
	}


	/**
	 * A positive test for SETUP annotations. If this test passes, the analyzer
	 * identified a description of the scene's initial state.
	 */
	@Test
	public void positiveSetupTest() {
		{
			Annotation doc = annotateClassifications("The scene takes place on the grass.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("takes place sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc3 = annotateClassifications("The ground is covered with grass.");
			CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
			assertThat("simple sentence classified wrong",
					sentence3.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
		{
			// The ground is covered with grass, the sky is blue.
			Annotation doc2 = annotateClassifications("The ground is covered with grass, the sky is blue.");
			CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
			assertThat("comma sentence classified wrong",
					sentence2.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}

	/*
	 * Hard problems get their own tests.
	 */
	/**
	 * Facing
	 */
	@Test
	public void hardFacingTest() {
		String text = "The boy is facing the girl. "
				+ "The boy is facing towards the girl. "
				// + "The boy is facing towards at the girl. " // sic!
				+ "Alice is facing away from the bunny. "
				+ "The cowboy is facing west. "
				+ "The camel is facing the camera. "
				+ "It is facing the viewer. "
				+ "The duckling is facing just slightly past the monkey. "
				+ "She is facing towards the viewer but turned 45° to the left of the stage. "
				+ "At the start the astronaught is facing to the front of the screen and the monster on wheels is positioned towards the back of the screen.";
		Annotation doc = annotateClassifications(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(SentenceClassificationAnnotation.class));
			assertThat("facing sentence classified wrong: " + sentence,
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Visibility
	 */
	@Test
	public void positiveHardSetupVisibilityTest() {
		{
			Annotation doc = annotateClassifications("The grinning cat is not visible.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("visible sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Special verb: Depict
	 */
	@Test
	public void positiveHardSetupDepictTest() {
		{
			Annotation doc = annotateClassifications("The start depicts a boy facing to the right of the screen, and a woman facing to the front.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("depicts sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Root is a noun
	 */
	@Test
	public void positiveHardSetupNounTest() {
		{
			Annotation doc = annotateClassifications("Rightmost of the stage, in the back, is a sunflower, facing towards the characters.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("sunflower sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc = annotateClassifications("At the start of the scene, on the left is a light bulb which is off.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("bulb sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
		// Next to the bulb on the ground is a switch, with a brown monkey next
		// to it, facing the button but slightly turned towards the viewer.
		{
			Annotation doc = annotateClassifications("Next to the bulb on the ground is a switch, "
					+ "with a brown monkey next to it, facing the button but slightly turned "
					+ "towards the viewer.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("switch sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}



	/**
	 * Other gerunds
	 */
	@Test
	public void positiveHardSetupGerundsTest() {
		String text = "The crown is cut off at the top at the stage. "
				+ "The ground is covered with grass, the sky is blue. "
				+ "The ground is covered with grass, the sky is blue.";
		Annotation doc = annotateClassifications(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(SentenceClassificationAnnotation.class));
			assertThat("facing sentence classified wrong: " + sentence,
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * takes place
	 */
	@Test
	public void positiveHardSetupTakesPlaceTest() {
		String text = "The scene takes place on the grass."
				+ "The scene takes place on the grass."
				+ "The scene takes place in the desert."
				+ "The scene takes place on a meadow."
				+ "The scene takes place on the moon surface.";

		Annotation doc = annotateClassifications(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(SentenceClassificationAnnotation.class));
			assertThat("takes place sentence classified wrong: " + sentence,
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}
	
	/**
	 * Root is an adjective
	 */
	@Test
	public void positiveHardSetupAdjectiveTest() {
		{
			Annotation doc = annotateClassifications("The grass is green and the sky is blue.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("green sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc = annotateClassifications("The sky is blue and the stage is a field of grass.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("blue sentence classified wrong",
					sentence.get(SentenceClassificationAnnotation.class),
					is(Classification.SetupDescription));
		}
	}
	
	/**
	 * This is the "hard" test for a time sentence. It's hard, because the
	 * tagger does not recognise the verb properly.
	 */
	@Test
	public void positiveHardTimeTest() {
		// this test should go through, but it currently isn't analyzed
		// properly.
		// stanford doesn't recognise the verb "passes".
		Annotation doc4 = annotateClassifications("A very short time passes.");
		CoreMap sentence4 = doc4.get(SentencesAnnotation.class).get(0);
		assertThat("passes sentence classified wrong",
				sentence4.get(SentenceClassificationAnnotation.class),
				is(Classification.TimeDescription));
	}
	
	/**
	 * This is the "hard" test for a time sentence. It's hard, because the
	 * tagger does not recognise the verb properly.
	 */
	@Test
	public void negativeHardErrorTest() {
		// this test should go through, but it currently isn't analyzed
		// properly.
		// stanford doesn't recognise the verb "passes".
		Annotation doc4 = annotateClassifications("A very short time passes.");
		CoreMap sentence4 = doc4.get(SentencesAnnotation.class).get(0);
		assertThat("passes sentence classified wrong",
				sentence4.get(SentenceClassificationAnnotation.class),
				not(is(Classification.ErrorDescription)));
	}
}
