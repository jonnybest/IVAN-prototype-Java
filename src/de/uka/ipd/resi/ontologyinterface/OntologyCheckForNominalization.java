package de.uka.ipd.resi.ontologyinterface;

import java.util.List;

import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for RuleCheckForNominalization
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleCheckForNominalization
 * @author Torben Brumm
 */
public interface OntologyCheckForNominalization {
	
	/**
	 * Returns possible process words for a possible nominalization.
	 * 
	 * @param possibleNominalization Word to check.
	 * @return List of possible process words.
	 * @throws NotConnectedException
	 * @throws WordNotFoundException
	 */
	public List<String> getProcessWordForNominalization(Word possibleNominalization) throws WordNotFoundException,
			NotConnectedException;
	
}
