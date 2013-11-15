package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.Tree;

public interface ILocationRule extends ISentenceRule {

	public boolean hasMultipleReferents();

	public Tree getPrepositionalModifierAsTree();

	public String getPrepositionalModifier();

	public Tree getWordAsTree();

	public IndexedWord getWord();

}