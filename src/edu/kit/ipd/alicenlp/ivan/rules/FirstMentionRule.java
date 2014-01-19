/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.extjwnl.JWNLException;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
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
	private List<Integer> resultSpans = new ArrayList<Integer>();

	/**
	 * Applies rule and tags applicable sentences. This will overwrite any
	 * previous tag information.
	 * 
	 * @param doc
	 *            document containing entityinformation
	 * @throws IvanException
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation)
	 */
	@Override
	public boolean apply(Annotation doc) throws JWNLException, IvanException {
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
	 * @throws IvanException
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation,
	 *      boolean)
	 */
	@Override
	public boolean apply(Annotation doc, boolean canWrite)
			throws JWNLException, IvanException {

		InitialState state = doc
				.get(IvanAnnotations.IvanEntitiesAnnotation.class);
		if (state == null)
			return false; // shortcut

		for (Pair<String, String> thing : state.getEntityNames()) {
			String handle = thing.first == null ? thing.second : thing.first;
			ArrayList<EntityInfo> infos = state.get(handle);
			if (infos == null)
				throw new IvanException("State is inconsistent. Name '"
						+ handle
						+ "' was given but has no attached entity information.");
			EntityInfo earliestMention = infos.get(0);
			EntityInfo earliestLocation = null;
			EntityInfo earliestDirection = null;
			for (int i = 0; i < infos.size(); i++) {
				EntityInfo ei = infos.get(i);
				// find first mention
				if (ei != earliestMention
						&& ei.getEntitySpan().isBefore(
								earliestMention.getEntitySpan())) {
					earliestMention = ei;
				}
				// find first location
				if (ei.hasLocation()) {
					if (earliestLocation == null
							|| ei.getEntitySpan().isBefore(
									earliestLocation.getEntitySpan())) {
						earliestLocation = ei;
					}
				}
				// find first direction
				if (ei.hasDirection()) {
					if (earliestDirection == null
							|| ei.getEntitySpan().isBefore(
									earliestDirection.getEntitySpan())) {
						earliestDirection = ei;
					}
				}
			}
			// first mentions have been found
			resultSpans.add(earliestMention.getEntitySpan().start());
			if (earliestMention != earliestDirection
					&& earliestDirection != null)
				resultSpans.add(earliestDirection.getEntitySpan().start());
			if (earliestDirection != earliestLocation
					&& earliestLocation != null)
				resultSpans.add(earliestMention.getEntitySpan().start());
		}
		Collections.sort(resultSpans);
		int mi = 0; // mention index
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (int i = 0; i < sentences.size(); i++) {
			CoreMap s = sentences.get(i);
			Integer sentenceEnd = s.get(
					CoreAnnotations.CharacterOffsetEndAnnotation.class);
			boolean addme = false;
			while (mi < resultSpans.size() && resultSpans.get(mi) < sentenceEnd) {
				addme = true;
				mi++; // converge to size()
			}
			// let's see if we found anything 
			if (addme){
				// if so, let's add the result
				results.add(i);
				if (canWrite) {
					s.set(IvanAnnotations.SentenceClassificationAnnotation.class, Classification.SetupDescription);
				}
			}
		}
		if (results.size() > 0) {
			return true;
		} else {
			results = null;
			return false;
		}
	}

	public Integer[] getResults() {
		return results != null ? results.toArray(new Integer[] {}) : null;
	}

}
