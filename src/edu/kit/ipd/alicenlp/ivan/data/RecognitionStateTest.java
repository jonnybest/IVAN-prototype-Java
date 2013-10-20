package edu.kit.ipd.alicenlp.ivan.data;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.CoreferenceResolver;
import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class RecognitionStateTest extends RecognitionState {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public final void testToString() {
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
		
		DeclarationPositionFinder dclpos = DeclarationPositionFinder.getInstance();
		this.setDeclarations(dclpos.getCurrentState());
		
		Annotation doc = new Annotation(input);		
		dclpos.getPipeline().annotate(doc);
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			dclpos.learnDeclarations(sentence);
		}
		
		String output = this.toString();
		
		assertEquals("Monkey and bucket", expected, output);
		
		//fail("Not yet implemented"); // TODO
		//CoreferenceResolver corefs = CoreferenceResolver.getInstance();		
	}

}
