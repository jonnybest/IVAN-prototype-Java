package edu.kit.ipd.alicenlp.ivan.data;

import java.util.List;

import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.stanford.nlp.util.TypesafeMap;

/**
 * Set of common annotations for Input & Verify Alice NLP. The classes defined
 * here are typesafe keys for getting and setting annotation values. These
 * classes need not be instantiated outside of this class.
 * 
 * @author Jonny
 * @see edu.stanford.nlp.ling.CoreAnnotations
 * 
 */
public class IvanAnnotations {

	/**
	 * This class may be used to retrieve document-wide errors. Find it in the
	 * Annotation map.
	 * 
	 * @author Jonny
	 * 
	 */
	public class DocumentErrorAnnotation implements
			TypesafeMap.Key<List<IvanErrorMessage>> {

	}

	/**
	 * This annotation contains the entities found in this document. Find it in
	 * the annotation map.
	 * 
	 * @author Jonny
	 * 
	 */
	public class IvanEntitiesAnnotation implements
			TypesafeMap.Key<DiscourseModel> {

	}

	/**
	 * This annotation indicates an issue with a sentence or a word. Find it in
	 * the sentence annotation.
	 * 
	 * @author Jonny
	 * 
	 */
	public class ErrorMessageAnnotation implements
			TypesafeMap.Key<IvanErrorMessage> {

	}

	/**
	 *  This annotation indicates what this sentence is about. 
	 *  
	 * @author Jonny
	 *
	 */
	public class SentenceClassificationAnnotation implements
			TypesafeMap.Key<Classification> {

	}
}