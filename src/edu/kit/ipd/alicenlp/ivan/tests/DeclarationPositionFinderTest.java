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
		System.out.print("{Reading files: ");
		for (File item : listOfFiles) {
			// skip things that are not files
			if (item.isFile()) {
				System.out.print(item.getName() + ", ");
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
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHasLocation() {
		DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
		for (String location : locations) {
			assertTrue("Expected a location in this sentence.", proto.hasLocation(annotateSingleSentence(location)));			
		}		
		for (String neg : negatives) {
			assertFalse("Expected no location in \"" + neg + "\"", proto.hasLocation(annotateSingleSentence(neg)));
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
		else {
			System.out.println("Good: " +inandon);
		}
	}

	@Test
	public void testGetDirection() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDeclarations() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRecogniseNames() {
		fail("Not yet implemented"); // TODO
	}

}
