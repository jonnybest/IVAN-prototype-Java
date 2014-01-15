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
	, WORDNET // represents a failed wordnet lookup
	/**  
	 * Graph errors are due to missing or misbehaving dependency information in Stanford CoreNLP.
	 * Since they coincide with the parse tree, this error also signals errors which occur in an
	 * earlier stage of syntax processing (like building the parse tree with Penn tags).
	 */
	, GRAPH // represents problems with Stanford CoreNlp's trees and depencendies
	/** 
	 * This error type describes issues with the text's style. Ivan will not process it.
	 */
	, STYLE // whatever the text was, it's not being processed by Ivan
	/** 
	 * This error type indicates incomplete entity description. The entity in question is missing 
	 * a location.
	 */
	, LOCATION // The entity in question does not have any location.
	/** 
	 * This error type indicates incomplete entity description. The entity in question is missing
	 * a direction.
	 */
	, DIRECTION // The entity in question does not have any direction
	
}
