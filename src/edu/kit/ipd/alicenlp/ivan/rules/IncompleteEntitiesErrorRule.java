package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.stanford.nlp.util.logging.Redwood.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;

/** This class checks the user's names and entites for consistency. It implements this rule:
 * 1. no entity may be a synonym of another entity
 * 2. unless both have names
 * 3. and the names do not collide
 * 
 * @author Jonny
 *
 */
public class IncompleteEntitiesErrorRule implements IDocumentRule
{
	ArrayList<IvanErrorMessage> msg;
	
	private InitialState state;

	/** Creates a new Error checking rule
	 * 
	 * @param stateToCheck the internal state of the entity recognition which should be verified 
	 */
	public IncompleteEntitiesErrorRule(InitialState stateToCheck) {
		state = stateToCheck;

	}
	
	/** @param doc 
	 * @param canWrite If true, this rule may extend exising document-wide errors
	 * 
	 */
	@Override
	public boolean apply(Annotation doc, boolean canWrite) throws JWNLException {
		// does not apply if no state is present
		if(state == null)
			return false;
		
		msg = new ArrayList<IvanErrorMessage>();

		// get the list of unique entities
		for (Pair<String, String> uniqueMapping : state.getEntityNames()) {
			// 
			String alias = uniqueMapping.second;
			// 
			String entity = uniqueMapping.first;

			// is properly initialized (pertaining to has-both-name-and-entity)
			boolean initPresent = state.hasEntity(alias);
			// has a direction?
			boolean dirPresent = false;
			// has a location?
			boolean locPresent = false;
			// first mentioned at
			Span errorspan = state.getSingle(alias).getEntitySpan();
			// check infos
			for (EntityInfo ei : state.get(alias)) {
				dirPresent = dirPresent | ei.hasDirection();
				locPresent = locPresent | ei.hasLocation();
			}
			
			// entity is missing a description of what exactly it is
			if(!initPresent)
			{
				IvanErrorMessage err = new IvanErrorMessage(IvanErrorType.UNKNOWN, errorspan, String.format("What or who %s? It's missing a description.", alias));
				msg.add(err);
			}
			// 
			final boolean hasName = state.hasName(entity);
			// entity is missing a location
			if(!locPresent)
			{
				IvanErrorMessage err = new IvanErrorMessage(IvanErrorType.LOCATION, errorspan, String.format("%s needs a position.", hasName ? alias : "The " + entity));
				msg.add(err);
			}
			// entity is missing a direction
			if(!dirPresent)
			{
				IvanErrorMessage err = new IvanErrorMessage(IvanErrorType.DIRECTION, errorspan, String.format("%s needs a direction.", hasName ? alias : "The " + entity));
				msg.add(err);
			}
		}

		// if there are any saved errors, we were successful
		if(msg.size() > 0)
		{
			if(canWrite)
			{
				List<IvanErrorMessage> list = doc.get(IvanAnnotations.DocumentErrorAnnotation.class);
				if(list == null)
				{
					list = msg;
					return true;
				}
				for (IvanErrorMessage ivanErrorMessage : msg) {
					if(!list.contains(ivanErrorMessage))
					{
						list.add(ivanErrorMessage);
					}
				}
			}
			return true;
		}
		else {
			// if there are no errors, we delete the empty list and return false
			msg = null;
			return false;
		}
	}

	@Override
	public boolean apply(Annotation doc) throws JWNLException {
		return apply(doc, false);
	}

	/** A list of issues with this documents' entities
	 * 
	 * @return A list (or NULL, if this rule did not apply)
	 */
	public List<IvanErrorMessage> getErrorMessages() {
		return msg;
	}
}
