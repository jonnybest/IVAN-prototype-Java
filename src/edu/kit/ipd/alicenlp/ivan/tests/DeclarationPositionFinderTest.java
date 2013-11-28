package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.annotateDeclarations;
import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.annotateSingleDeclaration;
import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.checkEntrySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.LocationAnnotation;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.LocationListAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 *
 */
/**
 * Tests whether the declarations and locations and directions are properly
 * found
 * 
 * @author Jonny
 * 
 */
public class DeclarationPositionFinderTest {

	static String[] locations = {
			"There is a bunny on the right side.",
			"A plate is on the table",
			"The girl stands in the far right corner.",
			"At the start the astronaut is facing to the front "
					+ "of the screen and the monster on wheels is positioned "
					+ "towards the back of the screen." };
	static String[] directions = {
			"Hank is turned left 3 degrees.",
			"Hank faces towards the south.",
			"Sary is facing southwards.",
			"At the start the astronaut is facing to the front of "
					+ "the screen and the monster on wheels is positioned "
					+ "towards the back of the screen." };
	static String[] negatives = {
			// "There is a bunny in the picture.", // this is pretty hard to fix
			"Being good is virtous.", "The bunny is tall.",
			"Both spin quickly five times, then they come to a rest.",
			"Bunny lies face down.", "Potter stands up." };

	static List<CoreMap> locationlist = new ArrayList<CoreMap>();
	static List<CoreMap> directionlist = new ArrayList<CoreMap>();
	static List<CoreMap> negativeslist = new ArrayList<CoreMap>();

	static boolean setupDone = false;

	/**
	 * Loads the batch test files and prepares the test lists
	 * 
	 */
	// @BeforeClass
	public static void setup() {
		if (setupDone)
			return;

		// load corpus examples for locations
		String inputlocs = loadTestFile("locations.txt");
		// annotate corpus examples
		annotateSentence(inputlocs, locationlist);

		// load corpus examples for directions
		String inputdirs = loadTestFile("directions.txt");
		// annotate corpus examples
		annotateSentence(inputdirs, directionlist);

		// load corpus examples for directions
		String inputboth = loadTestFile("bothdirectionsandlocations.txt");
		// annotate corpus examples
		annotateSentence(inputboth, locationlist);
		annotateSentence(inputboth, directionlist);

		// load corpus examples for negative examples
		String inputnegs = loadTestFile("negatives.txt");
		String inputdecls = loadTestFile("declarationonly.txt");
		// annotate corpus examples
		annotateSentence(inputnegs, negativeslist);
		annotateSentence(inputdecls, negativeslist);

		// annotate in-class examples (from static strings)
		for (String l : locations) {
			annotateSentence(l, locationlist);
		}
		for (String d : directions) {
			annotateSentence(d, directionlist);
		}
		for (String neg : negatives) {
			annotateSentence(neg, negativeslist);
		}
	}

