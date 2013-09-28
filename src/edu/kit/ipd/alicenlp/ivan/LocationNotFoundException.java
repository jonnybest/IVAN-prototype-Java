package edu.kit.ipd.alicenlp.ivan;

/**
 * This class signals that IVAN was supposed to extract a location argument from a sentence, but failed in doing so.
 * @author Jonny
 *
 */
public class LocationNotFoundException extends IvanException {

	public LocationNotFoundException(String string) {
		super(string);
	}
	
}
