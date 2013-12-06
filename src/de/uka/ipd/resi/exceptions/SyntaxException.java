package de.uka.ipd.resi.exceptions;

import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Sentence;

/**
 * Exception which gets thrown when there was an error in the structure of a sentence or a graph.
 * 
 * @author Torben Brumm
 */
public class SyntaxException extends Exception {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for graphs.
	 * 
	 * @param g Graph which caused the exception.
	 */
	public SyntaxException(final Graph g, final String message) {
		super(g.getName() + " graph " + g.toString() + ": " + message);
	}
	
	/**
	 * Constructor for sentences.
	 * 
	 * @param sentence Sentence which caused the exception.
	 */
	public SyntaxException(final Sentence sentence) {
		super("Sentence: " + sentence.getPlainSentence());
	}
}
