/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Font; 
import java.awt.event.ActionEvent;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

import org.jdesktop.application.Application; 
import org.jdesktop.application.ApplicationActionMap; 
import org.jdesktop.application.Task.BlockingScope;
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
		final public String Quickfix;
		final public String Problem;		

		public IvanErrorInstance(String category, List<CodePoint> codepoints, String qf, String prob)  
		{
			Category = category;
			Codepoints = codepoints;
			Quickfix = qf;
			Problem = prob;			
		}
		
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
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
		final String nl = "\n";
		StringBuilder sb = new StringBuilder();		
		
		for (Entry<String, JXTaskPane> pane : mypanes.entrySet()) {
			JXTaskPane pn = pane.getValue();
			ActionMap am = Application.getInstance().getContext().getActionMap(pn);
			if(am.size() == 0)
				continue;
			sb.append(pane.getKey());
			sb.append(nl);
			for (Object key : am.keys()) {
				Action ac = am.get(key);
				IvanErrorInstance err = (IvanErrorInstance) ac.getValue("error");
				
				sb.append("\t-");				
				sb.append(err.toString());
				sb.append(nl);
			}
		}
		return sb.toString();
		//return super.toString();
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
		if(description != null){
			JXLabel lbl = new JXLabel(description);
			lbl.setLineWrap(true);
		
			lbl.setFont(errorInfoFont);
			pane.add(lbl);
		}
		mypanes.put(title, pane);
	}

	public boolean createProblem(String category, String errormsg, List<CodePoint> codepoints) {
		JXTaskPane tsk = mypanes.get(category);
		if(tsk != null)
		{
			IvanErrorInstance error = new IvanErrorInstance(category, codepoints, "qf-ignore", errormsg);
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
	
	private void createQuickfixes(final IvanErrorInstance error) {
		IvanErrorsTaskPaneContainer tpc = this;
		JXTaskPane tsk = mypanes.get(error.Category);
		
		// createTaskPaneDemo()
		// "System" GROUP 
		
        tsk.setName(error.Quickfix); 
        tpc.add(tsk); 
        
        // bind()
        ApplicationActionMap map = Application.getInstance().getContext().getActionMap(tsk); 
        
//        Action IvanAction = new Action(){
//        	boolean isEnabled = true;
//        	boolean isSelected = false;
//        	
//        	@Override
//        	public Class<? extends Annotation> annotationType() {
//        		// TODO Auto-generated method stub
//        		return Action.class;
//        	}
//
//			@Override
//			public BlockingScope block() {
//				return BlockingScope.NONE;
//			}
//
//			@Override
//			public String enabledProperty() {
//				return "isEnabled";
//			}
//
//			@Override
//			public String name() {				
//				return error.Category;
//			}
//
//			@Override
//			public String selectedProperty() {
//				return "isSelected";
//			}
//        };
        
        //String name = error.Category;
        String quickfix = "Ignore problem in " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y;
        
        javax.swing.Action myAction = new AbstractAction(quickfix) {        	
        	@Override
			public void actionPerformed(ActionEvent e) {
				//String name = (String) getValue(SHORT_DESCRIPTION);
				System.out.println("This action's error is " + getValue("error"));
			}
		};
		myAction.putValue("error", error);
		
		
		tsk.add(myAction);
        map.put("qf-ignore", myAction);
        
	}
	
    @org.jdesktop.application.Action
    public void email() {
    	System.out.println("Email sent!");
    } 
     
    @org.jdesktop.application.Action 
    public void delete() { } 

	public boolean createProblem(String category, String errormsg, CodePoint codepoint) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{codepoint}));
	}

	public boolean createProblem(String category, String errormsg, int x, int y) {
		return createProblem(category, errormsg, new CodePoint(x, y));
	}
	
}
