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
public class AliasHearstRule implements ISentenceRule, ICorefResultRule {

	private CorefMention representative;
	private Map<IntPair, Set<CorefMention>> mentionMap = new HashMap<IntPair, Set<CorefMention>>();
	private CorefChain mychain;
	private ArrayList<CorefMention> mentionlist = new ArrayList<>();
	/** It's a mapping between Alias -> Entity
	 * 
	 */
	private HashMap<CorefMention, CorefMention> entitymap = new HashMap<>();

	/**
	 * Extracts
	 * 
	 * @see edu.kit.ipd.alicenlp.ivan.rules.ISentenceRule#apply(edu.stanford.nlp.util.CoreMap)
	 */
	@Override
	public boolean apply(CoreMap Sentence) throws JWNLException {
		// fetch graph
		SemanticGraph graph = Sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		// fetch default root. hope there are not more
		IndexedWord root = graph.getFirstRoot();
		
		// these are keywords which must be present
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
		
		// deal with entity mentions
		// retrieve subject. this is going to be the entity description
		IndexedWord entityHead = BaseRule.getSubject(graph);
		// if there is none, we fail
		if(entityHead == null)
			return false;		
		// save words for later
		ArrayList<IndexedWord> entitywords = new ArrayList<>();
		// retrieve multi-word subject phrase
		String entityString = BaseRule.resolveNN(entityHead, graph, entitywords);
		// fetch the sentence number. this is a 1-based index for the coref data structure
		int sentencenumber /* 1-based */ = Sentence.get(SentenceIndexAnnotation.class) + 1; /* zero-based */
		// this is the position description (sentence x, first mention)
		IntPair entitypos = new IntPair(sentencenumber, 1);
		
		// create a mention from our data
		CorefMention entitymention = buildMention(MentionType.NOMINAL, // non-alias, non-pronomial mention 
				entitywords.get(0).get(IndexAnnotation.class), // first word marks beginning of result span
				entityHead.get(IndexAnnotation.class), // last word (head) marks the end
				entitypos, entityString); // save position and the whole string for easy reading
		// this is for storing mentions in the map later
		HashSet<CorefMention> entityset = new HashSet<>();
		// save the entity mention
		entityset.add(entitymention);
		// map this collection to the document-relative, 1-based position
		mentionMap.put(entitypos, entityset);
		
		// deal with aliases
		// retrieve object. this is going to be the alias description
		IndexedWord aliasHead = BaseRule.getDirectObject(root, graph);
		// if there's none, we fail
		if(aliasHead == null)
			return false;
		// save all the words
		ArrayList<IndexedWord> aliaswords = new ArrayList<>();
		// fetch all the words
		String aliasString = BaseRule.resolveNN(aliasHead, graph, aliaswords);
		// save the position for later (sentence x, second mention)
		IntPair aliaspos = new IntPair(sentencenumber, 2);
		
		// create mention data
		CorefMention aliasmention = buildMention(MentionType.PROPER, // alias mention with a proper name 
				aliaswords.get(0).get(IndexAnnotation.class), 
				aliasHead.get(IndexAnnotation.class), 
				aliaspos, aliasString);
		// save the alias in the structure neccessary for coref
		HashSet<CorefMention> aliasset = new HashSet<>();
		aliasset.add(aliasmention);
		mentionMap.put(aliaspos, aliasset);
		
		// we think that aliases are more representative
		representative = aliasmention;
		
		buildChain(); // build a chain. this is not really neccessary, but we want to be as similar to actual coreference resolution as possible
		buildList(); // this list is neccessary to satisfy the ICorefResultRule interface
		
		// add result mapping
		entitymap.put(aliasmention, entitymention);
		
		return true;
	}

	/**
	 * utility for creating a coref chain
	 */
	void buildChain()
	{
		mychain = new CorefChain(0, mentionMap, representative);
	}
	
	/** utility for creating mentions
	 * 
	 * @param type
	 * @param startIndex
	 * @param endIndex
	 * @param position
	 * @param mentionSpan
	 * @return
	 */
	CorefMention buildMention(MentionType type, int startIndex, int endIndex,
			IntTuple position, String mentionSpan) {
		
		return new CorefMention(type, Number.SINGULAR, Gender.UNKNOWN,
				Animacy.ANIMATE, startIndex, endIndex, endIndex, 0, 0, position.get(0),
				position, mentionSpan);
	}

	/** If we found anything, this method provides the results. 
	 * 
	 * @return
	 */
	public List<CorefMention> getAliasMentions() {		
		return mentionlist;
	}
	
	/**
	 * Retrieve the entity mention for a given proper mention
	 */
	@Override
	public CorefMention getEntity(CorefMention alias) {
		return entitymap.get(alias);
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
