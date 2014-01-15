/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefAnnotation;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.TypesafeMap.Key;

/** Tests the Main window
 * 
 * @author Jonny
 *
 */
public class SwingWindowTest extends SwingWindow {

	/** Tests the file converter for creating git refs
	 * 
	 */
	@Test
	public void testFile2Ref() {
		assertThat(SwingWindow.file2ref("[a]s me..thing^?*:"), is("asome.thingoooo"));
	}

	/**
	 * Assert that our classification tests and our actual application run with the same config.
	 */
	@Test
	public void testPipeline()
	{
		assertThat(SwingWindow.PROPERTIES_ANNOTATORS, is(TestUtilities.PROPERTIES_ANNOTATORS));	
	}
}
