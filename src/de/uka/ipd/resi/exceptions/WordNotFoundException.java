package de.uka.ipd.resi.exceptions;

import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Ontology;

/**
 * Exception which is thrown when an ontology (or a graph) can't find a word in its database (or graph data).
 * 
 * @author Torben Brumm
 */
public class WordNotFoundException extends Exception {
	
	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for graphs.
	 * 
	 * @param g Graph which did not find the word.
	 * @param word Word which was not found.
	 */
	public WordNotFoundException(final Graph g, final String word) {
		super(word + " not found in " + g.getName() + " graph " + g.toString());
	}
	
	/**
	 * Constructor for ontologies.
	 * 
	 * @param o Ontology which did not find the word.
	 * @param word Word which was not found.
	 */
	public WordNotFoundException(final Ontology o, final String word) {
		super(word + " not found in " + o.getName() + " ontology " + o.toString());
	}
	
}
