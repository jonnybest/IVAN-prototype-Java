/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.ClausalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalPassiveSubjectGRAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.NominalSubjectGRAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This is a utility class which provides static methods to rules for easier checking.
 * @author Jonny
 *
 */
public abstract class BaseRule {
	
	/**
	 * @param graph
	 * @param startingWord
	 * @return 
	 */
	protected static List<IndexedWord> getPrepinRelations(IndexedWord startingWord, SemanticGraph graph) {
		return getPrepRelations(startingWord, graph, "in");
	}
	
	protected static boolean hasDeterminer(IndexedWord startingWord, SemanticGraph graph)
	{
		IndexedWord det = getDeterminer(startingWord, graph);
		return det != null;
	}

	protected static Boolean is1stPerson(IndexedWord root, SemanticGraph graph)
	{
		// not actually always first person, but for our corpus, it's good enough 
//		if ("VBP".equalsIgnoreCase(root.get(CoreAnnotations.PartOfSpeechAnnotation.class))) {
//			return true;
//		}
		GrammaticalRelation subjclass = GrammaticalRelation.getRelation(NominalSubjectGRAnnotation.class);
		IndexedWord subject = graph.getChildWithReln(root, subjclass);
		return subject == null || subject.word().equalsIgnoreCase("I");
	}
	
	protected static IndexedWord getParticle(IndexedWord word, SemanticGraph graph)
	{
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	protected static boolean hasAdverbMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.AdverbialModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}

	protected static boolean hasDirectObjectNP(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		if (graph.hasChildWithReln(word, reln)) {
			String pos = graph.getChildWithReln(word, reln).get(PartOfSpeechAnnotation.class);
			if (pos.equalsIgnoreCase("NN")) {
				return true;
			}
		}
		return false;
	}

	protected static Boolean hasParticle(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}
	
