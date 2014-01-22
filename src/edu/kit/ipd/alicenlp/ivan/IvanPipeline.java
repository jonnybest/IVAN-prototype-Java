/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan;

import java.util.Properties;

import javax.swing.SwingWorker;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * @author Jonny
 * 
 */
public class IvanPipeline extends SwingWorker<Annotation, Object> {
	String text;

	/**
	 * This is the central pipeline which classifies text. This should never be
	 * directly accessed. Use getPipeline() instead.
	 */
	private static StanfordCoreNLP stanfordCentralPipeline;

	public IvanPipeline(String document) {
		text = document;
	}

	@Override
	protected Annotation doInBackground() throws Exception {
//		System.out.println("NEW WORKER");
		Annotation thing = annotateClassifications(text);
		return thing;
	}

	/**
	 * Annotates a document with our customized pipeline.
	 * 
	 * @param text
	 *            A text to process
	 * @return The annotated text
	 */
	public static synchronized Annotation annotateClassifications(String text) {	
//		System.out.println("NEW PIPELINE");
		Annotation doc = new Annotation(text);
		getPipeline().annotate(doc);
		return doc;
	}

	private static StanfordCoreNLP getPipeline() {
		if (stanfordCentralPipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization,
			// NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternative: wsj-bidirectional
			try {
				props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// adding our own annotator property
			props.put("customAnnotatorClass.sdclassifier", "edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");
			// adding our declaration finder
			props.put("customAnnotatorClass.declarations", "edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
			// configure pipeline
			props.put(//					"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, declarations, sdclassifier"); //$NON-NLS-1$ //$NON-NLS-2$
					"annotators", SwingWindow.PROPERTIES_ANNOTATORS); //$NON-NLS-1$ //$NON-NLS-2$
			stanfordCentralPipeline = new StanfordCoreNLP(props);
		}

		return stanfordCentralPipeline;
	}

	public static void prepare() {
		getPipeline();
	}
}
