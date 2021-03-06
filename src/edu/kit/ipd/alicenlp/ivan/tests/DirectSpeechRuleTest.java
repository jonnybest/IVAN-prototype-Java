package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.DirectSpeechRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;


public class DirectSpeechRuleTest {

	@Test
	public final void test() 
	{
		
		CoreMap negativedoc = TestUtilities.annotateSingleDeclaration("While she goes, she says: “Ohh…” and after a little while: “Okay…”.");
		DirectSpeechRule negativerule = new DirectSpeechRule();
		boolean resultnegative = negativerule.apply(negativedoc);
		assertFalse("direct speech rule should not have tagged this one", resultnegative);
		
		Annotation positivedoc = TestUtilities.annotateDeclarations("While the bunny jumps, Alice turns to the bunny and says: “Hey! Wait!”");
		CoreMap pos = positivedoc.get(SentencesAnnotation.class).get(1);
		DirectSpeechRule positiverule = new DirectSpeechRule();
		boolean resultpositive = positiverule.apply(pos);
		assertTrue(resultpositive);
		
		Annotation negativeBecauseUseful = TestUtilities.annotateDeclarations("\"Where are you going\" says the cheerleader before she moves to the middle.");
		CoreMap useful = negativeBecauseUseful.get(SentencesAnnotation.class).get(0);
		assertFalse("this useful sentence is being ignored", new DirectSpeechRule().apply(useful));
	}

}
