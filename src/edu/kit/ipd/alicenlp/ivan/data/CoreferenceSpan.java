/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

import edu.stanford.nlp.util.IntPair;

/** This class saves spanning data (ranges) with the same semantics as the deterministic coreference module.
 * That means it identifies mentions by sentence number an 1-based token indeces.
 * 
 * @author Jonny
 *
 */
public class CoreferenceSpan {
	/** 1-based sentence index
	 * 
	 */
	public int Sentence;
	
	/** 1-based token index. The first word in a sentence has the index "1".
	 * 
	 */
	public int BeginIndex;
	
	/** Identifies the last token in this span (inclusive number).
	 * 
	 */
	public int EndIndex;

	/** Create a new span
	 * 
	 * @param sentence
	 * @param tuple words indeces
	 */
	public CoreferenceSpan(Integer sentence, IntPair tuple) {
		this.Sentence = sentence;
		this.BeginIndex = tuple.getSource();
		this.EndIndex = tuple.getTarget();
	}

	/** The sentence-relative tuple for this mention. 
	 * 
	 * @return new IntPair(BeginIndex, BeginIndex);
	 */
	public IntPair getTuple() {
		return new IntPair(BeginIndex, BeginIndex);
	}
}
