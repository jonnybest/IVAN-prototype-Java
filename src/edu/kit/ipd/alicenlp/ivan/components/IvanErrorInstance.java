package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.text.Caret;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

/** This is the error representation for the Errors Task Pane
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
	 * This string identifies the quick fix for this problem (like "qf-add[boy, girl]") 
	 */
	final public String Quickfix;		
	/**
	 * This is the sentence which is cause for the error.
	 */
	final public String Problem;
	/** 
	 * These components are the display things which belong to this error 
	 */
	public final LinkedList<Component> Components = new LinkedList<Component>();
	/**
	 * This caret selects all the text for this issue.  
	 */
	public Caret StandardCaret;
	
	/** Creates a new specific error
	 * 
	 * @param category Headline for this type of error
	 * @param codepoints Where this error occurs (spans in the text)
	 * @param qf The identifier for the quick fix, if available
	 * @param prob The problematic sentence in verbatim
	 */
	public IvanErrorInstance(final String category, final List<CodePoint> codepoints, final String qf, final String prob)  
	{
		Category = category;
		Codepoints = codepoints;
		Quickfix = qf;
		Problem = prob;			
		Reference = null;
	}
	/** Creates a new specific error
	 * 
	 * @param category Headline for this type of error
	 * @param codepoints Where this error occurs (spans in the text)
	 * @param qf The identifier for the quick fix, if available
	 * @param prob The problematic sentence in verbatim
	 * @param refs Problematic words or short passages from the text
	 */
	public IvanErrorInstance(String category, List<CodePoint> codepoints, String qf, String prob, String[] refs)  
	{
		Category = category;
		Codepoints = codepoints;
		Quickfix = qf;
		Problem = prob;
		Reference = refs;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IvanErrorInstance)
		{
			if (StandardCaret == null) {
				// if the caret is null, this error has not been displayed to the user (yet)
				IvanErrorInstance otherError = (IvanErrorInstance) obj;
				if (Category.equals(otherError.Category)
						&& Problem.equals(otherError.Problem)
						&& Codepoints.size() == otherError.Codepoints
								.size()) {
					for (CodePoint cp : Codepoints) {
						if (!otherError.Codepoints.contains(cp)) {
							return false;
						}
					}
					return true;
				}
			}
			else {
				// if the caret is present, this error has been displayed to the user and bounds may have changed since.
				// compare last bounds only
				IvanErrorInstance otherError = (IvanErrorInstance) obj;
				if (Category.equals(otherError.Category)
						&& Problem.equals(otherError.Problem)){
					CodePoint oErrlastCodepoint = otherError.Codepoints.get(otherError.Codepoints.size()-1);
					CodePoint tErrCp = new CodePoint(StandardCaret.getDot(), StandardCaret.getMark());
					return tErrCp.equals(oErrlastCodepoint);
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder outstr = new StringBuilder();
		for (CodePoint cp : Codepoints) {
			outstr.append(cp.x + "," +cp.y);
			outstr.append("|");
		}
		outstr.deleteCharAt(outstr.length()-1);
		outstr.append("  ");
		outstr.append(Quickfix);
		
		if(Problem != null){			
			outstr.append("\t");
			outstr.append("("+ Problem + ")");
			outstr.append("\n");
		}
		
		return outstr.toString();
	}
	@Override
	public int hashCode() {			
		return Category.hashCode() ^ toString().intern().hashCode();
	}
	
	/** Component
	 * 
	 * @param component
	 */
	public void addComponent(Component co) {
		Components.add(co);
	}
}