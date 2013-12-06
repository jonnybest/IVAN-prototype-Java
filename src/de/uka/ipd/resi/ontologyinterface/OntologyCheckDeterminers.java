package de.uka.ipd.resi.ontologyinterface;

import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for RuleCheckDeterminers
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleCheckDeterminers
 * @author Torben Brumm
 */
public interface OntologyCheckDeterminers {
	
	/**
	 * Returns further determiner information for the given Word.
	 * 
	 * @param word Word to get determiner information for.
	 * @return Determiner information.
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 */
	public DeterminerInfo getDeterminerInfoForWord(Word word) throws WordNotFoundException, NotConnectedException;
	
}