    protected static boolean hasPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}    

	protected static IndexedWord getDeterminer(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	protected static IndexedWord getDirectObject(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}

	/**
	 * Finds any prepositions relating to {@code word}. Requires a non-collapsed graph.
	 * @param word The word which is being modified
	 * @param graph A basic graph (non-collapsed) 
	 * @return
	 */
	protected static CoreLabel getPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	protected static IndexedWord getAdvMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AdverbialModifierGRAnnotation.class);
		IndexedWord advmod = graph.getChildWithReln(word, reln);
		if (advmod == null) {
			GrammaticalRelation reln2 = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AdvClauseModifierGRAnnotation.class);
			return graph.getChildWithReln(word, reln2);
		}
		else {
			return advmod;			
		}
	}
	
	protected static boolean isPassive(IndexedWord root, SemanticGraph graph) {
		// Examples: 
		// “Dole was defeated by Clinton” nsubjpass(defeated, Dole)
		GrammaticalRelation nsubjpass = GrammaticalRelation.getRelation(NominalPassiveSubjectGRAnnotation.class);
		// “That she lied was suspected by everyone” csubjpass(suspected, lied)
		GrammaticalRelation csubjpass = GrammaticalRelation.getRelation(ClausalPassiveSubjectGRAnnotation.class);
		// “Kennedy was killed” auxpass(killed, was)
		GrammaticalRelation auxrel = GrammaticalRelation.getRelation(EnglishGrammaticalRelations.AuxPassiveGRAnnotation.class);
		Boolean passive = false;
		passive = passive || graph.hasChildWithReln(root, nsubjpass);
		passive = passive || graph.hasChildWithReln(root, csubjpass);
		passive = passive || graph.hasChildWithReln(root, auxrel);
		return passive;
	}

	protected static List<IndexedWord> getPrepRelations(IndexedWord startingWord, SemanticGraph graph, String preposition) {
		//GrammaticalRelation prepreln = GrammaticalRelation.getRelation(PrepositionalModifierGRAnnotation.class);
		GrammaticalRelation prepreln = EnglishGrammaticalRelations.getPrep(preposition);		
		// checking rule: root->prep_in->det
		return graph.getChildrenWithReln(startingWord, prepreln);
	}
	
	/**
	 * Returns the whole noun phrase for words with a determiner. If there is no determiner present, only the word itself is returned.
	 * @param word
	 * @param sentence
	 * @return
	 */
	protected static String getNounPhrase(IndexedWord word, CoreMap sentence){
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord det = getDeterminer(word, graph);
		int offset = sentence.get(CharacterOffsetBeginAnnotation.class);
		if(det != null)
		{
			int start = det.beginPosition() - offset;
			int end = word.endPosition() - offset;
			return sentence.get(TextAnnotation.class).substring(start, end);
		}
		else {
			return word.originalText();			
		}
	}

	/**
	 * Returns the primary thing this sentence is talking about. 
	 * More precisely: Returns any subject or a passive subject of the sentence, or the root if none applies.
	 * @param graph
	 * @return
	 */
	public static IndexedWord getSubject(SemanticGraph graph) {
		if (graph.isEmpty()) {
			return null;
		}
		GrammaticalRelation[] subjects = { 
				EnglishGrammaticalRelations.NOMINAL_SUBJECT,
				EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT
		};		
		IndexedWord firstRoot = graph.getFirstRoot();
		List<IndexedWord> children = graph.getChildrenWithRelns(firstRoot, Arrays.asList(subjects));
		if (children != null && children.size() > 0) {
			assert children.size() == 1; // not really dangerous. But we need to change our implementation to return a list, if there are more than one subject.
			
			return children.get(0);
		}
		// return null;
		return graph.getFirstRoot(); // in a subject-less sentence, the root is as good as the subject
	}

	/**
	 * Prints the part of the sentence which contains the {@code startingWord}
	 * all the verteces below it. TODO: implement real DFS to find the bounds.
	 * 
	 * @param startingWord
	 * @param graph
	 * @return
	 */
	protected String printSubGraph(IndexedWord startingWord, CoreMap sentence) {
		// get me a graph
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		// get the edges which point outwards from the starting word
		Iterable<SemanticGraphEdge> outiter = graph.outgoingEdgeIterable(startingWord);

		// get the starting offset for this sentence
		int offset = sentence.get(CharacterOffsetBeginAnnotation.class);
				
		// set the default bounds to the startingWord 
		int start = startingWord.beginPosition() - offset;
		int end = startingWord.endPosition() - offset;
		
		// search the next level for larger bounds
		// assume that everything in between the bounds belongs to the sub-graph of the startingWord
		for (SemanticGraphEdge edge : outiter) {
//			System.out.println("out:" + edge.toString());
			start = Math.min(start, edge.getGovernor().beginPosition() - offset);
			start = Math.min(start, edge.getDependent().beginPosition() - offset);
			end = Math.max(end, edge.getGovernor().endPosition() - offset);
			end = Math.max(end, edge.getDependent().endPosition() - offset);
		}

		// FIXME: bounds are wrong if the input has more than one sentence.
		//System.out.println(graph);
		System.out.println(sentence.get(TextAnnotation.class).substring(start, end));
		return sentence.get(TextAnnotation.class).substring(start, end);
	}
	

	/** This method attempts to resolve noun phrases which consist of more than one word.
	 * More precisely, it looks for nn dependencies below {@code head} and creates an entity.
	 * @param head The head of the noun phrase
	 * @param graph The sentence to look in.
	 * @return A distinct word
	 */
	public static String resolveNN(IndexedWord head, SemanticGraph graph) {
		List<IndexedWord> nns = graph.getChildrenWithReln(head, EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER);
		String name = "";
		// check for nulls. if there is nothing here, we have nothing to do.
		if (nns != null) {
			for (IndexedWord part : nns) {
				name += part.word();
				name += " ";
			}
			name += head.word();
			return name;
		}
		else {
			return null;
		}
	}
	
	/** This method attempts to resolve noun phrases and conjunction. 
	 * More precisely, it looks for nn and con_and dependencies below {@code head} and creates a list of entities.
	 * @param head The head of the noun phrase
	 * @param graph The sentence to look in.
	 * @return A list of distinct words or names, grouped by "and"
	 */
	public static ArrayList<String> resolveCc(IndexedWord head,
			SemanticGraph graph) {
		// list of names
		ArrayList<String> names = new ArrayList<String>();
		// adding this subject
		names.add(resolveNN(head, graph));
		// check for more!
		// more names can be reached with "and". Get us an "and":
		GrammaticalRelation andrel = EnglishGrammaticalRelations.getConj("and");
		// ask the graph for everything that is connected by "and":
		List<IndexedWord> ands = graph.getChildrenWithReln(head, andrel);
		for (IndexedWord w : ands) {
			// add 'em
			names.add(resolveNN(w, graph));
		}
		// hope those are all
		return names;
	}

}
