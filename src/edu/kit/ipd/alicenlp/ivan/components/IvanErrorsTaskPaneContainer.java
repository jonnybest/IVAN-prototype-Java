/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.BorderLayout; 
import java.awt.Dimension; 
import java.awt.Font; 
import java.awt.Label;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JEditorPane; 
import javax.swing.JFrame; 
import javax.swing.JPanel; 
import javax.swing.JScrollPane; 
import javax.swing.SwingUtilities; 
import javax.swing.UIManager; 
import javax.swing.text.html.HTMLDocument; 
 



import org.jdesktop.application.Action;
import org.jdesktop.application.Application; 
import org.jdesktop.application.ApplicationActionMap; 
import org.jdesktop.swingx.JXLabel;
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

	public class IvanErrorInstance {

		final public String Category;
		final public List<CodePoint> Codepoints;

		public IvanErrorInstance(String category, List<CodePoint> codepoints)  
		{
			Category = category;
			Codepoints = codepoints;
		}
		
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
	}

	private Map<String, JXTaskPane> mypanes = new TreeMap<String, JXTaskPane>();
	private Set<IvanErrorInstance> bagofProblems = new HashSet<IvanErrorInstance>();
	final private Font errorInfoFont = new Font("Calibri", 0, 11);

	public class CodePoint extends Tuple<Integer, Integer>{

		public CodePoint(Integer x, Integer y) {
			super(x, y);		}
		
	}
	
	public class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
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

	/** Create a category of errors which should be presented to the user in a single taskpane. 
	 * This method should only be called once per runtime and per category. 
	 * 
	 * @param title
	 * @param description
	 */
	public void createCategory(String title, String description) {
		JXTaskPane pane = new JXTaskPane();
		pane.setTitle(title);
		JXLabel lbl = new JXLabel(description);
		lbl.setLineWrap(true);
		
		lbl.setFont(errorInfoFont);
		pane.add(lbl);		
		mypanes.put(title, pane);
	}

	public boolean createProblem(String category, String errormsg, List<CodePoint> codepoints) {
		JXTaskPane tsk = mypanes.get(category);
		if(tsk != null)
		{
			IvanErrorInstance error = new IvanErrorInstance(category, codepoints);
			boolean present = this.bagofProblems.contains(error);
			if(!present){
				bagofProblems.add(error);
				createQuickfixes(error);
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
	
	private void createQuickfixes(IvanErrorInstance error) {
		IvanErrorsTaskPaneContainer tpc = this;
		JXTaskPane tsk = mypanes.get(error.Category);
		
		// createTaskPaneDemo()
		// "System" GROUP 
        JXTaskPane systemGroup = tsk; 
        systemGroup.setName("systemGroup"); 
        tpc.add(systemGroup); 
        
        // bind()
        ApplicationActionMap map = Application.getInstance().getContext().getActionMap(this); 
        
        systemGroup.add(map.get("email")); 
        systemGroup.add(map.get("delete")); 
	}
	
    @Action 
    public void email() { } 
     
    @Action 
    public void delete() { } 

	public boolean createProblem(String category, String errormsg, CodePoint codepoint) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{codepoint}));
	}

	public boolean createProblem(String category, String errormsg, int x, int y) {
		return createProblem(category, errormsg, new CodePoint(x, y));
	}
}
