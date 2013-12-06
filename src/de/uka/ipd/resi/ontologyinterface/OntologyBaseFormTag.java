package de.uka.ipd.resi.ontologyinterface;

import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for taggers which can add base forms to words.
 * 
 * @author Torben Brumm
 */
public interface OntologyBaseFormTag {
	
	/**
	 * Adds base forms to the words of the sentence (if applicable).
	 * 
	 * @param sentence Sentence to add base forms to.
	 * @throws WordNotFoundException TODO
	 */
	public void tagSentenceWithBaseForm(Sentence sentence) throws WordNotFoundException;
	
}
