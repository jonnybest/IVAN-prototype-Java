/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.getDeterminer;
import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.getPrepRelations;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;

/** The "towards" rule is used for finding out if there's a direction
 * @author Jonny
 *
 */
public class TowardsPresentRule implements ISentenceRule {

	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IGraphRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	private String prepositionalModifier;

	@Override
	public boolean apply(CoreMap Sentence) {
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if (root == null) {
			return false;
		}
		IndexedWord subject = graph.getChildWithReln(root, EnglishGrammaticalRelations.NOMINAL_SUBJECT);
		if (subject==null) {
			return false;
		}
		List<IndexedWord> things = getPrepRelations(subject, graph, "towards");
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
	
	
	/**
	 * Returns the whole noun phrase for words with a determiner. If there is no determiner present, only the word itself is returned.
	 * @param word
	 * @param sentence
	 * @return
	 */
	protected static String getNounPhrase(IndexedWord word, CoreMap sentence){
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord det = getDeterminer(word, graph);
		int offset = sentence.get(CharacterOffsetBeginAnnotation.class);
		if(det != null)
		{
			int start = det.beginPosition() - offset;
			int end = word.endPosition() - offset;
			return sentence.get(TextAnnotation.class).substring(start, end);
		}
		else {
			return word.originalText();			
		}
	}
}
