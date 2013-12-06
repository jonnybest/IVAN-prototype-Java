package de.uka.ipd.resi;

import java.util.HashMap;
import java.util.Map;

import javatools.datatypes.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.recaacommons.Sense;

/**
 * @author Torben Brumm
 */
public abstract class Ontology {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Ontology.class);
	
	/**
	 * Map which stores constants temporarily for all words that are looked up. Used to avoid multiple questioning
	 * during application of a rule. Key: id of the word. Value: constant name.
	 */
	private final Map<String, String> constantCache = new HashMap<String, String>();
	
	/**
	 * Map which stores constants that are used for all occurrences of a word. Key: First letter of POS tag + base form
	 * of the word. Value: constant name.
	 */
	private final Map<String, String> constantMap = new HashMap<String, String>();
	
	/**
	 * Clears the cache that is used during an application of a rule to avoid multiple questioning for the same word.
	 */
	public void clearConstantCache() {
		Ontology.logger.debug("[>>>]clearConstantCache()");
		this.constantCache.clear();
		Ontology.logger.debug("[<<<]clearConstantCache()");
	}
	
	/**
	 * Returns the ontology constant for the given word if it is has recently been selected for this word or it has been
	 * selected for another occurrence of the word.
	 * 
	 * @param word Word the constant is wanted for.
	 * @return Constant for the word if found, else null.
	 */
	private String getConstantFromCacheOrSameWords(final Word word) {
		Ontology.logger.debug("[>>>]getConstantFromCacheOrSameWords( word = {})", word);
		// if we have recently selected the sense, it should be in the cache
		String constant = this.constantCache.get(word.getId());
		
		// special characters do not get a sense (they have a POS tag that does not start with a letter)
		if (constant == null && word.getPennTreebankTag() != null && word.getPennTreebankTag().length() > 0
				&& !word.getPennTreebankTag().substring(0, 1).matches("[A-Za-z]")) {
			constant = "";
		}
		
		if (constant == null) {
			// if we have already selected the sense for another occurrence of the word, use it
			String key;
			if (word.getPennTreebankTag() == null || word.getPennTreebankTag().length() == 0) {
				key = "X" + word.getBaseFormIfPresent();
			}
			else {
				key = word.getPennTreebankTag().substring(0, 1) + word.getBaseFormIfPresent();
			}
			constant = this.constantMap.get(key);
		}
		Ontology.logger.debug("[<<<]getConstantFromCacheOrSameWords(): {}", constant);
		return constant;
	}
	
	/**
	 * Returns the String that will be stored in the graph to represent the given Sense (varies for different
	 * ontologies).
	 * 
	 * @param sense Sense to get a String representation for.
	 * @return Sense as a String.
	 */
	protected abstract String getConstantToStoreFromSense(Sense sense);
	
	/**
	 * Returns the type of the ontology.
	 * 
	 * @return Name of ontology type.
	 */
	public abstract String getName();
	
	/**
	 * Puts a constant for the given Word into the cache.
	 * 
	 * @param word Word the constant belongs to.
	 * @param constant Constant for the word.
	 */
	private void putToConstantCache(final Word word, final String constant) {
		Ontology.logger.debug("[>>>]putToConstantCache( word = {}, constant = {})", word, constant);
		this.constantCache.put(word.getId(), constant);
		Ontology.logger.debug("[<<<]putToConstantCache()");
	}
	
	/**
	 * Puts a constant for the given word into the cache so it can be used for other occurrences of the same word.
	 * 
	 * @param word Word the constant belongs to.
	 * @param constant Constant for the word.
	 */
	private void putToConstantMapForSameWords(final Word word, final String constant) {
		Ontology.logger.debug("[>>>]putToConstantMapForSameWords( word = {}, constant = {})", word, constant);
		if (word.getPennTreebankTag() == null || word.getPennTreebankTag().length() == 0) {
			this.constantMap.put("X" + word.getBaseFormIfPresent(), constant);
		}
		else {
			this.constantMap.put(word.getPennTreebankTag().substring(0, 1) + word.getBaseFormIfPresent(), constant);
		}
		Ontology.logger.debug("[<<<]putToConstantMapForSameWords()");
	}
	
	/**
	 * Selects a sense for a word from a list of sense and stores it in the word. If there is already a sense selected,
	 * don't overwrite it.
	 * 
	 * @param word Word to select a sense for.
	 * @param senses Senses to select from.
	 */
	protected void selectOntologyConstant(final Word word, final Sense[] senses) {
		if (word.getOntologyConstant(this.getName()) == null) {
			// try to get from cache or from other occurrences of this word
			final String preSelectedSense = this.getConstantFromCacheOrSameWords(word);
			
			if (preSelectedSense != null) {
				// use this sense & put it into cache
				word.setOntologyConstant(this.getName(), preSelectedSense);
				word.setDirty(true);
				this.putToConstantCache(word, preSelectedSense);
			}
			else {
				// we have to get it first
				// ask user for correct sense
				final Pair<Boolean, Sense> selectedSense = Application.getInstance().selectWordSense(word, senses,
						this.getName());
				if (selectedSense.second() != null) {
					String senseToStore;
					// special case if there was no match in ontology
					if (selectedSense.second().equals(Sense.NO_SENSE_FOUND)) {
						senseToStore = "";
					}
					else {
						senseToStore = this.getConstantToStoreFromSense(selectedSense.second());
					}
					word.setOntologyConstant(this.getName(), senseToStore);
					word.setDirty(true);
					this.putToConstantCache(word, senseToStore);
					
					// store for later use if necessary
					if (selectedSense.first) {
						this.putToConstantMapForSameWords(word, senseToStore);
					}
				}
			}
			
		}
	}
	
	/**
	 * Returns if the ontology supports the Rule.
	 * 
	 * @param r Rule which might be supported.
	 * @return Is the Rule supported?
	 */
	public boolean supports(final Rule r) {
		try {
			if (Class.forName("de.uka.ipd.resi.ontologyinterface." + r.getOntologyInterface())
					.isInstance(this)) {
				return true;
			}
		}
		catch (final ClassNotFoundException e) {
			return false;
		}
		return false;
	}
	
}
