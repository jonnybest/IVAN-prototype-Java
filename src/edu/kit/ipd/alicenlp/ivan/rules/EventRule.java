/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.UtteranceAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class decides whether a given sentence describes an event (or not).
 * 
 * @author Jonny
 * 
 */
public class EventRule implements ISentenceRule {

	private static Dictionary dictionary;
	private static Set<Synset> allowedSenses = new ArraySet<Synset>();

	/**
	 * Creates a new Event rule (and prepares wordnet and stuff)
	 * 
	 * @throws JWNLException
	 */
	public EventRule() throws JWNLException {
		setupWordNet();
		setupSenses();

	}

	/**
	 * This method constructs a list of keywords which signal an event. In
	 * particular, this means "appear", "disappear" and all their synonyms.
	 * 
	 * @throws JWNLException
	 */
	private static void setupSenses() throws JWNLException {
		// our primary keyword is "appear"
		Word appear = dictionary.getWordBySenseKey("appear%2:30:00::");
		allowedSenses.add(appear.getSynset());

		// synonyms of appear
		for (PointerTarget synonym : appear.getSynset().getTargets(
				PointerType.HYPONYM)) {
			allowedSenses.add(synonym.getSynset());
		}

		// disappear
		Word disappear = dictionary.getWordBySenseKey("disappear%2:30:00::");
		allowedSenses.add(disappear.getSynset());

		// synonyms of disappear
		for (PointerTarget synonym : disappear.getSynset().getTargets(
				PointerType.HYPONYM)) {
			allowedSenses.add(synonym.getSynset());
		}
	}

	/** TRUE if this is an event. FALSE if it is not. 
	 * Default is FALSE.
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap Sentence) throws JWNLException {
		List<CoreLabel> verbs = new ArrayList<CoreLabel>(); // get verbs for
															// this sentence
		for (CoreLabel word : Sentence.get(TokensAnnotation.class)) {
			Integer ut = word.get(UtteranceAnnotation.class);
			if (ut != null && ut > 0) {
				continue; // skip dialogue
			}
			if (BaseRule.isPOSFamily(word, "VBZ") // "He appears on top of the stairs."
					|| BaseRule.isPOSFamily(word, "VBP") // "They appear on top of the stairs."
			) {
				// add this verb to evaluation list
				verbs.add(word);
			}
		}
		for (CoreLabel v : verbs) {

			IndexWord wnw = dictionary.getIndexWord(POS.VERB, v.lemma());
			if(wnw == null)
				return false;
			/*
			 * This loop is where the real magic happens. If the word
			 * <code>v</code> can be interpreted as meaning anything on
			 * <code>allowedSenses</code>, however unlikely, that means this one
			 * qualifies as event.
			 */
			for (Synset sense : wnw.getSenses()) {
				if (allowedSenses.contains(sense)) {
					double threshold = 0.35;
					// likelihood-based check. everything less likely than .8 is
					// skipped.
					if (!LikelihoodRule.apply(threshold, wnw, sense)) {
						System.out.println("Skipping (" + sense);
						continue;
					}
					// TODO: implement sanitity check with sentence frame.
					if (new SentenceFrameRule(sense.getVerbFrames())
							.apply(Sentence))
						return true;
					else
						return false;
				}
			}

		}

		return false;
	}

	protected void setupWordNet() throws JWNLException {
		// set up properties file
		String propsFile = "file_properties.xml";
		FileInputStream properties = null;
		try {
			properties = new FileInputStream(propsFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// create a dictionary and run the analytics
		// run
		if (dictionary == null) {
			// new style, instance dictionary
			dictionary = Dictionary.getInstance(properties);
		}
	}
}
