/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import net.sf.extjwnl.JWNLException;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.stanford.nlp.pipeline.Annotation;

/** These rules require a document as input.
 * @author Jonny
 *
 */
public interface IDocumentRule {

	/** Apply this rule to the annotated document.
	 * 
	 * @param doc 
	 * @return TRUE if this rule applies, FALSE if not
	 * @throws JWNLException 
	 * @throws IvanException 
	 */
	boolean apply(Annotation doc) throws JWNLException, IvanException;

	/** Apply this rule to the annotated document and modify the existing annotations.
	 * 
	 * @param doc 
	 * @param canWrite TRUE indicates that this rule is allowed to modify existing annotations 
	 * @return TRUE if this rule applies, FALSE if not
	 * @throws JWNLException 
	 * @throws IvanException 
	 */
	boolean apply(Annotation doc, boolean canWrite) throws JWNLException, IvanException;
}
