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
				setVerb(root);

				setSubject(getSubject(graph));
				// 1. check for and extract "towards"
				List<IndexedWord> towlistsubj = getPrepRelations(getSubject(), graph, "towards");
				List<IndexedWord> towlistroot = getPrepRelations(root, graph, "towards");
				if (towlistsubj.size() > 0) {
					setDirection(getNounPhrase(towlistsubj.get(0), Sentence));
					return true;
				} 
				else if(towlistroot.size() > 0)
				{
					setDirection(getNounPhrase(towlistroot.get(0), Sentence));
					return true;
				}
				// 2. check for and extract adverbial modifier
				IndexedWord advmod = getAdvMod(root, graph);
				if (advmod != null) {
					setDirection(printSubGraph(advmod, graph));
					return true;
				}
				// 3. check for and extract "to"
				List<IndexedWord> tolistsubj = getPrepRelations(getSubject(), graph, "to");
				List<IndexedWord> tolistroot = getPrepRelations(root, graph, "to");
				if (tolistsubj.size() > 0) {
					setDirection(getNounPhrase(tolistsubj.get(0), Sentence));
					return true;
				} 
				else if(tolistroot.size() > 0)
				{
					setDirection(getNounPhrase(tolistroot.get(0), Sentence));
					return true;
				}
				// 4. check for and extract direct object
				IndexedWord dobj = getDirectObject(root, graph);
				if (dobj != null) {
					setDirection(printSubGraph(dobj, graph));
					return true;
				}
				// stop after the first match
				break;
			}
		}
		return false;
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	private void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * @return the verb
	 */
	public IndexedWord getVerb() {
		return verb;
	}

	/**
	 * @param verb the verb to set
	 */
	private void setVerb(IndexedWord verb) {
		this.verb = verb;
	}

	/**
	 * @return the subject
	 */
	public IndexedWord getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	private void setSubject(IndexedWord subject) {
		this.subject = subject;
	}
}
