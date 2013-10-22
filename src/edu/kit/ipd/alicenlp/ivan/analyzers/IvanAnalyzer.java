/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.analyzers;

import edu.stanford.nlp.pipeline.Annotator.Requirement;
import edu.stanford.nlp.util.TypesafeMap;

/** This is the base class for AliceNLP classifiers used in Ivan. It contains the requirements constants for the pipeline extension.
 * @author Jonny
 *
 */
public abstract class IvanAnalyzer {

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
}
