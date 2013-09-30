/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.omg.CORBA.Request;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
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
		if(det != null)
		{
			int start = det.beginPosition();
			int end = word.endPosition();
			return sentence.get(TextAnnotation.class).substring(start, end);
		}
		else {
			return word.originalText();			
		}
	}

	/**
	 * Returns any subject or a passive subject of the sentence
	 * @param graph
	 * @return
	 */
	protected IndexedWord getSubject(SemanticGraph graph) {
		GrammaticalRelation[] subjects = { 
				EnglishGrammaticalRelations.NOMINAL_SUBJECT,
				EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_SUBJECT,
				EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT
		};		
		IndexedWord firstRoot = graph.getFirstRoot();
		if (firstRoot == null) {
			return null;
		}
		List<IndexedWord> children = graph.getChildrenWithRelns(firstRoot, Arrays.asList(subjects));
		if (children != null && children.size() > 0) {
			return children.get(0);
		}
		return null;
	}

	/**
	 * Prints the part of the sentence which contains the {@code startingWord} all the verteces below it.
	 * TODO: implement real DFS to find the bounds. 
	 * @param startingWord
	 * @param graph
	 * @return
	 */
	protected String printSubGraph(IndexedWord startingWord, SemanticGraph graph) {
			Iterable<SemanticGraphEdge> outiter = graph.outgoingEdgeIterable(startingWord);
	
			int start = startingWord.beginPosition(), end = startingWord.endPosition();
			for (SemanticGraphEdge edge : outiter) {
	//			System.out.println("out:" + edge.toString());
	//			System.out.println("gov: " + edge.getGovernor());
	//			System.out.println("dep: " + edge.getDependent());
				start = Math.min(start, edge.getGovernor().beginPosition());
				end = Math.max(end, edge.getDependent().endPosition());			
			}
			
			return graph.toRecoveredSentenceString().substring(start, end);
		}
}
