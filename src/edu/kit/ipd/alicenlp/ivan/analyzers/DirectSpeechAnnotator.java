package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.util.List;
import java.util.LinkedList;

import org.apache.xpath.operations.Quo;

import edu.stanford.nlp.ling.CoreAnnotations.BagOfWordsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

public class DirectSpeechAnnotator {

	public void annotate(Annotation mydoc) {
		// POS for start quote: ``
		// POS for end quote: ''
		/*
		 * I assume that valid direct speech conform to the
		 * "parentheses pattern". This is just a stack implementation that
		 * counts open quotes and decreases the count for each closed quote. If
		 * the outcome is exactly 0, all the words "inside" receive an
		 * EnclosedByQuotesAnnotation
		 */
		List<CoreLabel> annotationCandidates = new LinkedList<CoreLabel>();
		int openQuotes = 0;

		for (CoreLabel word : mydoc.get(TokensAnnotation.class)) {
			// for all words in this document
			// is this word a quotation mark?
			if (word.tag().equals("``")) {
				openQuotes++;
			} else if (word.tag().equals("''")) {
				openQuotes--;
			} else if (openQuotes > 0) {
				// the current word is enclosed in quotes
				annotationCandidates.add(word);
			}
		}
		// end sentence
		if (openQuotes == 0) {
			// success! let's tag all the words
			for (CoreLabel word : annotationCandidates) {
				// this annotation means that the word is enclosed between
				// quotation marks
				word.set(QuotationAnnotation.class, true);
			}
		}
	}
}
