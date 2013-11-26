/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.stanford.nlp.util.logging.Redwood.log;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.data.DocumentErrorAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanError;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SpanAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

/**
 * @author Jonny
 * 
 */
public class StaticDynamicClassifierTest {

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

	/**
	 * Test method for
	 * {@link edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier#classifySentence(edu.stanford.nlp.ling.IndexedWord, edu.stanford.nlp.util.CoreMap)}
	 * .
	 */
	@Test
	public void classifyFilesSetupTest() {
		// get instance
		StaticDynamicClassifier proto = StaticDynamicClassifier.getInstance();

		// annotate corpus examples
		annotateSentence(inputlocs, setuplist);

		for (CoreMap sentence : setuplist) {
			IndexedWord root = null;
			try {
				root = BaseRule.getRoot(sentence);
			} catch (RuntimeException e) {
				fail("Rooting \"" + sentence + "\" caused an exception. "
						+ e.getMessage());
			}
			Classification result = null;
			try {
				result = proto.classifySentence(root, sentence);
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
			IndexedWord root = BaseRule.getRoot(sentence);
			Classification result = null;
			try {
				result = proto.classifySentence(root, sentence);
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
		Annotation doc = annotateText(text);

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
	 * Annotates a document with our customized pipeline.
	 * 
	 * @param text
	 *            A text to process
	 * @return The annotated text
	 */
	private static Annotation annotateText(String text) {
		Annotation doc = new Annotation(text);

		StanfordCoreNLP pipeline;

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		// alternative: wsj-bidirectional
		try {
			props.put(
					"pos.model",
					"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// adding our own annotator property
		props.put("customAnnotatorClass.sdclassifier",
				"edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");
		// adding our declaration finder
		props.put("customAnnotatorClass.declarations",
				"edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");

		// configure pipeline
		props.put(
				"annotators", "tokenize, ssplit, pos, lemma, ner, parse, declarations, sdclassifier"); //$NON-NLS-1$ //$NON-NLS-2$
		pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(doc);
		return doc;
	}

	/**
	 * A positive test for ACTION annotations. If this test passes, the analyzer
	 * has correctly identified an action.
	 */
	@Test
	public void positiveActionTest() {
		Annotation doc = annotateText("The penguin jumps once.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertThat("jumps sentence classified wrong",
				sentence.get(Classification.class),
				is(Classification.ActionDescription));

		Annotation doc2 = annotateText("The boy and the girl lift their left arms simulteanously as well.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("lift sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.ActionDescription));

		Annotation doc3 = annotateText("After a short pause, the penguin turns around towards the back of the bucket behind it, jumps onto its stomach and slides towards the bucket, flapping its wings again.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("turns around sentence classified wrong",
				sentence3.get(Classification.class),
				is(Classification.ActionDescription));

	}

	/**
	 * A negative test for ACTION annotations. If this test passes, the analyzer
	 * has correctly identified an non-action.
	 */
	@Test
	public void negativeActionTest() {
		Annotation doc = annotateText("A very short time passes.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("time passes sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.ActionDescription)));

		Annotation doc2 = annotateText("A frog sits left of the Brokkoli facing it.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("sits sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.ActionDescription)));

		// The start depicts a boy facing to the right of the screen, and a
		// woman facing to the front.
		Annotation doc3 = annotateText("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("appears sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.ActionDescription)));

	}

	/**
	 * A positive test for EVENT annotations. If this test passes, the analyzer
	 * has correctly identified an event.
	 */
	@Test
	public void positiveEventTest() {
		Annotation doc = annotateText("The grinning cat appears in the branches of the tree.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertThat("appears sentence classified wrong",
				sentence.get(Classification.class),
				is(Classification.EventDescription));

		Annotation doc2 = annotateText("The bunny jumps into the hole and disappears.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("jumps, disappears sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.EventDescription));

		Annotation doc4 = annotateText("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence4 = doc4.get(SentencesAnnotation.class).get(0);
		assertThat("flame sentence classified wrong",
				sentence4.get(Classification.class),
				is(Classification.EventDescription));

	}

	/**
	 * The test for "stop" events.
	 * 
	 */
	@Test
	public void positiveHardEventTest() {
		Annotation doc3 = annotateText("She stops in front of the rabbit.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("flame sentence classified wrong",
				sentence3.get(Classification.class),
				is(Classification.EventDescription));
	}

	/**
	 * A negative test for EVENT annotations. If this test passes, the analyzer
	 * has correctly identified something that is not an event.
	 */
	@Test
	public void negativeEventTest() {
		Annotation doc = annotateText("A very short time passes.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("time passes sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.EventDescription)));

		Annotation doc2 = annotateText("A frog sits left of the Brokkoli facing it.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("frog sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.EventDescription)));

		// The start depicts a boy facing to the right of the screen, and a
		// woman facing to the front.
		Annotation doc3 = annotateText("The start depicts a boy facing to the right of the screen, and a woman facing to the front.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("depicts sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.EventDescription)));

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
		Annotation doc2 = annotateText("A very short time passes.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("passes sentence classified wrong",
				sentence2.get(Classification.class),
				is(Classification.TimeDescription));

		// contains duration
		Annotation doc3 = annotateText("The fire lasts for around 10 seconds.");
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
	 * A negative test for TIME annotations. If this test passes, the analyzer
	 * has correctly identified something that is not a description of a time
	 * period.
	 */
	@Test
	public void negativeTimeTest() {
		Annotation doc = annotateText("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("flame sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.TimeDescription)));

		Annotation doc2 = annotateText("The scene takes place in the desert.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("desert sentence classified wrong",
				sentence2.get(Classification.class),
				is(not(Classification.TimeDescription)));

		Annotation doc3 = annotateText("Ground is covered with green grass.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("Ground sentence classified wrong",
				sentence3.get(Classification.class),
				is(not(Classification.TimeDescription)));

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
			Annotation doc = annotateText("I see a palm tree on the left of the screen, a mailbox in front of it.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			System.out.println(sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class));
			assertThat("I see sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.ErrorDescription));
		}
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
			Annotation doc = annotateText("On the left side in the background, there is a rabbit. "
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

			List<ErrorMessageAnnotation> myerrors = doc.get(DocumentErrorAnnotation.class);
			assertNotNull("Error tag is missing.",
					myerrors);

			assertThat("hare/bunny error classified wrong",
					sentence.get(Classification.class),
					is(Classification.ErrorDescription));

			assertThat(myerrors.get(0).getSpan(), is(bunnyspan));
		}
	}
	
	/**
	 * A positive test for ERROR annotations. If this test passes, the analyzer
	 * identified an undesirable sentence.
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
				Annotation doc = annotateText("On the left side in the background, there is a rabbit."
						+ " The rabbit is called Harry."
						+ " On the right side, in the foreground, there is a hare."
						+ " The hare is called Lucas.");
	
				List<ErrorMessageAnnotation> errors = doc
						.get(DocumentErrorAnnotation.class);
				for (ErrorMessageAnnotation err : errors) {
					
					assertThat("Error tag should not be present if proper names are used!",
							err.getType(), is(not(IvanError.SYNONYMS)));
				}
			}
			/*
			 * rule: no synonyms, no errors.
			 */
			{
				/** Make sure that non-synonymous usages are alright.
				 * 
				 */
				Annotation doc = annotateText("On the left side in the background, there is a bunny."
						+ " On the right side, in the foreground, there is a hare."
						);
	
				List<ErrorMessageAnnotation> errors = doc
						.get(DocumentErrorAnnotation.class);
				for (ErrorMessageAnnotation err : errors) {
					
					assertThat("Error tag should not be present for non-synonyms!",
							err.getType(), is(not(IvanError.SYNONYMS)));
				}
			}
		}
	}

	private Span makeSpan(CoreLabel label) {
		return Span.fromValues(label.get(CharacterOffsetBeginAnnotation.class),
				label.get(CharacterOffsetEndAnnotation.class));
	}

	private CoreLabel findWord(String name, List<CoreLabel> list) {
		for (CoreLabel c : list) {
			if (c.originalText().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * A negative test for ERROR annotations. If this test passes, the analyzer
	 * has found a valid sentence.
	 */
	@Test
	public void negativeErrorTest() {
		Annotation doc = annotateText("The rabbit screams \"Ahhhhhhhh!\" and turns around towards the hole.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("flame sentence classified wrong",
				sentence.get(Classification.class),
				is(not(Classification.ErrorDescription)));
	}

	/**
	 * A positive test for SETUP annotations. If this test passes, the analyzer
	 * identified a description of the scene's initial state.
	 */
	@Test
	public void positiveSetupTest() {
		{
			Annotation doc = annotateText("The scene takes place on the grass.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("takes place sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc3 = annotateText("The ground is covered with grass.");
			CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
			assertThat("simple sentence classified wrong",
					sentence3.get(Classification.class),
					is(Classification.SetupDescription));
		}
		{
			// The ground is covered with grass, the sky is blue.
			Annotation doc2 = annotateText("The ground is covered with grass, the sky is blue.");
			CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
			assertThat("comma sentence classified wrong",
					sentence2.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * A negative test for SETUP annotations. If this test passes, the analyzer
	 * has found something other then a setup description.
	 */
	@Test
	public void negativeSetupTest() {
		{
			Annotation doc = annotateText("The rabbit screams \"You cannot stand on the right side!\" and turns around towards the hole.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertNotNull("class is missing",
					sentence.get(Classification.class));
			assertThat("utterance sentence classified wrong",
					sentence.get(Classification.class),
					is(not(Classification.SetupDescription)));
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
		Annotation doc = annotateText(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(Classification.class));
			assertThat("facing sentence classified wrong: " + sentence,
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Visibility
	 */
	@Test
	public void positiveHardSetupVisibilityTest() {
		{
			Annotation doc = annotateText("The grinning cat is not visible.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("visible sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Special verb: Depict
	 */
	@Test
	public void positiveHardSetupDepictTest() {
		{
			Annotation doc = annotateText("The start depicts a boy facing to the right of the screen, and a woman facing to the front.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("depicts sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Root is a noun
	 */
	@Test
	public void positiveHardSetupNounTest() {
		{
			Annotation doc = annotateText("Rightmost of the stage, in the back, is a sunflower, facing towards the characters.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("sunflower sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc = annotateText("At the start of the scene, on the left is a light bulb which is off.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("bulb sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
		// Next to the bulb on the ground is a switch, with a brown monkey next
		// to it, facing the button but slightly turned towards the viewer.
		{
			Annotation doc = annotateText("Next to the bulb on the ground is a switch, "
					+ "with a brown monkey next to it, facing the button but slightly turned "
					+ "towards the viewer.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("switch sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}

	/**
	 * Root is an adjective
	 */
	@Test
	public void positiveHardSetupAdjectiveTest() {
		{
			Annotation doc = annotateText("The grass is green and the sky is blue.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("green sentence classified wrong",
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
		{
			Annotation doc = annotateText("The sky is blue and the stage is a field of grass.");
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
			assertThat("blue sentence classified wrong",
					sentence.get(Classification.class),
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
		Annotation doc = annotateText(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(Classification.class));
			assertThat("facing sentence classified wrong: " + sentence,
					sentence.get(Classification.class),
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

		Annotation doc = annotateText(text);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertNotNull("class is missing",
					sentence.get(Classification.class));
			assertThat("takes place sentence classified wrong: " + sentence,
					sentence.get(Classification.class),
					is(Classification.SetupDescription));
		}
	}
}
