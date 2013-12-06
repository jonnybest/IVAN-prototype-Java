package de.uka.ipd.resi.ontologyimpl;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Ontology;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.ontologyinterface.OntologyPOSTag;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Ontology (in fact a tagger) which uses a Stanford POS tagger.
 * 
 * @author Torben Brumm
 */
public class StanfordPOSTagger extends Ontology implements OntologyPOSTag {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(StanfordPOSTagger.class);
	
	/**
	 * Tagger which does the work.
	 */
	private final MaxentTagger tagger;
	
	/**
	 * Initializes a tagger.
	 * 
	 * @param localPathToTagger Local Path where to find the wanted tagger.
	 * @throws NotConnectedException
	 */
	public StanfordPOSTagger(final String localPathToTagger) throws NotConnectedException {
		StanfordPOSTagger.logger.debug("[>>>]StanfordPOSTagger( localPathToTagger = {} )", localPathToTagger);
		try {
			this.tagger = new MaxentTagger(localPathToTagger);
			StanfordPOSTagger.logger.info("Stanford POS tagger initialized with the tagger at {}.", localPathToTagger);
		}
		catch (final Exception e) {
			throw new NotConnectedException(this, e);
		}
		StanfordPOSTagger.logger.debug("[<<<]StanfordPOSTagger()");
	}
	
	@Override
	protected String getConstantToStoreFromSense(final Sense sense) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Ontology#getName()
	 */
	@Override
	public String getName() {
		return "StanfordPOSTagger";
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.ontologyinterface.OntologyPOSTag#tagSentence(de.uka.ipd.specificationimprover
	 * .Sentence)
	 */
	@Override
	public void tagSentenceWithPOS(final Sentence sentence) throws SyntaxException {
		StanfordPOSTagger.logger.debug("[>>>]tagSentenceWithPOS( sentence = {} )", sentence);
		final ArrayList<String> wordList = new ArrayList<String>();
		for (final Word word : sentence.getWords()) {
			wordList.add(word.getPlainWord());
		}
		
		final ArrayList<TaggedWord> taggedWords = this.tagger.apply(edu.stanford.nlp.ling.Sentence
				.toWordList((String[]) wordList.toArray()));
		final Iterator<Word> wordIterator = sentence.getWords().iterator();
		for (final TaggedWord taggedWord : taggedWords) {
			if (!wordIterator.hasNext()) {
				throw new SyntaxException(sentence);
			}
			final Word word = wordIterator.next();
			if (!word.getPlainWord().equals(taggedWord.word())) {
				throw new SyntaxException(sentence);
			}
			word.setPennTreebankTag(taggedWord.tag());
			word.setDirty(true);
		}
		StanfordPOSTagger.logger.debug("[<<<]tagSentenceWithPOS()");
	}
	
}
