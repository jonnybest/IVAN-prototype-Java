/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.util.CoreMap;

/**
 * This method applies a rule which works on a given sentence.
 * @author Jonny
 *
 */
public interface ISentenceRule {
	
	/**
	 * Checks whether the given sentence satisfies the rule. This method is generally stateful and modifies the internal state of the rule object.
	 * @param Sentence
	 * @return
	 */
	boolean apply(CoreMap Sentence);
}
