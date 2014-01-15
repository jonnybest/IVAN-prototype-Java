/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import edu.stanford.nlp.dcoref.CorefChain.CorefMention;

/** This interface provides a simple key/value based view on coreference-like results.
 * 
 * 
 * @author Jonny
 *
 */
public interface ICorefResultRule {

	/** If the rule found anything, this method returns the mentions which contain aliases
	 * @return A list of aliases
	 * 
	 */
	List<CorefMention> getAliasMentions();
	
	/** If the rule found anything, this method retrieves the first nominal mention for the given alias.
	 * @param alias The name to look for
	 * @return A nominal mention which has a reference to the given name
	 */
	CorefMention getEntity(CorefMention alias);
}
