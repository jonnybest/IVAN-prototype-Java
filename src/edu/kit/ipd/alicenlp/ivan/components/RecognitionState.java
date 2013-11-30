/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;



import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.stanford.nlp.util.Pair;

/** This is a glorified printer which translates the internal state 
 * of the recognition programs into human readable descriptions.
 * 
 * @author Jonny
 *
 */
public class RecognitionState {

	char separator = ' ';
	//char separator = '\n';
	
	private InitialState declarations;

	public RecognitionState()
	{
		// default is alright
	}
	
	public RecognitionState(InitialState entitiesState) {
		declarations = entitiesState;
	}

	@Override
	public String toString() {
		if(declarations == null)
			return super.toString();
		
		// do some real work
		/** Process DECLARATIONS part
		 * 
		 */
		StringBuilder sb = new StringBuilder();
		
		// 1. talk about the names
		if(!declarations.hasNames())
		{
			sb.append("None of the entities in this description have a name.");
			sep(sb);
		}	
		else {
			@SuppressWarnings("unused")
			List<Pair<String, String>> names = declarations.getEntityNames();
			// todo: build
		}
		
		// 2. list the declarations
		if(declarations.size() == 0)
		{
			sb.append("No entities have been declared, yet.");
		}
		else {
			/* In order to write a nice text, you need a stackview on the entites:
			 *   when the stack is empty, put a period
			 *   when the stack has two items left, write "A $entity1 is $pos1 [and $pos2], $dir; and a $entity2 is $pos3 [and $pos4], $dir."
			 *   when the stack has more items left, write "A $entity is $pos1 [and $pos2], $dir."
			 *   TODO: I need to write different sentences, depending on whether they already have a name ("a cat"/"Fluffy") or whether the entity starts with a vowel ("a"/"an")   
			*/
			// make stack
			Deque<EntityInfo> entities = new ArrayDeque<EntityInfo>();
			entities.addAll(declarations);
			// loop
			while(!entities.isEmpty())
			{
				if(entities.size() > 2){
					EntityInfo entity = entities.remove();
					sb.append("A " + entity.getEntity() + " is " + entity.getLocation() + ", " + entity.getDirection() + ".");
					sep(sb);
				}
				else {
					appendSentence(entities.remove(), entities.remove(), sb);
					// no break, because the queue should be empty now anyway
				}
			}
		}
		
		return sb.toString();
	}

	private static void appendSentence(EntityInfo first, EntityInfo second,
			StringBuilder sb) {
		// first
		sb.append("A " + first.getEntity() + " is " + first.getLocation() + ", " + first.getDirection());
		// separate
		sb.append("; and ");
		// second
		sb.append("a " + second.getEntity() + " is " + second.getLocation() + ", " + second.getDirection());
		// finalize
		sb.append(".");
	}

	/** this function separates two sentences
	 * 
	 * @param sb
	 */
	private void sep(StringBuilder sb) {
		sb.append(separator);
	}

	protected void setDeclarations(InitialState currentState) {
		this.declarations = currentState;		
	}
}
