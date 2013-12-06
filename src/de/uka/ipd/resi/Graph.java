package de.uka.ipd.resi;

import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * @author Torben Brumm
 */
public abstract class Graph {
	
	/**
	 * Id of the attribute which stores the word's base form.
	 */
	public static final int WORD_ATTRIBUTE_BASEFORM = 1;
	
	/**
	 * Id of the attribute which stores the word's POS tag.
	 */
	public static final int WORD_ATTRIBUTE_POSTAG = 0;
	
	/**
	 * Returns the type of the graph.
	 * 
	 * @return Name of graph type.
	 */
	public abstract String getName();
	
	/**
	 * Returns all sentences stored in the graph.
	 * 
	 * @return All sentences.
	 * @throws SyntaxException
	 */
	public abstract Sentence[] getSentences() throws SyntaxException;
	
	/**
	 * Returns if the graph supports the Rule.
	 * 
	 * @param r Rule which might be supported.
	 * @return Is the Rule supported?
	 */
	public boolean supports(final Rule r) {
		try {
			if (Class.forName("de.uka.ipd.resi.graphinterface." + r.getGraphInterface()).isInstance(
					this)) {
				return true;
			}
		}
		catch (final ClassNotFoundException e) {
			return false;
		}
		return false;
	}
	
	/**
	 * Sets all attributes of a word in this graph to the values in the given Word object.
	 * 
	 * @param word The word to change.
	 * @throws WordNotFoundException
	 */
	public abstract void updateWordAttributes(Word word) throws WordNotFoundException;
	
}
