package de.uka.ipd.resi.graphinterface;

import java.util.Set;

import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;


/**
* Graph interface for RuleCheckForNominalization.
* 
* @see de.uka.ipd.resi.ruleimpl.RuleCheckForNominalization
* @author Torben Brumm
*/
public interface GraphCheckForNominalization {
	
	/**
	 * Returns all nouns in the graph (which are all a possible nominalization).
	 * 
	 * @return All nouns.
	 * @throws SyntaxException
	 */
	public Set<Word> getAllNouns() throws SyntaxException;

	/**
	 * Returns the nominalization mark set for the given word or null if there is none.
	 * 
	 * @param word Word to find the mark for.
	 * @return Mark for the word.
	 * @throws WordNotFoundException 
	 */
	public Mark getNominalizationMark(Word word) throws WordNotFoundException;

	/**
	 * Sets the nominalization mark for the given word and overwrites an existing one. If mark is null, an exinsting mark is deleted.
	 * 
	 * @param word Word to set the mark for.
	 * @param mark Mark for the word.
	 * @throws WordNotFoundException 
	 */
	public void setNominalizationMark(Word word, Mark mark) throws WordNotFoundException;
	
}
