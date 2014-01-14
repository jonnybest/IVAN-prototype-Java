/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.DocumentErrorAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.rules.AliasByCorefRule;
import edu.kit.ipd.alicenlp.ivan.rules.IncompleteEntitiesErrorRule;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.logging.PrettyLogger;

/**
 * @author Jonny
 *
 */
public class IncompleteEntitiesErrorRuleTest {

	/**
	 * Test method for {@link edu.kit.ipd.alicenlp.ivan.rules.IncompleteEntitiesErrorRule#apply(edu.stanford.nlp.pipeline.Annotation, boolean)}.
	 * @throws JWNLException 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testApplyAnnotationBoolean() throws JWNLException {
		
		String text = "Lucas is a wizard. Lucas is on the right hand side. Lucas is looking north.";
		Annotation annotate = TestUtilities.annotateClassifications(text);
		InitialState state = annotate.get(IvanEntitiesAnnotation.class);
		
		IncompleteEntitiesErrorRule rule = new IncompleteEntitiesErrorRule(state);
		boolean okay = rule.apply(annotate);
		assertFalse("recognition failed", okay);
		
		assertNull(annotate.get(DocumentErrorAnnotation.class));
		
		PrettyLogger.log(annotate.get(CorefChainAnnotation.class));
		PrettyLogger.log(state);
		
		assertThat("wizard not recognised", state.getEntity("Lucas"), is("wizard"));
		
		assertThat("error in entity list", state.getEntityNames().get(0).first, is("wizard"));
	}

}
