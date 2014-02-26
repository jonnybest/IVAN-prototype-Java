package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.DiscourseModel;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.rules.AliasByCorefRule;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.PrettyLogger;

@SuppressWarnings("javadoc")
public class AliasByCorefRuleTest 
{

	/** Tests a specific text that yielded odd results. 
	 * 
	 * @throws JWNLException
	 */
	@Test
	public void testLucas() throws JWNLException
	{
		String text = "Lucas is a wizard. Lucas is on the right hand side. Lucas is looking north.";
		Annotation annotate = annotate(text);
		AliasByCorefRule rule = new AliasByCorefRule();
		boolean okay = rule.apply(annotate);
		assertTrue("recognition failed", okay);
		
		DiscourseModel state = annotate.get(IvanAnnotations.IvanEntitiesAnnotation.class);		
		PrettyLogger.log(annotate.get(CorefChainAnnotation.class));
		PrettyLogger.log(state);
		
		assertThat("wizard not recognised", state.getEntity("Lucas"), is("wizard"));
		
		assertThat("error in entity list", state.getEntityNames().get(0).first, is("wizard"));
	}
	
	

	
	/**
	 * Asser that the rule can find a name in a sample sentence.
	 * @throws JWNLException 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testApplyAnnotation() throws JWNLException {
		String samplesentence = "Spanky is a dog."; 
		Annotation thing = annotate(samplesentence);

		AliasByCorefRule rule = new AliasByCorefRule();
		
		assertTrue("Nothing recognised", rule.apply(thing)); // runs rule
				
		List<CorefMention> mentions = rule.getAliasMentions();
		
		assertThat("Too few results", mentions.size(), is(1));
		
		CorefMention m = mentions.get(0);
		
		assertThat("Name does not match", m.mentionSpan, is("Spanky"));
		
		assertThat("Description does not match", rule.getEntity(m).mentionSpan, is("a dog"));
	}
	
	@Test
	public void indexTest() throws JWNLException
	{
		String samplesentence = "Terror in the nights outlasted Batman's second cousin. Spanky is a dog."; 
		Annotation annotation = annotate(samplesentence);

		AliasByCorefRule aliasRule = new AliasByCorefRule();		
		assertTrue("Nothing recognised", aliasRule.apply(annotation)); // runs rule				
		List<CorefMention> mentions = aliasRule.getAliasMentions();
		
		CorefMention ms = mentions.get(3);		
		
		assertThat(ms.mentionSpan, is("Spanky"));
		assertThat(aliasRule.getEntity(ms).mentionSpan, is("a dog"));
		
		int entityHeadIndex = aliasRule.getEntity(ms).headIndex - 1;
		int sentenceNum = aliasRule.getEntity(ms).sentNum - 1;
		CoreMap sen = annotation.get(SentencesAnnotation.class).get(sentenceNum);
		CoreLabel head = sen.get(TokensAnnotation.class).get(entityHeadIndex);
		String headstring = head.lemma();
		
		assertThat(headstring, is("dog"));
	}

	/** Verify that the spans have been set (at all)
	 * @throws JWNLException 
	 * 
	 */
	@Test
	public void spanTest() throws JWNLException
	{
		String samplesentence = "The ground is covered with grass, the sky is blue. \n"
				+ "In the background on the left hand side there is a PalmTree. \n"
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. \n"
				+ "Right to the mailbox there is a Frog facing east. \n"
				+ "In front of the Bunny there is a Broccoli. \n"
				+ "In the foreground on the right hand side there is a Bunny facing southwest. \n"
				+ "The Bunny turns to face the Broccoli. \n"
				+ "The Bunny hops three times to the Broccoli. \n"
				+ "The Bunny eats the Broccoli. \n"
				+ "The Bunny turns to face the Frog. \n"
				+ "The Bunny taps his foot twice. \n"
				+ "The Frog ribbits. The Frog turns to face northeast. \n"
				+ "The frog hops three times to northeast. \n"
				+ "The Bunny turns to face the Mailbox. \n"
				+ "The Bunny hops three times to the Mailbox. \n"
				+ "The Bunny opens the Mailbox. \n"
				+ "The Bunny looks in the Mailbox and at the same time the Frog turns to face the Bunny. \n"
				+ "The Frog hops two times to the Bunny. \n"
				+ "The Frog disappears. A short time passes.";
		
		Annotation annotation = annotate(samplesentence);

		AliasByCorefRule aliasRule = new AliasByCorefRule();		
		assertTrue("Nothing recognised", aliasRule.apply(annotation)); // runs rule				
		
		DiscourseModel state = annotation.get(IvanAnnotations.IvanEntitiesAnnotation.class);
		for (EntityInfo entityInfo : state) {
			assertNotNull("span is missing", entityInfo.getEntitySpan());
		}
	}
	
	private static Annotation annotate(String text) {
		Annotation doc = new Annotation(text);
		StanfordCoreNLP pipeline;
			
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    // alternativ: wsj-bidirectional 
	    try {
			props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	    props.put("customAnnotatorClass.decl", "edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
	    
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, decl"); //$NON-NLS-1$ //$NON-NLS-2$
	    pipeline = new StanfordCoreNLP(props);	
	    
	    pipeline.annotate(doc);
	    return doc;
	}
}
