package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.sf.extjwnl.JWNLException;
import edu.stanford.nlp.ling.CoreAnnotations.SpeakerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
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
	public boolean apply(CoreMap Sentence)
	{

		return checkSentence(Sentence);
	}

	/**
	 * @param Sentence
	 * @return
	 */
	public static boolean checkSentence(CoreMap Sentence) {

		List<CoreLabel> tokens = Sentence.get(TokensAnnotation.class);
		if (tokens.size() == 0)
			return false;
		if (tokens.size() == 1) {
			// if there is only one token, we use this
			return isSpeech(tokens.get(0));
		} else {
			// if the first token is speech, we return right here
			if(isSpeech(tokens.get(0)))
			{
				return true;
			}
			
			// if there are more tokens, the first is likely to be quotation
			// marks -- punctuation. punctuation is always labelled with the
			// narrator id.
			if(isSpeech(tokens.get(1)))
			{
				// okay, this sentence obviously begins with speech. let's see if we can find anything useful still.
				// we need to find a token which is not punctuation and not spoken text. If that's the case, we can attempt processing it.  
				for (CoreLabel t : tokens) {
					if(t.get(SpeakerAnnotation.class) == null)
						continue;
					// non-spoken?
					if(t.get(SpeakerAnnotation.class).equals("PER0"))
					{
						// it is a narrator's token. 
						// is it punctuation?
						if(!isPunctuation(t.lemma())){
							// if it is not punctuation, then we can use this sentence!
							return false; // not all-speech!
						}
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
	}

	private static boolean isPunctuation(String lemma) {
		// if alphanumeric, it's not punctuation
		if(StringUtils.isAlphanumeric(lemma))
		{
			return false;
		}
		else {
			// if it's not alphanumeric and only 1 character, it must be punctuation
			if(lemma.length() == 1)
				return true;
		}
		
		// these are multi-character punctuation marks:
		String[] specialpunctuation = {"...", "``", "\''"};
		if(Arrays.binarySearch(specialpunctuation, lemma) > 0)
		{
			return true;
		}
		else {
			// we now know that the string is mixed alpha and non-alphanumeric chars 
			// and it is two characters or longer, so it's probably a word
			return false;
		}
	}

	/**
	 * @param token
	 * @return
	 */
	public static boolean isSpeech(final CoreLabel token) {
		String speakerID = token.get(SpeakerAnnotation.class);
		
		if (speakerID == null)
			return false; // this is actually an exceptional case. if the
							// speaker annotation is missing, something is
							// wrong.

		if (speakerID.equals("PER0")) {
			return false; // narrator id. no direct speech
		} else {			
			return true; // everything else is speech
		}
	}

}
