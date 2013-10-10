/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.Label;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/** This is a special JXTaskPaneContainer, which can display errors and warnings that occur in IVAN.
 * It provides an cues to the UI where to render errors (line numbers or character offsets),
 * actions for fixing and ignoring errors, and human-readable state for easy instrumentation.
 * 
 * JXTaskPaneContainer provides an elegant view to display a list of tasks ordered by groups (org.jdesktop.swingx.JXTaskPanes). 
 * @author Jonny
 *
 */
@SuppressWarnings("serial")
public class IvanErrorsTaskPaneContainer extends JXTaskPaneContainer {

	/**
	 * @author Jonny
	 *
	 */
	public class IvanErrorInstance {
		
	}

	/** This class represents a catergory if errors.
	 * It contains instances of this error.
	 * @author Jonny
	 *
	 */
	public class IvanCategory 
	{
		public String Name;
		public String Description;
	}
	
	enum ErrorCategory {
		Warning,
		Error
	}


	/**
	 */
	public IvanErrorsTaskPaneContainer() {
	}

	@Override
	public void add(JXTaskPane group) {
		// TODO Auto-generated method stub
		super.add(group);
	}
	
	@Override
	public void remove(JXTaskPane group) {
		// TODO Auto-generated method stub
		super.remove(group);
	}
	
	/** This method allows printing all the currently displayed errors and warnings in a readable, diff-able manner 
	 * 
	 */
	@Override
	public String toString() {
		// TODO print something that shows which stuff is currently being displayed
//		for(Category c : this.errors.entrySet())
//		{
//			System.out.println(c.Name);
//			for(Instance i : c)
//			{
//				System.out.println("\t- " + i.toString());
//			}
//		}
//		System.out.println();#
		return super.toString();
	}

	public void createCategory(String title, String description) {
		JXTaskPane pane = new JXTaskPane();
		pane.setTitle(title);
		pane.add(new Label(description));
		Map<String, JXTaskPane> mypanes = null;
		mypanes.put(title, pane);
	}

	public void createProblem(String string, String string2, int i, int j) {
		// TODO Auto-generated method stub
		
	}
}
