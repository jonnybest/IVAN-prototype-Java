package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

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

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.EntityInfo;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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
	
	@BeforeClass
	public static void setup()
	{
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
		List<EntityInfo> entities = proto.findAll(sentence);
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
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		for (CoreMap location : locationlist) {
			assertTrue("Expected a location in this sentence: " + location, proto.hasLocation(location));			
		}		
		for (CoreMap neg : negativeslist) {
			assertFalse("Expected no location in \"" + neg + "\"", proto.hasLocation(neg));
		}
	}

	@Test
	public void testGetLocation() {
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		EntityInfo simplein = proto.getLocation(annotateSingleSentence("There is a ghost in this house."));
		if (!"in this house".equals(simplein.getLocation())) {
			fail("Location is wrong"); 			
		}
		EntityInfo inandon = proto.getLocation(annotateSingleSentence("The house is in the background on the left hand side."));
		if (!"in the background on the left hand side".equals(inandon.getLocation())) {
			fail("Location is wrong");
		}
		EntityInfo behind = proto.getLocation(annotateSingleSentence("Behind it to the right is a yellow duckling wearing red socks, a crown and a scepter."));
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
	public void testGetDeclarations() {
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
	public void testRecogniseNames() {

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
		

		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();

		for (Entry<String, String[]> sol : solutions.entrySet()) {
			CoreMap annoSentence = annotateSingleSentence(sol.getKey());
			List<String> einfos = proto.recogniseNames(annoSentence);
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
}
