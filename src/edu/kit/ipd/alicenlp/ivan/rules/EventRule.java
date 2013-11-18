/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.util.CoreMap;

/** This class decides whether a given sentence describes an event (or not).
 * @author Jonny
 *
 */
public class EventRule implements ISentenceRule {

	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap Sentence) {
		
		return true;
	}

}
