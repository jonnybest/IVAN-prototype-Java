/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdesktop.swingx.search.ListSearchable;

import opennlp.tools.cmdline.coref.CoreferencerTool;
import net.sf.extjwnl.JWNLException;
import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader.CorefMentionAnnotation;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCluster;
import edu.stanford.nlp.dcoref.Dictionaries.Animacy;
import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.dcoref.Dictionaries.Number;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.IntTuple;

/**
 * This rule implements a hearst pattern for assigning an alias to an entity.
 * Specifically, it implements "The NPx is called NPy" and "the NPx named NPy".
 * 
 * @author Jonny
 * 
 */
public class AliasHearstRule implements ISentenceRule {

	private CorefMention representative;
	private Map<IntPair, Set<CorefMention>> mentionMap = new HashMap<IntPair, Set<CorefMention>>();
	private CorefChain mychain;
	private ArrayList<CorefMention> mentionlist = new ArrayList<>();

	/**
	 * Extracts
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap Sentence) throws JWNLException {
		
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = graph.getFirstRoot();
		
		String[] validheads = {"called", "named"};

		// does the validheads contain root?
		int result = Arrays.binarySearch(validheads, root.originalText());		
		if(result < 0)
		{
			// head is not valid for this rule
			return false;
		}
		
		// does the pattern fit?
		// must be passive
		if(!root.tag().equals("VBN"))
		{
			return false;
		}
		
		IndexedWord entityHead = BaseRule.getSubject(graph);
		if(entityHead == null)
			return false;		
		ArrayList<IndexedWord> entitywords = new ArrayList<>();
		String entityString = BaseRule.resolveNN(entityHead, graph, entitywords);
		
		int sentencenumber = Sentence.get(SentenceIndexAnnotation.class);
		IntPair entitypos = new IntPair(sentencenumber, 1);
		
		CorefMention entitymention = buildMention(MentionType.NOMINAL, 
				entitywords.get(0).get(BeginIndexAnnotation.class), 
				entityHead.get(EndIndexAnnotation.class), 
				entitypos, entityString);
		HashSet<CorefMention> entityset = new HashSet<>();
		entityset.add(entitymention);
		mentionMap.put(entitypos, entityset);
		
		IndexedWord aliasHead = BaseRule.getDirectObject(root, graph);
		if(aliasHead == null)
			return false;		
		ArrayList<IndexedWord> aliaswords = new ArrayList<>();
		String aliasString = BaseRule.resolveNN(aliasHead, graph, aliaswords);
		
		IntPair aliaspos = new IntPair(sentencenumber, 2);
		
		CorefMention aliasmention = buildMention(MentionType.PROPER, 
				aliaswords.get(0).get(BeginIndexAnnotation.class), 
				aliasHead.get(EndIndexAnnotation.class), 
				aliaspos, aliasString);
		HashSet<CorefMention> aliasset = new HashSet<>();
		aliasset.add(aliasmention);
		mentionMap.put(aliaspos, aliasset);
		
		representative = aliasmention;
		
		buildChain();
		buildList();
		
		return true;
	}

	void buildChain()
	{
		mychain = new CorefChain(0, mentionMap, representative);
	}
	
	CorefMention buildMention(MentionType type, int startIndex, int endIndex,
			IntTuple position, String mentionSpan) {
		
		return new CorefMention(type, Number.SINGULAR, Gender.UNKNOWN,
				Animacy.ANIMATE, startIndex, endIndex, 0, 0, 0, position.get(0),
				position, mentionSpan);
	}

	/** If we found anything, this method provides the results. 
	 * 
	 * @return
	 */
	public List<CorefMention> getMentions() {		
		return mentionlist;
	}

	/**
	 * 
	 */
	public void buildList() {
		CorefChain chain = mychain;
		for (CorefMention m : chain.getMentionsInTextualOrder()) {				
			if(m.mentionType == MentionType.PROPER)
			{
				mentionlist.add(m);
				break;
			}
		}
	}
}
