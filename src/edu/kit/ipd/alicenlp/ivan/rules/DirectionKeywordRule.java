package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * Applies a list of keywords and attempts to extract a direction from the sentence. 
 * Directions are valid if they are specified as prepositional modfier with "to" or "towards" or as a adverbial modifier or as a direct object. 
 * @author Jonny
 *
 */
public class DirectionKeywordRule extends BaseRule implements IGraphRule {

	/**
	 * This list of keywords signals that the subject's direction is being described.
	 */
	public final String[] Keywords = {
			"face", 
			"turn", 
			"orient", 
			"look"
	};
	
	private IndexedWord verb;
	private IndexedWord subject;
	private String direction;
	
	@Override
	public boolean apply(CoreMap Sentence) {
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		
		// check for kw match
		for (String kw : Keywords) {
			// if the root matches our keywords, use it.
			if (root.lemma().equalsIgnoreCase(kw)) {
				// save the found verb. I assume that these verbs are always roots
				verb = root;
				
				// TODO: implement extraction
				subject = getSubject(graph);
				// 1. check for and extract "towards"
				List<IndexedWord> towlistsubj = getPrepRelations(subject, graph, "towards");
				List<IndexedWord> towlistroot = getPrepRelations(root, graph, "towards");
				if (towlistsubj.size() > 0) {
					direction = getNounPhrase(towlistsubj.get(0), Sentence);
				} 
				else if(towlistroot.size() > 0)
				{
					direction = getNounPhrase(towlistroot.get(0), Sentence);
				}
				// 2. check for and extract adverbial modifier
				IndexedWord advmod = getAdvMod(root, graph);
				if (advmod != null) {
					direction = printSubGraph(advmod, graph);
				}
				// 3. check for and extract "to"
				List<IndexedWord> tolistsubj = getPrepRelations(subject, graph, "to");
				List<IndexedWord> tolistroot = getPrepRelations(root, graph, "to");
				if (tolistsubj.size() > 0) {
					direction = getNounPhrase(tolistsubj.get(0), Sentence);
				} 
				else if(tolistroot.size() > 0)
				{
					direction = getNounPhrase(tolistroot.get(0), Sentence);
				}
				// 4. check for and extract direct object
				// stop after the first match
				break;
			}
		}
		return false;
	}

}
