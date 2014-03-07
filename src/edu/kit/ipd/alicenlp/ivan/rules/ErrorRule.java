/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.CONJUNCT;
import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.COPULA;
import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.PREDICATE;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CategoryAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SpeakerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 * 
 */
public class ErrorRule implements ISentenceRule, IErrorRule {
	
	private static final String ERROR_THIS_IS_NOT_A_PROPER_SENTENCE = "This is not a proper sentence, because it has no root: '%s'";
	private static final String ERROR_THIS_SENTENCE_NEEDS_A_VERB = "This sentence needs a verb. If the sentence already has a verb, we could not recognise it. Try using the verb's progressive form instead (with -ing). Text: '%s'";
	private static final String ERROR_THIS_SENTENCE_IS_INTELLIGIBLE = "This sentence is intelligible. It probably needs to be longer.";
	private static final String ERROR_TWO_VERBS_ONE_ENTITY = "The verbs in this sentence both refer to the same thing. Instead, please use one verb per sentence. Otherwise we'll probably get it wrong.";
	private static final String ERROR_MORE_THAN_A_SINGLE_TOPIC = "IVAN cannot handle sentences with more than a single topic. Try splitting the sentence into two sentences.";
	private static final String ERROR_SENTENCES_WITH_I = "We have trouble understanding sentences with \"I\". Please try not to use \"I\" in a sentence.";
	private static final String ERROR_FRAGMENT = "This sentence seems to contain a fragment. The sentence is either incomplete or it contains parts which should go in their own sentence.";

	private static Logger log = Logger.getLogger(ErrorRule.class.toString());

	IvanErrorMessage msg;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.
	 * util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		// check for a graph
		if (applyNeedsGraph(sentence)) {
			log.info("ErrorRule.apply(): No graph in '"
					+ sentence.toString());
			return true;
		}
		// check for bad penn tags
		if (applyBadPennTags(sentence)) {
			System.out
					.println("ErrorRule.apply(): Sentence contains fragment: '"
							+ sentence.toString());
			return true;
		}

		// checking roots:
		if (applyRoots(sentence)) {
			System.out
					.println("ErrorRule.apply(): Sentence must have a single root: '"
							+ sentence.toString());
			return true;
		}

		if (applyNeedsOneVerb(sentence)) {
			System.out
					.println("ErrorRule.apply(): Sentence must have at least one verb: '"
							+ sentence.toString());
			return true;
		}

		// verbs that are joined by a conjunction are probably missing arguments
		// and cannot be properly parsed with dependencies
		if (applyCCVerbs(sentence)) {
			System.out
					.println("ErrorRule.apply(): Verbs are conjoined and therefore likely to lack arguments: '"
							+ sentence.toString());
			return true;
		}

		// first person sentences do not have a verb that reflects the action on
		// the scene
		if (applyNo1stPerson(sentence)) {
			System.out
					.println("ErrorRule.apply(): Sentence is in 1st person: '"
							+ sentence.toString());
			return true;
		}

