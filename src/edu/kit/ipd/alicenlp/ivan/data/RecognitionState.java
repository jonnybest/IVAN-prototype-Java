/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;



import java.util.List;

import edu.stanford.nlp.util.Pair;

/**
 * @author Jonny
 *
 */
public class RecognitionState {

	char separator = ' ';
	//char separator = '\n';
	
	private InitialState declarations;

	@Override
	public String toString() {
		if(declarations == null)
			return super.toString();
		
		// do some real work
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
			
		}
		
		return sb.toString();
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
