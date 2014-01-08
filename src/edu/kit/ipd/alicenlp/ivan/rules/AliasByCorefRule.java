/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	private ArrayList<CorefMention> mentions = new ArrayList<>();

	/* (non-Javadoc)
	 * @see edu.kit.ipd.alicenlp.ivan.rules.IDocumentRule#apply(edu.stanford.nlp.pipeline.Annotation)
	 */
	@Override
	public boolean apply(Annotation doc) throws JWNLException {
		
		Map<Integer, CorefChain> map = doc.get(CorefChainAnnotation.class);
		
		for(int i = 1; i <= map.size(); i++)
		{
			CorefChain chain = map.get(i);
			
			for (CorefMention m : chain.getMentionsInTextualOrder()) {
				if(m.mentionType == MentionType.PROPER)
				{
					mentions.add(m);
					break;
				}
			}
		}
		
		if(mentions.size() > 0)
		{
			return true;
		}
		else {
			mentions = null;
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
	public List<CorefMention> getMentions() {
		return mentions;
	}

}
