package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class DeclarationPositionFinderTest {
	
	static String[] locations = {
			"There is a bunny on the right side.",
			"A plate is on the table",
			"The girl stands in the far right corner.",
			"At the start the astronaut is facing to the front "
			+ "of the screen and the monster on wheels is positioned "
			+ "towards the back of the screen."
			};
	static String[] directions = {
			"Hank is turned left 3 degrees.", 
			"Hank faces towards the south.",
			"Sary is facing southwards.",
			"At the start the astronaut is facing to the front of "
			+ "the screen and the monster on wheels is positioned "
			+ "towards the back of the screen."
	};
	static String[] negatives = {
			//"There is a bunny in the picture.", // this is pretty hard to fix
			"Being good is virtous.",
			"The bunny is tall.",
			"Both spin quickly five times, then they come to a rest.",
			"Bunny lies face down.",
			"Potter stands up."
	};
	
	static List<CoreMap> locationlist = new ArrayList<CoreMap>();
	static List<CoreMap> directionlist = new ArrayList<CoreMap>();
	static List<CoreMap> negativeslist = new ArrayList<CoreMap>();
	
	static boolean setupDone = false;
	
	//@BeforeClass
	public static void setup()
	{
		if(setupDone)
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
		//File[] listOfFiles = folder.listFiles();
		File[] listOfFiles = {infile};
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
	protected static void annotateSentence(String location, List<CoreMap> sentencelist) {
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		Annotation anno = new Annotation(location);
		proto.getPipeline().annotate(anno);
		List<CoreMap> maps = anno.get(SentencesAnnotation.class);
		for (CoreMap coreMap : maps) {
			sentencelist.add(coreMap);
		}
	}
	
	private CoreMap annotateSingleSentence(String text)
	{
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		Annotation anno = new Annotation(text);
		proto.getPipeline().annotate(anno);
		List<CoreMap> maps = anno.get(SentencesAnnotation.class);
		for (CoreMap coreMap : maps) {
			return coreMap;
		}
		return null;
	}
	
	@Test
	public void testFindAll() {
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		String text = "Behind the Broccoli, there is a Bunny facing south.",
				name = "Bunny",
				direction = "south",
				location = "Behind the Broccoli";
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
			assertTrue("Expected a location in this sentence: " + location, DeclarationPositionFinder.hasLocation(location));			
		}		
		for (CoreMap neg : negativeslist) {
			assertFalse("Expected no location in \"" + neg + "\"", DeclarationPositionFinder.hasLocation(neg));
		}
	}

	@Test
	public void testGetLocation() {
		EntityInfo simplein = DeclarationPositionFinder.getLocation(annotateSingleSentence("There is a ghost in this house."));
		if (!"in this house".equals(simplein.getLocation())) {
			fail("Location is wrong"); 			
		}
		EntityInfo inandon = DeclarationPositionFinder.getLocation(annotateSingleSentence("The house is in the background on the left hand side."));
		if (!"in the background on the left hand side".equals(inandon.getLocation())) {
			fail("Location is wrong");
		}
		EntityInfo behind = DeclarationPositionFinder.getLocation(annotateSingleSentence("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."));
		if (!"Behind it to the right".equals(behind.getLocation())) {
			fail("Location is wrong: " + behind);
		}
	}

	@Test
	public void testGetDirection() {
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		String text = "Behind the Broccoli, there is a Bunny facing south.",
				name = "Bunny",
				direction = "south";

		CoreMap sentence = annotateSingleSentence(text);
		EntityInfo entity = proto.getDirection(sentence);

		if (entity.getDirection().equalsIgnoreCase(direction)) {
			fail("Analyzer did not find correct direction in sample.");
		}
		
		if (entity.getEntity().equalsIgnoreCase(name)) {
			fail("Analyzer did not find correct name in direction sample.");
		}
	}

	@Test
	public void testGetDeclarations() throws IvanException {
//		String text = "There is a boy and a girl." + " "
//				+ "The characters are a ninja tortoise, a rabbit and a T-Rex."+ " "
//				+ "In the scene there are a boy and a girl."+ " "
//				+ "The start depicts a boy facing to the right of the screen, and a woman facing to the front."+ " "
//				+ "In the far left is a Mailbox and in front of it is a Frog."+ " "
//				+ "Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."+ " "
//				+ "In the foreground there sits a frog on the left and a hare on the right of the screen.";
		
		Map<String, String[]> solutions = new TreeMap<String, String[]>();
		solutions.put("There is a boy and a girl.", 
				new String[]{"boy", "girl"});		
		solutions.put("The characters are a ninja tortoise, a rabbit and a T-Rex.", 
				new String[]{"ninja tortoise", "rabbit", "T-Rex"});
		solutions.put("In the scene there are a boy and a girl.", 
				new String[]{"boy", "girl"});
		solutions.put("In the far left is a Mailbox and in front of it is a Frog.", 
				new String[]{"Mailbox", "Frog"});
		solutions.put("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter.", 
				new String[]{"duckling"});
		solutions.put("In the foreground there sits a frog on the left and a hare on the right of the screen.", 
				new String[]{"frog", "hare"});
		
//		List<CoreMap> sentencelist = new ArrayList<CoreMap>();
//		annotateSentence(text, sentencelist);
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
//		for (CoreMap se : sentencelist) {
//			//proto.recogniseNames(se);
//			List<EntityInfo> ei = proto.getDeclarations(se);
//			if (ei.isEmpty()) {
//				fail("Analyzer failed to find any declarations in sample sentence: " + se);				
//			}
//			System.out.println("These are declarations: " + ei);
//		}
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
					fail("Entity not recognised: " + info.getEntity() + " in sentence \"" + sol.getKey() +"\"");
				}
			}
		}
	}

	@Test
	public void testRecogniseNames() throws IvanException {
		// I want to print a list of all recognitions to get an idea for what kinds of sentences are still improperly recognised
//		printRecognitionAnalysis();

		Map<String, String[]> solutions = new TreeMap<String, String[]>();
		solutions.put("There is a boy and a girl.", 
				new String[]{"boy", "girl"});		
		solutions.put("The characters are a ninja tortoise, a rabbit and a T-Rex.", 
				new String[]{"ninja tortoise", "rabbit", "T-Rex"});
		solutions.put("In the scene there are a boy and a girl.", 
				new String[]{"boy", "girl"});
		solutions.put("In the far left is a Mailbox and in front of it is a Frog.", 
				new String[]{"Mailbox", "Frog"});
		solutions.put("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter.", 
				new String[]{"duckling"});
		solutions.put("In the foreground there sits a frog on the left and a hare on the right of the screen.", 
				new String[]{"frog", "hare"});
		solutions.put("At the start the astronaut is facing to the front of the screen and the monster on wheels is positioned towards the back of the screen.",
				new String[]{"astronaut", "monster"});
		solutions.put("A piece of Brokkoli is in front of him.", new String[]{"piece of Brokkoli"});
		solutions.put("I see a palm tree on the left of the screen and a mailbox in front of it. ", new String[]{"palm tree", "mailbox"});
		solutions.put("In the foreground there is a frog on the left facing east-southeast and a broccoli on the right.", 
				new String[]{"frog", "broccoli"});
		solutions.put("In the foreground there sits a frog on the left and a hare on the right of the screen.", 
				new String[]{"frog", "hare"}); // probleme mit dem parser?
		solutions.put("In the foreground on the left, there sits a green frog with a yellow belly facing eastsoutheast.", 
				new String[]{"frog"});
		solutions.put("In the background on the right, there sits a white bunny facing southsouthwest. ", 
				new String[]{"bunny"});
		solutions.put("In the background, slightly on the moon stands a space ship with a sign reading \"UNITED STATES\".", 
				new String[]{"space ship"});
		solutions.put("In the foreground, to the left of the stage stands the girl Alice.", 
				new String[]{"girl"});
		// these are pretty hard
		solutions.put("Next to the bulb on the ground is a switch, with a brown monkey next to it, facing the button but slightly turned towards the viewer. ", 
				new String[]{"switch", "monkey"});
		solutions.put("On the right side of the palm tree there sits a frog.", new String[]{"frog"});
		// This one isn't specific enough, I think. From our PoV, the entites could also be penguin, face, blue back, and wings.
//		solutions.put("To the right is a penguin with white stomach and face and blue back and wings.", new String[]{"penguin"});
//		solutions.put("samplesentence", new String[]{"entity", "entity"});
		

		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();

		for (Entry<String, String[]> sol : solutions.entrySet()) {
			CoreMap annoSentence = annotateSingleSentence(sol.getKey());
			List<String> einfos = proto.recogniseNames(annoSentence);
			if (einfos.size() != sol.getValue().length) {
				fail("Some entities were not recognised in in sentence \"" + sol.getKey() +"\"");
			}
			for (String foundname : einfos) {
				boolean matched = false;
				for (String solutionname : sol.getValue()) {
					if (solutionname.equalsIgnoreCase(foundname)) {
						matched = true;
						continue;
					}					
				}
				if (!matched) {
					fail("Entity not recognised: " + foundname + " in sentence \"" + sol.getKey() +"\"");
				}
			}
		}
	}
	
	@Test
	public void testLearnDeclarations() throws IvanException
	{
		String input = "The ground is covered with grass. "
				+ "In the foreground there is a monkey in the middle facing southwest. "
				+ "On the right side of the ground, there is a broccoli. "
				+ "Behind the monkey, to the right, there is a bucket.";
		
		ArrayList<EntityInfo> output = new ArrayList<EntityInfo>();
		output.add(new EntityInfo("ground"));
		output.add(new EntityInfo("monkey", "in the middle", "facing southwest"));
		output.add(new EntityInfo("broccoli", "On the right side of the ground", null));
		output.add(new EntityInfo("bucket", "Behind the monkey, to the right", null));
		
		Annotation doc = new Annotation(input);
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		proto.getPipeline().annotate(doc);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			proto.learnDeclarations(sentence);
		}
		
		InitialState state = proto.getCurrentState();
		assertEquals("count mismatch", output.size(), state.size());
		
		EntityInfo ground = state.getSingle("ground");
		assertEquals("basic equality test", ground, output.get(0));
		
		for (EntityInfo ei : output) {
			assertTrue("missing entity info: " + ei, state.contains(ei));
		}
	}

