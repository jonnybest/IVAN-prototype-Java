package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.LocationAnnotation;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.LocationListAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

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
	private static StanfordCoreNLP pipeline = null;

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
	protected static void annotateSentence(String location,
			List<CoreMap> sentencelist) {
		sentencelist = annotate(location).get(SentencesAnnotation.class);
	}

	private CoreMap annotateSingleSentence(String text) {
		return annotate(text).get(SentencesAnnotation.class).get(0);
	}

	@Test
	public void testFindAll() {
		String text = "Behind the Broccoli, there is a Bunny facing south.", name = "Bunny", direction = "south", location = "Behind the Broccoli";
		EntityInfo sample = new EntityInfo(name, location, direction);
		CoreMap sentence = annotateSingleSentence(text);
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

	@Test
	public void testGetLocation() {
		EntityInfo simplein = DeclarationPositionFinder
				.getLocation(annotateSingleSentence("There is a ghost in this house."));
		assertNotNull("simple failed", simplein);
		if (!"in this house".equals(simplein.getLocation())) {
			fail("LocationListAnnotation is wrong");
		}
		EntityInfo inandon = DeclarationPositionFinder
				.getLocation(annotateSingleSentence("The house is in the background on the left hand side."));
		assertNotNull("in/on failed", inandon);
		if (!"in the background on the left hand side".equals(inandon
				.getLocation())) {
			fail("LocationListAnnotation is wrong");
		}
		EntityInfo behind = DeclarationPositionFinder
				.getLocation(annotateSingleSentence("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."));
		assertNotNull("behind failed", behind);
		if (!"Behind it to the right".equals(behind.getLocation())) {
			fail("LocationListAnnotation is wrong: " + behind);
		}
	}

	@Test
	public void testGetDirection() {
		String text = "Behind the Broccoli, there is a Bunny facing south.", name = "Bunny", direction = "south";

		CoreMap sentence = annotateSingleSentence(text);
		EntityInfo entity = DeclarationPositionFinder.getDirection(sentence);

		// direction
		assertEquals("Analyzer did not find correct direction in sample.",
				direction, entity.getDirection());
		// name
		assertEquals("Analyzer did not find correct name in direction sample.",
				name, entity.getEntity());
	}

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
			CoreMap anno = annotateSingleSentence(sol.getKey());
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

	private String warn(String string) {
		String out = "Warning: " + string;
		System.err.println(out);
		return out;
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

	@Test
	public void testRecogniseTortoise() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"The characters are a ninja tortoise, a rabbit and a T-Rex.",
				new String[] { "ninja tortoise", "rabbit", "T-Rex" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseAlice() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground, to the left of the stage stands the girl Alice.",
				new String[] { "girl" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseSitsFrog() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground on the left, there sits a green frog with a yellow belly facing eastsoutheast.",
				new String[] { "frog" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseFrogBroccoli() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground there is a frog on the left facing east-southeast and a broccoli on the right.",
				new String[] { "frog", "broccoli" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseFrogHare() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground there sits a frog on the left and a hare on the right of the screen.",
				new String[] { "frog", "hare" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseDuckling() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter.",
				new String[] { "duckling" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseBoy() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the scene there are a boy and a girl.", new String[] {
						"boy", "girl" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseSwitch() throws IvanException {
		Entry<String, String[]> // these are pretty hard
		sol = new AbstractMap.SimpleEntry<String, String[]>(
				"Next to the bulb on the ground is a switch, with a brown monkey next to it, facing the button but slightly turned towards the viewer. ",
				new String[] { "switch", "monkey" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseThereSitsAFrog() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"On the right side of the palm tree there sits a frog.",
				new String[] { "frog" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseMonsteronwheels() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"At the start the astronaut is facing to the front of the screen and the monster on wheels is positioned towards the back of the screen.",
				new String[] { "astronaut", "monster" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecognisePieceofbrok() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"A piece of Brokkoli is in front of him.",
				new String[] { "A piece of Brokkoli" });
		checkEntrySet(sol);
	}

	@Test
	public void testRecogniseIsee() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"I see a palm tree on the left of the screen and a mailbox in front of it. ",
				new String[] { "palm tree", "mailbox" });
		checkEntrySet(sol);
	}

	/**
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseBunny() throws IvanException {
		checkEntrySet(new AbstractMap.SimpleEntry<String, String[]>(
				"In the background on the right, there sits a white bunny facing southsouthwest. ",
				new String[] { "bunny" }));
	}

	/**
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseSpaceship() throws IvanException {
		checkEntrySet(new AbstractMap.SimpleEntry<String, String[]>(
				"In the background, slightly on the moon stands a space ship with a sign reading \"UNITED STATES\".",
				new String[] { "space ship" }));
	}

	/** This checker method verifies a sample sentence against its solution, 
	 * unless the sample sentence was recognized as bad input.
	 * @param solution
	 * @throws IvanException
	 */
	private void checkEntrySet(Entry<String, String[]> solution)
			throws IvanException {
		CoreMap annoSentence = annotateSingleSentence(solution.getKey());
		annoSentence.get(TreeAnnotation.class).pennPrint();
		List<String> einfos = DeclarationPositionFinder
				.recogniseNames(annoSentence);
		if(annoSentence.get(Classification.class) != null
				&& annoSentence.get(Classification.class).equals(Classification.ErrorDescription))
		{
			System.out.println("Error: \"" + solution.getKey() + "\"\nis not valid IVAN input.");
			return;
		}
		if (einfos.size() != solution.getValue().length) {
			fail("Some entities were not recognised in in sentence \""
					+ solution.getKey() + "\"");
		}
		for (String foundname : einfos) {
			boolean matched = false;
			for (String solutionname : solution.getValue()) {
				if (solutionname.equalsIgnoreCase(foundname)) {
					matched = true;
					continue;
				}
			}
			if (!matched) {
				fail(foundname + " not recognised in sentence \""
						+ solution.getKey() + "\"");
			}
		}
	}

	@Test
	public void testLearnDeclarations() throws IvanException {
		String input = "The ground is covered with grass. "
				// +
				// "In the foreground there is a monkey in the middle facing southwest. "
				+ "In the foreground there is a monkey facing southwest. "
				+ "On the right side of the ground, there is a broccoli. "
				+ "Behind the monkey, to the right, there is a bucket.";

		ArrayList<EntityInfo> output = new ArrayList<EntityInfo>();
		output.add(new EntityInfo("ground"));
		output.add(new EntityInfo("monkey", "In the foreground", "southwest"));
		output.add(new EntityInfo("broccoli",
				"On the right side of the ground", null));
		output.add(new EntityInfo("bucket", "Behind the monkey, to the right",
				null));

		DeclarationPositionFinder proto = DeclarationPositionFinder
				.getInstance();
		Annotation doc = annotate(input);

		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			proto.learnDeclarations(sentence);
		}

		InitialState state = proto.getCurrentState();
		assertEquals("count mismatch", output.size(), state.size());

		// EntityInfo ground = state.getSingle("ground");
		// assertEquals("basic equality test", ground, output.get(0));

		for (EntityInfo ei : output) {
			EntityInfo sibling = state.getSingle(ei.getEntity());
			if (sibling != null) {
				// System.out.println("Siblings are " + (sibling.equals(ei) ?
				// "equal" : "not equal: " + ei + " and " + sibling));
				assertEquals("Siblings are "
						+ (sibling.equals(ei) ? "equal" : "not equal: " + ei
								+ " and " + sibling), ei.toString(),
						sibling.toString());
			}
			assertTrue("missing entity info: " + ei + ", possible match: "
					+ sibling, state.contains(ei));
		}
	}

	// private void printRecognitionAnalysis() throws IvanException {
	// // merge the two positive lists while eliminating duplicates
	// TreeSet<String> mysentences = new TreeSet<String>();
	// mysentences.addAll(Arrays.asList(directions));
	// mysentences.addAll(Arrays.asList(locations));
	//
	// // lets load some input files for good measure
	// List<String> splitfiles = new ArrayList<String>();
	// String newlineregex = "\r\n"; // this is a regex and just means
	// "new line"
	// splitfiles.addAll(Arrays.asList(loadTestFile("bothdirectionsandlocations.txt").split(newlineregex)));
	// splitfiles.addAll(Arrays.asList(loadTestFile("directions.txt").split(newlineregex)));
	// splitfiles.addAll(Arrays.asList(loadTestFile("locations.txt").split(newlineregex)));
	// mysentences.addAll(splitfiles);
	//
	// //
	// DeclarationPositionFinder proto =
	// DeclarationPositionFinder.getInstance();
	// for (String text : mysentences) {
	// CoreMap cm = annotateSingleSentence(text);
	// if (cm == null) {
	// continue;
	// }
	// List<String> names = proto.recogniseNames(cm);
	// // the names go into a left "column"
	// StringBuilder namesstring = new StringBuilder();
	// for (String n : names) {
	// namesstring.append(n);
	// namesstring.append(", ");
	// }
	// namesstring.delete(namesstring.length() - 2, namesstring.length());
	// // fill the left column until it is 40 chars wide
	// for (int i = namesstring.length(); i < 22; i++)
	// {
	// namesstring.append(' ');
	// }
	// // print names into first column
	// System.out.print(namesstring);
	// System.out.print("\t");
	// // print sentence into second column
	// System.out.println(text);
	// }
	// nop();
	// }

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
		Annotation doc = annotate(text);

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

	@Test
	public void inFrontOfTest() {
		String text = "In front of the Bunny there is a Broccoli.";
		Annotation doc = annotate(text);

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

	@Test
	public void ReferentTestStanford() {
		{
			String text = "In the background on the left hand side there is a PalmTree.";
			Annotation doc = annotate(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleSentence(
					"In the background on the left hand side").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location not correct", expected, actual);

			Tree expectedRef = annotateSingleSentence("a PalmTree")
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
			Annotation doc = annotate(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleSentence(
					"In the foreground on the right hand side").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location not correct", expected, actual);

			Tree expectedRef = annotateSingleSentence("a Bunny").get(
					TreeAnnotation.class).skipRoot(); // .getChild(2).getChild(1);
														// // picking the
														// location "by hand"
			assertEquals("Referent is not correct", expectedRef,
					loc.getReferent());
		}
		{
			String text = "In the foreground there is Mik Jagger on the right hand side facing southwest.";
			Annotation doc = annotate(text);
			CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);

			LocationListAnnotation list = sentence
					.get(LocationListAnnotation.class);
			assertEquals("Too few locations", 2, list.size());

			LocationAnnotation loc = list.get(0);
			Tree actual = loc.getLocation();
			Tree expected = annotateSingleSentence("In the foreground").get(
					TreeAnnotation.class).skipRoot();
			assertEquals("Location 1 not correct", expected, actual);

			LocationAnnotation loc2 = list.get(1);
			Tree actual2 = loc2.getLocation();
			Tree expected2 = annotateSingleSentence(text)
					.get(TreeAnnotation.class).skipRoot().getChild(2)
					.getChild(1).getChild(1);
			System.err.println(annotateSingleSentence(text).get(
					TreeAnnotation.class).skipRoot());
			assertEquals("Location 2 not correct", expected2, actual2);

			assertEquals("Referents are not identical", loc.getReferent(),
					loc2.getReferent());
			Tree expectedRef = annotateSingleSentence("Mik Jagger").get(
					TreeAnnotation.class).skipRoot(); // skip root element
			assertEquals("Referent is not correct", expectedRef,
					loc.getReferent());
		}
	}

	private static Annotation annotate(String text) {
		Annotation doc = new Annotation(text);

		if (pipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
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
			// konfiguriere pipeline
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse, decl"); //$NON-NLS-1$ //$NON-NLS-2$
			pipeline = new StanfordCoreNLP(props);
		}

		pipeline.annotate(doc);
		return doc;
	}

	// @Test
	public void test() {
		Annotation anno = annotate("Behind the Mailbox to the right, is a PalmTree.");
		System.out.println(anno.get(SentencesAnnotation.class).get(0)
				.get(CollapsedCCProcessedDependenciesAnnotation.class));
	}
}
