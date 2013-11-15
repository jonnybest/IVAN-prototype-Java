/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class implements the word->prep_in->det rule which is commonly used to find for location modifiers.
 * Please create at least one rule per sentence.
 * The class is stateful and the results of the application can be accessed by the provided getters.
 * @author Jonny
 *
 */
public class WordPrepInDetRule extends BaseRule implements ISentenceRule, ILocationRule
{
	private IndexedWord word = null;
	private String prepositionalModifier = null;
	private Tree locationtree = null;
	private Tree wordTree = null;
	
	private boolean multipleReferents = false;

	/** 
	 * This method tries to find a word that is modified by the preposition in + a determiner. 
	 * It starts with the root and then checks again with the subject.  
	 * @param sentence  
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		GrammaticalRelation nsubjreln = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		IndexedWord subject = graph.getChildWithReln(root, nsubjreln);
		
		if(subject == null) {
		    // okay, this sucks
			this.word = getSubject(graph);
		}
		else {
			this.word = subject;
		}
		
		if (!apply(root, sentence)) {
			if (!apply(subject, sentence))
			{
				return false;
			}
		}
		
		// for now we only allow one referent per location, so we only return one. But we should want to know if there are more than one referents in this sentence.  
		this.multipleReferents = BaseRule.resolveCc(word, graph, null).size() > 1;
			
		// word TREE
		Tree mytree = sentence.get(TreeAnnotation.class);
		wordTree = match(word, mytree, "NP", false);
		
		return true;
	}

	public boolean apply(IndexedWord governor, CoreMap sentence)
	{	
		// ANALYSIS part
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);		
		if (governor == null) {
			return false;
		}

		List<IndexedWord> prep_ins = getPrepinRelations(governor, graph);
		if(prep_ins.isEmpty())
			return false;
		
		// TREE extraction part
		Tree mytree = sentence.get(TreeAnnotation.class);

		// location TREE
		IndexedWord inword = prep_ins.get(0); // I don't think stanford produces more than one of these per head
		locationtree = match(inword, mytree, "PP", true);
		
		if(locationtree == null)
			return false;
		
		// PRINTING part		
		this.prepositionalModifier = printTree(locationtree);
		
//		for (IndexedWord iw : prep_ins) {
//			if (hasDeterminer(iw, graph)) {
//				this.prepositionalModifier = "in " + printSubGraph(iw, sentence);
//				return true;
//			}
//		}
		return true;
	}

	@Override
	public IndexedWord getWord() {
		return word;
	}
	
	@Override
	public Tree getWordAsTree()
	{
		return wordTree;
	}

	@Override
	public String getPrepositionalModifier() {
		return prepositionalModifier;
	}
	
	@Override
	public Tree getPrepositionalModifierAsTree() {
		return locationtree;
	}

	/**
	 * @return the multipleReferents
	 */
	@Override
	public boolean hasMultipleReferents() {
		return multipleReferents;
	}
}
