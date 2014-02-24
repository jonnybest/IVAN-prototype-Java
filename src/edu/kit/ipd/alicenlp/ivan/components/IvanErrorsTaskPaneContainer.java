/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;
import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

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

	// headline constants
	/** This constant contains the headline for meta problems.
	 */
	private static final String CATEGORY_META = "meta";
	/** The label of a category headline */
	public static final String CATEGORY_DIRECTION = "direction";
	/** The label of a category headline */
	public static final String CATEGORY_LOCATION = "location";
	/** The label of a category headline */
	public static final String CATEGORY_EFFECT = "effect";
	/** The label of a category headline */
	public static final String CATEGORY_GRAMMAR = "grammar";
	/** The label of a category headline */
	public static final String CATEGORY_STYLE = "style";
	/** The label of a category headline */
	public static final String CATEGORY_AMBIGOUS = "ambigous";
	
	// quick fix constants (action keys)
	private static final String QF_ERROR = "error";
	private static final String QF_NAME = "qf-name";

	/** This action invokes the pipeline and checks the sentence
	 * 
	 * @author Jonny
	 *
	 */
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

	/** This action restores all previously ignored problems
	 * 
	 * @author Jonny
	 *
	 */
	private final class RestoreAllMetaAction extends AbstractAction {
		private RestoreAllMetaAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("Restoring error display");
			// TODO: implement RestoreAllMetaAction
			ignoredProblems.clear();
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

	/** This action ignores all currently displayed problems and clears the panel 
	 * 
	 * @author Jonny
	 *
	 */
	private final class IgnoreAllMetaAction extends AbstractAction {
		private IgnoreAllMetaAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("Ignoring all currently displayed errors");
			// TODO: implement me
			// if qf_name == "qf-ignore"
			// for each panel...
			for (Component comp : getComponents()) {
				if(comp instanceof JXTaskPane)
				{
					JXTaskPane panel = (JXTaskPane) comp;
					ApplicationActionMap map = Application.getInstance()
							.getContext().getActionMap(panel);
					List<Action> keepme = new LinkedList<Action>();
					if(map.size() > 0)
					{
						for (Object key : map.keys()) {
							Action otherQuickfix = map.get(key);
							IvanErrorInstance otherError = (IvanErrorInstance) otherQuickfix.getValue(QF_ERROR);
							if (otherError == null) {
								System.out.print("Saving this one for later: ");
								System.out.println(otherQuickfix);
								
								keepme.add(otherQuickfix);
							} else {
								map.remove(key); // throw it away
								// save this problem as "ignored" 
								ignoredProblems.add(otherError);
								// remove it from the problems which are currently of concern
								bagofProblems.remove(otherError);
							}
						}
						panel.removeAll();
						for (Action action : keepme) {
							panel.add(action);
						}
					}
				}
			}
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

	/** This action ignores a single problem
	 * 
	 * @author Jonny
	 *
	 */
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
			// FIXME change behaviour of saving to iterate panel.getContentPane().getComponents() and 
			// only remove pertaining components OR retaining all non-action components by default or something like that
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
			// clean the panel of excess elements
			panel.removeAll();
			for (Action action : keepme) {
				panel.add(action);
			}
			panel.updateUI();
			// save this problem as "ignored" 
			ignoredProblems.add(error);
			// remove it from the problems which are currently of concern
			bagofProblems.remove(error);
			System.out.println("This action's error is " + getValue(QF_ERROR));
			System.out.println(tp.toString());
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	/** This action implements a quick fix: it adds a location
	 * 
	 * @author Jonny
	 *
	 */
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
			insertSentenceStub(myerror, stubs, " is in the …. ", "in the …");
			System.out.println("This action's error is " + getValue(QF_ERROR));
		}


		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
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

	/** This action implements a quick fix: it deletes the offending sentence
	 * 
	 * @author Jonny
	 *
	 */
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
			int lb = findSentenceStart(error.Codepoints);
			int rb = findInsertionPoint(error.Codepoints);
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

		private int findSentenceStart(List<CodePoint> codepoints) {
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
		private List<String> stubs = new ArrayList<String>();
		private IvanErrorInstance myerror;

		private AddDirectionAction(String name, IvanErrorInstance error) {
			super(name);
			myerror = error;
			stubs.addAll(Arrays.asList(new String[]{
					" is facing the camera.",
					" is facing front.",
					" is turned to the right."}));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//String name = (String) getValue(SHORT_DESCRIPTION);
			System.out.println("I'm adding a direction.");
			insertSentenceStub(myerror, stubs, " is facing ….", "…");
			System.out.println("This action's error is " + getValue(QF_ERROR));
		}

		@Override
		public String toString() {
			return qfActionPrinter(this);
		}
	}

	/** This is the error representation for the Errors Task Pane
	 * 
	 * @author Jonny
	 *
	 */
	public class IvanErrorInstance {

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
					return true;
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
	}

	private Map<String, JXTaskPane> mypanes = new TreeMap<String, JXTaskPane>();
	final private Font errorInfoFont = new Font("Calibri", 0, 11);
	private JTextComponent txtEditor = null;

	/** A bag of problems which have been ignored by the user and should subsequently not be displayed any more.
	 * 
	 */
	private Set<IvanErrorInstance> ignoredProblems = new HashSet<IvanErrorsTaskPaneContainer.IvanErrorInstance>();
	private Set<IvanErrorInstance>  bagofProblems = new HashSet<IvanErrorsTaskPaneContainer.IvanErrorInstance>();
	private Collection<Error> gen0 = new HashSet<>();
	

	/** Create a new component. This component displays errors and user action. It can also modify text in a text component. 
	 * @param editor The text to work with.
	 */
	public IvanErrorsTaskPaneContainer(JTextComponent editor) {
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
		// does this pane already exist?
		if(mypanes.containsKey(title))
		{
			// yes. nop
			return;
		}
		
		JXTaskPane pane = new JXTaskPane();
		pane.setTitle(title);
		if(description != null){
			description = "<html>" + description; // enables word wrap
			JLabel lbl = new JLabel(description);		
			lbl.setFont(errorInfoFont);
			pane.add(lbl);
		}
		mypanes.put(title, pane);
	}

	/** This method inserts a new problem into the <code>bagofProblems</code>. It also retrieves appropriate quick fixes.
	 * Problems are not added, if the user previously ignored them.
	 * 
	 * @param category Problem category (heading)
	 * @param errormsg User-readable error message with advice
	 * @param codepoints Points in the document that should be highlighted to the user
	 * @param references Relating text (like specific names in the document)
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, List<CodePoint> codepoints, String[] references) {
		JXTaskPane tsk = mypanes.get(category);
		if(tsk != null)
		{
			IvanErrorInstance error = new IvanErrorInstance(category, codepoints, null, errormsg, references);
			// has the user previously ignored this error?
			boolean ignored = this.ignoredProblems.contains(error);
			if(!ignored){
				// is this error already listed?
				if(!this.bagofProblems.add(error))
				{
					return false;
				}
				// add another quick fix for this category
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
		
		tsk.setName(error.Quickfix); 
        tpc.add(tsk); 
        
        ApplicationActionMap map = Application.getInstance().getContext().getActionMap(tsk); 

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
		
		String ref = StringUtils.abbreviate(StringUtils.join(error.Reference, ", "), 22);
		// this list will contain the availale fixes
		List<javax.swing.Action> myQuickfixesForThisError = new ArrayList<Action>();
		
		/* The DELETE_SENTENCE action */
		if(error.Category.equals(CATEGORY_EFFECT) 
				|| error.Category.equals(CATEGORY_GRAMMAR)
				|| error.Category.equals(CATEGORY_STYLE)) // for sentences without effect only
		{
			String displayDescription = "Delete sentence " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y + " '"+ref+"'";
	        javax.swing.Action myAction = new DeleteSentenceAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-delete");
			myQuickfixesForThisError.add(myAction);
		}
		
		/* The ADD_SENTENCE action */
		if(error.Category.equals(CATEGORY_LOCATION)) /* LOCATION */
		{
			String[] references = error.Reference;
			String displayDescription = "Add a location after " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y + " '"+ref+"'";
	        javax.swing.Action myAction = new AddLocationAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-add"+ Arrays.toString(references));
			myQuickfixesForThisError.add(myAction);
		} 
		else if(error.Category.equals(CATEGORY_DIRECTION)) /* DIRECTION */
		{ 
			String[] references = error.Reference;
			String displayDescription = "Add a direction after " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y + " '"+ref+"'";
	        javax.swing.Action myAction = new AddDirectionAction(displayDescription, error);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-add"+ Arrays.toString(references));
			myQuickfixesForThisError.add(myAction);
		}
				
		/* The IGNORE action is almost always available */
		if(!error.Category.equals(CATEGORY_META))
		{
			// instance 
			final IvanErrorsTaskPaneContainer tp = this;
			// the description to display
	        String displayDescription = "Ignore problem in " + error.Codepoints.get(0).x + "," + error.Codepoints.get(0).y + " '"+ref+"'";
	        
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

	/** This method inserts a new problem into the <code>bagofProblems</code>. It also retrieves appropriate quick fixes.
	 * Problems are not added, if the user previously ignored them.
	 * 
	 * @param category Problem category (heading)
	 * @param errormsg User-readable error message with advice
	 * @param codepoint Single point in the document that should be highlighted to the user
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, CodePoint codepoint) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{codepoint}), null);
	}

	/** This method inserts a new problem into the <code>bagofProblems</code>. It also retrieves appropriate quick fixes.
	 * Problems are not added, if the user previously ignored them.
	 * 
	 * @param category Problem category (heading)
	 * @param errormsg User-readable error message with advice
	 * @param i Code point coordinate number one
	 * @param j Code point coordinate number two
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, int i, int j) {
		return createProblem(category, errormsg, new CodePoint(i, j));
	}

	/** This method inserts a new problem into the <code>bagofProblems</code>. It also retrieves appropriate quick fixes.
	 * Problems are not added, if the user previously ignored them.
	 * 
	 * @param category Problem category (heading)
	 * @param errormsg User-readable error message with advice
	 * @param i Code point coordinate number one
	 * @param j Code point coordinate number two 
	 * @param references Relating text (like specific names in the document)
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, int i, int j,
			String[] references) {
		return createProblem(category, errormsg, Arrays.asList(new CodePoint[]{new CodePoint(i, j)}), references);
	}

	/** Unified "toString" method for quick fix actions. (Because it seems that ActionX.toString() can't share an implementation.)
	 * @return ActionX.toString()
	 */
	protected String qfActionPrinter(Action action) {
		IvanErrorInstance err = (IvanErrorInstance) action.getValue(QF_ERROR);
		String qf_shorthand = (String) action.getValue(QF_NAME);
		
		// in case something goes wrong
		if(err == null)
		{
			System.err.println("IvanErrorType instance was not set.");
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

	/** This method sets the editor window to allow this component direct modification of user text.
	 * @param txtEditor
	 */
	public void setEditor(JTextComponent txtEditor) {
		this.txtEditor = txtEditor;
	}

	/** This applies quick fixes which require insertion in the editor panel.
	 * 
	 * @param markThisPart 
	 * 
	 */
	private void insertSentenceStub(IvanErrorInstance myerror, List<String> stubs, String defaultStub, String markThisPart) {
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

		String sentence;
		if(stubs.size() > 0){
			// build a sentence from a stub
			sentence = "\n" + unlocatedNames[0] + stubs.get(0) + " ";
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
			sentence = "\n" + unlocatedNames[0] + defaultStub;
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
			int dotspoint = txtEditor.getText().indexOf(markThisPart, insertionpoint);
			if(dotspoint > 0){
				txtEditor.setCaretPosition(dotspoint);
				txtEditor.moveCaretPosition(dotspoint + markThisPart.length());
			}				
		}
	}

	/** 
	 * Removes categories and "problems" which have not been updated recently.
	 * More precisely, it maintains a list of generations and removes all display things which 
	 * are not a member of the recent generation.
	 */
	public void purge() {
		for (Error error : gen0) {
			// TODO: remove an error
		}
		gen0.clear();
	}
}
