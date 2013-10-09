/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.State;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.extjwnl.JWNLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier.Classification;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

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
	 * Test method for {@link edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier#classifySentence(edu.stanford.nlp.ling.IndexedWord, edu.stanford.nlp.util.CoreMap)}.
	 */
	@Test
	public void testClassifySentence() {	
		// get instance
		StaticDynamicClassifier proto = StaticDynamicClassifier.getInstance();
		
		// annotate corpus examples
		annotateSentence(inputlocs, setuplist);

		for (CoreMap sentence : setuplist) {
			IndexedWord root = BaseRule.getRoot(sentence);
			Classification result = null;
			try {
				result = proto.classifySentence(root, sentence);
			} catch (JWNLException e) {
				fail("Classifying \"" + sentence + "\" caused an exception.");
			}
			if (result != Classification.SetupDescription) {
				fail("Wrong classification for setup sentence \"" + sentence + "\"");
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
				fail("Wrong classification for action sentence \"" + sentence + "\"");
			}
		}
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

}
