package edu.kit.ipd.alicenlp.ivan.rules;

import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;

/** This is the interface for rules that indicate errors 
 * 
 * @author Jonny
 *
 */
public interface IErrorRule {

	/** Gets a message
	 * 
	 * @return A error message
	 */
	ErrorMessageAnnotation getErrorMessage();
	
}
