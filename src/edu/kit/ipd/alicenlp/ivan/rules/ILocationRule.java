package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.Tree;

/**
 * this is the interface for rules which find locations in a single sentence.
 * 
 * @author Jonny
 * 
 */
public interface ILocationRule extends ISentenceRule {

	/**
	 * Indicates that this location referes to multiple entities.
	 * 
	 * @return TRUE, if the location refers to multiple entites. FALSE, if the
	 *         location refers to a single entity
	 */
	public boolean hasMultipleReferents();

	/**
	 * This method gives the location.
	 * 
	 * @return The location as a tree
	 */
	public Tree getFirstModifier();

	/**
	 * Prints location
	 * 
	 * @return The location as a string.
	 */
	public String printFirstModifier();

	/**
	 * Prints referent
	 * 
	 * @return the tree which describes the entity to which the location in this
	 *         sentence refers to
	 */
	public Tree getWordAsTree();

	/**
	 * Gives a single word which describes the entity in this sentence (the one
	 * with the location)
	 * 
	 * 
	 * @return the word for this entity
	 */
	public IndexedWord getWord();

}