package de.uka.ipd.resi.graphinterface;

import java.util.Set;

import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Graph interface for RuleCheckDeterminers.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleCheckDeterminers
 * @author Torben Brumm
 */
public interface GraphCheckDeterminers {
	
	/**
	 * Returns all determiners in the graph.
	 * 
	 * @return All determiners.
	 * @throws SyntaxException
	 */
	public Set<Word> getAllDeterminers() throws SyntaxException;
	
	/**
	 * Returns further determiner information for the given Word.
	 * 
	 * @param word Word to get determiner information for.
	 * @return Determiner information.
	 * @throws WordNotFoundException
	 */
	public DeterminerInfo getDeterminerInfoForWord(Word word) throws WordNotFoundException;
	
	/**
	 * Sets further determiner information for the given Word.
	 * 
	 * @param word Word to set determiner information for.
	 * @return determinerInfo Determiner information to set.
	 * @throws WordNotFoundException
	 */
	public void setDeterminerInfoForWord(Word word, DeterminerInfo determinerInfo) throws WordNotFoundException;
	
}
