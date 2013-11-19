package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.Tree;

public interface ILocationRule extends ISentenceRule {

	public boolean hasMultipleReferents();

	public Tree getFirstModifier();

	public String printFirstModifier();

	public Tree getWordAsTree();

	public IndexedWord getWord();

}