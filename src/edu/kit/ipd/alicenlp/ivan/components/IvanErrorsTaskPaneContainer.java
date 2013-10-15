/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JXTextPane;
import javax.swing.LineNumbersTextPane;
import javax.swing.text.BadLocationException;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;
import edu.kit.ipd.alicenlp.ivan.components.IvanErrorsTaskPaneContainer.IvanErrorInstance;

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

	private static final String QF_ERROR = "error";
	private static final String QF_NAME = "qf-name";

	private final class CheckSentencesMetaAction extends AbstractAction {
		private CheckSentencesMetaAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("Running pipeline");
			SwingWindow.processText();
		}

		@Override
		public String toString() {
			String qf_shorthand = (String) getValue(QF_NAME);
			
			StringBuilder outstr = new StringBuilder();
			
			//outstr.append("\t");	    			
			outstr.append(qf_shorthand);
			
			return outstr.toString();
		}
	}

	private final class RestoreAllMetaAction extends AbstractAction {
		private RestoreAllMetaAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("Restoring error display");
		}

		@Override
		public String toString() {
			String qf_shorthand = (String) getValue(QF_NAME);
			
			StringBuilder outstr = new StringBuilder();
			
			//outstr.append("\t");	    			
			outstr.append(qf_shorthand);
			
			return outstr.toString();
		}
	}

	private final class IgnoreAllMetaAction extends AbstractAction {
		private IgnoreAllMetaAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("Ignoring all currently displayed errors");
		}

		@Override
		public String toString() {
			String qf_shorthand = (String) getValue(QF_NAME);
			
			StringBuilder outstr = new StringBuilder();
			
			//outstr.append("\t");	    			
			outstr.append(qf_shorthand);
			
			return outstr.toString();
		}
	}

	private final class IgnoreProblemAction extends AbstractAction {
		private final IvanErrorInstance error;
		private final IvanErrorsTaskPaneContainer tp;

		private IgnoreProblemAction(String name, IvanErrorInstance error,
				IvanErrorsTaskPaneContainer tp) {
			super(name);
			this.error = error;
			this.tp = tp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			//error = (IvanErrorInstance) getValue(QF_ERROR);
			System.out.println("Ignoring " + error);
			// get this category panel
			JXTaskPane panel = mypanes.get(error.Category);
			// get the actions configured for this panel
			ApplicationActionMap map = Application.getInstance().getContext().getActionMap(panel);
			List<Action> keepme = new LinkedList<Action>();
			for (Object key : map.keys()) {
				Action otherQuickfix = map.get(key);
				Object otherError = (IvanErrorInstance) otherQuickfix.getValue(QF_ERROR);
				if(!error.equals(otherError))
				{
					System.out.println("Saving this one for later.");
					keepme.add(otherQuickfix);
				}
				else {
					map.remove(key); // throw it away
				}
			}
			panel.removeAll();
			for (Action action : keepme) {
				panel.add(action);
			}
			ignoredProblems.add(error);
			System.out.println("This action's error is " + getValue(QF_ERROR));
			System.out.println(tp.toString());
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	private final class AddLocationAction extends AbstractAction {
		final private IvanErrorInstance myerror;
		private List<String> stubs = new ArrayList<String>();

		private AddLocationAction(String name, IvanErrorInstance error2) {
			super(name);
			this.myerror = error2;
			stubs.addAll(Arrays.asList(new String[]{
				" is in the left front.",
				" is in the right front.",
				" is in the background to the left."}));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("I'm adding a location.");
			String[] unlocatedNames = myerror.Reference;
			/* Create location sentences.
			 * 1. find the insertion point. The insertion point is somewhere to the right of the last cue.
			 * 2. set the caret to the insertion point
			 * 3. for each Name without location, insert a sentence. (unlocatedNames)
			 *   a) build a sentence: Name + Stub. Then insert it.
			 *   b) if you run out of stubs, create Name + " is on the … side." and then select the three dots.
			 **/
			// focus is important, so the user can readily start typing after clicking
			txtEditor.requestFocusInWindow();
			// get insertion point
			int insertionpoint = findInsertionPoint(myerror.Codepoints);
			// set the caret
			txtEditor.setCaretPosition(insertionpoint);
			for (int i = 0; i < unlocatedNames.length; i++) {
				String sentence;
				if(stubs.size() > 0){
					// build a sentence from a stub
					sentence = "\n" + unlocatedNames[i] + stubs.get(0) + " ";
					stubs.remove(0);
					// finalise last sentence with a period, if not present
					try {
						String text = txtEditor.getText(insertionpoint - 1, 1);
						if(!text.equals("."))
						{
							sentence = "." + sentence;
						}
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					// insert the sentence
					txtEditor.replaceSelection(sentence);
				}
				else {
					sentence = "\n" + unlocatedNames[i] + " is on the … side. ";
					// finalise last sentence with a period, if not present
					try {
						String text = txtEditor.getText(insertionpoint - 1, 1);
						if(!text.equals("."))
						{
							sentence = "." + sentence;
						}
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					// insert the sentence
					txtEditor.replaceSelection(sentence);
					// select the … 
					int dotspoint = txtEditor.getText().indexOf("…", insertionpoint);
					txtEditor.setCaretPosition(dotspoint);
					txtEditor.moveCaretPosition(dotspoint + 1);
				}
				
				
				
			}
			System.out.println("This action's error is " + getValue(QF_ERROR));
		}

		/** Finds the position where a new sentence can be inserted.
		 * More precisely it returns the character index after the next sentence termination mark. 
		 * @param codepoints
		 * @return
		 */
		private int findInsertionPoint(List<CodePoint> codepoints) {
			// find the last character index for this error
			int lastcp = 0;
			for (CodePoint po : codepoints) {
				if(po.y > lastcp)
					lastcp = po.y;
			}
			// text shortcut
			String txt = txtEditor.getText();
			// get the maximum index for this text
			int maxlength = txt.length();
			// find the index of the next Period, Question mark or exclamation mark
			int lastPer = txt.indexOf(".", lastcp);
			int lastQue = txt.indexOf("?", lastcp);
			int lastExc = txt.indexOf("!", lastcp);
			// figure out which of the three occurs the earliest
			int lastMark = maxlength;
			if(lastPer > 0)
				lastMark = Math.min(lastPer, lastMark) + 1;
			if(lastQue > 0)
				lastMark = Math.min(lastQue, lastMark) + 1;
			if(lastExc > 0)
				lastMark = Math.min(lastExc, lastMark) + 1;
			// returns either the earliest mark or EOF if no mark is present
			return lastMark;
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	private final class DeleteSentenceAction extends AbstractAction {
		private final IvanErrorInstance error;

		private DeleteSentenceAction(String name, IvanErrorInstance error) {
			super(name);
			this.error = error;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			//System.out.println("I'm deleting this sentence.");
			/* 1. figure out the sentence bounds (Periods, Question Marks, and Exclamation Points)
			 *   a) search right for a sentence punctuation
			 *   b) search left for a sentence punctuation
			 * 2. delete everything in between left bound and right bound
			 **/
			int lb = searchLeftBound(error.Codepoints);
			int rb = searchRightBound(error.Codepoints);
			// select the improper sentence
//	        		txtEditor.setSelectionStart(lb);
//	        		txtEditor.setSelectionEnd(rb);
			txtEditor.setCaretPosition(lb);
			txtEditor.moveCaretPosition(rb);	        		
			// delete it. this is undoable
			txtEditor.replaceSelection("");
			// get the focus so user can start editing right away
			txtEditor.requestFocusInWindow();
			//System.out.println("This action's error is " + getValue(QF_ERROR));
		}

		private int searchRightBound(List<CodePoint> codepoints) {
			int maxEndPoint = codepoints.get(0).y;
			for (CodePoint cp : codepoints) {
				maxEndPoint = Math.max(cp.y, maxEndPoint);
			}
			assert maxEndPoint > 0; // assertion to make sure that all the codepoints were valid (greater 0)
			
			final int contentlength = txtEditor.getText().length();
			int lastMark = contentlength;
			int maxlen = lastMark-maxEndPoint;
			if (maxlen > 0) {
				int lastPeriod;
				try {
					lastPeriod = txtEditor.getText(maxEndPoint, maxlen)
							.indexOf(".");
					if(lastPeriod > 0)
						lastMark = Math.min(lastMark, lastPeriod);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				int lastEx;
				try {
					lastEx = txtEditor.getText(maxEndPoint, maxlen)
							.indexOf("!");
					if(lastEx > 0)
						lastMark = Math.min(lastMark, lastEx);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				int lastQues;
				try {
					lastQues = txtEditor.getText(maxEndPoint, maxlen)
							.indexOf("?");
					if(lastQues > 0)
						lastMark = Math.min(lastMark, lastQues);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			//int lastNL = txtEditor.getText().lastIndexOf("\n", minStartingPoint);
			if(lastMark < contentlength)
			{
				// we have found a sentence separation mark to the right, so return its position
				return maxEndPoint + lastMark + 1;
			}
			else
			{
				// we have not found any sentence separator to the right, so return EOF
				return contentlength;
			}
		}

		private int searchLeftBound(List<CodePoint> codepoints) {
			// the first hint we have is the left point of the first "codepoint"
			int minStartingPoint = codepoints.get(0).x;
			// now try finding the smallest known left codepoint
			for (CodePoint cp : codepoints) {
				minStartingPoint = Math.min(cp.x, minStartingPoint);
			}
			assert minStartingPoint > 0; // assertion to make sure that all the codepoints were valid (greater 0)
			
			// we may have to start all the way to the left
			int lastMark = 0;
			// search to the right for a period
			int lastPeriod = txtEditor.getText().lastIndexOf(".", minStartingPoint);
			lastMark = Math.max(lastMark, lastPeriod);
			int lastEx = txtEditor.getText().lastIndexOf("!", minStartingPoint);
			lastMark = Math.max(lastMark, lastEx);
			int lastQues = txtEditor.getText().lastIndexOf("?", minStartingPoint);
			lastMark = Math.max(lastMark, lastQues);
			//int lastNL = txtEditor.getText().lastIndexOf("\n", minStartingPoint);
			
			// this is either a 0 or greater
			minStartingPoint = lastMark;

			return minStartingPoint;
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	private final class AddDirectionAction extends AbstractAction {
		private AddDirectionAction(String name, IvanErrorInstance error) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("I'm adding a direction.");
			System.out.println("This action's error is " + getValue(QF_ERROR));
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	public class IvanErrorInstance {

		final public String[] Reference;
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
			Reference = null;
		}
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
				IvanErrorInstance otherError = (IvanErrorInstance) obj;
				if(Category.equals(otherError.Category)
						&& Problem.equals(otherError.Problem)
						&& Codepoints.size() == otherError.Codepoints.size())
				{
					for (CodePoint cp : Codepoints) {
						if(!otherError.Codepoints.contains(cp))
						{
							return false;
						}
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
	}

	private Map<String, JXTaskPane> mypanes = new TreeMap<String, JXTaskPane>();
	final private Font errorInfoFont = new Font("Calibri", 0, 11);
	private JXTextPane txtEditor = null;

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
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Tuple<?, ?>)
			{
				Tuple<?, ?> other = (Tuple<?, ?>) obj;
				if (x.equals(other.x) && y.equals(other.y))
					return true;
			}
			return false;
		}
	}

	private Set<IvanErrorInstance> ignoredProblems = new HashSet<IvanErrorsTaskPaneContainer.IvanErrorInstance>();
	private Set<IvanErrorInstance>  bagofProblems = new HashSet<IvanErrorsTaskPaneContainer.IvanErrorInstance>();
	

	/**
	 */
	public IvanErrorsTaskPaneContainer(JXTextPane editor) {
		txtEditor = editor;
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
				sb.append("\t");				
				sb.append(ac.toString());
				//sb.append(nl);
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

	public boolean createProblem(String category, String errormsg, List<CodePoint> codepoints, String[] references) {
		JXTaskPane tsk = mypanes.get(category);
		if(tsk != null)
		{
			IvanErrorInstance error = new IvanErrorInstance(category, codepoints, null, errormsg, references);
			boolean present = this.ignoredProblems.contains(error);
			if(!present){
				this.bagofProblems.add(error);
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

	    //this is just an annotation, not something you can run:
//      Action IvanAction = new Action(){

        
        //String name = error.Category;
        
        List<javax.swing.Action> myQuickfixesForThisError = createAvailableQuickfixes(error);
        
        for (Action act : myQuickfixesForThisError) {
        	tsk.add(act);
        	map.put(act.getValue(QF_NAME), act);			
		}
        
	}

	/** This method looks up available quickfixes in the quickfix map and creates appropriate actions
	 * @param error
	 * @return
	 */
	protected List<javax.swing.Action> createAvailableQuickfixes(
			final IvanErrorInstance error) {
		
		// this list will contain the availale fixes
		List<javax.swing.Action> myQuickfixesForThisError = new ArrayList<Action>();
		
		/* The DELETE_SENTENCE action */
		if(error.Category.equals("effect")) // for sentences without effect only
		{
			String displayDescription = "Delete sentence " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y;
	        javax.swing.Action myAction = new DeleteSentenceAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-delete");
			myQuickfixesForThisError.add(myAction);
		}
		
		/* The ADD_SENTENCE action */
		if(error.Category.equals("location")) /* LOCATION */
		{
			String[] references = error.Reference;
			String displayDescription = "Add a location after " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y;
	        javax.swing.Action myAction = new AddLocationAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-add"+ Arrays.toString(references));
			myQuickfixesForThisError.add(myAction);
		} 
		else if(error.Category.equals("direction")) /* DIRECTION */
		{ 
			String[] references = error.Reference;
			String displayDescription = "Add a direction after " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y;
	        javax.swing.Action myAction = new AddDirectionAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-add"+ Arrays.toString(references));
			myQuickfixesForThisError.add(myAction);
		}
				
		/* The IGNORE action is almost always available */
		if(!error.Category.equals("meta"))
		{
			// instance 
			final IvanErrorsTaskPaneContainer tp = this;
			// the description to display
	        String displayDescription = "Ignore problem in " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y;
	        
	        javax.swing.Action myAction = new IgnoreProblemAction(displayDescription, error, tp);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-ignore");
			myQuickfixesForThisError.add(myAction);
		}
		else { /** Create META actions */
			/* IGNORE ALL */
			{
				// the description to display
		        String displayDescription = "Ignore all current problems";	        
		        javax.swing.Action myAction = new IgnoreAllMetaAction(displayDescription);
				// set the shorthand notation for this qf
				myAction.putValue(QF_NAME, "mf-ignore-all");
				myQuickfixesForThisError.add(myAction);
			}
			/* RESET IGNORED */
			{
				// the description to display
		        String displayDescription = "Restore ignored problems";	        
		        javax.swing.Action myAction = new RestoreAllMetaAction(displayDescription);
				// set the shorthand notation for this qf
				myAction.putValue(QF_NAME, "mf-reset-ignore");
				myQuickfixesForThisError.add(myAction);
			}
			/* CHECK all sentences */
			{
				// the description to display
		        String displayDescription = "Check all sentences";	        
		        javax.swing.Action myAction = new CheckSentencesMetaAction(displayDescription);
				// set the shorthand notation for this qf
				myAction.putValue(QF_NAME, "mf-check");
				myQuickfixesForThisError.add(myAction);
			}
		}
		
		return myQuickfixesForThisError;
	}
//	
//    @org.jdesktop.application.Action
//    public void email() {
//    	System.out.println("Email sent!");
//    } 
//     
//    @org.jdesktop.application.Action 
//    public void delete() { } 

	public boolean createProblem(String category, String errormsg, CodePoint codepoint) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{codepoint}), null);
	}

	public boolean createProblem(String category, String errormsg, int x, int y) {
		return createProblem(category, errormsg, new CodePoint(x, y));
	}

	public boolean createProblem(String category, String errormsg, int i, int j,
			String[] references) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{new CodePoint(i, j)}), references);
	}

	/**
	 * @return
	 */
	protected String qfActionPrinter(Action action) {
		IvanErrorInstance err = (IvanErrorInstance) action.getValue(QF_ERROR);
		String qf_shorthand = (String) action.getValue(QF_NAME);
		
		// in case something goes wrong
		if(err == null)
		{
			System.err.println("IvanError instance was not set.");
			return super.toString();
		}
		
		StringBuilder outstr = new StringBuilder();
		for (CodePoint cp : err.Codepoints) {
			outstr.append(cp.x + "," +cp.y);
			outstr.append("|");
		}
		outstr.deleteCharAt(outstr.length()-1);
		outstr.append("  ");	    			
		outstr.append(qf_shorthand);
		
		if(err.Problem != null){			
			outstr.append("\t");
			outstr.append("("+ err.Problem + ")");
			outstr.append("\n");
		}
		
		return outstr.toString();
	}

	public void setEditor(LineNumbersTextPane txtEditor) {
		this.txtEditor = txtEditor;
	}
}
