/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.extjwnl.JWNLException;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * @author Jonny
 *
 */
public class AliasByCorefRule implements IDocumentRule {

	private ArrayList<CorefMention> aliasmentions = new ArrayList<>();
	private HashMap<CorefMention, CorefMention> entitymap = new HashMap<>();

	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation)
	 */
	@Override
	public boolean apply(Annotation doc) throws JWNLException {
		
		Map<Integer, CorefChain> map = doc.get(CorefChainAnnotation.class);
		if(map == null)
			return false; // can't work with that
		
		for(Entry<Integer, CorefChain> thing : map.entrySet())
		{
			CorefChain chain = thing.getValue();
			if(chain == null)
				continue;
			
			for (CorefMention alias : chain.getMentionsInTextualOrder()) {
				if(alias.mentionType == MentionType.PROPER) // found an alias
				{
					// add the alias
					aliasmentions.add(alias);
					// search for a nominal mention
					// we simply take the first nominal mention that pops up
					for (CorefMention entity : chain.getMentionsInTextualOrder()) {
						if(entity.mentionType == MentionType.NOMINAL) // found an non-alias mention
						{
							// map the alias onto this nominal mention
							entitymap.put(alias, entity);
						}
					}
					// go to next coref chain chain
					break;
				}
			}
		}
		
		if(aliasmentions.size() > 0)
		{
			return true;
		}
		else {
			aliasmentions = null;
			return false;
		}
	}

	/* Do not use.
	 */
	@Override
	public boolean apply(Annotation doc, boolean canWrite) throws JWNLException {
		return apply(doc);
	}

	/** If the rule found anything, this method returns the mentions which contain aliases
	 * @return A list of aliases
	 * 
	 */
	public List<CorefMention> getAliasMentions() {
		return aliasmentions;
	}

	/** If the rule found anything, this method retrieves the first nominal mention for the given alias.
	 * @param alias The name to look for
	 * @return A nominal mention which has a reference to the given name
	 */
	public CorefMention getEntity(CorefMention alias)
	{
		return entitymap.get(alias);
	}
}
