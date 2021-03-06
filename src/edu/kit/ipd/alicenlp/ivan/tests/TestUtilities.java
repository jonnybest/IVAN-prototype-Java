package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.SentenceClassificationAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This test contains several utility methods for easier testing. Remember,
 * testing is the future!
 * 
 * @author Jonny
 * 
 */
public abstract class TestUtilities {

	public static final String PROPERTIES_ANNOTATORS = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, declarations, sdclassifier";
	private static StanfordCoreNLP declarationsPipeline;
	private static StanfordCoreNLP classificationsPipeline;
	// this pipeline breaks sentences only at line breaks
	private static StanfordCoreNLP evaluationPipeline;

	/**
	 * Annotates a document with our customized pipeline.
	 * 
	 * @param text
	 *            A text to process
	 * @return The annotated text
	 */
	public static Annotation annotateClassifications(String text) {
		Annotation doc = new Annotation(text);

		if (classificationsPipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization,
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
					"annotators", PROPERTIES_ANNOTATORS); //$NON-NLS-1$ //$NON-NLS-2$
			classificationsPipeline = new StanfordCoreNLP(props);
		}

		classificationsPipeline.annotate(doc);
		return doc;
	}

	static Annotation annotateDeclarations(String text) {
		Annotation doc = new Annotation(text);

		if (declarationsPipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternativ: wsj-bidirectional
			try {
				props.put(
						"pos.model",
						"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
			} catch (Exception e) {
				e.printStackTrace();
			}
			props.put("customAnnotatorClass.decl",
					"edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
			// konfiguriere declarationsPipeline
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, decl"); //$NON-NLS-1$ //$NON-NLS-2$
			declarationsPipeline = new StanfordCoreNLP(props);
		}

		declarationsPipeline.annotate(doc);
		return doc;
	}

	/**
	 * This checker method verifies a sample sentence against its solution,
	 * unless the sample sentence was recognized as bad input.
	 * 
	 * @param solution
	 * @throws IvanException
	 */
	static void checkEntrySet(Entry<String, String[]> solution)
			throws IvanException {
		CoreMap annoSentence = annotateSingleDeclaration(solution.getKey());
		annoSentence.get(TreeAnnotation.class).pennPrint();
		List<String> einfos = DeclarationPositionFinder
				.recogniseEntities(annoSentence);
		if (annoSentence.get(SentenceClassificationAnnotation.class) != null
				&& annoSentence.get(SentenceClassificationAnnotation.class).equals(
						Classification.ErrorDescription)) {
			System.out.println("Error: \"" + solution.getKey()
					+ "\"\nis not valid IVAN input.");
			return;
		}
		if (einfos.size() != solution.getValue().length) {
			fail("Some entities were not recognised in in sentence \""
					+ solution.getKey() + "\"");
		}
		for (String foundname : einfos) {
			boolean matched = false;
			for (String solutionname : solution.getValue()) {
				if (solutionname.equalsIgnoreCase(foundname)) {
					matched = true;
					continue;
				}
			}
			if (!matched) {
				fail(foundname + " not recognised in sentence \""
						+ solution.getKey() + "\"");
			}
		}
	}

	static CoreMap annotateSingleDeclaration(String text) {
		return annotateDeclarations(text).get(SentencesAnnotation.class).get(0);
	}

	static CoreMap annotateSingleClassification(String text) {
		Annotation doc = new Annotation(text);

		if (evaluationPipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternativ: wsj-bidirectional
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
			// do not split sentences inside direct speech
			props.setProperty("ssplit.eolonly", "true");
			// konfiguriere declarationsPipeline
			props.put("annotators", PROPERTIES_ANNOTATORS); //$NON-NLS-1$ //$NON-NLS-2$
			evaluationPipeline = new StanfordCoreNLP(props);
		}

		evaluationPipeline.annotate(doc);
		return doc.get(SentencesAnnotation.class).get(0);
	}

}
