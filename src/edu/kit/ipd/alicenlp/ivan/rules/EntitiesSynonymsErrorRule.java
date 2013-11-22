package edu.kit.ipd.alicenlp.ivan.rules;

import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.stanford.nlp.pipeline.Annotation;

public class EntitiesSynonymsErrorRule implements IDocumentRule, IErrorRule
{
	private InitialState state;
	ErrorMessageAnnotation msg;

	/** Creates a new Error checking rule
	 * 
	 * @param stateToCheck the internal state of the entity recognition which should be verified 
	 */
	public EntitiesSynonymsErrorRule(InitialState stateToCheck) {
		state = stateToCheck;
	}
	
	@Override
	public boolean apply(Annotation doc, boolean canWrite) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ErrorMessageAnnotation getErrorMessage() {
		return msg;
	}

	@Override
	public boolean apply(Annotation doc) {
		return apply(doc, false);
	}

	
}
