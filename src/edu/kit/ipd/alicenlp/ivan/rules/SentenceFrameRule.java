/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.logging.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreeTransformer;
import edu.stanford.nlp.util.CoreMap;

/** This rule calculates whether a given sentence frame is applicable to a given sentence. 
 * @author Jonny
 *
 */
public class SentenceFrameRule implements ISentenceRule 
{
	private String[] frames;
	protected Logger log = Logger.getLogger(getClass().getName());
	/**
	 * 
	 */
	public SentenceFrameRule(String sentenceframe) {
		frames = new String[]{sentenceframe};
	}

	public SentenceFrameRule(String[] verbFrames) {
		frames = verbFrames;
	}

	/**
	 * @param tokens
	 * @param tree 
	 */
	private void walkTree(String[] tokens, Tree tree) {
		Tree w = tree.skipRoot(); // "w" stands for "work tree"
		int i = 0;
		while(i < tokens.length)
		{
			switch(tokens[i])
			{
				case "Something":
					// generic NP 
					break;
				case "Somebody":
					break;
				case "It":
					break;
				case "Somebody's":
					// unique: Somebody's (body part) ----s 
					break;
				case "----s":
					break;
				case "is":
					// "is ----ing"
					// take next token, too
					break;
				case "something":
					break;
				case "somebody":
					break;
				case "that":
					break;
				default:
					break;
			}
			// done. next:
			i++;
		}
	}

	private String[] tokenize(String words) {
		return words.split(" ");
	}

	@Override
	public boolean apply(CoreMap sentence) {
		Tree tree = sentence.get(TreeAnnotation.class);
		if(tree == null)
		{
			System.err.println("Tree annotations are missing.");
			return false;
		}
		tree.transform(new TreeTransformer() {
			
			@Override
			public Tree transformTree(Tree t) {
				log.info(t.toString());
				return t;
			}
		});
		walkTree(tokenize(frames[0]), tree);
//		walkTree(tokens, tree);
		return true;
	}

	public CoreLabel getVerb() {
		// TODO Auto-generated method stub
		return null;
	}

}
