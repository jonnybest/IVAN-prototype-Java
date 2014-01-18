/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

import net.sf.extjwnl.JWNLException;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.Pair;

/**
 * This rule reads entity information and -- for each entity -- declares the
 * first declaration, the first location and the first direction as SETUP
 * description. Results are returned as a list of sentences indexes, suitable
 * for retrieving a sentence by means of SentencesAnnotation. (That means
 * 0-based index.)
 * 
 * @author Jonny
 * 
 */
public class FirstMentionRule implements IDocumentRule {

	private List<Integer> results = new ArrayList<>();

	/**
	 * Applies rule and tags applicable sentences. This will overwrite any
	 * previous tag information.
	 * 
	 * @param doc
	 *            document containing entityinformation
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation)
	 */
	@Override
	public boolean apply(Annotation doc) throws JWNLException {
		return apply(doc, true);
	}

	/**
	 * Applies rule and optionally tags sentences.
	 * 
	 * @param doc
	 *            document containing entity information -- (
	 *            {@link IvanEntitiesAnnotation})
	 * @param canWrite
	 *            TRUE overwrites any tags with the new tag information found by
	 *            this rule. FALSE preserves tags.
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation,
	 *      boolean)
	 */
	@Override
	public boolean apply(Annotation doc, boolean canWrite) throws JWNLException {
		
		InitialState state = doc
				.get(IvanAnnotations.IvanEntitiesAnnotation.class);
		if (state == null)
			return false; // shortcut

		for (Pair<String, String> thing : state.getEntityNames()) {
			String handle = thing.first == null ? thing.second : thing.first;
			ArrayList<EntityInfo> infos = state.get(handle);
			// find first mention
			// find first location
			// find first direction
			
		}

		results = null;
		return false;
	}

	public Integer[] getResults() {
		return results != null ? results.toArray(new Integer[]{}) : null;
	}

}
