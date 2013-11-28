/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * This is a utility class which provides static methods to rules for easier checking.
 * @author Jonny
 *
 */
public abstract class BaseRule {
	

	/** The <code>word</code> has a tag with prefix <code>tag</code>.
	 * Ex. "drives/VBZ" is a member of pos-family "VB" 
	 * 
	 * @param word
	 * @param tag A prefix tag
	 * @return pos.startsWith(tag)
	 */
	public static boolean isPOSFamily(CoreLabel word, String tag) {
		String pos = word.get(PartOfSpeechAnnotation.class).toUpperCase();
		return pos.startsWith(tag.toUpperCase());
	}

	/** Finds a determiner. ex: "the"
	 * @param word
	 * @param graph
	 * @return
	 */
	public static IndexedWord getDeterminer(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	/** Finds a direct object. ex: the chair in "move the chair"
	 * 
	 * @param word
	 * @param graph
	 * @return
	 */
	public static IndexedWord getDirectObject(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}

	/** Gets the prepositions for this word in this sentence
	 * 
	 * @param startingWord 
	 * @param graph Sentence
	 * @param preposition A seeding expression. Ex: "in" or "in_front_of"
	 * @return
	 */
	public static List<IndexedWord> getPrepRelations(IndexedWord startingWord, SemanticGraph graph, String preposition) {
		//GrammaticalRelation prepreln = GrammaticalRelation.getRelation(PrepositionalModifierGRAnnotation.class);
		GrammaticalRelation prepreln = EnglishGrammaticalRelations.getPrep(preposition);		
		// checking rule: root->prep_in->det
		return graph.getChildrenWithReln(startingWord, prepreln);
	}

	/**
	 * Returns the primary thing this sentence is talking about. 
	 * More precisely: Returns any subject or a passive subject of the sentence, or the root if none applies.
	 * @param graph
	 * @return
	 */
	public static IndexedWord getSubject(SemanticGraph graph) {
		if (graph.isEmpty()) {
			return null;
		}
		GrammaticalRelation[] subjects = { 
				EnglishGrammaticalRelations.NOMINAL_SUBJECT,
				EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT
		};		
		IndexedWord firstRoot = graph.getFirstRoot();
		List<IndexedWord> children = graph.getChildrenWithRelns(firstRoot, Arrays.asList(subjects));
		if (children != null && children.size() > 0) {
			assert children.size() == 1; // not really dangerous. But we need to change our implementation to return a list, if there are more than one subject.
			
			return children.get(0);
		}
		// return null;
		return graph.getFirstRoot(); // in a subject-less sentence, the root is as good as the subject
	}

	/** This method creates a string which represents the part of the sentence this <code>tree</code> stands for.
	 * @param tree A (partial) syntax tree 
	 * @return The original sentence part
	 */
	public static String printTree(Tree tree) {
		final StringBuilder sb = new StringBuilder();
		
		for ( final Tree t : tree.getLeaves() ) {
		     sb.append(t.toString()).append(" ");
		}
		return sb.toString().trim();
	}

	/** This method searches for an IndexedWord inside a Tree. It returns a subtree which represents the word's phrase, 
	 * or NULL, if none was found. Runs in 2*O(n), since Tree does not provide index-based lookup.  
	 * @param wordToFind an IndexedWord or at least a CoreLabel with BeginIndexAnnotation and EndIndexAnnotation
	 * @param treeToSearch
	 * @param expectedPOS the Part-of-speech that the caller wants to work with (usually NP or PP) 
	 * @param canGoUp if TRUE, this method returns the largest match it can find. if false, it returns the first match
	 * @return A subtree representing the indexed word.
	 */
	static public Tree match(IndexedWord wordToFind, Tree treeToSearch, String expectedPOS, boolean canGoUp) {
		return match(wordToFind, treeToSearch, expectedPOS, canGoUp, 0);
	}
	/** This method searches for an index word in a sentence tree
	 * 
	 * @param wordToFind 
	 * @param treeToSearch 
	 * @param expectedPOS The expected POS tag for the result. If this is NULL, the method tries to find a phrase.  
	 * @param canGoUp If TRUE the method will walk up the tree to find a phrase.
	 * @param skip Set to "1" if you want to find the phrase for "in front of". Set to "0" otherwise.
	 * @return The largest matching tree.
	 */
	static public Tree match(IndexedWord wordToFind, Tree treeToSearch, String expectedPOS, boolean canGoUp, int skip) {
		int end = wordToFind.get(EndIndexAnnotation.class);
		int begin = wordToFind.get(BeginIndexAnnotation.class);
		
		// first, find whatever is at the word's index
		for (Tree tree : treeToSearch) {
			CoreLabel lbl = ((CoreLabel) tree.label());
			
			if(lbl != null && 
					lbl.get(EndIndexAnnotation.class) != null &&
					lbl.get(EndIndexAnnotation.class) == end)
			{
				if(lbl.get(BeginIndexAnnotation.class) == begin)
				{
					// we found the first subtree at the word's index
					// now, check if the word here is our searchword
					if(tree.getLeaves().get(0).label().value().equals(wordToFind.value()))
					{
						// we have found the label.
						Tree candidate = tree;
						
						if(expectedPOS != null)
						{
							// if we know our desired POS, just keep walking up the tree to find the first instance of the expected pos 
							while(!expectedPOS.equals(candidate.value())){
								// if we don't have the right POS, just try our parent
								candidate = candidate.parent(treeToSearch);
								
								if(candidate == null)
								{
									return null;
								}
							}
							candidate = skip(candidate, treeToSearch, expectedPOS, skip);
						}
						else {
							// else walk up the tree again to find the corresponding phrase
							while(!candidate.isPhrasal())
							{
								candidate = candidate.parent(treeToSearch); // edu.stanford.nlp.trees.Tree.parent(Tree root)
								
								if(candidate == null)
								{
									return null;
								}
							}
						}						
						
						if (canGoUp) {
							// now keep walking as long as the phrase does not change. this should yield the largest representative phrase for this word.
							String phrase = candidate.value();
							while (phrase.equals(candidate.parent(treeToSearch)
									.value())) {
								candidate = candidate.parent(treeToSearch);

								if (candidate == null) {
									return null;
								}
							}
						}
						return candidate;
					}
				}
			}
		}
		return null;
	}

	static private Tree skip(Tree candidate, Tree parent, String expectedPOS, int skip) {
		if(skip == 0)
			return candidate;
		
		Tree lastvalid = candidate;
		
		// we are allowed to skip non-matching phrases
		while (skip > 0) {
			skip--;
			
			// we walk up the 
			do {
				// if we don't have the right POS, just try our parent
				candidate = candidate.parent(parent);

				if (candidate == null) {
					// we are already on top
					return lastvalid;
				}
				else if (expectedPOS.equals(candidate.value())) {
					// we have found a good match. this does not count as a skip
					lastvalid = candidate;
				}
			} while (skip >= 0 && !expectedPOS.equals(candidate.value()));
		}
		return lastvalid;
	}

	/** This method attempts to resolve noun phrases which consist of more than one word.
	 * More precisely, it looks for nn dependencies below {@code head} and creates an entity.
	 * @param head The head of the noun phrase
	 * @param graph The sentence to look in.
	 * @return A distinct word
	 */
	private static String resolveNN(IndexedWord head, SemanticGraph graph) {
		List<IndexedWord> nns = graph.getChildrenWithReln(head, EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER);
		String name = "";
		// check for nulls. if there is nothing here, we have nothing to do.
		if (nns != null) {
			for (IndexedWord part : nns) {
				name += part.word();
				name += " ";
			}
			name += head.word();
			return name;
		}
		else {
			return null;
		}
	}
	
	/** This method attempts to resolve noun phrases and conjunction. 
	 * More precisely, it looks for nn and con_and dependencies below {@code head} and creates a list of entities.
	 * @param head The head of the noun phrase
	 * @param sentence The sentence to which <code>head</code> belongs
	 * @param namesIW The IndexedWords which form the head of each noun phrase
	 * @return A list of distinct words or names, grouped by "and"
	 */
	public static ArrayList<String> resolveCc(IndexedWord head,
			CoreMap sentence, ArrayList<IndexedWord> namesIW) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		// list of names
		ArrayList<String> names = new ArrayList<String>();
		if (namesIW == null)
			namesIW = new ArrayList<IndexedWord>();
		// adding this subject
		names.add(resolveNN(head, graph));
		//names.add(printTree(match(head, tree, "NP", true, 0)));
		namesIW.add(head);
		// check for more!
		// more names can be reached with "and". Get us an "and":
		GrammaticalRelation andrel = EnglishGrammaticalRelations.getConj("and");
		// ask the graph for everything that is connected by "and":
		List<IndexedWord> ands = graph.getChildrenWithReln(head, andrel);
		for (IndexedWord w : ands) {
			// add 'em
			names.add(resolveNN(w, graph));
			namesIW.add(w);
		}
		// hope those are all
		return names;
	}
}
