package de.uka.ipd.resi;

import java.util.List;

/**
 * Represents a sentence in a specification.
 * 
 * @author Torben Brumm
 */
public class Sentence {
	
	/**
	 * Graph this sentence belongs to.
	 */
	private Graph graph;
	
	/**
	 * Id which identifies the sentence in the corresponding graph.
	 */
	private String id;
	
	/**
	 * Whole sentence as String.
	 */
	private String plainSentence;
	
	/**
	 * Sorted list of Words that build this sentence.
	 */
	private List<Word> words;
	
	/**
	 * Constructor. Sets the sentence attribute of the given Words to this Sentence object.
	 * 
	 * @param graph Graph this sentence belongs to.
	 * @param id Id which identifies the sentence in the corresponding graph.
	 * @param plainSentence Whole sentence as String.
	 * @param words Sorted list of Words that build this sentence.
	 */
	public Sentence(final Graph graph, final String id, final String plainSentence, final List<Word> words) {
		this.setGraph(graph);
		this.id = id;
		this.plainSentence = plainSentence;
		this.words = words;
		for (final Word word : this.words) {
			word.setSentence(this);
		}
	}
	
	/**
	 * Returns the graph this sentence belongs to.
	 * 
	 * @return Graph of this sentence.
	 */
	public Graph getGraph() {
		return this.graph;
	}
	
	/**
	 * Returns the id which identifies the sentence in the corresponding graph.
	 * 
	 * @return Id of the sentence.
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns the whole sentence as String.
	 * 
	 * @return Whole sentence.
	 */
	public String getPlainSentence() {
		return this.plainSentence;
	}
	
	/**
	 * Returns the sorted list of Words that build this sentence.
	 * 
	 * @return Sorted list of words.
	 */
	public List<Word> getWords() {
		return this.words;
	}
	
	/**
	 * Returns the whole sentence with all its properties and all contained words with their properties.
	 * 
	 * @return Everything about this sentence.
	 */
	public String printCompleteSentence() {
		String returnValue = this.id + ": " + this.plainSentence + "\n================\n";
		for (final Word word : this.words) {
			returnValue += word.printCompleteWord() + "\n";
		}
		return returnValue;
	}
	
	/**
	 * Sets the graph this sentence belongs to.
	 * 
	 * @param graph New graph.
	 */
	public void setGraph(final Graph graph) {
		this.graph = graph;
	}
	
	/**
	 * Sets the id which identifies the sentence in the corresponding graph.
	 * 
	 * @param id New id of the sentence.
	 */
	public void setId(final String id) {
		this.id = id;
	}
	
	/**
	 * Sets the String for the whole sentence.
	 * 
	 * @param plainSentence New whole sentence.
	 */
	public void setPlainSentence(final String plainSentence) {
		this.plainSentence = plainSentence;
	}
	
	/**
	 * Sets the sorted list of Words that build this sentence.
	 * 
	 * @param words New sorted list of words.
	 */
	public void setWords(final List<Word> words) {
		this.words = words;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.plainSentence;
	}
	
}
