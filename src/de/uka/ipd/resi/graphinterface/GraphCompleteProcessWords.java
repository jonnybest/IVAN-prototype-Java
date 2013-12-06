package de.uka.ipd.resi.graphinterface;

import java.util.List;

import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Graph interface for RuleCompleteProcessWords.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleCompleteProcessWords
 * @author Torben Brumm
 */
public interface GraphCompleteProcessWords {
	
	/**
	 * Returns all process words from the graph. Each process word is represented by a List of VerbFrames (one VerbFrame
	 * for each ontology plus VerbFrames with suggestions from the graph (ontology name null)).
	 * 
	 * @return List of process words.
	 * @throws SyntaxException 
	 */
	public List<List<VerbFrame>> getProcessWords() throws SyntaxException;
	
	/**
	 * Updates the graph with the information found in the given VerbFrame.
	 * 
	 * @param verbFrame VerbFrame with new information.
	 * @throws WordNotFoundException
	 */
	public void updateVerbFrame(VerbFrame verbFrame) throws WordNotFoundException;
}