	private static String loadTestFile(String testfilename) {
		/**
		 * this stuff is c&p'd from edu.kit.alicenlp.konkordanz
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
	 * @param location
	 * @param sentencelist
	 */
	private static void annotateSentence(String location,
			List<CoreMap> sentencelist) {
		sentencelist = annotateDeclarations(location).get(
				SentencesAnnotation.class);
	}

	/**
	 * A simple test about creating the proper entity info
	 * 
	 */
	@Test
	public void testFindBunny() {
		String text = "Behind the Broccoli, there is a Bunny facing south.", name = "Bunny", direction = "south", location = "Behind the Broccoli";
		EntityInfo sample = new EntityInfo(name, location, direction);
		CoreMap sentence = annotateSingleDeclaration(text);
		List<EntityInfo> entities = DeclarationPositionFinder.findAll(sentence);
		for (EntityInfo en : entities) {
			if (!sample.equals(en)) {
				fail(en + " did not satisfy test case.");
			}
			System.out.println("Success! " + en + " equals " + sample);
		}
		if (entities.isEmpty()) {
			fail("Analyzer did not find anything in test sentence.");
		}
	}

	/**
	 * Tests the location files for locations with hasLocation
	 * 
	 */
	@Test
	public void testHasLocation() {
		setup();
		for (CoreMap location : locationlist) {
			assertTrue("Expected a location in this sentence: " + location,
					DeclarationPositionFinder.hasLocation(location));
		}
		for (CoreMap neg : negativeslist) {
			assertFalse("Expected no location in \"" + neg + "\"",
					DeclarationPositionFinder.hasLocation(neg));
		}
	}

	/**
	 * Tests the location files with getLocation
	 * 
	 */
	@Test
	public void testGetLocation() {
		EntityInfo simplein = DeclarationPositionFinder
				.getLocation(annotateSingleDeclaration("There is a ghost in this house."));
		assertNotNull("simple failed", simplein);
		if (!"in this house".equals(simplein.getLocation())) {
			fail("LocationListAnnotation is wrong");
		}
		EntityInfo inandon = DeclarationPositionFinder
				.getLocation(annotateSingleDeclaration("The house is in the background on the left hand side."));
		assertNotNull("in/on failed", inandon);
		if (!"in the background on the left hand side".equals(inandon
				.getLocation())) {
			fail("LocationListAnnotation is wrong");
		}
		EntityInfo behind = DeclarationPositionFinder
				.getLocation(annotateSingleDeclaration("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."));
		assertNotNull("behind failed", behind);
		if (!"Behind it to the right".equals(behind.getLocation())) {
			fail("LocationListAnnotation is wrong: " + behind);
		}
	}

	/**
	 * Tests a single sentence with getDirection
	 * 
	 */
	@Test
	public void testGetDirection() {
		String text = "Behind the Broccoli, there is a Bunny facing south.", name = "Bunny", direction = "south";

		CoreMap sentence = annotateSingleDeclaration(text);
		EntityInfo entity = DeclarationPositionFinder.getDirection(sentence);

		// direction
		assertEquals("Analyzer did not find correct direction in sample.",
				direction, entity.getDirection());
		// name
		assertEquals("Analyzer did not find correct name in direction sample.",
				name, entity.getEntity());
	}

	/**
	 * Tests a few sentences with whole declarations
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testGetDeclarations() throws IvanException {
		// String text = "There is a boy and a girl." + " "
		// + "The characters are a ninja tortoise, a rabbit and a T-Rex."+ " "
		// + "In the scene there are a boy and a girl."+ " "
		// +
		// "The start depicts a boy facing to the right of the screen, and a woman facing to the front."+
		// " "
		// + "In the far left is a Mailbox and in front of it is a Frog."+ " "
		// +
		// "Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."+
		// " "
		// +
		// "In the foreground there sits a frog on the left and a hare on the right of the screen.";

		Map<String, String[]> solutions = new TreeMap<String, String[]>();
		// solutions.put( // bug in stanford: ninja tortoise is not annotated
		// with noun compound modifier :(
		// "The characters are a ninja tortoise, a rabbit and a T-Rex.",
		// new String[] { "tortoise", "rabbit", "T-Rex" });
		solutions.put(
				"The characters are a street lamp, a rabbit and a T-Rex.",
				new String[] { "street lamp", "rabbit", "T-Rex" });
		// grammar error?
		// solutions.put("In the far left is a Mailbox and in front of it is a Frog.",
		solutions
				.put("In the far left there is a Mailbox and in front of it there is a Frog.",
						new String[] { "Mailbox", "Frog" });
		solutions
				.put("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter.",
						new String[] { "duckling" });
		// grammar error?
		// solutions.put("In the foreground there sits a frog on the left and a hare on the right of the screen.",
		solutions
				.put("In the foreground sits a frog on the left and a hare on the right of the screen.",
						new String[] { "frog", "hare" });

		// List<CoreMap> sentencelist = new ArrayList<CoreMap>();
		// annotateSentence(text, sentencelist);
		DeclarationPositionFinder proto = DeclarationPositionFinder
				.getInstance();
		// for (CoreMap se : sentencelist) {
		// //proto.recogniseNames(se);
		// List<EntityInfo> ei = proto.getDeclarations(se);
		// if (ei.isEmpty()) {
		// fail("Analyzer failed to find any declarations in sample sentence: "
		// + se);
		// }
		// System.out.println("These are declarations: " + ei);
		// }
		boolean fail = false;
		for (Entry<String, String[]> sol : solutions.entrySet()) {
			CoreMap anno = annotateSingleDeclaration(sol.getKey());
			List<EntityInfo> einfos = proto.getDeclarations(anno);
			for (EntityInfo info : einfos) {
				boolean matched = false;
				for (String name : sol.getValue()) {
					if (name.equalsIgnoreCase(info.getEntity())) {
						matched = true;
						continue;
					}
				}
				if (!matched) {
					// fail("Entity not recognised: " + info.getEntity() +
					// " in sentence \"" + sol.getKey() +"\"");
					fail("Entity not recognised:\n " + info.getEntity()
							+ "\n in sentence\n \"" + sol.getKey() + "\"");
					fail = true;
					System.out
							.println(anno
									.get(CollapsedCCProcessedDependenciesAnnotation.class));
				} else {
					System.out.println("Good: " + info.getEntity());
				}
			}
		}
		if (fail)
			fail("Failed test. See warnings for details.");
	}

	/**
	 * Some select cases for the recognition, which are hard, but should work.
	 * Non-working cases go into testRecogniseNamesHard.
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseThereIsBoyGirl() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"There is a boy and a girl.", new String[] { "boy", "girl" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseDuckling() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter.",
				new String[] { "duckling" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseBoy() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the scene there are a boy and a girl.", new String[] {
						"boy", "girl" });
		checkEntrySet(sol);
	}

	/**
	 * This test checks wether the class properly learns entities that are in
	 * the same document.
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testLearnDeclarations() throws IvanException {
		String input = "The ground is covered with grass. "
				// +
				// "In the foreground there is a monkey in the middle facing southwest. "
				+ "In the foreground there is a monkey facing southwest. "
				+ "On the right side of the ground, there is a broccoli. "
				+ "Behind the monkey, to the right, there is a bucket.";

		ArrayList<EntityInfo> reference = new ArrayList<EntityInfo>();
		reference.add(new EntityInfo("ground"));
		reference
				.add(new EntityInfo("monkey", "In the foreground", "southwest"));
		reference.add(new EntityInfo("broccoli",
				"On the right side of the ground", null));
		reference.add(new EntityInfo("bucket",
				"Behind the monkey, to the right", null));

		DeclarationPositionFinder proto = DeclarationPositionFinder
				.getInstance();
		Annotation doc = annotateDeclarations(input);

		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			proto.learnDeclarations(sentence);
		}

		InitialState actual = proto.getCurrentState();
		assertThat("count mismatch", actual.size(), is(reference.size()));

		// EntityInfo ground = state.getSingle("ground");
		// assertEquals("basic equality test", ground, output.get(0));

		for (EntityInfo ei : reference) {
			EntityInfo sibling = actual.getSingle(ei.getEntity());
			if (sibling != null) {
				// System.out.println("Siblings are " + (sibling.equals(ei) ?
				// "equal" : "not equal: " + ei + " and " + sibling));
				assertEquals("Siblings are "
						+ (sibling.equals(ei) ? "equal" : "not equal: " + ei
								+ " and " + sibling), ei.toString(),
						sibling.toString());
			}
			assertTrue("missing entity info: " + ei + ", possible match: "
					+ sibling, actual.contains(ei));
		}
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

		// constructin test
		@SuppressWarnings("unused")
		DeclarationPositionFinder myannotator = new DeclarationPositionFinder(
				"hi", new Properties());

		// functional test
		String text = "In the background on the left hand side there is a PalmTree. "
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. "
				// + "To the right of the mailbox there is a Frog facing east. "
				// // this one is probably tough?
				+ "In the foreground on the right hand side there is a Bunny facing southwest. ";
		Annotation doc = annotateDeclarations(text);

		// lets see if there are any annotations at all
		assertEquals("Sentences are missing", 3 /* 5 */,
				doc.get(SentencesAnnotation.class).size());

