/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.getPrepRelations;
import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.getSubject;
import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.match;
import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.printTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
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
public class PrepositionalRule implements ISentenceRule
{
	private IndexedWord word = null;
	private List<Tree> locationtrees = new ArrayList<Tree>();
	private List<Tree> wordTrees = new ArrayList<Tree>();
	
	final private String[] protoPrepositions = {
			"on", "in", "beyond", "between", "at", "above", "to", "behind"
		};
	final private String infrontof = "in_front_of";
	
	private boolean multipleReferents = false;

	/** 
	 * This method tries to find a word that is modified by the preposition in + a determiner. 
	 * It starts with the root and then checks again with the subject.  
	 * @param sentence  
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		boolean hasLocation = false;
		
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
		
		// do it once for the root 
		hasLocation = hasLocation | apply(root, sentence);
		
		// and once for the subject (if different)
		if(!root.equals(subject))
			hasLocation = hasLocation | apply(subject, sentence);
		
		// if we found nothing, stop right here
		if(!hasLocation)
			return false;
		
		// for now we only allow one referent per location, so we only return one. But we should want to know if there are more than one referents in this sentence.  
		this.multipleReferents = BaseRule.resolveCc(word, sentence, null).size() > 1;
			
		// word TREE
		Tree mytree = sentence.get(TreeAnnotation.class);
		wordTrees.add(match(word, mytree, "NP", false));
		
		// sort phrases by index
		sort();
		
		return true;
	}

	public boolean apply(IndexedWord governor, CoreMap sentence)
	{	
		// ANALYSIS part
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);		
		if (governor == null) {
			return false;
		}

		for (String proto : protoPrepositions) {
			List<IndexedWord> preps = getPrepRelations(governor, graph, proto);
			
			// skip this preposition, if it does not occur
			if (preps.isEmpty())
				continue;
			
			// TREE extraction part
			Tree mytree = sentence.get(TreeAnnotation.class);
			for (IndexedWord occurrence : preps) {
				// location TREE
				locationtrees.add(match(occurrence, mytree, "PP", true));
			}
		}
		{
			// for "in front of"
			List<IndexedWord> preps = getPrepRelations(governor, graph, infrontof);
			
			// skip this preposition, if it does not occur
			if (!preps.isEmpty())
			{				
				// TREE extraction part
				Tree mytree = sentence.get(TreeAnnotation.class);
				for (IndexedWord occurrence : preps) {
					// location TREE
					locationtrees.add(match(occurrence, mytree, "PP", true, 1)); // we need to skip an NP
				}
			}
		}
		
		// check if we had results
		return locationtrees.size() > 0;
	}
	
	/** The word for this location
	 * @return
	 */
	public Tree getReferent()
	{
		return wordTrees.get(0);
	}

	/** Creates a string from the modifiers which were found in this rule's sentence.
	 * 
	 * @return 
	 */
	public String printModifiers()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < locationtrees.size() - 1; i++) {
			sb.append(printTree(locationtrees.get(i)));
			sb.append(", ");
		}
		sb.append(printTree(locationtrees.get(locationtrees.size() - 1)));
		return sb.toString();
	}
	
	/** the locations in this sentence
	 * 
	 * @return
	 */
	public List<Tree> getPrepositionalModifiers() {
		return locationtrees;
	}

	/**
	 * @return the multipleReferents
	 */
	public boolean hasMultipleReferents() {
		return multipleReferents;
	}
	
	private void sort()
	{
		java.util.Collections.sort(locationtrees, new Comparator<Tree>() {

			@Override
			public int compare(Tree t1, Tree t2) {
				Tree left = t1.getLeaves().get(0);
				Tree right = t2.getLeaves().get(0);
				return ((CoreLabel) left.label()).get(BeginIndexAnnotation.class)
						.compareTo(((CoreLabel) right.label()).get(BeginIndexAnnotation.class));
			}
			
		});
	}
}
