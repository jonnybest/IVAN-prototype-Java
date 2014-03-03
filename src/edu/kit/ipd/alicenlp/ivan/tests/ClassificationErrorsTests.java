package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.SentenceClassificationAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Tests the sentence type classifier with working tests which are absolutely
 * neccessary
 * 
 * @author Jonny
 * 
 */
public class ClassificationErrorsTests {

	/**
	 * These tests are cases where the sentence should be classified as ERROR
	 * because it does not have a verb. If these tests fail because Stanford
	 * CoreNLP suddenly recognises them better, please remove them.
	 */
	@Test
	public void testNoVerb() {
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("The bunny says: “Come on! What time is it?\"");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("Alice answers to the bunny.");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
		{
			// expected Parse tree: (ROOT (NP (NP (NNP Alice) (NNS answers)) (PP
			// (TO to) (NP (DT the) (NN bunny))) (. .)))
			CoreMap thing = TestUtilities
					.annotateSingleClassification("Alice Liddell says, \"Oh...\" and then moves to the bunny.");
			assertThat("This sentence should not have any verb",
					thing.get(SentenceClassificationAnnotation.class),
					is(Classification.ErrorDescription));
		}
	}

	/**
	 * There are problems with these sentences and they produce weird outputs,
	 * but I don't know why or how to fix them.
	 * 
	 */
	@Test
	public void testUnknownProblem()
	{
		String words = "Alice answers to the bunny. "+
				"to Alice. "+
				"The bunny says: \"Aaaah.\" "+
				"Alice says: \"Hey! "+
				"A cheshire cat appears on the tree and says: \":-D\". "+
				"The girl points at the boy. "+
				"They wave again. "+
				"Next she extends her right hand and shakes. "+
				"As his last action, the boy waves. "+
				"After raising their arms, both the boy and the girl spin quickly five times. "+
				"When the alien is almost next to him, he says, \"...one...\". "+
				", and takes a step towards the alien. "+
				"The alien points at the astronaut. "+
				"Alien off to the right. "+
				", to which the T-Rex replies with another loud \"ROAAAAAAAAR!!\" "+
				"The Bunny taps its foot twice. "+
				"Sentence is in 1st person: 'I see a palm tree on the left of the screen, a mailbox in front of it. "+
				"Right front: bunny in white colour. "+
				"In front of bunny on right side: green broccoli. "+
				"The bunny taps its foot. "+
				"The Bunny taps its left foot leg twice on the ground. "+
				"The bunny taps its left foot two times. "+
				"The bunny taps two times on the ground. "+
				"A very short time passes. "+
				"The Bunny taps its foot. "+
				"A very short time passes. "+
				"A very short time passes. "+
				"A very short time passes. "+
				"The bunny taps its foot twice. "+
				"A very short time passes. "+
				"A very short time passes. "+
				"The bunny taps its left foot twice. "+
				"The cheerleader cheers. "+
				"The penguin flaps his wings twice. "+
				"The penguin flaps his wings once. "+
				"The cheerleader cheers. "+
				"After the cheerleader cheers the penguin flaps twice with his wings. "+
				"But before the cheerleader jumps again she cheers. "+
				"The penguin flaps its wings twice. "+
				"After turning its head, the penguin flaps its wings once. "+
				"When the scene starts, the cheerleader cheers. "+
				"Before the last jump, she cheers. "+
				"Before that and after that, he flaps his wings. "+
				"But at the beginning of the scene, she cheers. "+
				"Before that the penguin waves its wings two times. "+
				"At the beginning the cheerleader cheers. "+
				"Before the penguin flaps his wings twice, the cheerleader cheers. "+
				"Afterwards it cheers again. "+
				"The cheerleader cheers. "+
				"Shortly thereafter the penguin flaps its wings twice. "+
				"Previously it flaps its wings once. "+
				"At first the cheerleader cheers followed by the penguin flapping wings twice. "+
				"and moves to the middle of the foreground. "+
				"And a penguin in the foreground to the right. "+
				"The cheerleader cheers. "+
				"At the beginning the cheerleader cheers slowly, turning to face the penguin. "+
				"Both the cowboy and the camel nod four times. "+
				"The cowboy steps in the middle of the picture. "+
				"Then the cowboy and the camel nod for four times. "+
				"The camel and the cowboy look at each other. "+
				"Finally, the cowboy and the camel nod four times. "+
				"The cowboy and the camel lower their heads four times. "+
				"At the end the cowboy and the camel both nod 4 times. "+
				"Afterwards both, the cowboy and the dromedary bow four times. "+
				"Afterwards both, the cowboy and the camel nod four times. "+
				"After the dragon says: \"What a beautiful windmill\", the dog turns to face the windmill. "+
				"Before that the dragon turns to face the windmill. "+
				"after she turned to face the dragon. "+
				"Afterwards the Woman moves towards the Windmill. "+
				"Right after that the dragon turns back to face south, flies in the air and lands back on the ground. "+
				"The boy and the girl lower both their arms. "+
				"The boy and the girl wave with their right arms. "+
				"The spaceman says: “He?” "+
				"A space ship with the label \"United States of America\" stands on the moon. "+
				"The alien nods and says \"/%$(&)!!!\" "+
				"while he takes a great step towards the alien. "+
				"The astronaut says, \"...one...\" and then says, \"...giant leap for...\". "+
				"The alien turns to face the astronaut, it says, \"/%$(&)$!!!\" "+
				"while moving one step toward the alien. ";
		
		
		System.out.println("This is what the whole document looks like:");
		Annotation doc = TestUtilities.annotateClassifications(words);
		for (CoreMap sentence  : doc.get(SentencesAnnotation.class)) {
			System.out.println(sentence);
			System.out.println(sentence.get(IvanAnnotations.ErrorMessageAnnotation.class));
			System.out.println(sentence.get(TreeAnnotation.class));
			System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}

//		System.err.println("---");
//		System.out.println("And this is what the single sentence looks like:");
//		{
//			CoreMap sentence = TestUtilities.annotateSingleClassification(words);
//			System.out.println(sentence);
//			System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
//		}
	}
//	
//	@Test
//	public void testBadRoot() {
//		{
//			// the problem with this sentence is manyfold. For one, its root
//			// points to "going" instead of "says". Then the coref is wrong.
//			// Then there is an appos annotation which should be clausal
//			// complement (or something like that) instead.
//			String words = "While going to the bunny, Alice says: \"Okay.\"";
//			CoreMap tags = TestUtilities.annotateSingleClassification(words);
//			assertThat("", tags.get(SentenceClassificationAnnotation.class),
//					is(Classification.ErrorDescription));
//		}
//
//	}
}