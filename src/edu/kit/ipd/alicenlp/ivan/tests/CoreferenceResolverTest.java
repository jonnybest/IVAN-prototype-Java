/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 * 
 */
public class CoreferenceResolverTest extends CoreferenceResolver {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CoreferenceResolver.getInstance();
	}

	private StanfordCoreNLP mypipeline;

	/**
	 * Test method for
	 * {@link edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver#findName(java.lang.String, int, java.lang.String)}
	 * .
	 */
	@Test
	public final void testFindName() {
		String htext = "Take your time. Harry is a dog. The dog is on the right side.";
		String hname = "dog";
		// find out where this word is in the text (careful, there may be more
		// than one instance)
		int hstartIndex = htext.indexOf(hname); // debug
		String harry = findName(hname, hstartIndex, htext);
		if (!harry.equals("Harry"))
			fail("Harry failed.");
		String mname = "Melissa";
		String mtext = "Nothing happens for a second. Harry is looking at a dog.";
		int mindex = mtext.indexOf(mname);
		String melissa = findName(mname, mindex, mtext);
		if (melissa != null)
			fail("Melissa failed");
	}

	/**
	 * Test method for
	 * {@link edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver#resolve(edu.stanford.nlp.ling.IndexedWord)}
	 * .
	 * @throws IvanException 
	 */
	@Test
	public final void testResolve() throws IvanException {				
		String text = ""
				+ "The woman facing to the front is called \"Lisa.\""
				+ "The start depicts a boy facing to the right of the screen, and the woman facing to the front. "
				+ "The woman then raises her left arm and a speech bubble appears from her mouth which reads \"hello.\""
				+ "She then proceeds to wave, still facing forwards, and turns to face the man. "
				+ "Next she extends her right hand and shakes. "
				+ "A speech bubble then appears from the mouth of the man which also reads \"hello,\" "
				+ "and he raises his left arm and waves. He then places his arm back by his side. "
				+ "Both characters then raise their left arm upwards in a smooth motion at a 90 degree angle and "
				+ "rotate multiple times. "
				+ "Both arms are then replaced at the characters hips before they raise their "
				+ "left arm 90 defrees once again and wave. The animation is then complete.";
		String searchword = "she";
		String name = "Lisa";

		Annotation doc = new Annotation(text);
		// run my pipeline on it
		getPipeline().annotate(doc);
		/* okay, how to go about finding the other mentions for this exact word?
		*  suppose I want to use getMentionsWithSameHead(sentenceNumber, headIndex) -- headIndex means the position of the word which is the head of the NP (if the name has more than one word)
		*  first, I need to find out where this word occurs: in which sentence and at which index
		*    to go about that, I will iterate the sentences to find out if the startIndex is within the sentences bounds, then get the indexword at that position, and then note the word's index
		*    after that I can iterate the CorefChains and for each chain invoke getMentionsWithSameHead
		*    when I have found the right chain, I return the representative phrase
		*/ 
		CoreLabel labelForMyWord = null;
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (CoreMap s : sentences) {
			List<CoreLabel> tokens = s.get(TokensAnnotation.class);
			// tokens.
			for (CoreLabel lbl : tokens) {
				if (lbl.word().equals(searchword)) // this words for single-word names
												// only!
				{
					labelForMyWord = lbl;
					break;
				}
			}
		}
		
		String result = resolve(labelForMyWord, doc);
		Assert.assertEquals("test 1 failed", name, result); // test1 end
		
		// test 2 for the overload
		setDocument(doc);
		result = resolve(labelForMyWord);
		Assert.assertEquals("Test 2 failed", name, result); // test2
	}

	private StanfordCoreNLP getPipeline() {
		StanfordCoreNLP pipeline;
		if (mypipeline == null) {
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
			// konfiguriere pipeline
			props.put(
					"annotators", "tokenize, ssplit, pos, lemma, parse, ner, dcoref"); //$NON-NLS-1$ //$NON-NLS-2$
			pipeline = new StanfordCoreNLP(props);
			mypipeline = pipeline;
		} else {
			pipeline = mypipeline;
		}
		return pipeline;
	}

}
