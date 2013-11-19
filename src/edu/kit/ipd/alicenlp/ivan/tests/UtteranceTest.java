package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.UtteranceAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class UtteranceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public final void test() {
		fail("Not yet implemented"); // TODO
		
		String text = "When the scene starts, the t-rex is lifting his head and spreading his jaws voicing a loud \"ROOOOAAAAAR!!!\". "+
				"The torsoise says \"Hey, evil T-Rex, stop scaring my friend!\". "+
				"Again The t-rex is lifting his head and spreading his jaws voicing a loud \"ROOOOAAAAAR!!!\". "+
				"The tortoise replies \"Well I guess I have no choice but to kick your butt\". "+
				"As the scene starts, the girl is waving with her right arm, while saying: \"Hello\". "+
				"The boy turns to face the camera and then waves his right arm while saying: \"Hello\". "+
				"The girl turns to face the camera. "+
				"The scene starts by the T-Rex making a loud \"ROAAAAAAAAR!!!\", opening its mouth wide. "+
				"It says, \"Hey, evil T-Rex, stop scaring my friend!\", to which the T-Rex replies with another loud \"ROAAAAAAAAR!!\". "+
				"The tortoise says, \"Well, I guess I have no choice but to kick your butt.\" "+
				"The alien moves towards the right foreground. "+
				"While moving, the alien's head faces the astronaut. "+
				"The astronaut says: \"That's one small step for man\" and takes a step. "+
				"He says: \"...one...\". "+
				"The alien reaches the foreground and stops moving. "+
				"The astronaut says: \"...giant leap for...\" and turns his head towards the alien. "+
				"He says: \"He?\" "+
				"The alien turns to face the astronaut. "+
				"The alien nods and says: \"/§$%5a§$&\". "+
				"The astronaut turns to face the alien. "+
				"He says: \"I come in Peace!\" and takes one step towards the alien. "+
				"The alien points at the astronaut. "+
				"A giant flame column appears and consumes the astronaut. "+
				"The alien turns 180 degrees. "+
				"Alien off to the right. "+
				"The moon lander says: \"Houston...\" and \"...we have a problem!\" " +
				"While waving for the first time, the girl says : \"Hello\"."+ 
				"The woman then raises her left arm and a speech bubble appears from her mouth which reads \"hello.\" She then proceeds to wave, still facing forwards, and turns to face the man. Next she extends her right hand and shakes. A speech bubble then appears from the mouth of the man which also reads \"hello,\" and he raises his left arm and waves. He then places his arm back by his side. Both characters then raise their left arm upwards in a smooth motion at a 90 degree angle and rotate multiple times. Both arms are then replaced at the characters hips before they raise their left arm 90 defrees once again and wave. The animation is then complete.";
		
		Annotation doc = annotate(text);
		
		for (CoreMap sentence : doc.get(SentencesAnnotation.class)) {
			for (CoreMap token : sentence.get(TokensAnnotation.class)) {
				Integer ut = token.get(UtteranceAnnotation.class);
				if(ut > 0)
				{
					System.out.print(ut);
					System.out.println(": " + token);
				}
			}
		}
	}

	private Annotation annotate(String text) {
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
	    //props.put("customAnnotatorClass.decl", "edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
	    
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, dcoref"); //decl"); //$NON-NLS-1$ //$NON-NLS-2$
	    pipeline = new StanfordCoreNLP(props);	
	    
	    pipeline.annotate(doc);
	    return doc;
	}
	
}
