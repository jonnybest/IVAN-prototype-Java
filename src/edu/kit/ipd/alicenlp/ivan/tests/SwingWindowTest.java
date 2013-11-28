/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;

/** Tests the Main window
 * 
 * @author Jonny
 *
 */
public class SwingWindowTest {

	/** Tests the file converter for creating git refs
	 * 
	 */
	@Test
	public void testFile2Ref() {
		assertThat(SwingWindow.file2ref("[a]s me..thing^?*:"), is("asome.thingoooo"));
	}

}
