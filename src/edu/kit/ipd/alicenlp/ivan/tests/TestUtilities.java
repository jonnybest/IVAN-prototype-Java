package edu.kit.ipd.alicenlp.ivan.tests;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/** This test contains several utility methods for easier testing.
 * Remember, testing is the future!
 * 
 * @author Jonny
 *
 */
public abstract class TestUtilities {


	/**
	 * Annotates a document with our customized pipeline.
	 * 
	 * @param text
	 *            A text to process
	 * @return The annotated text
	 */
	public static Annotation annotateClassifications(String text) {
		Annotation doc = new Annotation(text);

		StanfordCoreNLP pipeline;

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		// alternative: wsj-bidirectional
		try {
			props.put(
					"pos.model",
					"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// adding our own annotator property
		props.put("customAnnotatorClass.sdclassifier",
				"edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");
		// adding our declaration finder
		props.put("customAnnotatorClass.declarations",
				"edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");

		// configure pipeline
		props.put(
				"annotators", "tokenize, ssplit, pos, lemma, ner, parse, declarations, sdclassifier"); //$NON-NLS-1$ //$NON-NLS-2$
		pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(doc);
		return doc;
	}
}
