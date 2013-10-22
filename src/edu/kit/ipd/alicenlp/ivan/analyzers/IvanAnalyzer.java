/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.util.List;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.Annotator.Requirement;
import edu.stanford.nlp.util.TypesafeMap;

/** This is the base class for AliceNLP classifiers used in Ivan. It contains the requirements constants for the pipeline extension.
 * @author Jonny
 *
 */
public abstract class IvanAnalyzer implements Annotator  
{
	/** This list contains such noun phrases which refer to one or more locations. 
	 * 
	 * @author Jonny
	 *
	 */
	public abstract class Locations implements TypesafeMap.Key<Locations>, List<String>
	{
		
	}

	/** Classification refers to the meaning of the sentence and its role within the description of the Alice word.
	 * 
	 * @author Jonny
	 *
	 */
	public enum Classification implements TypesafeMap.Key<Classification> 
	{
		/**
		 * A SetupDescription defines the initial state of the scene, like the position of an entity.
		 */
		SetupDescription,
		/**
		 * An ActionDescription relates to an entity on the scene doing something specific, like a person taking a step.
		 */
		ActionDescription,
		/**
		 * An EventDescription relates to an observable event in the scene, like a ball dropping onto the scene from out of view.
		 */
		EventDescription,
		/**
		 * This sentence describes that time is passing.
		 */
		TimeDescription,
		/**
		 * This sentence contains something that we don't want users to write. (The ErrorDescription is not intended to replace exceptions.)
		 */
		ErrorDescription
	}

	/** This requirement implies that setup sentences are tagged.
	 * Setup sentences are such sentences which relate to the initial state of the Alice world. Setup sentences are also "static" sentences.
	 */
	public static final Requirement SETUP_CLASSIFICATION = new Requirement("alicesetup");

	/** This requirement implies that actions are tagged.
	 * Actions are descriptions where an agent performs an action, such as a person raising a hand. 
	 */
	public static final Requirement ACTION_CLASSIFICATION = new Requirement("aliceaction");
	
	/** This requirement implies that events are tagged.
	 * Events are such sentences which imply a change of state without an agent, such as a star appearing in the night sky or the sun rising.
	 */
	public static final Requirement EVENT_CLASSIFICATION = new Requirement("aliceevent");
	
	/** This requirement implies that sentences with time constraints are tagged.
	 * Time sentences usually indicate that time is passing.
	 */
	public static final Requirement TIME_CLASSIFICATION = new Requirement("alicetime");
	
	/** This requirement implies that declarations are tagged.
	 * Declaring sentences assert that some entity exists. These tags should provide some way to retrieve the declared entities.
	 */
	public static final Requirement DECLARATION_REQUIREMENT = new Requirement("alicedecl");
	
	/** This requirement implies that locations are tagged.
	 * The tags should provide some way to retrieve the locations.
	 */
	public static final Requirement LOCATION_REQUIREMENT = new Requirement("alicelocation");
	
	/** This requirement implies that direction are tagged.
	 * The tags should provide some way to retrieve the direction.
	 */
	public static final Requirement DIRECTION_REQUIREMENT = new Requirement("alicedirection");
	
}
