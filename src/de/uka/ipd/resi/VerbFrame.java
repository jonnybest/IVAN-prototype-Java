package de.uka.ipd.resi;

import java.util.ArrayList;
import java.util.List;

import de.uka.ipd.recaacommons.Sense;

/**
 * @author Torben Brumm
 */
public class VerbFrame {
	
	/**
	 * @author Torben Brumm
	 */
	public static class VerbArgument {
		
		/**
		 * Semantic role of this argument regarding the verb.
		 */
		private Sense semanticRole;
		
		/**
		 * Syntactic role of this argument in the sentence.
		 */
		private String syntacticRole;
		
		/**
		 * Word that is assigned to this argument.
		 */
		private Word word;
		
		/**
		 * Constructor.
		 * 
		 * @param semanticRole Semantic role of this argument regarding the verb.
		 * @param syntacticRole Syntactic role of this argument in the sentence.
		 */
		public VerbArgument(final Sense semanticRole, final String syntacticRole) {
			this.semanticRole = semanticRole;
			this.syntacticRole = syntacticRole;
		}
		
		/**
		 * Returns the semantic role of this argument regarding the verb.
		 * 
		 * @return Semantic role.
		 */
		public Sense getSemanticRole() {
			return this.semanticRole;
		}
		
		/**
		 * Returns the syntactic role of this argument in the sentence.
		 * 
		 * @return Syntactic role.
		 */
		public String getSyntacticRole() {
			return this.syntacticRole;
		}
		
		/**
		 * Returns the Word that is assigned to this argument.
		 * 
		 * @return Assigned Word.
		 */
		public Word getWord() {
			return this.word;
		}
		
		/**
		 * Sets the semantic role of this argument regarding the verb.
		 * 
		 * @param semanticRole New semantic role.
		 */
		public void setSemanticRole(final Sense semanticRole) {
			this.semanticRole = semanticRole;
		}
		
		/**
		 * Sets the syntactic role of this argument in the sentence.
		 * 
		 * @param syntacticRole New syntactic role.
		 */
		public void setSyntacticRole(final String syntacticRole) {
			this.syntacticRole = syntacticRole;
		}
		
		/**
		 * Sets the Word that is assigned to this argument.
		 * 
		 * @param word New Word to assign.
		 */
		public void setWord(final Word word) {
			this.word = word;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.syntacticRole + ": " + this.semanticRole + ": " + this.word;
		}
		
	}
	
	/**
	 * List of arguments of this verb.
	 */
	private List<VerbArgument> arguments = new ArrayList<VerbArgument>();
	
	/**
	 * Mark for comments in the specification regarding this VerbFrame.
	 */
	private Mark mark;
	
	/**
	 * Name of the ontology which created this VerbFrame.
	 */
	private final String ontologyName;
	
	/**
	 * Word which is represented by this verb.
	 */
	private final Word word;
	
	/**
	 * Constructor.
	 * 
	 * @param ontologyName Name of the ontology which created this VerbFrame.
	 * @param verb Word which is represented by this verb.
	 */
	public VerbFrame(final String ontologyName, final Word verb) {
		this.ontologyName = ontologyName;
		this.word = verb;
	}
	
	/**
	 * Returns the list of arguments of this verb.
	 * 
	 * @return List of arguments.
	 */
	public List<VerbArgument> getArguments() {
		return this.arguments;
	}
	
	/**
	 * Returns the Mark for comments in the specification regarding this VerbFrame.
	 * 
	 * @return Mark or null if there is no mark.
	 */
	public Mark getMark() {
		return this.mark;
	}
	
	/**
	 * Returns the name of the ontology which created this VerbFrame.
	 * 
	 * @return Name of the ontology.
	 */
	public String getOntologyName() {
		return this.ontologyName;
	}
	
	/**
	 * Returns the word which is represented by this verb.
	 * 
	 * @return word.
	 */
	public Word getWord() {
		return this.word;
	}
	
	/**
	 * Sets the list of arguments of this verb.
	 * 
	 * @param arguments New list of arguments.
	 */
	public void setArguments(final List<VerbArgument> arguments) {
		this.arguments = arguments;
	}
	
	/**
	 * Sets the Mark for comments in the specification regarding this VerbFrame.
	 * 
	 * @param mark New Mark (null for no mark).
	 */
	public void setMark(final Mark mark) {
		this.mark = mark;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.word.getBaseFormIfPresent() + "\nSense: " + this.word.getOntologyConstant(this.getOntologyName())
				+ "\nArguments: " + this.arguments + "\nMark: " + this.mark;
	}
	
}
