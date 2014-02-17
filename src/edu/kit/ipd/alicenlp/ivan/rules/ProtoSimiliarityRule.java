package edu.kit.ipd.alicenlp.ivan.rules;

import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 * This rule classifies verbs based on similarity to prototypical verbs.
 * 
 * @author Jonny
 * 
 */
public class ProtoSimiliarityRule implements ISentenceRule {
	
	private Classification result;

	@Override
	public boolean apply(CoreMap Sentence) {
		RelatednessCalculator comparer = new Lesk(db);
		
		SemanticGraph deps = Sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = deps.getFirstRoot();
		if (!BaseRule.isPOSFamily(root, "VB")) {
			return false;
		}
		
		String lemma = root.lemma();
		Concept co = new Concept(lemma + "#v#1");
		
		Concept stand = new Concept("stand#v#1");
		Concept jump = new Concept("jump#v#1");
		
		Relatedness setupScore = comparer.calcRelatednessOfSynset(co, stand);		
		Relatedness actionScore = comparer.calcRelatednessOfSynset(co, jump);

		if(setupScore.getScore() > actionScore.getScore())
		{
			result = Classification.SetupDescription;
		}
		else {
			result = Classification.ActionDescription;
		}
		
		return true;
	}

	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

	private static void compare(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		for (RelatednessCalculator rc : rcs) {
			double s = rc.calcRelatednessOfWords(word1, word2);
			System.out.println(rc.getClass().getName() + "\t" + s);
		}
	}

	/**
	 * @return the result
	 */
	public Classification getResult() {
		return result;
	}

}
