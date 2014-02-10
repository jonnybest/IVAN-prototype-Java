package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import net.sf.extjwnl.JWNLException;
import edu.stanford.nlp.ling.CoreAnnotations.SpeakerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
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

		String speakerID; // speaker

		List<CoreLabel> tokens = Sentence.get(TokensAnnotation.class);
		if (tokens.size() == 0)
			return false;
		if (tokens.size() == 1) {
			// if there is only one token, we use this
			speakerID = tokens.get(0).get(SpeakerAnnotation.class);
		} else {
			// if there are more tokens, the first is likely to be quotation
			// marks -- punctuation. punctuation is always labelled with the
			// narrator id.
			speakerID = tokens.get(1).get(SpeakerAnnotation.class);
		}

		if (speakerID == null)
			return false; // this is actually an exceptional case. if the
							// speaker annotation is missing, something is
							// wrong.

		if (speakerID == "PER0") {
			return false; // narrator id. no direct speech
		} else {
			return true; // everything else is speech
		}
	}

}
