package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.BaseRule;
import edu.kit.ipd.alicenlp.ivan.rules.DirectionKeywordRule;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/** These tests test a single rule (the direction keyword rule)
 * 
 * @author Jonny
 *
 */
@SuppressWarnings("javadoc")
public class DirectionKeywordRuleTest {

	@Test
	public void testApply() {
		{
			CoreMap sentence = annotate("The boy looks at the girl.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: at the girl", rule.getDirection(), is("at the girl"));
		}
		{
			CoreMap sentence = annotate("The boy is turned to the girl.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: to the girl", rule.getDirection(), is("to the girl"));
		}
	}
	
	@Test
	public void testAdverbialModifier() {
		{
			CoreMap sentence = annotate("The boy looks west.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: west", rule.getDirection(), is("west"));
		}
	}
	
	@Test
	public void testDirectObject() {
		{
			CoreMap sentence = annotate("The boy is turned 45 degrees.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: 45 degrees", rule.getDirection(), is("45 degrees"));
		}
	}

	@Test
	public void testParticipalModifier() {
		{
			CoreMap sentence = annotate("There is a boy facing north.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: north", rule.getDirection(), is("north"));
		}
	}
	
	@Test
	public void testRelativeClause() {
		{
			CoreMap sentence = annotate("The boy, who is turned 45 degrees, stands on the left hand side.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: 45 degrees", rule.getDirection(), is("45 degrees"));
		}
	}
	
	@Test
	public void testGetDirection() {
		{
			CoreMap sentence = annotate("The boy is looking towards the girl.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			assertThat("direction: at the girl", rule.getDirection(), is("towards the girl"));
		}
	}

	@Test
	public void testGetVerb() {
		{
			CoreMap sentence = annotate("The boy is looking towards the girl.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			IndexedWord verb = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class).getFirstRoot();
			assertThat("verb: towards", rule.getVerb(), is(verb));
		}
	}

	@Test
	public void testGetSubject() {
		{
			CoreMap sentence = annotate("The boy is looking towards the girl.").get(SentencesAnnotation.class).get(0);
			DirectionKeywordRule rule = new DirectionKeywordRule();
			assertTrue("direction not recognised", rule.apply(sentence));
			IndexedWord subject = BaseRule.getSubject(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
			assertThat("subject: the boy", rule.getSubject(), is(subject));
		}
	}

	private Annotation annotate(String text) {
		Annotation doc = new Annotation(text);
		StanfordCoreNLP pipeline;
			
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    // alternativ: wsj-bidirectional 
	    try {
			props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	    props.put("customAnnotatorClass.decl", "edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
	    
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse, decl"); //$NON-NLS-1$ //$NON-NLS-2$
	    pipeline = new StanfordCoreNLP(props);	
	    
	    pipeline.annotate(doc);
	    return doc;
	}
}
