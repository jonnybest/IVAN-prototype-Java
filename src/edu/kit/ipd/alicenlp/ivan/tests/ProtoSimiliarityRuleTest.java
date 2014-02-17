package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
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

public class ProtoSimiliarityRuleTest {

	@Test
	public final void test() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testComparers() {
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = { new HirstStOnge(db),
				new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
				new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

		String[] setupSynsets = { "be#v", "sit#v", "cover#v",
				"depict#v", "consist#v" };

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
		WS4JConfiguration.getInstance().setMFS(true);
		
		List<Synset> thing = edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets("jump", POS.v);
		Concept concept = new Concept(thing.get(0).getSynset());
		
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = { new HirstStOnge(db),
				new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
				new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

		String[] setupSynsets = { "be#v#3", "sit#v#1", "cover#v#2",
				"depict#v#1", "consist#v#4" };
		
		Concept stand = new Concept(edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets("stand", POS.v).get(0).getSynset());
		stand.setPos(POS.v);
		Concept jump = new Concept(edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets("jump", POS.v).get(0).getSynset());
		jump.setPos(POS.v);
		Concept co = new Concept(edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets("say", POS.v).get(0).getSynset());
		co.setPos(POS.v);

		for (RelatednessCalculator rc : rcs) {
			if(rc == null){
				System.err.println("RC is null!");
				continue;
			}
			
			Relatedness setupScore = rc.calcRelatednessOfSynset(co, stand);		
			Relatedness actionScore = rc.calcRelatednessOfSynset(co, jump);
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
		WS4JConfiguration.getInstance().setMFS(true);
		for ( RelatednessCalculator rc : rcs ) {
			double s = rc.calcRelatednessOfWords(word1, word2);
			System.out.println( rc.getClass().getName()+"\t"+s );
		}
	}

	@Test
	public void testSimpleThings(){
		double thing = WS4J.runHSO("jump#v", "stand#v");
		nop();
	}

	private void nop() {
		// TODO Auto-generated method stub
		
	}
}
