/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;

/**
 * This class decides how likely a given synset is to be observed.
 * 
 * @author Jonny
 * 
 */
public class LikelihoodRule {
	/**
	 * Checks whether a given word is at least <code>threshold</code>-likely to
	 * mean <code>meaning</code>.
	 * 
	 * @param threshold
	 *            The minimum prior likelihood.
	 * @param word
	 *            The word in the text.
	 * @param meaning
	 *            The assumed meaning of the word.
	 * @return TRUE if the word is likely to mean <code>meaning</code>.
	 * @throws JWNLException
	 */
	public static boolean apply(double threshold, IndexWord word, Synset meaning)
			throws JWNLException {
		double likelihood = rate(word, meaning);
		if (likelihood >= threshold)
			return true;
		else
			return false;
	}

	/**
	 * Calculates a likelihood rating between (0,1]. Since many entries in
	 * Wordnet do not have any occurrences, the rating is smoothed by a
	 * constant.
	 * 
	 * @param word
	 *            The word in the text.
	 * @param meaning
	 *            The synset to match.
	 * @return The prior likelihood that <code>word</code> means
	 *         <code>meaning</code>.
	 * @throws JWNLException
	 */
	public static double rate(IndexWord word, Synset meaning)
			throws JWNLException {
		final double smoothing = 0.1;
		double occurrences = getUseCount(meaning, word.getLemma()) + smoothing;
		double total = 0;

		IndexWord meanings = meaning.getDictionary().getIndexWord(
				meaning.getPOS(), word.getLemma());
		for (Synset syn : meanings.getSenses()) {
			// get count
			// add count + 1 to occurrences (count+1 because of smoothing)
			int cnt = getUseCount(syn, word.getLemma());
			total = total + cnt + smoothing;
		}
		if (total == 0)
			System.err.println("division by zero!");
		double prior = occurrences / total;
		System.out.println(word.getLemma() + " is " + prior
				+ " likely to mean \'" + meaning.getGloss() + " \'");
		return prior;
	}

	/**
	 * Stolen from net.sf.extjwnl.data.IndexWord.getUseCount(Synset, String)
	 * 
	 * @param synset
	 * @param lemma
	 * @return
	 */
	private static int getUseCount(Synset synset, String lemma) {
		for (Word w : synset.getWords()) {
			if (w.getLemma().equalsIgnoreCase(lemma)) {
				if (0 < w.getUseCount()) {
					return w.getUseCount();
				}
			}
		}
		return 0;
	}
}
