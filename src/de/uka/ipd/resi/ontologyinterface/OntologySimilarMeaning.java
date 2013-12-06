package de.uka.ipd.resi.ontologyinterface;

import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * Ontology interface for RuleSimilarMeaning.
 * 
 * @see de.uka.ipd.resi.ruleimpl.RuleSimilarMeaning
 * @author Torben Brumm
 */
public interface OntologySimilarMeaning {
	
	/**
	 * Returns the similarity of two nouns. The more similar they are, the greater the absolute value of the returned
	 * value is (max is 1). If the returned value is greater than 0, the first noun is more general, if it is less than
	 * 0, the second noun is more general.
	 * 
	 * @param noun1 First noun.
	 * @param noun2 Second noun.
	 * @return Similarity (on a scale from -1 to 1).
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 */
	public float getSimilarity(Word noun1, Word noun2) throws WordNotFoundException, NotConnectedException;
}
