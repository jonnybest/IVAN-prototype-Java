package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class DeclarationPositionFinderTest {
	
	String[] locations = {
			"There is a bunny on the right side."
			};
	String[] directions = {
			"Hank is turned left 3 degrees.", 
			"Hank faces towards the south.",
			"Sary is facing southwards."
	};
	String[] negatives = {
			"There is a bunny in the picture.",
			"Being good is virtous.",
			"The bunny is tall.",
			"Both spin quickly five times, then they come to a rest."
	};
	
	@Test
	public void testFindAll() {
		for (String location : locations) {
			DeclarationPositionFinder proto = DeclarationPositionFinder.getInstance();
			Annotation anno = new Annotation(location);
			proto.getPipeline().annotate(anno);
			CoreMap sentence = anno.get(key)
			proto.findAll(name, root, sentence);
		}
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHasLocation() {
		
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetLocation() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDirection() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDeclarations() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRecogniseNames() {
		fail("Not yet implemented"); // TODO
	}

}