//	private void printRecognitionAnalysis() throws IvanException {
//		// merge the two positive lists while eliminating duplicates
//		TreeSet<String> mysentences = new TreeSet<String>();
//		mysentences.addAll(Arrays.asList(directions));
//		mysentences.addAll(Arrays.asList(locations));
//		
//		// lets load some input files for good measure
//		List<String> splitfiles = new ArrayList<String>();
//		String newlineregex = "\r\n"; // this is a regex and just means "new line"
//		splitfiles.addAll(Arrays.asList(loadTestFile("bothdirectionsandlocations.txt").split(newlineregex)));
//		splitfiles.addAll(Arrays.asList(loadTestFile("directions.txt").split(newlineregex)));
//		splitfiles.addAll(Arrays.asList(loadTestFile("locations.txt").split(newlineregex)));
//		mysentences.addAll(splitfiles);
//		
//		// 
//		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
//		for (String text : mysentences) {
//			CoreMap cm = annotateSingleSentence(text);
//			if (cm == null) {
//				continue;
//			}
//			List<String> names = proto.recogniseNames(cm);
//			// the names go into a left "column"
//			StringBuilder namesstring = new StringBuilder();
//			for (String n : names) {
//				namesstring.append(n);
//				namesstring.append(", ");
//			}
//			namesstring.delete(namesstring.length() - 2, namesstring.length());
//			// fill the left column until it is 40 chars wide
//			for (int i = namesstring.length(); i < 22; i++)
//			{
//				namesstring.append(' ');			
//			}
//			// print names into first column
//			System.out.print(namesstring);
//			System.out.print("\t");
//			// print sentence into second column
//			System.out.println(text);
//		}
//		nop();
//	}

}
