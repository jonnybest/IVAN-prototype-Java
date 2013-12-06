package de.uka.ipd.resi.ontologyinterface;

import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.exceptions.SyntaxException;

/**
 * Ontology interface for taggers which can add Penn Treebank tags to words.
 * 
 * @author Torben Brumm
 */
public interface OntologyPOSTag {
	
	/**
	 * Adds Penn Treebank tags (POS tags) to the words of the sentence
	 * 
	 * @param sentence Sentence to add Penn Treebank tags to.
	 * @throws SyntaxException
	 */
	public void tagSentenceWithPOS(Sentence sentence) throws SyntaxException;
	
}
