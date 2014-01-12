/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 *
 */
public class ErrorRule implements ISentenceRule, IErrorRule
{

	IvanErrorMessage msg;
	
	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		// check for a graph
		if(applyNeedsGraph(sentence))
			return true;		
		// checking roots:
		if(applyRoots(sentence))
			return true;
		
		if(applyNeedsOneVerb(sentence))
			return true;
		
		// TODO: sentences with the "fragment" tree annotation are not desirable
		// also todo: find out what that fragment thing looks like
		
		// verbs that are joined by a conjunction are probably missing arguments and cannot be properly parsed with dependencies
		if(applyCCVerbs(sentence))
			return true;
		
		// first person sentences do not have a verb that reflects the action on the scene
		if(applyNo1stPerson(sentence))
			return true;
		
		// everything seems to be fine.
		return false;
	}

	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyNo1stPerson(CoreMap sentence) {
		// no 1st person descriptions (because of missing verb)
		if(ErrorRule.is1stPerson(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class)))
		{
			error(IvanErrorType.STYLE, "Sentences with \"I\" are difficult to understand. "
					+ "Please try not to use \"I\" in a sentence.", sentence);
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
		if(hasCC(getMainVerb(sentence), sentence))
		{
			error(IvanErrorType.GRAPH, "The verbs in this sentence both refer to the same thing."
					+ " Instead, please use one verb per sentence."
					+ " Otherwise we'll probably get it wrong.", sentence);
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
		if((getMainVerb(sentence) == null))
		{
			error(IvanErrorType.GRAPH, "This sentence needs a verb.", sentence);
			return true;
		}
		return false;
	}
	
	/**
	 * @param sentence
	 * @return
	 */
	public boolean applyNeedsGraph(CoreMap sentence) {
		// checking the graph. if the sentence does not have a graph, we can't process it
		if(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class).isEmpty())
		{
			error(IvanErrorType.GRAPH, "This sentence is intelligible. It probably needs to be longer.", sentence);
			return true;
		}
		return false;
	}

	/**
	 * @param sentence
	 * @return 
	 */
	public boolean applyRoots(CoreMap sentence) {
		Collection<IndexedWord> roots = sentence.get(BasicDependenciesAnnotation.class).getRoots();
		// if the sentence has no root, it's an error
		if(roots.size() == 0)
		{
			error(IvanErrorType.GRAPH, "This is not a proper sentence.", sentence);
			return true;
		}
		// if the sentence has more than one roots, we're probably missing out
		else if(roots.size() > 1)
		{
			error(IvanErrorType.STYLE, "IVAN cannot handle sentences with more than a single topic."
					+ " Try splitting the sentence into two sentences.", sentence);
			return true;
		}
		return false;
	}

	private static boolean hasCC(IndexedWord mainVerb, CoreMap sentence) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		return mainVerb == null ? false :graph.hasChildWithReln(mainVerb, CONJUNCT);
	}

	private static IndexedWord getMainVerb(CoreMap sentence) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		if(BaseRule.isPOSFamily(root, "VB"))
		{
			return root;
		}
		// root is not a verb. look for a cop near root^
		Collection<GrammaticalRelation> rels = Arrays.asList(new GrammaticalRelation[]{
				COPULA,
				PREDICATE});
		List<IndexedWord> verbcandidates = graph.getChildrenWithRelns(root, rels);
		for (IndexedWord w : verbcandidates) {
			return w;
		}
		return null;
	}

	private void error(String message, CoreMap sentence) {
		msg = new IvanErrorMessage(				
				sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), 
				sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class),
				message);
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

	/** Decides whether this sentence contains the subject "I"
	 * @param graph
	 * @return
	 */
	public static Boolean is1stPerson(final SemanticGraph graph)
	{
		IndexedWord root = graph.getFirstRoot();
		// first person = nominal subject is "I"
		GrammaticalRelation subjclass = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		IndexedWord subject = graph.getChildWithReln(root, subjclass);
		return subject == null || subject.word().equalsIgnoreCase("I");
	}

}
