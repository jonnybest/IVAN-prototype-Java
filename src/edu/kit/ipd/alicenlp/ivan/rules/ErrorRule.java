/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.Collection;

import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
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
		boolean hasVerb = false;
		for (CoreLabel word : sentence.get(TokensAnnotation.class)) {
			if(BaseRule.isPOSFamily(word, "VB"))
			{
				hasVerb = true;
				break;
			}
		}
		if(!hasVerb)
		{
			error("This sentence needs a verb.", sentence);
			return true;
		}
		
		// TODO: sentences with the fragment annotation are not desirable
		
		// TODO: conjoined verbs are not proper use and lead to parameter errors
		
		// everything seems to be fine.
		return false;
	}

	private void error(String string, CoreMap sentence) {
		// TODO Auto-generated method stub
		
	}

	public ErrorMessageAnnotation getMessage() {
		return msg;
	}

}
