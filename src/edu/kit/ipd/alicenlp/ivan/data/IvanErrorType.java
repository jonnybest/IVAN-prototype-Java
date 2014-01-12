/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

/** A list of error types that are recognised by the classifier. 
 * 
 * @author Jonny
 *
 */
public enum IvanErrorType {
	
	/**
	 * This is a generic error indication.
	 */
	UNKNOWN // generic error type
	
	/**
	 * This error means that two distinct entities share a synonym.
	 */
	, SYNONYMS // represents an error found by EntitiesSynonymsErrorRule
	
	/**
	 * This error means that a pronoun (or maybe a name) could not be resolved to an entity. 
	 */
	, COREFERENCE // represents a coreference error found by DeclarationPositionFinder
	/**
	 * This error means that the analyzer tried to process this sentence and could not proceed,
	 * because a word was missing in WordNET.
	 */
	, WNWMISSING // represents a failed wordnet lookup
	
}
