/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class implements the word->prep_in->det rule which is commonly used to find for location modifiers.
 * Please create at least one rule per sentence.
 * The class is stateful and the results of the application can be accessed by the provided getters.
 * @author Jonny
 *
 */
public class WordPrepInDetRule extends BaseRule implements IGraphRule
{
	private IndexedWord word = null;
	private IndexedWord prepositionalModifier = null;

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
		if (apply(root, sentence)) {
			return true;			
		}
		else if (apply(subject, sentence)){
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean apply(IndexedWord governor, CoreMap sentence)
	{		
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);		
		if (governor == null) {
			return false;
		}
		this.word = governor;
		List<IndexedWord> prep_ins = getPrepinRelations(governor, graph);
		for (IndexedWord iw : prep_ins) {
			if (hasDeterminer(iw, graph)) {
				this.prepositionalModifier = iw;
				return true;
			}
		}
		return false;
	}

	public IndexedWord getWord() {
		return word;
	}

	public IndexedWord getPrepositionalModifier() {
		return prepositionalModifier;
	}
	
	
}
