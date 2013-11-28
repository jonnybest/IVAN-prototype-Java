/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static edu.kit.ipd.alicenlp.ivan.tests.TestUtilities.checkEntrySet;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;

/**
 * @author Jonny
 *
 */
public class HardDeclarationsTests {


	/** Find tortoise
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseTortoise() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"The characters are a ninja tortoise, a rabbit and a T-Rex.",
				new String[] { "ninja tortoise", "rabbit", "T-Rex" });
		checkEntrySet(sol);
	}

	/** Find alice
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseAlice() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground, to the left of the stage stands the girl Alice.",
				new String[] { "girl" });
		checkEntrySet(sol);
	}

	/** Find broccoli facing east-southeast
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseFrogBroccoli() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground there is a frog on the left facing east-southeast and a broccoli on the right.",
				new String[] { "frog", "broccoli" });
		checkEntrySet(sol);
	}

	/** find frog on the left
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseFrogHare() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground there sits a frog on the left and a hare on the right of the screen.",
				new String[] { "frog", "hare" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseSwitch() throws IvanException {
		Entry<String, String[]> // these are pretty hard
		sol = new AbstractMap.SimpleEntry<String, String[]>(
				"Next to the bulb on the ground is a switch, with a brown monkey next to it, facing the button but slightly turned towards the viewer. ",
				new String[] { "switch", "monkey" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseThereSitsAFrog() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"On the right side of the palm tree there sits a frog.",
				new String[] { "frog" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseMonsteronwheels() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"At the start the astronaut is facing to the front of the screen and the monster on wheels is positioned towards the back of the screen.",
				new String[] { "astronaut", "monster" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecognisePieceofbrok() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"A piece of Brokkoli is in front of him.",
				new String[] { "A piece of Brokkoli" });
		checkEntrySet(sol);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testRecogniseIsee() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"I see a palm tree on the left of the screen and a mailbox in front of it. ",
				new String[] { "palm tree", "mailbox" });
		checkEntrySet(sol);
	}

	/** Tests a bunny facing southsouthwest
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseBunny() throws IvanException {
		checkEntrySet(new AbstractMap.SimpleEntry<String, String[]>(
				"In the background on the right, there sits a white bunny facing southsouthwest. ",
				new String[] { "bunny" }));
	}

	/** Tests a space ship with a sign. 
	 * 
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseSpaceship() throws IvanException {
		checkEntrySet(new AbstractMap.SimpleEntry<String, String[]>(
				"In the background, slightly on the moon stands a space ship with a sign reading \"UNITED STATES\".",
				new String[] { "space ship" }));
	}

	/** Find frog
	 * @throws IvanException
	 */
	@Test
	public void testRecogniseSitsFrog() throws IvanException {
		Entry<String, String[]> sol = new AbstractMap.SimpleEntry<String, String[]>(
				"In the foreground on the left, there sits a green frog with a yellow belly facing eastsoutheast.",
				new String[] { "frog" });
		checkEntrySet(sol);
	}
	
}
