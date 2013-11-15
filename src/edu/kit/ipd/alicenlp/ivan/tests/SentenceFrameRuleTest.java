package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.SentenceFrameRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class SentenceFrameRuleTest 
{
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setupWordNet();
	}

	private static Dictionary dictionary;

	@Test
	public final void testSomethingAndSomethingDO() throws JWNLException {
		/* 
		 * Set up grammar analysis
		 */
		String text = "A giant flame column appears and it consumes the astronaut.";
		Annotation doc = annotateText(text);
		CoreMap sentence = doc.get(SentencesAnnotation.class).get(0);
//		SemanticGraph deps = sentence.get(BasicDependenciesAnnotation.class);
		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
		String[] verbtokens = {"VBZ", "VBG", "VBD", "VBN"};
		ArrayList<CoreLabel> verbs = new ArrayList<CoreLabel>();
		
		// ready to analyze
		for (CoreLabel token : tokens) {
			if(Arrays.asList(verbtokens).contains(token.tag()))
			{
				verbs.add(token);
			}
		}		
		
		/* 
		 * Set up wordnet sentence frame
		 */
//		IndexWord word = dictionary.lookupIndexWord(POS.VERB, "appear");
//		List<Synset> senses = word.getSenses();
//		Synset meaning = senses.get(1); 
//		String[] frames = meaning.getVerbFrames();

		{
			// for "appear": Something appears; A giant flame column appears.
			String frame = "Something ----s";		
			SentenceFrameRule rule = new SentenceFrameRule(frame);
			boolean result = rule.apply(sentence);
			assertTrue(frame + " does not fit.", result);
			assertThat(rule.getVerb(), is(verbs.get(0)));
		}
		{
			// for "consume": Something consumes somebody; A giant flame column consumes the astronaut.
			String frame = "Something ----s somebody ";		
			SentenceFrameRule rule = new SentenceFrameRule(frame);
			boolean result = rule.apply(sentence);
			assertTrue(frame + " does not fit.", result);
			assertThat(rule.getVerb(), is(verbs.get(1)));
		}
		// negative tests. These should not fit.
		{
			String frame = "Somebody ----s";		
			SentenceFrameRule rule = new SentenceFrameRule(frame);
			boolean result = rule.apply(sentence);
			assertFalse(frame + " fits but shouldn't.", result);
		}
		{
			String frame = "Something is ----ing PP";		
			SentenceFrameRule rule = new SentenceFrameRule(frame);
			boolean result = rule.apply(sentence);
			assertFalse(frame + " fits but shouldn't.", result);
		}
		{
			String frame = "Somebody ----s PP";		
			SentenceFrameRule rule = new SentenceFrameRule(frame);
			boolean result = rule.apply(sentence);
			assertFalse(frame + " fits but shouldn't.", result);
		}
	}


	/**
	 * @param text
	 * @return
	 */
	private Annotation annotateText(String text) {
		Annotation doc = new Annotation(text);

		StanfordCoreNLP pipeline;

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		// alternativ: wsj-bidirectional
		try {
			props.put(
					"pos.model",
					"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
		} catch (Exception e) {
			e.printStackTrace();
		}
		props.put("customAnnotatorClass.sdclassifier",
				"edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");

		// konfiguriere pipeline
		props.put(
				"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref"); //$NON-NLS-1$ //$NON-NLS-2$
		pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(doc);
		return doc;
	}
	
	/**
	 * 
	 */
	protected static void setupWordNet() {
		// set up properties file
	    String propsFile = "file_properties.xml";
	    FileInputStream properties = null;
	    try {
	    	properties = new FileInputStream(propsFile);
	    } catch (FileNotFoundException e1) {
	    	e1.printStackTrace();
	    }
	    
	    // create a dictionary and run the analytics
	    try {
	    	
	    	// run
	    	if (dictionary == null) {
				//new style, instance dictionary
				dictionary = Dictionary.getInstance(properties);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
	}
}
