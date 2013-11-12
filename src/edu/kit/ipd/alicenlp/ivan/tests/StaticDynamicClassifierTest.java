/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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

	/** This is a test for Stanford Pipeline compliance. 
	 *  
	 * Here are the steps: (from the FAQ)
	 * 1. extend the class edu.stanford.nlp.pipeline.Annotator 
	 * 2. I assume you're writing your own code to do the processing. Whatever code you write, you want to call it from a class that is a subclass of Annotator. Look at any of the existing Annotator classes, such as POSTaggerAnnotator, and try to emulate what it does.
	 * 3. Have a constructor with the signature (String, Properties)
	 * 4. If your new annotator is FilterAnnotator, for example, it must have a constructor FilterAnnotator(String name, Properties props) in order to work.
	 * 5. Add the property customAnnotatorClass.FOO=BAR
	 * 6. Using the same example, suppose your full class name is com.foo.FilterAnnotator, and you want the new annotator to have the name "filter". When creating the CoreNLP properties, you need to add the flag 
	 * 		customAnnotatorClass.filter=com.foo.FilterAnnotator
	 * 7. You can then add "filter" to the list of annotators in the annotators property. When you do that, the constructor FilterAnnotator(String, Properties) will be called with the name "filter" and the properties files you run CoreNLP with. This lets you define any property flag you want. For example, you could name a flag filter.verbose and then extract that flag from the properties to determine the verbosity of your new annotator.
	 */
	@Test
	public void StanfordPipelineTest(){
		
		// constructin test
		@SuppressWarnings("unused")
		StaticDynamicClassifier myannotator = new StaticDynamicClassifier("hi", new Properties());
		
		// functional test 
		String text = "In the background on the left hand side there is a PalmTree. "
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. "
				+ "Right to the mailbox there is a Frog facing east. "
				+ "In the foreground on the right hand side there is a Bunny facing southwest. "
				+ "In front of the Bunny there is a Broccoli.";
		Annotation doc = annotateText(text);

	    // lets see if there are any annotations at all
	    assertEquals("Sentences are missing", 5, doc.get(SentencesAnnotation.class).size());
	    
	    // the sentences in this test should all have some annotation or another
	    for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			assertTrue("Sentences was not classified: " + sentence.toString(), sentence.containsKey(Classification.class));
			assertNotNull(sentence.get(Classification.class));
			System.out.println(sentence.get(Classification.class) + ": " + sentence.toString());
		}
	    
	}

	/**
	 * @param text
	 * @return
	 */
	private Annotation annotateText(String text) {
		Annotation doc = new Annotation(text);
		
		StanfordCoreNLP pipeline;
			
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    // alternativ: wsj-bidirectional 
	    try {
			props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	    props.put("customAnnotatorClass.sdclassifier", "edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");
	    
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sdclassifier"); //$NON-NLS-1$ //$NON-NLS-2$
	    pipeline = new StanfordCoreNLP(props);	
	    
	    pipeline.annotate(doc);
		return doc;
	}
	
	/** A positive test for ACTION annotations.
	 *  If this test passes, the analyzer has correctly identified an action.   
	 */
	@Test
	public void positiveActionTest()
	{
		Annotation doc = annotateText("The penguin jumps once.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertThat("jumps sentence classified wrong", sentence.get(Classification.class), is(Classification.ActionDescription));
		
		Annotation doc2 = annotateText("The boy and the girl lift their left arms simulteanously as well.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertThat("lift sentence classified wrong", sentence2.get(Classification.class), is(Classification.ActionDescription));
		
		Annotation doc3 = annotateText("After a short pause, the penguin turns around towards the back of the bucket behind it, jumps onto its stomach and slides towards the bucket, flapping its wings again.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertThat("turns around sentence classified wrong", sentence3.get(Classification.class), is(Classification.ActionDescription));
		
	}
	
	/** A negative test for ACTION annotations.
	 *  If this test passes, the analyzer has correctly identified an non-action.   
	 */
	@Test
	public void negativeActionTest()
	{
		Annotation doc = annotateText("A very short time passes.");
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence.get(Classification.class));
		assertThat("time passes sentence classified wrong", sentence.get(Classification.class), is(not(Classification.ActionDescription)));
		
		Annotation doc2 = annotateText("A frog sits left of the Brokkoli facing it.");
		CoreMap sentence2 = doc2.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence2.get(Classification.class));
		assertThat("sits sentence classified wrong", sentence2.get(Classification.class), is(not(Classification.ActionDescription)));

		// The start depicts a boy facing to the right of the screen, and a woman facing to the front.
		Annotation doc3 = annotateText("A giant flame column appears and consumes the astronaut.");
		CoreMap sentence3 = doc3.get(SentencesAnnotation.class).get(0);
		assertNotNull("class is missing", sentence3.get(Classification.class));
		assertThat("appears sentence classified wrong", sentence3.get(Classification.class), is(not(Classification.ActionDescription)));

	}
}
