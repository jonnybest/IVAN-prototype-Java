package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.WordnetSynAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
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
	
	static {
		WS4JConfiguration.getInstance().setLeskNormalize(false);
		WS4JConfiguration.getInstance().setMFS(false);
		WS4JConfiguration.getInstance().setTrace(true);
		WS4JConfiguration.getInstance().setStem(false);
	}
	
	private Classification result;

	@Override
	public boolean apply(CoreMap Sentence) throws IvanException {
		RelatednessCalculator comparer = new Lesk(db); // new Lin(db); //new Path(db); // 
		
		SemanticGraph deps = Sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		IndexedWord root = deps.getFirstRoot();
		if (!BaseRule.isPOSFamily(root, "VB")) {
			return false;
		}
		
		String word = root.lemma();
		List<Synset> synsets = WordNetUtil.wordToSynsets(word, POS.v);
		Synset mysynset = synsets.get(0);		
		Concept co = new Concept(mysynset.getSynset(), POS.v, mysynset.getName(), mysynset.getSrc());
		
		Concept stand = getSynset("stand#v#1");
		Concept jump = getSynset("jump#v#1");
		
		double setupScore = compare(comparer, co, stand);		
		double actionScore = compare(comparer, co, jump);
		double score = compare(comparer, stand, jump);

		double threshold = 0.2f;
		if(Math.abs(setupScore - actionScore) < threshold){
			// this rule failed to detect any difference
			return false;
		}
		else if(setupScore > actionScore)
		{
			result = Classification.SetupDescription;
		}
		else {
			result = Classification.ActionDescription;
		}		
		
		return true;
	}

	private static double compare(RelatednessCalculator comparer, Concept one,
			Concept other) throws IvanException {
		Relatedness res = comparer.calcRelatednessOfSynset(one, other);
		if(StringUtils.isNotBlank(res.getError()))
		{
			throw new IvanException("WordNET similiarity for " + one + " and " + other + " failed with this error: "+ res.getError() + "\n" + res.getTrace());
		}
		return res.getScore();
	}

	/**
	 * 
	 * @param wordnetword a string of the format lemma#pos#num. E.g. jump#v#1 or house#n#2
	 * @return a synset identifier for WS4J
	 */
	private static Concept getSynset(String wordnetword) {
		String[] parts = StringUtils.split(wordnetword, "#");
		String lemma = parts[0];
		POS mypos = POS.valueOf(parts[1]);
		int index = Integer.parseInt(parts[2]) + 1;
		Synset synset = WordNetUtil.wordToSynsets(lemma, mypos).get(index);
		String synstring = synset.getSynset();
		return new Concept(synstring, mypos, lemma, synset.getSrc());
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
