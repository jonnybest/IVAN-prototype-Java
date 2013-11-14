/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

/** This rule calculates whether a given sentence frame is applicable to a given sentence. 
 * @author Jonny
 *
 */
public class SentenceFrameRule implements IGraphRule 
{

	/**
	 * 
	 */
	public SentenceFrameRule(String sentenceframe) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean apply(CoreMap Sentence) {
		// TODO Auto-generated method stub
		return false;
	}

	public CoreLabel getVerb() {
		// TODO Auto-generated method stub
		return null;
	}

}
