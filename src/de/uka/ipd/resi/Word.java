package de.uka.ipd.resi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a word in a sentence in a graph.
 * 
 * @see Sentence
 * @author Torben Brumm
 */
public class Word {
	
	/**
	 * Base form of the word (not relevant for all words).
	 */
	private String baseForm;
	
	/**
	 * Have attributes changed since the last update to the corresponding graph?
	 */
	private boolean dirty = false;
	
	/**
	 * Id which identifies the word in the corresponding graph.
	 */
	private String id;
	
	/**
	 * Mark for comments in the specification regarding this Word.
	 */
	private Mark mark;
	
	/**
	 * Map which maps an ontology name to a constant name which represents this word in the ontology.
	 */
	private final Map<String, String> ontologyConstants = new HashMap<String, String>();
	
	/**
	 * Penn Treebank tag (POS tag).
	 */
	private String pennTreebankTag;
	
	/**
	 * Original word like it is in the sentence.
	 */
	private String plainWord;
	
	/**
	 * Sentence the word is part of.
	 */
	private Sentence sentence;
	
	/**
	 * Constructor.
	 * 
	 * @param id Id which identifies the word in the corresponding graph.
	 * @param plainWord Original word like it is in the sentence.
	 */
	public Word(final String id, final String plainWord) {
		this.id = id;
		this.plainWord = plainWord;
	}
	
	/**
	 * For comparing and sorting of words, returns the result of a comparison of the base forms (if present). This function is case-insensitive.
	 * 
	 * @see java.lang.String.compareToIgnoreCase
	 * @param otherWord Word to compare with.
	 * @return Same values as String.compareToIgnoreCase
	 */
	public int compareToIgnoreCase(final Word otherWord) {
		return this.getBaseFormIfPresent().compareToIgnoreCase(otherWord.getBaseFormIfPresent());
	}
	
	/**
	 * Returns the base form of the word (not relevant for all words).
	 * 
	 * @return Base form.
	 */
	public String getBaseForm() {
		return this.baseForm;
	}
	
	/**
	 * Returns the base form of the word if set, else the original word (all in lower case).
	 * 
	 * @return Base form or original word.
	 */
	public String getBaseFormIfPresent() {
		if (this.baseForm != null && !this.baseForm.equals("")) {
			return this.baseForm.toLowerCase();
		}
		return this.plainWord.toLowerCase();
	}
	
	/**
	 * Returns the Id which identifies the word in the corresponding graph.
	 * 
	 * @return Id.
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns the Mark for comments in the specification regarding this Word.
	 * 
	 * @return Mark or null if there is no mark.
	 */
	public Mark getMark() {
		return this.mark;
	}
	
	/**
	 * Returns the constant name which represents this word in the given ontology.
	 * 
	 * @param ontologyName Name of the ontology.
	 * @return Constant name.
	 */
	public String getOntologyConstant(final String ontologyName) {
		return this.ontologyConstants.get(ontologyName);
	}
	
	/**
	 * Returns the map which maps an ontology name to a constant name which represents this word in the ontology.
	 * 
	 * @return Map.
	 */
	public Map<String, String> getOntologyConstants() {
		return this.ontologyConstants;
	}
	
	/**
	 * Returns the Penn Treebank tag (POS tag).
	 * 
	 * @return Penn Treebank tag.
	 */
	public String getPennTreebankTag() {
		return this.pennTreebankTag;
	}
	
	/**
	 * Returns the original word like it is in the sentence.
	 * 
	 * @return Original word.
	 */
	public String getPlainWord() {
		return this.plainWord;
	}
	
	/**
	 * Returns the Sentence the word is part of.
	 * 
	 * @return Sentence.
	 */
	public Sentence getSentence() {
		return this.sentence;
	}
	
	/**
	 * Returns if attributes have changed since the last update to the corresponding graph.
	 * 
	 * @return Changes?
	 */
	public boolean isDirty() {
		return this.dirty;
	}
	
	/**
	 * Returns a String representation of this Word with all its attributes.
	 * 
	 * @return Complete word as String.
	 */
	public String printCompleteWord() {
		String returnString = this.id + ": " + this.plainWord + "\n\tBase form: " + this.baseForm
				+ "\n\tPenn Treebank tag: " + this.pennTreebankTag + "\n\tMark: " + this.mark;
		for (final Entry<String, String> mapEntry : this.ontologyConstants.entrySet()) {
			returnString += "\n\t" + mapEntry.getKey() + " constant: " + mapEntry.getValue();
		}
		return returnString;
	}
	
	/**
	 * Sets the base form of the word (not relevant for all words).
	 * 
	 * @param baseForm New base form.
	 */
	public void setBaseForm(final String baseForm) {
		this.baseForm = baseForm;
	}
	
	/**
	 * Sets if attributes have changed since the last update to the corresponding graph.
	 * 
	 * @param dirty Changes?
	 */
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	/**
	 * Sets the id which identifies the word in the corresponding graph.
	 * 
	 * @param id New id.
	 */
	public void setId(final String id) {
		this.id = id;
	}
	
	/**
	 * Sets the Mark for comments in the specification regarding this Word.
	 * 
	 * @param mark New Mark (null for no mark).
	 */
	public void setMark(final Mark mark) {
		this.mark = mark;
	}
	
	/**
	 * Sets the constant name which represents this word in the given ontology.
	 * 
	 * @param ontologyName Name of the ontology.
	 * @param ontologyConstant New constant name.
	 */
	public void setOntologyConstant(final String ontologyName, final String ontologyConstant) {
		this.ontologyConstants.put(ontologyName, ontologyConstant);
	}
	
	/**
	 * Sets the Penn Treebank tag (POS tag).
	 * 
	 * @param pennTreebankTag Penn Treebank tag.
	 */
	public void setPennTreebankTag(final String pennTreebankTag) {
		this.pennTreebankTag = pennTreebankTag;
	}
	
	/**
	 * Sets the original word like it is in the sentence.
	 * 
	 * @param plainWord New original word.
	 */
	public void setPlainWord(final String plainWord) {
		this.plainWord = plainWord;
	}
	
	/**
	 * Sets the Sentence the word is part of.
	 * 
	 * @param New Sentence.
	 */
	public void setSentence(final Sentence sentence) {
		this.sentence = sentence;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.plainWord;
	}
	
}
