/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Jonny
 *
 */
public class TimeRule implements ISentenceRule {

	/**
	 * A sentence is a time sentence, if it has a "duration" in it or
	 * it explicitly references "passing time".
	 */
	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap sentence) {
		// check for annotations first. this is pretty simple
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String duration = token.get(NamedEntityTagAnnotation.class);
			if("DURATION".equals(duration))
			{
				return true;
			}
		}
		
		// check for reference to passing time then
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		// check root: cannot be null
		if(root == null)
		{
			// no luck
			return false;
		}
		// check root: it may be "time". in that case, Stanford chose the topic for us.
		if("time".equalsIgnoreCase(root.lemma()))
			return true;
		// check root. needs to be a form of "to pass". (may be improved with WordNet)
		if(!"pass".equalsIgnoreCase(root.lemma()))
			return false;
		// only "time" is a valid referent
		IndexedWord subject = graph.getChildWithReln(root, EnglishGrammaticalRelations.NOMINAL_SUBJECT);
		if(subject.lemma().equalsIgnoreCase("time"))
		{
			// alright! this sentence talks about passing time.
			return true;
		}
		
		// nothing to do here
		return false;
	}

}
