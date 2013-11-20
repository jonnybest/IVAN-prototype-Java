/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 *
 */
public class ErrorRule implements ISentenceRule {

	ErrorMessageAnnotation msg;
	
	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		// checking roots:
		Collection<IndexedWord> roots = sentence.get(BasicDependenciesAnnotation.class).getRoots();
		// if the sentence has no root, it's an error
		if(roots.size() == 0)
		{
			error("This is not a proper sentence.", sentence);
			return true;
		}
		// if the sentence has more than one roots, we're probably missing out
		else if(roots.size() > 1)
		{
			error("IVAN cannot handle sentences with more than a single topic."
					+ " Try splitting the sentence into two sentences.", sentence);
			return true;
		}
		
		// checking verbs. each sentence needs at least one
		if((getMainVerb(sentence) == null))
		{
			error("This sentence needs a verb.", sentence);
			return true;
		}
		
		// TODO: sentences with the "fragment" tree annotation are not desirable
		// also todo: find out what that fragment thing looks like
		
		// conjoined verbs are not proper use and lead to parameter errors
		if(hasCC(getMainVerb(sentence), sentence))
		{
			error("The verbs in this sentence both refer to the same thing."
					+ " Instead, please use one verb per sentence."
					+ " Otherwise we'll probably get it wrong.", sentence);
			return true;
		}
		
		// no 1st person descriptions (because of missing verb)
		if(BaseRule.is1stPerson(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class)))
		{
			error("Sentences with \"I\" are difficult to understand. "
					+ "Please try not to use \"I\" in a sentence.", sentence);
			return true;
		}
		
		
		// everything seems to be fine.
		return false;
	}

	private boolean hasCC(IndexedWord mainVerb, CoreMap sentence) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		return mainVerb == null ? false :graph.hasChildWithReln(mainVerb, CONJUNCT);
	}

	private IndexedWord getMainVerb(CoreMap sentence) {
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
		msg = new ErrorMessageAnnotation(
				sentence.get(DocIDAnnotation.class), 
				sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), 
				sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class),
				message);
	}

	public ErrorMessageAnnotation getMessage() {
		return msg;
	}

}
