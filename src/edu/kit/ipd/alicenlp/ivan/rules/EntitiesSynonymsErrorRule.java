package edu.kit.ipd.alicenlp.ivan.rules;

import static edu.stanford.nlp.util.logging.Redwood.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.ErrorMessageAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanError;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

public class EntitiesSynonymsErrorRule implements IDocumentRule, IErrorRule
{
	ErrorMessageAnnotation msg;

	private InitialState state;
	private Dictionary mydictionary;
	private Map<Synset, String> mappings = new HashMap<Synset, String>();
	private EntityInfo firstOffendingEntityInfo;
	private EntityInfo secondOffendingEntityInfo;

	/** Creates a new Error checking rule
	 * 
	 * @param stateToCheck the internal state of the entity recognition which should be verified 
	 */
	public EntitiesSynonymsErrorRule(InitialState stateToCheck) {
		state = stateToCheck;
		setupWordNet();
	}
	
	@Override
	public boolean apply(Annotation doc, boolean canWrite) throws JWNLException {
		for (EntityInfo info : state) {
			// doesn't make sense
			if(info.getEntity() == null)
				continue;
			
			// check this word's usage. if it's okay, save it to the mappings
			if(isOkay(info.getEntity()))
			{
				saveInfo(info.getEntity());
			}
			else {
				createError(doc);
				if(canWrite)
					rewriteSentenceTag(doc);
				return true;
			}
		}
		return false;
	}

	/** This method attempts to find the sentence where the offending entity
	 * was defined and reclassify it as an error.
	 * @param doc
	 */
	private void rewriteSentenceTag(Annotation doc) {
		/* here's the plan:
		 * 1. find sentence
		 * 2. put our error message in
		 * 3. re-tag sentence as ERROR
		 */
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			// find the right sentence 
			if(sentence.get(CharacterOffsetEndAnnotation.class) >= msg.getSpan().end())
			{
				if(sentence.get(CharacterOffsetBeginAnnotation.class) <= msg.getSpan().start())
				{
					// we are here. now put the error in the sentence annotation
					sentence.set(ErrorMessageAnnotation.class, msg);
					// also, reclassify the sentence as ERROR
					Classification previousclass = sentence.set(Classification.class, Classification.ErrorDescription);
					log(Redwood.DBG, "Sentence tagged as ERROR. Previous tag: " + previousclass);
					
				}
			}
		}
	}

	private void createError(Annotation doc) {
		/* there's a bad and a good entity. the bad one is the 
		 * one which is introduced later in the text
		 */
		EntityInfo badone;
		EntityInfo other;
		if(firstOffendingEntityInfo.getEntitySpan().isAfter(secondOffendingEntityInfo.getEntitySpan()))
		{
			badone = firstOffendingEntityInfo;
			other = secondOffendingEntityInfo;
		}
		else {
			badone = secondOffendingEntityInfo;
			other = firstOffendingEntityInfo;
		}
		Span errorspan = badone.getEntitySpan();
		msg = new ErrorMessageAnnotation(
				IvanError.SYNONYMS,
				doc.get(DocIDAnnotation.class), 
				errorspan,
				"\""+ badone +"\" is a synonym of a previously used name \"" + other + "\"");
	}

	private void saveInfo(String entity) throws JWNLException {
		// create mappings from this entity
		IndexWord meanings = mydictionary.lookupIndexWord(POS.NOUN, entity);
		for(Synset syn : meanings.getSenses())
		{
			mappings.put(syn, entity);
		}
	}

	/** For a single entity, this method checks whether the usage is permissive.
	 * That means, if there are synsets mapped to this entity, the lemma must be identical.
	 * @param entity
	 * @return Entity may be used in this way.
	 * @throws JWNLException
	 */
	private boolean isOkay(String entity) throws JWNLException {
		IndexWord meanings = mydictionary.lookupIndexWord(POS.NOUN, entity);
		// check the mappings for all synsets
		for (Synset syn : meanings.getSenses()) {
			// get the lemmas 
			// lemma may be NULL if we encounter it the first time
			String lemma = mappings.get(syn);
			// if we found something that is different from what we already have, this is going to cause problems and is not okay
			if(lemma != null 
					&& !entity.equalsIgnoreCase(lemma))
			{
				firstOffendingEntityInfo = state.getSingle(lemma);
				secondOffendingEntityInfo = state.getSingle(entity);
				return false;
			}
			// if this is the same as the entity, everything is fine
		}
		return true;
	}

	@Override
	public ErrorMessageAnnotation getErrorMessage() {
		return msg;
	}

	@Override
	public boolean apply(Annotation doc) throws JWNLException {
		return apply(doc, false);
	}

	/**
	 * 
	 */
	protected void setupWordNet() {
		// set up properties file
	    String propsFile = "file_properties.xml";
	    FileInputStream properties = null;
	    try {
	    	properties = new FileInputStream(propsFile);
	    } catch (FileNotFoundException e1) {
	    	e1.printStackTrace();
	    }
	    
	    // create a dictionary and run the analytics
	    try {
	    	
	    	// run
	    	if (mydictionary == null) {
				//new style, instance dictionary
				mydictionary = Dictionary.getInstance(properties);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
	}
}
