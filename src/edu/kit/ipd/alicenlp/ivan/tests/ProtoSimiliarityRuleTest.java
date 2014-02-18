package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import edu.cmu.lti.ws4j.util.Traverser;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.rules.ProtoSimiliarityRule;
import edu.stanford.nlp.util.CoreMap;

public class ProtoSimiliarityRuleTest {

	/** set the settings.
	 * 
	 */
	@Before
	public void prepare(){
		WS4JConfiguration.getInstance().setTrace(true); // some kind of debug messages 
		WS4JConfiguration.getInstance().setMFS(false); // some kind of shortcut? maybe "use most frequent sense"
		WS4JConfiguration.getInstance().setStem(false);	// turns of the internal stemmer. our words are already lemmas
		WS4JConfiguration.getInstance().setLeskNormalize(false); // lesk normalization seems to be broken.
		
	}
	
	@Test
	public final void test() throws IvanException {
		ProtoSimiliarityRule rule = new ProtoSimiliarityRule();
		CoreMap sent = TestUtilities.annotateSingleBasics("A dog is running away.");
		rule.apply(sent);
		
		assertThat(rule.getResult(), is(Classification.ActionDescription));
		
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testComparers() {
		WS4JConfiguration.getInstance().setTrace(true);
		
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = { new HirstStOnge(db),
				new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
				new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

		String[] setupSynsets = { "be#v", "sit#v", "position#v",
				"stand#v", "consist#v" };

		System.out.println("Values for " + Arrays.toString(setupSynsets));
		
		for (RelatednessCalculator rc : rcs) {
			System.out.println(rc.getClass());
			double[][] selfsimilarity = rc.getNormalizedSimilarityMatrix(
					setupSynsets, setupSynsets);
			for (int i = 0; i < setupSynsets.length; i++) {
				for (int j = 0; j < setupSynsets.length; j++) {
					System.out.printf("%5f ", selfsimilarity[i][j]);
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	@Test
	public void testTwoWords(){
		
		List<Synset> thing = edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets("jump", POS.v);
		Concept concept = new Concept(thing.get(0).getSynset());
		
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = { new HirstStOnge(db),
				new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
				new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

		String[] setupSynsets = { "be#v#3", "sit#v#1", "cover#v#2",
				"depict#v#1", "consist#v#4" };
		
		
		Concept stand = getSynset("stand#v#1");
		stand.setPos(POS.v);
		Concept jump = getSynset("jump#v#1");
		jump.setPos(POS.v);
		Concept co = getSynset("say#v#1");
		co.setPos(POS.v);


		for (RelatednessCalculator rc : rcs) {
			if(rc == null){
				System.err.println("RC is null!");
				continue;
			}
			
			Relatedness setupScore = rc.calcRelatednessOfSynset(co, stand);		
			System.out.println(String.format("Trace for %s of %s and %s: %n%s", rc.getClass().getSimpleName(), co.getName(), stand.getName(), setupScore.getTrace()));
			System.out.println(co.getName() + " scored with setup: " + setupScore.getScore());
			Relatedness actionScore = rc.calcRelatednessOfSynset(co, jump);
			System.out.println(String.format("Trace for %s of %s and %s: %n%s", rc.getClass().getSimpleName(), co.getName(), jump.getName(), actionScore.getTrace()));
			System.out.println(co.getName() + " scored with action: " + actionScore.getScore());
			System.out.println(rc.getClass());
			double value = rc.calcRelatednessOfWords("stand", "jump");
			
			System.out.println(value);
		}
	}
	
	 
	@Test
	public void testDemo() {
		long t0 = System.currentTimeMillis();
		run( "act","moderate" );
		long t1 = System.currentTimeMillis();
		System.out.println( "Done in "+(t1-t0)+" msec." );
	}
	
	private static void run( String word1, String word2 ) {
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = {
				new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
				new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
				};

		for ( RelatednessCalculator rc : rcs ) {
			double s = rc.calcRelatednessOfWords(word1, word2);
			System.out.println( rc.getClass().getName()+"\t"+s );
		}
	}

	@Test
	public void testSimpleThings() throws IvanException{
		Concept stand = getSynset("stand#v#1");
		Concept jump = getSynset("jump#v#1");
		RelatednessCalculator comparer = new Lesk(new NictWordNet());
		double score = compare(comparer , jump, stand);
		// done!    

		nop();
	}
	
	// utility
	private static double compare(RelatednessCalculator comparer, Concept one,
			Concept other) throws IvanException {
		Relatedness res = comparer.calcRelatednessOfSynset(one, other);
		if(StringUtils.isNotBlank(res.getError()))
		{
			throw new IvanException ("WordNET similiarity for " + one + " and " + other + " failed with this error: "+ res.getError() + "\n" + res.getTrace());
		}
		if(StringUtils.isNotBlank(res.getTrace()))
			System.out.println(String.format("Trace for %s of %s and %s: %n%s", comparer.getClass().getSimpleName(), one.getName(), other.getName(), res.getTrace()));
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
	
	private void nop() {
		// TODO Auto-generated method stub
		
	}
}
