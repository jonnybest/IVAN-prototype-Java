/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 *
 */
public class NounRootRule implements IGraphRule {

	private IndexedWord noun;
	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IGraphRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap Sentence) {
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if (root != null) {
			String pos = root.get(PartOfSpeechAnnotation.class);
			if (pos != null && pos.startsWith("NN")) {
				setNoun(root);
				return true;
			}
		}
		return false;
	}
	/**
	 * @return the noun
	 */
	public IndexedWord getNoun() {
		return noun;
	}
	/**
	 * @param noun the noun to set
	 */
	private void setNoun(IndexedWord noun) {
		this.noun = noun;
	}
}
