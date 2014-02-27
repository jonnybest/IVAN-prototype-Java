/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

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

	private static final String CHECK_ALL_SENTENCES = "Check all sentences";

	Logger log = Logger.getLogger("IvanErrorsTaskPaneContainer");
	
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
	static final String QF_ERROR = "error";
	static final String QF_NAME = "qf-name";

	
	
	Map<String, JXTaskPane> mypanes = new TreeMap<String, JXTaskPane>();
	final private static Font errorInfoFont = new Font("Calibri", 0, 11);
	JTextComponent txtEditor = null;

	/** A bag of problems which have been ignored by the user and should subsequently not be displayed any more.
	 * 
	 */
	Set<IvanErrorInstance> ignoredProblems = new HashSet<IvanErrorInstance>();
	Set<IvanErrorInstance>  bagofProblems = new HashSet<IvanErrorInstance>();
	private Collection<IvanErrorInstance> gen0 = new HashSet<>();
	

	/** Create a new component. This component displays errors and user action. It can also modify text in a text component. 
	 * @param editor The text to work with.
	 */
	public IvanErrorsTaskPaneContainer(JTextComponent editor) {
		txtEditor = editor;
		

		/** Create META actions */
		IvanErrorsTaskPaneContainer tpc = this;
		
		JXTaskPane tsk = createCategory(CATEGORY_META, "Click '" + CHECK_ALL_SENTENCES + "' to begin or hit Ctrl+S.");
		
		tsk.setName(CATEGORY_META); 
        tpc.add(tsk); 
        
        ApplicationActionMap map = Application.getInstance().getContext().getActionMap(tsk); 
        
		/* IGNORE ALL */
		{
			// the description to display
	        String displayDescription = "Ignore all current problems";	        
	        javax.swing.Action myAction = new MetaActionIgnoreAll(this, displayDescription);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-ignore-all");
			tsk.add(myAction);
	        map.put(myAction.getValue(QF_NAME), myAction);	
		}
		/* RESET IGNORED */
		{
			// the description to display
	        String displayDescription = "Restore ignored problems";	        
	        javax.swing.Action myAction = new MetaActionRestoreAll(this, displayDescription);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-reset-ignore");
			tsk.add(myAction);
	        map.put(myAction.getValue(QF_NAME), myAction);	
		}
		/* CHECK all sentences */
		{
			javax.swing.Action myAction = new MetaActionCheckSentences(CHECK_ALL_SENTENCES);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-check");
			tsk.add(myAction);
	        map.put(myAction.getValue(QF_NAME), myAction);	
		}
	
	}
	
	/** This method allows printing all the currently displayed errors and warnings in a readable, diff-able manner 
	 * 
	 */
	@Override
	public String toString() {
		// TODO print something that shows which stuff is currently being displayed
//		for(Category c : this.errors.entrySet())
//		{
//			l.info(c.Name);
//			for(Instance i : c)
//			{
//				l.info("\t- " + i.toString());
//			}
//		}
//		l.info();#
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
	 * @return 
	 */
	public JXTaskPane createCategory(String title, String description) {
		// does this pane already exist?
		if(mypanes.containsKey(title))
		{
			// yes. nop.
			return mypanes.get(title);
		}
		
		JXTaskPane pane = new JXTaskPane();
		pane.setTitle(title);
		if(description != null){
//			description = "<html>" + description; // enables word wrap
			final JXLabel lbl = new JXLabel(description);
			lbl.setFont(errorInfoFont);
			lbl.setLineWrap(true);
			lbl.addComponentListener(new ComponentListener() {
				
				@Override
				public void componentShown(ComponentEvent arg0) {}
				
				@Override
				public void componentResized(ComponentEvent arg0) {
					log.info("resiz");
					lbl.setPreferredSize(getPreferredSize(lbl.getText(), true, 220));
				}
				
				@Override
				public void componentMoved(ComponentEvent arg0) {
					lbl.setPreferredSize(getPreferredSize(lbl.getText(), true, 220));					
				}
				
				@Override
				public void componentHidden(ComponentEvent arg0) {}
			});
			pane.add(lbl);
		}
		mypanes.put(title, pane);
		return pane;
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
			// this issue is present, so prevent it from being purged
			gen0.remove(error);
			// has the user previously ignored this error?
			boolean ignored = this.ignoredProblems.contains(error);
			if(!ignored){
				if(!CATEGORY_META.equals(category) // if this is a meta error, do not attempt to add it
						&& !this.bagofProblems.add(error)) // is this error already listed? 
				{
					return false;
				}
				// add another quick fix for this category
				if(!CATEGORY_META.equals(category)){
					error.StandardCaret = installCaret(error);
				}
				createQuickfixes(error);
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
	
	private Caret installCaret(IvanErrorInstance Error) {
		CodePoint codep = Error.Codepoints.get(Error.Codepoints.size()-1);
		return installCaret(codep);
	}
	
	private Caret installCaret(CodePoint codep) {
		// The caret will track the positions across the users' editings. 
		DefaultCaret place = new DefaultCaret();
		place.install(txtEditor);
		place.setVisible(false);
		place.setDot(codep.x);
		place.moveDot(codep.y);
		log.info("Installed Caret for " + codep);
		return place;
	}
	
	private void createQuickfixes(final IvanErrorInstance error) {
		IvanErrorsTaskPaneContainer tpc = this;
		JXTaskPane tsk = mypanes.get(error.Category);
		
		tsk.setName(error.Quickfix); 
        tpc.add(tsk); 
        
        ApplicationActionMap map = Application.getInstance().getContext().getActionMap(tsk); 

        List<javax.swing.Action> myQuickfixesForThisError = createAvailableQuickfixes(error);
        
        for (Action act : myQuickfixesForThisError) {
        	Component component = tsk.add(act);
        	error.addComponent(component);
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
	        javax.swing.Action myAction = new QuickfixDeleteSentence(this, displayDescription, error);
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
	        javax.swing.Action myAction = new QuickfixAddLocation(this, displayDescription, error);
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
	        javax.swing.Action myAction = new QuickfixAddDirection(this, displayDescription, error);
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
	        
	        javax.swing.Action myAction = new QuickfixIgnoreProblem(this, displayDescription, error, tp);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-ignore");
			myQuickfixesForThisError.add(myAction);
		}
		else {}
		
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
			log.warning("IvanErrorType instance was not set.");
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

	/** 
	 * Removes categories and "problems" which have not been updated recently.
	 * More precisely, it maintains a list of generations and removes all display things which 
	 * are not a member of the recent generation.
	 */
	public void purge() {
		log.info("purging");
		log.log(Level.FINE, String.format("Generation 0: %d, all problems %d, and ignored problems %d ", gen0.size(), bagofProblems.size(), ignoredProblems.size()));
		for (IvanErrorInstance error : gen0) {
			// TODO: remove an error
			for (Component co : error.Components) {
				co.getParent().remove(co);
				log.fine("Component removed from " + error.Category);
			}
			error.Components.clear();
		}
		gen0.clear();
		gen0.addAll(bagofProblems);
		gen0.addAll(ignoredProblems);
	}
	
	private static final JXLabel resizer = new JXLabel();
	 
    /**Returns the preferred size to set a component at in order to render
     * an html string.  You can specify the size of one dimension.
     * @param html 
     * @param width 
     * @param prefSize 
     * @return */
    public static java.awt.Dimension getPreferredSize(String html,
                                                      boolean width, int prefSize) {
 
        resizer.setText(html);
        resizer.setLineWrap(true);
        resizer.setFont(errorInfoFont);
 
        View view = (View) resizer.getClientProperty(
                javax.swing.plaf.basic.BasicHTML.propertyKey);
 
        view.setSize(width?prefSize:0,width?0:prefSize);
 
        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);
 
        return new java.awt.Dimension((int) Math.ceil(w),
                (int) Math.ceil(h));
    }
}
