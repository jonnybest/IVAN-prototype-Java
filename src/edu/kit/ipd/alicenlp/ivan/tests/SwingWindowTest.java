/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;

/**
 * @author Jonny
 *
 */
public class SwingWindowTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testFile2Ref() {
		assertThat(SwingWindow.file2ref("[a]s me..thing^?*:"), is("asome.thingoooo"));
	}

}