		// everything seems to be fine.
		return false;
	}

	/**
	 * Marks sentences with undesirable Penn tags. Specifically: FRAG - Fragment
	 * (because too much information is missing)
	 * 
	 * @param sentence
	 *            input
	 * @return FALSE if all the tags are "allowed"
	 */
	public boolean applyBadPennTags(CoreMap sentence) {
		// maybe do these too?
		// UCP - Unlike Coordinated Phrase (because it's a pairing of unlike
		// constituents)
		// SBAR - Subordinate clause, complementizer or WH- and sentence
		// (because they do not fit the genre)

		// fetch a tree
		Tree t = sentence.get(TreeAnnotation.class);
		// iterate through all the nodes
		for (LabeledScoredTreeNode thing : t
				.toArray(new LabeledScoredTreeNode[] {})) {
			// fetches the label which contains tags
			CoreLabel lbl = (CoreLabel) thing.label();
			// fetches the tag which is called a "category" here
			String cat = lbl.get(CategoryAnnotation.class);
			// if the category is "Fragment", we don't want it
			if (cat != null && cat.equals("FRAG")) {
				// check if this fragment is inside direct speech. if so, we can
				// ignore it.
				int numleaves = thing.getLeaves().size(); // how long is this
															// fragment?
				// where does the fragment begin?
				int beginFragment = ((CoreLabel) thing.getLeaves().get(0).label())
						.get(BeginIndexAnnotation.class); //
				// retrieve the tokens for this sentence
				List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
				// check the tokens inside this fragment
				for (int i = beginFragment; i < beginFragment + numleaves - 1; i++) {
					// get the single token
					CoreLabel token = tokens.get(i);
					// get the speaker annotation. if this is not the narrator
					// ("PER0"), we know it is speech and therefore not subject
					// to analysis.
					if (token.get(SpeakerAnnotation.class) == null)
						continue;
					else if(!"PER0".equals(token.get(SpeakerAnnotation.class)))
						return false;
				}

				int tmpstart = Integer.MAX_VALUE, tmpend = 0;
				for (Tree tree : thing.getLeaves()) {
					Integer start = ((CoreLabel) tree.label())
							.get(CharacterOffsetBeginAnnotation.class);
					if (start < tmpstart)
						tmpstart = start;
					Integer end = ((CoreLabel) tree.label())
							.get(CharacterOffsetEndAnnotation.class);
					if (end > tmpend)
						tmpend = end;
				}

				msg = new IvanErrorMessage(
						IvanErrorType.GRAPH,
						Span.fromValues(tmpstart, tmpend),
						ERROR_FRAGMENT);
				return true;
			}
		}
		// meh. didn't find anything
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyNo1stPerson(CoreMap sentence) {
		// no 1st person descriptions (because of missing verb)
		if (ErrorRule.is1stPerson(sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class))) {
			error(IvanErrorType.STYLE,
					ERROR_SENTENCES_WITH_I,
					sentence);
			return true;
		}
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyCCVerbs(CoreMap sentence) {
		// conjoined verbs are not proper use and lead to parameter errors
		if (hasCC(getMainVerb(sentence), sentence)) {
			error(IvanErrorType.GRAPH,
					ERROR_TWO_VERBS_ONE_ENTITY,
					sentence);
			return true;
		}
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyNeedsOneVerb(CoreMap sentence) {
		// checking verbs. each sentence needs at least one
		if ((getMainVerb(sentence) == null)) {
			error(IvanErrorType.GRAPH, String.format(ERROR_THIS_SENTENCE_NEEDS_A_VERB, sentence.toString()), sentence);
			return true;
		}
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyNeedsGraph(CoreMap sentence) {
		// checking the graph. if the sentence does not have a graph, we can't
		// process it
		if (sentence.get(CollapsedCCProcessedDependenciesAnnotation.class)
				.isEmpty()) {
			error(IvanErrorType.GRAPH,
					ERROR_THIS_SENTENCE_IS_INTELLIGIBLE,
					sentence);
			return true;
		}
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyRoots(CoreMap sentence) {
		Collection<IndexedWord> roots = sentence.get(
				BasicDependenciesAnnotation.class).getRoots();
		// if the sentence has no root, it's an error
		if (roots.size() == 0) {
			error(IvanErrorType.GRAPH, String.format(ERROR_THIS_IS_NOT_A_PROPER_SENTENCE, sentence.toString()),
					sentence);
			return true;
		}
		// if the sentence has more than one roots, we're probably missing out
		else if (roots.size() > 1) {
			error(IvanErrorType.STYLE,
					ERROR_MORE_THAN_A_SINGLE_TOPIC,
					sentence);
			return true;
		}
		return false;
	}

	private static boolean hasCC(IndexedWord mainVerb, CoreMap sentence) {
		SemanticGraph graph = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		return mainVerb == null ? false : graph.hasChildWithReln(mainVerb,
				CONJUNCT);
	}

	private static IndexedWord getMainVerb(CoreMap sentence) {
		SemanticGraph graph = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if (BaseRule.isPOSFamily(root, "VB")) {
			return root;
		}
		// root is not a verb. look for a cop near root^
		Collection<GrammaticalRelation> rels = Arrays
				.asList(new GrammaticalRelation[] { COPULA, PREDICATE });
		List<IndexedWord> verbcandidates = graph.getChildrenWithRelns(root,
				rels);
		for (IndexedWord w : verbcandidates) {
			return w;
		}
		return null;
	}

	private void error(IvanErrorType type, String message, CoreMap sentence) {
		msg = new IvanErrorMessage(
				type,
				sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
				sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class),
				message);
	}

	public IvanErrorMessage getErrorMessage() {
		return msg;
	}

	/**
	 * Decides whether this sentence contains the subject "I"
	 * 
	 * @param graph
	 * @return
	 */
	public static Boolean is1stPerson(final SemanticGraph graph) {
		IndexedWord root = graph.getFirstRoot();
		// first person = nominal subject is "I"
		GrammaticalRelation subjclass = GrammaticalRelation
				.getRelation(NominalSubjectGRAnnotation.class);
		IndexedWord subject = graph.getChildWithReln(root, subjclass);
		return subject != null && subject.word().equalsIgnoreCase("I");
	}

}
