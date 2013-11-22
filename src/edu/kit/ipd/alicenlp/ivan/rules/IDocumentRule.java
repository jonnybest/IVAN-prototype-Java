/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

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
	 */
	boolean apply(Annotation doc);

	/** Apply this rule to the annotated document and modify the existing annotations.
	 * 
	 * @param doc 
	 * @param canWrite TRUE indicates that this rule is allowed to modify existing annotations 
	 * @return TRUE if this rule applies, FALSE if not
	 */
	boolean apply(Annotation doc, boolean canWrite);
}
