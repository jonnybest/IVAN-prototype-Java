package de.uka.ipd.resi.ontologyinterface;

import java.util.ArrayList;

import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for RuleCompleteProcessWords.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleCompleteProcessWords
 * @author Torben Brumm
 */
public interface OntologyCompleteProcessWords {
	
	/**
	 * Returns a VerbFrame with all arguments this process word has.
	 * 
	 * @param processWord Word to check.
	 * @return VerbFrame with the arguments of the process word.
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 */
	public ArrayList<VerbFrame> getProcessWordArgs(Word processWord) throws WordNotFoundException,
			NotConnectedException;
	
}
