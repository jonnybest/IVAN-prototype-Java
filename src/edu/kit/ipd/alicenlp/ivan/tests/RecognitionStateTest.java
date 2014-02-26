package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.components.IvanDiscourseModelPrinter;
import edu.kit.ipd.alicenlp.ivan.data.DiscourseModel;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.stanford.nlp.pipeline.Annotation;

/** This class tests the emitter. It also tests the internal conditions.
 * 
 * @author Jonny
 *
 */
public class RecognitionStateTest extends IvanDiscourseModelPrinter {

	/** creates a RecognitionStatePrinter 
	 * 
	 * @throws IvanException
	 */
	@Test
	public final void testToString() throws IvanException {
//		String testfile = "The ground is covered with grass."
//				+ "In the background there is a sunflower on the far right facing southwest."
//				+ "In the foreground there is a monkey in the middle facing southwest."
//				+ "To the monkey's right, there is a penguin facing south."
//				+ "To the monkey's left, there is a remote."
//				+ "Left of the remote, there is a lightbulb."
//				+ "Behind the light bulb, to the right, there is a duck prince facing southeast."
//				+ "Behind the monkey, to the right, there is a bucket."
//				+ "The duck prince turns to face the monkey."
//				+ "The duck prince commands."
//				+ "The monkey sighs."
//				+ "The monkey turns to face the remote."
//				+ "The monkey jumps."
//				+ "The monkey presses a button."
//				+ "A very short time passes."
//				+ "The penguin turns to face the bucket."
//				+ "The penguin glides."
//				+ "The duck prince quickly turns to face the penguin."
//				+ "The duck prince scolds."
//				+ "A very short time passes."
//				+ "The monkey laughs."
//				+ "The monkey presses a button."
//				+ "The penguin jumps once.";

		String input = "In the foreground there is a monkey in the middle facing southwest." + "\n"
				+ "Behind the monkey, to the right, there is a bucket.";
		String expected = "None of the entities in this description have a name. "
				+ "The scene has the following entities: "
				+ "A monkey is in the middle, facing southwest and"
				+ "a bucket is behind the monkey and to the right.";
		
		
		Annotation doc = TestUtilities.annotateDeclarations(input);
		DiscourseModel state = doc.get(IvanAnnotations.IvanEntitiesAnnotation.class);
		
		this.setDeclarations(state);
		
		
		String output = this.toString();
		
		assertThat("Monkey and bucket", output, is(expected));
		
		//fail("Not yet implemented"); // TODO
		//CoreferenceResolver corefs = CoreferenceResolver.getInstance();		
	}

}
