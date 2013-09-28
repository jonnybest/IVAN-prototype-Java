/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/** The "towards" rule is used for finding out if there's a direction
 * @author Jonny
 *
 */
public class TowardsPresentRule extends BaseRule implements IGraphRule {

	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IGraphRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	private String prepositionalModifier;

	@Override
	public boolean apply(CoreMap Sentence) {
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		List<IndexedWord> things = getPrepRelations(null, graph, "towards");
		if (things.size() > 0) {
			this.prepositionalModifier = getNounPhrase(things.get(0), Sentence);				
			return true;
		}
		return false;
	}

	/**
	 * @return the prepositionalModifier
	 */
	public String getPrepositionalModifier() {
		return prepositionalModifier;
	}
}
