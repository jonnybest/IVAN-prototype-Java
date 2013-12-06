package de.uka.ipd.resi.ontologyimpl;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Ontology;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.ontologyinterface.OntologyBaseFormTag;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;
import de.uka.ipd.resi.ontologyinterface.OntologyAvoidAmbiguousWords;
import edu.brandeis.cs.steele.wn.DictionaryDatabase;
import edu.brandeis.cs.steele.wn.FileBackedDictionary;
import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.POS;
import edu.brandeis.cs.steele.wn.Pointer;
import edu.brandeis.cs.steele.wn.PointerType;
import edu.brandeis.cs.steele.wn.RemoteFileManager;
import edu.brandeis.cs.steele.wn.Synset;

/**
 * Ontology which connects to a WordNetServer or uses a local database.
 * 
 * @author Torben Brumm
 */
public class WordNetOntology extends Ontology implements OntologySimilarMeaning, OntologyBaseFormTag,
		OntologyAvoidAmbiguousWords {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(WordNetOntology.class);
	
	/**
	 * WordNet database.
	 */
	private final DictionaryDatabase database;
	
	/**
	 * Constructor which connects to a local WordNet database.
	 * 
	 * @param localDatabasePath Absolute path where the local WordNet database is located.
	 */
	public WordNetOntology(final String localDatabasePath) {
		WordNetOntology.logger.debug("[>>>]WordNetOntology( localDatabasePath = {} )", localDatabasePath);
		this.database = new FileBackedDictionary(localDatabasePath);
		WordNetOntology.logger.info("Connected to local WordNet database at {}.", localDatabasePath);
		WordNetOntology.logger.debug("[<<<]WordNetOntology()");
	}
	
	/**
	 * Constructor which connects to a remote WordNetServer.
	 * 
	 * @param serverAddress Address of the remote server to connect to.
	 * @param serverPort Server port to connect to.
	 * @throws NotConnectedException
	 */
	public WordNetOntology(final String serverAddress, final int serverPort) throws NotConnectedException {
		WordNetOntology.logger.debug("[>>>]WordNetOntology( serverAddress = {}, serverPort = {} )", serverAddress,
				serverPort);
		try {
			this.database = new FileBackedDictionary(RemoteFileManager.lookup(serverAddress, serverPort));
			WordNetOntology.logger.info("Connected to WordNetServer at {}:{}.", serverAddress, serverPort);
		}
		catch (final AccessException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final RemoteException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final NotBoundException e) {
			throw new NotConnectedException(this, e);
		}
		WordNetOntology.logger.debug("[<<<]WordNetOntology()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.ontologyinterface.OntologyGetOntologyConstants#assureOntologyConstantIsSelected
	 * (de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public void assureOntologyConstantIsSelected(final Word word) throws WordNotFoundException {
		WordNetOntology.logger.debug("[>>>]assureOntologyConstantIsSelected( word = {} )", word);
		this.getSynsetForWord(word, this.getPosFromPosTag(word.getPennTreebankTag()));
		WordNetOntology.logger.debug("[<<<]assureOntologyConstantIsSelected()");
	}
	
	@Override
	protected String getConstantToStoreFromSense(final Sense sense) {
		return sense.getFullInformation();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Ontology#getName()
	 */
	@Override
	public String getName() {
		return "WordNet";
	}
	
	/**
	 * Gets the WordNet-POS for a given Penn Treebank tag.
	 * 
	 * @param posTag Penn Treebank tag.
	 * @return POS.
	 */
	private POS getPosFromPosTag(final String posTag) {
		WordNetOntology.logger.debug("[>>>]getPosFromPosTag( posTag = {} )", posTag);
		POS returnValue = null;
		if (posTag != null) {
			if (posTag.startsWith("N")) {
				returnValue = POS.NOUN;
			}
			else if (posTag.startsWith("V")) {
				returnValue = POS.VERB;
			}
			else if (posTag.startsWith("J")) {
				returnValue = POS.ADJ;
			}
			else if (posTag.startsWith("RB")) {
				returnValue = POS.ADV;
			}
		}
		WordNetOntology.logger.debug("[<<<]getPosFromPosTag(): ", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyCompareNouns#getSimilarity(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public float getSimilarity(final Word noun1, final Word noun2) throws WordNotFoundException {
		// TODO check if they are really nouns?
		WordNetOntology.logger.debug("[>>>]getSimilarity( noun1 = {}, noun2 = {} )", noun1, noun2);
		float probability1 = 0;
		float probability2 = 0;
		
		final Synset synset1 = this.getSynsetForWord(noun1, POS.NOUN);
		final Synset synset2 = this.getSynsetForWord(noun2, POS.NOUN);
		
		if (synset1 != null && synset2 != null) {
			probability1 = this.hypernymProbability(synset1, synset2);
			probability2 = this.hypernymProbability(synset2, synset1);
		}
		
		float returnValue;
		if (probability1 > probability2) {
			returnValue = probability1;
		}
		else {
			returnValue = (-1) * probability2;
		}
		WordNetOntology.logger.debug("[<<<]getSimilarity(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Returns a Synset for the given word. If it is not set in the word, look it up and set it first.
	 * 
	 * @param word Word to get the Synset for.
	 * @param pos POS of the Word.
	 * @return Synset for the word.
	 * @throws WordNotFoundException
	 */
	private Synset getSynsetForWord(final Word word, final POS pos) throws WordNotFoundException {
		WordNetOntology.logger.debug("[>>>]getSynsetForWord( word = {}, pos = {} )", word, pos);
		Synset returnValue = null;
		Synset[] synsetList = new Synset[0];
		Sense[] senses = new Sense[0];
		
		if (pos != null) {
			final IndexWord indexWord = this.database.lookupIndexWord(pos, word.getBaseFormIfPresent());
			if (indexWord != null) {
				// we build a list of synsets if possible
				synsetList = indexWord.getSenses();
				senses = new Sense[synsetList.length];
				for (int i = 0; i < synsetList.length; i++) {
					senses[i] = new Sense(synsetList[i].getDescription(), synsetList[i].getGloss());
				}
			}
		}
		
		// select a sense if needed
		this.selectOntologyConstant(word, senses);
		
		// by now we have a sense selected (if we want to)
		final String selectedSense = word.getOntologyConstant(this.getName());
		if (selectedSense != null) {
			for (final Synset possibleSynset : synsetList) {
				if (possibleSynset.getLongDescription().equals(selectedSense)) {
					returnValue = possibleSynset;
					break;
				}
			}
		}
		
		// No Synset found => Exception
		if (returnValue == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		
		WordNetOntology.logger.debug("[<<<]getSynsetForWord(): ", returnValue);
		return returnValue;
	}
	
	/**
	 * Returns the probability (on a scale from 0 to 1) of one word beig the hypernym of another one.
	 * 
	 * @param possibleHypernym Possible Hypernym.
	 * @param possibleHyponym Possible Hyponym.
	 * @return Probability (0 to 1)
	 */
	private float hypernymProbability(final Synset possibleHypernym, final Synset possibleHyponym) {
		WordNetOntology.logger.debug("[>>>]hypernymProbability( possibleHypernym = {}, possibleHyponym = {} )",
				possibleHypernym, possibleHyponym);
		if (possibleHypernym.equals(possibleHyponym)) {
			return 1;
		}
		final Pointer[] directHypernymPointers = possibleHyponym.getPointers(PointerType.HYPERNYM);
		float combinedProbability = 0;
		for (final Pointer directHypernymPointer : directHypernymPointers) {
			combinedProbability += this.hypernymProbability(possibleHypernym, (Synset) directHypernymPointer
					.getTarget());
		}
		if (directHypernymPointers.length == 0) {
			return 0;
		}
		return combinedProbability / directHypernymPointers.length;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyBaseFormTag#tagSentenceWithBaseForm(de.uka.ipd.
	 * specificationimprover.Sentence)
	 */
	@Override
	public void tagSentenceWithBaseForm(final Sentence sentence) {
		WordNetOntology.logger.debug("[>>>]tagSentenceWithBaseForm( sentence = {} )", sentence);
		for (final Word word : sentence.getWords()) {
			final String posTag = word.getPennTreebankTag();
			if (posTag != null) {
				if (posTag.startsWith("N")) {
					word.setBaseForm(this.database.lookupBaseForm(POS.NOUN, word.getPlainWord()));
				}
				else if (posTag.startsWith("V")) {
					word.setBaseForm(this.database.lookupBaseForm(POS.VERB, word.getPlainWord()));
				}
				else if (posTag.startsWith("J")) {
					word.setBaseForm(this.database.lookupBaseForm(POS.ADJ, word.getPlainWord()));
				}
				else if (posTag.startsWith("RB")) {
					word.setBaseForm(this.database.lookupBaseForm(POS.ADV, word.getPlainWord()));
				}
				word.setDirty(true);
			}
		}
		WordNetOntology.logger.debug("[<<<]tagSentenceWithBaseForm()");
	}
	
}
