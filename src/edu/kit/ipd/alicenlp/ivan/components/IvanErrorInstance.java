package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import javax.swing.text.Caret;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

/**
 * This is the error representation for the Errors Task Pane
 * 
 * @author Jonny
 * 
 */
public class IvanErrorInstance {
	Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Related text from the document (like an offending preposition)
	 */
	final public String[] Reference;
	/**
	 * The Category is defines the heading under which this error is displayed
	 */
	final public String Category;
	/**
	 * The code points are the places related to this specific error instance.
	 */
	final public List<CodePoint> Codepoints;
	
	/**
	 * This is the sentence which is cause for the error.
	 */
	final public String Problem;
	/**
	 * These components are the display things which belong to this error
	 */
	public final LinkedList<Component> Components = new LinkedList<Component>();

	/**
	 * Creates a new specific error
	 * 
	 * @param category
	 *            Headline for this type of error
	 * @param codepoints
	 *            Where this error occurs (spans in the text)
	 * @param qf
	 *            The identifier for the quick fix, if available
	 * @param prob
	 *            The problematic sentence in verbatim
	 */
	public IvanErrorInstance(final String category,
			final List<CodePoint> codepoints, final String qf, final String prob) {
		Category = category;
		Codepoints = codepoints;

		Problem = prob;
		Reference = null;
	}

	/**
	 * Creates a new specific error
	 * 
	 * @param category
	 *            Headline for this type of error
	 * @param codepoints
	 *            Where this error occurs (spans in the text)
	 * @param qf
	 *            The identifier for the quick fix, if available
	 * @param prob
	 *            The problematic sentence in verbatim
	 * @param refs
	 *            Problematic words or short passages from the text
	 */
	public IvanErrorInstance(String category, List<CodePoint> codepoints,
			String qf, String prob, String[] refs) {
		Category = category;
		Codepoints = codepoints;

		Problem = prob;
		Reference = refs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IvanErrorInstance) {

			IvanErrorInstance otherError = (IvanErrorInstance) obj;
			if (Category.equals(otherError.Category)
					&& Problem.equals(otherError.Problem)
					&& Codepoints.size() == otherError.Codepoints.size()) {
				
				return true;
			}

		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder outstr = new StringBuilder();
		CodePoint cp = Codepoints.get(0);		
		outstr.append(String.format("%3d,%3d", cp.x, cp.y));

		if (Problem != null) {
			outstr.append("\t");
			outstr.append("(" + Problem + ")");
			outstr.append("\n");
		}

		return outstr.toString();
	}

	@Override
	public int hashCode() {
		return Category.hashCode() ^ toString().intern().hashCode();
	}

	/**
	 * Component
	 * 
	 * @param component
	 */
	public void addComponent(Component co) {
		Components.add(co);
	}
}