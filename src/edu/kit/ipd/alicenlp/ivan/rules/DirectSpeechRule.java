package edu.kit.ipd.alicenlp.ivan.rules;

import net.sf.extjwnl.JWNLException;
import edu.stanford.nlp.ling.CoreAnnotations.SpeakerAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This rule finds speech. Utterances classify as speech, when they consist only
 * of direct speech. The rule examines the speaker annotation of the first two
 * tokens and returns TRUE if they signal a non-zero speaker.
 * 
 * @author Jonny
 * 
 */
public class DirectSpeechRule implements ISentenceRule {

	@Override
	public boolean apply(CoreMap Sentence) throws JWNLException {
		String speak = Sentence.get(SpeakerAnnotation.class);

		if (speak == null)
			return false; // this is actually an exceptional case. if the
							// speaker annotation is missing, something is
							// wrong.

		if (speak == "PER0") {
			return false; // narrator id. no direct speech
		} else {
			return true; // everything else is speech
		}
	}

}
