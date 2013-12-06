package de.uka.ipd.resi.graphinterface;

import java.util.Set;

import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Graph interface for RuleSimilarMeaning.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleSimilarMeaning
 * @author Torben Brumm
 */
public interface GraphSimilarMeaning {
	
	/**
	 * Returns all nouns in the graph.
	 * 
	 * @return All nouns.
	 * @throws SyntaxException
	 */
	public Set<Word> getAllNouns() throws SyntaxException;
	
	/**
	 * Replaces the old noun with the new noun.
	 * 
	 * @param oldNoun Noun which is replaced.
	 * @param newNoun Noun which replaces the other noun.
	 * @throws WordNotFoundException
	 */
	public void replaceNoun(Word oldNoun, Word newNoun) throws WordNotFoundException;
}