		// the sentences in this test should all have some annotation or another
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertTrue("Sentences was not analyzed: " + sentence.toString(),
					sentence.containsKey(LocationListAnnotation.class));
			LocationListAnnotation locs = sentence
					.get(LocationListAnnotation.class);
			assertNotNull(locs);
			System.out.println(sentence.get(LocationListAnnotation.class)
					+ ": " + sentence.toString());

			assertTrue("there are no locations in this location",
					locs.size() > 0);

			for (LocationAnnotation l : locs) {
				assertNotNull(l);
				assertTrue("description is too short",
						l.getLocation().size() > 2);
				assertTrue("referent is too short", l.getReferent().size() > 2);
			}
		}
	}

	/**
	 * Tests the use of "in front of"
	 * 
	 */
	@Test
	public void inFrontOfTest() {
		String text = "In front of the Bunny there is a Broccoli.";
		Annotation doc = annotateDeclarations(text);

		// lets see if there are any annotations at all
		assertEquals("Sentences are missing", 1 /* 5 */,
				doc.get(SentencesAnnotation.class).size());

		// the sentences in this test should all have some annotation or another
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertTrue("Sentences was not analyzed: " + sentence.toString(),
				sentence.containsKey(LocationListAnnotation.class));
		LocationListAnnotation locs = sentence
				.get(LocationListAnnotation.class);
		assertNotNull(locs);
		System.out.println(sentence.get(LocationListAnnotation.class) + ": "
				+ sentence.toString());

		assertTrue("there are no locations in this location", locs.size() > 0);

		Tree t = sentence.get(TreeAnnotation.class).skipRoot().firstChild();

		for (LocationAnnotation l : locs) {
			assertNotNull(l);
			Assert.assertThat("location is not correct", l.getLocation(), is(t));
			assertTrue("referent is too short", l.getReferent().size() > 2);
		}

	}

	/**
	 * Tests whether the tree annotations match up
	 * 
	 */
	@Test
	public void ReferentTestStanford() {
		{
			String text = "In the background on the left hand side there is a PalmTree.";
			Annotation doc = annotateDeclarations(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleDeclaration(
					"In the background on the left hand side").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location not correct", expected, actual);

			Tree expectedRef = annotateSingleDeclaration("a PalmTree")
					.get(TreeAnnotation.class).skipRoot().firstChild(); // skip
																		// root
																		// element
																		// and
																		// fragment
																		// element
			assertEquals("Referent is not correct", expectedRef,
					loc.getReferent());
		}
		{
			String text = "In the foreground on the right hand side there is a Bunny facing southwest.";
			Annotation doc = annotateDeclarations(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleDeclaration(
					"In the foreground on the right hand side").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location not correct", expected, actual);

			Tree expectedRef = annotateSingleDeclaration("a Bunny").get(
					TreeAnnotation.class).skipRoot(); // .getChild(2).getChild(1);
														// // picking the
														// location "by hand"
			assertEquals("Referent is not correct", expectedRef,
					loc.getReferent());
		}
		{
			String text = "In the foreground there is Mik Jagger on the right hand side facing southwest.";
			Annotation doc = annotateDeclarations(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			assertEquals("Too few locations", 2, list.size());

			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleDeclaration("In the foreground").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location 1 not correct", expected, actual);

			LocationAnnotation loc2 = list.get(1);
			Tree actual2 = loc2.getLocation();
			Tree expected2 = annotateSingleDeclaration(text)
					.get(TreeAnnotation.class).skipRoot().getChild(2)
					.getChild(1).getChild(1);
			System.err.println(annotateSingleDeclaration(text).get(
					TreeAnnotation.class).skipRoot());
			assertEquals("Location 2 not correct", expected2, actual2);

			assertEquals("Referents are not identical", loc.getReferent(),
					loc2.getReferent());
			Tree expectedRef = annotateSingleDeclaration("Mik Jagger").get(
					TreeAnnotation.class).skipRoot(); // skip root element
			assertEquals("Referent is not correct", expectedRef,
					loc.getReferent());
		}
	}

}
