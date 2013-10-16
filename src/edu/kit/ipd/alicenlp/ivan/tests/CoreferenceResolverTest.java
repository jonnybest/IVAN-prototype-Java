/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver;

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

	/**
	 * Test method for {@link edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver#findName(java.lang.String, int, java.lang.String)}.
	 */
	@Test
	public final void testFindName() {
		findName("dog", 0, "Harry is a dog. The dog is on the right side.");
	}

	/**
	 * Test method for {@link edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver#extractPredictedMentions(edu.stanford.nlp.pipeline.Annotation, int, edu.stanford.nlp.dcoref.Dictionaries)}.
	 */
	@Test
	public final void testExtractPredictedMentions() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver#resolve(edu.stanford.nlp.ling.IndexedWord)}.
	 */
	@Test
	public final void testResolve() {
		fail("Not yet implemented"); // TODO
	}

}
