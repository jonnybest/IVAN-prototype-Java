package de.uka.ipd.resi.ontologyinterface;

import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for RuleAvoidAmbiguousWords.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleAvoidAmbiguousWords
 * @author Torben Brumm
 */
public interface OntologyAvoidAmbiguousWords {
	
	/**
	 * Assures that an ontology constant is selected for the given word (if the user wants to select one).
	 * 
	 * @param word Word to select the constant for (if it is not already selected).
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 */
	void assureOntologyConstantIsSelected(Word word) throws WordNotFoundException, NotConnectedException;
	
}
