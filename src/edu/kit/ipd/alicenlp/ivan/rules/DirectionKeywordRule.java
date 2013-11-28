package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.kit.ipd.alicenlp.ivan.rules.BaseRule.*;

import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.AdvClauseModifierGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.AdverbialModifierGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;


/**
 * Applies a list of keywords and attempts to extract a direction from the
 * sentence. Directions are valid if they are specified as prepositional modfier
 * with "to" or "towards" or as a adverbial modifier or as a direct object.
 * 
 * @author Jonny
 * 
 */
public class DirectionKeywordRule implements ISentenceRule {

	/**
	 * This list of keywords signals that the subject's direction is being
	 * described.
	 */
	public final String[] VerbKeywords = { "face", "turn", "orient", "look" };

	/**
	 * These are prepositions that signal a direction. If the verb keywords are
	 * found, we extract the actual direction with these prepositions.
	 */
	public final String[] PrepKeywords = { "towards", "at", "into", "to" };

	private IndexedWord verb;
	private IndexedWord subject;
	private String direction;

	@Override
	public boolean apply(CoreMap Sentence) {
		SemanticGraph graph = Sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		Tree tree = Sentence.get(TreeAnnotation.class);

		// check for kw match
		for (String kw : VerbKeywords) {
			// if the root matches our keywords, use it.
			if (root.lemma().equalsIgnoreCase(kw)) {
				// save the found verb. I assume that these verbs are always
				// roots
				setVerb(root);

				setSubject(BaseRule.getSubject(graph));
				// 1. check for and extract prepositional modifier
				for (String prepbase : PrepKeywords) {

					List<IndexedWord> preplistsubj = getPrepRelations(
							getSubject(), graph, prepbase);
					List<IndexedWord> preplistroot = getPrepRelations(root,
							graph, prepbase);
					if (preplistsubj.size() > 0) {
						// setDirection(printSubGraph(preplistsubj.get(0),
						// Sentence));
						setDirection(preplistsubj.get(0), tree, "PP");
						return true;
					} else if (preplistroot.size() > 0) {
						setDirection(preplistroot.get(0), tree, "PP");
						return true;
					}
				}

				// 2. check for and extract adverbial modifier
				IndexedWord advmod = DirectionKeywordRule.getAdvMod(root, graph);
				if (advmod != null) {
					setDirection(advmod, tree, "ADVP");
					return true;
				}

				// 3. check for and extract direct object
				IndexedWord dobj = getDirectObject(root, graph);
				if (dobj != null) {
					setDirection(printSubGraph(dobj, Sentence));
					return true;
				}
				// stop after the first match
				break;
			}
		}

		// String[] partmodkw = new String[ ]{"face", "turn"};

		// participal modifier rules for facing
		{
			String kw = "face";
			GrammaticalRelation rel = EnglishGrammaticalRelations.PARTICIPIAL_MODIFIER;
			List<SemanticGraphEdge> lst = graph.findAllRelns(rel);
			if (!lst.isEmpty()) {
				// partmod.lemma().equalsIgnoreCase(kw))
				for (SemanticGraphEdge edge : lst) {
					if (edge.getDependent().lemma().equalsIgnoreCase(kw)) {
						subject = edge.getGovernor();
						verb = edge.getDependent();
						// the modifier is probably the last dependent of this
						// verb
						IndexedWord mod = graph.getChildList(verb).get(
								graph.getChildList(verb).size() - 1);
						// setDirection(printTree(match(edge.getDependent(),
						// tree)));
						setDirection(printTree(match(mod, tree, null, true)));
						return true;
					}
				}
			}
		}

		// relative clause modifier rules for turned
		{
			String kw = "turn";
			GrammaticalRelation rel = EnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER;
			List<SemanticGraphEdge> lst = graph.findAllRelns(rel);
			if (!lst.isEmpty()) {
				// partmod.lemma().equalsIgnoreCase(kw))
				for (SemanticGraphEdge edge : lst) {
					if (edge.getDependent().lemma().equalsIgnoreCase(kw)) {
						subject = edge.getGovernor();
						verb = edge.getDependent();
						// the modifier is probably the last dependent of this
						// verb
						IndexedWord mod = graph.getChildList(verb).get(
								graph.getChildList(verb).size() - 1);
						// direction = printSubGraph(edge.getDependent(),
						// Sentence);
						setDirection(printTree(match(mod, tree, null, true)));
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * @param word
	 * @param tree
	 */
	private void setDirection(IndexedWord word, Tree tree, String expectedPOS) {
		setDirection(printTree(match(word, tree, expectedPOS, true)));
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
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
	 * @param verb
	 *            the verb to set
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
	 * @param subject
	 *            the subject to set
	 */
	private void setSubject(IndexedWord subject) {
		this.subject = subject;
	}

	/** Finds an adverbial modifier
	 * 
	 * @param word
	 * @param graph
	 * @return
	 */
	public static IndexedWord getAdvMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AdverbialModifierGRAnnotation.class);
		IndexedWord advmod = graph.getChildWithReln(word, reln);
		if (advmod == null) {
			GrammaticalRelation reln2 = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AdvClauseModifierGRAnnotation.class);
			return graph.getChildWithReln(word, reln2);
		}
		else {
			return advmod;			
		}
	}
}
