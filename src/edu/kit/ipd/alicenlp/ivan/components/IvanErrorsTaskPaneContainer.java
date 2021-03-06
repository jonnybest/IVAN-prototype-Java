/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;

/**
 * This is a special JXTaskPaneContainer, which can display errors and warnings
 * that occur in IVAN. It provides an cues to the UI where to render errors
 * (line numbers or character offsets), actions for fixing and ignoring errors,
 * and human-readable state for easy instrumentation.
 * 
 * JXTaskPaneContainer provides an elegant view to display a list of tasks
 * ordered by groups (org.jdesktop.swingx.JXTaskPanes).
 * 
 * @author Jonny
 * 
 */
@SuppressWarnings("serial")
public class IvanErrorsTaskPaneContainer extends JXTaskPaneContainer {

	private static final String CHECK_ALL_SENTENCES = "Check all sentences";

	Logger log = Logger.getLogger("IvanErrorsTaskPaneContainer");

	// headline constants
	/**
	 * This constant contains the headline for meta problems.
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

	private static final int MAXIMUM_LABEL_LENGTH = 40;
	JTextComponent txtEditor = null;

	/**
	 * A bag of problems which have been ignored by the user and should
	 * subsequently not be displayed any more.
	 * 
	 */
	Set<IvanErrorInstance> ignoredProblems = new HashSet<IvanErrorInstance>();
	Set<IvanErrorInstance> bagofProblems = new HashSet<IvanErrorInstance>();
	/** this collection contains all the errors which survived the last "purge()"
	 * 
	 */
	private Collection<IvanErrorInstance> gen0 = new HashSet<>();

	/**
	 * Create a new component. This component displays errors and user action.
	 * It can also modify text in a text component.
	 * 
	 * @param editor
	 *            The text to work with.
	 */
	public IvanErrorsTaskPaneContainer(JTextComponent editor) {
		txtEditor = editor;

		/** Create META actions */
		IvanErrorsTaskPaneContainer tpc = this;

		JXTaskPane tsk = createCategory(CATEGORY_META, "Click '"
				+ CHECK_ALL_SENTENCES + "' to begin or hit Ctrl+S.");

		tsk.setName(CATEGORY_META);
		tpc.add(tsk);

		ApplicationActionMap map = Application.getInstance().getContext()
				.getActionMap(tsk);

		/* IGNORE ALL */
		{
			// the description to display
			String displayDescription = "Ignore all current problems";
			javax.swing.Action myAction = new MetaActionIgnoreAll(this,
					displayDescription);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-ignore-all");
			tsk.add(myAction);
			map.put(myAction.getValue(QF_NAME), myAction);
		}
		/* RESET IGNORED */
		{
			// the description to display
			String displayDescription = "Restore ignored problems";
			javax.swing.Action myAction = new MetaActionRestoreAll(this,
					displayDescription);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-reset-ignore");
			tsk.add(myAction);
			map.put(myAction.getValue(QF_NAME), myAction);
		}
		/* CHECK all sentences */
		{
			javax.swing.Action myAction = new MetaActionCheckSentences(
					CHECK_ALL_SENTENCES);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "mf-check");
			tsk.add(myAction);
			map.put(myAction.getValue(QF_NAME), myAction);
		}

	}

	/**
	 * This method allows printing all the currently displayed errors and
	 * warnings in a readable, diff-able manner
	 * 
	 */
	@Override
	public String toString() {
		// TODO print something that shows which stuff is currently being
		// displayed
		// for(Category c : this.errors.entrySet())
		// {
		// l.info(c.Name);
		// for(Instance i : c)
		// {
		// l.info("\t- " + i.toString());
		// }
		// }
		// l.info();#
		final String nl = "\n";
		StringBuilder sb = new StringBuilder();
		log.info("now printing "+ mypanes.entrySet().size() + " panels");
		log.info("actions: "+ Application.getInstance().getContext().getActionMap().size());
		sb.append("panels: " + nl);
		for (String panel : mypanes.keySet()) {
			sb.append(panel  +nl);
		}
		sb.append(nl + "shown issues:" + nl);
		for ( IvanErrorInstance err : this.bagofProblems) {
			sb.append(err);
		}
		sb.append(nl + "ignored issues:" + nl);
		for ( IvanErrorInstance err : this.ignoredProblems) {
			sb.append(err);
		}
		return sb.toString();
		// return super.toString();
	}

	/**
	 * Create a category of errors which should be presented to the user in a
	 * single taskpane. This method should only be called once per runtime and
	 * per category.
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public JXTaskPane createCategory(String title, String description) {
		// does this pane already exist?
		if (mypanes.containsKey(title)) {
			// yes. nop.
			return mypanes.get(title);
		}

		JXTaskPane pane = new JXTaskPane();
		pane.setTitle(title);
		if (description != null) {
			String[] lines = splitDescription(description, MAXIMUM_LABEL_LENGTH);
			for (String l : lines) {
				JLabel lbl = (JLabel) pane.add(new JLabel(l));
				lbl.setFont(errorInfoFont);
			}
		}
		mypanes.put(title, pane);
		return pane;
	}

	/**
	 * a simple splitter function from
	 * http://stackoverflow.com/a/14160155/651720
	 * 
	 * @param input
	 * @param maxCharInLine
	 * @return
	 */
	private static String[] splitDescription(String input, int maxCharInLine) {
		StringTokenizer tok = new StringTokenizer(input, " ");
		StringBuilder output = new StringBuilder(input.length());
		int lineLen = 0;
		while (tok.hasMoreTokens()) {
			String word = tok.nextToken();

			while (word.length() > maxCharInLine) {
				output.append(word.substring(0, maxCharInLine - lineLen) + "\n");
				word = word.substring(maxCharInLine - lineLen);
				lineLen = 0;
			}

			if (lineLen + word.length() > maxCharInLine) {
				output.append("\n");
				lineLen = 0;
			}
			output.append(word + " ");

			lineLen += word.length() + 1;
		}
		// output.split();
		// return output.toString();
		return output.toString().split("\n");
	}

	/**
	 * This method inserts a new problem into the <code>bagofProblems</code>. It
	 * also retrieves appropriate quick fixes. Problems are not added, if the
	 * user previously ignored them.
	 * 
	 * @param category
	 *            Problem category (heading)
	 * @param errormsg
	 *            User-readable error message with advice
	 * @param codepoints
	 *            Points in the document that should be highlighted to the user
	 * @param references
	 *            Relating text (like specific names in the document)
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg,
			List<CodePoint> codepoints, String[] references) {
		JXTaskPane tsk = mypanes.get(category);
		if (tsk != null) {
			IvanErrorInstance error = new IvanErrorInstance(category,
					codepoints, null, errormsg, references);
			// this issue is present, so prevent it from being purged
			gen0.remove(error);
			// has the user previously ignored this error?
			boolean ignored = this.ignoredProblems.contains(error);
			if (!ignored) {
				if (!CATEGORY_META.equals(category) // if this is a meta error,
													// do not attempt to add it
						&& !this.bagofProblems.add(error)) // is this error
															// already listed?
				{
					return false;
				}
				createQuickfixes(error);
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * This method inserts a new problem into the <code>bagofProblems</code>. It
	 * also retrieves appropriate quick fixes. Problems are not added, if the
	 * user previously ignored them.
	 * 
	 * @param category
	 *            The category under which this error should be filed.
	 * @param pipelineError
	 *            The error object retrieved from the annotations.
	 * @param sentenceCodepoint
	 *            The sentence's coordinates inside the text.
	 * @return TRUE if the problem should be shown to the user, otherwise FALSE.
	 * @throws BadLocationException
	 *             The given codepoints do not refer to valid coordinates inside
	 *             the text.
	 */
	public boolean createProblem(String category,
			IvanErrorMessage pipelineError, CodePoint sentenceCodepoint)
			throws BadLocationException {
		String errormsg = pipelineError.getMessage();

		final List<CodePoint> codepoints;
		if (sentenceCodepoint != null)
			// if we received any specific sentence bounds, put them in the last
			// bucket
			codepoints = Arrays.asList(new CodePoint(pipelineError.getSpan()),
					sentenceCodepoint);
		else
			// if we didn't receive any specific bounds, let's try our luck
			// instead
			codepoints = Arrays.asList(new CodePoint(pipelineError.getSpan()));

		// the "problem zone" is the offending part of the text. like a word, or
		// a phrase
		int lengthOfProblemzone = pipelineError.getSpan().end()
				- pipelineError.getSpan().start();
		int startOfProblemzone = pipelineError.getSpan().start();
		String problemzone = txtEditor.getText(startOfProblemzone,
				lengthOfProblemzone);
		String[] references = new String[] { problemzone };

		JXTaskPane tsk = mypanes.get(category);
		if (tsk != null) {
			IvanErrorInstance error = new IvanErrorInstance(category,
					codepoints, null, errormsg, references);
			// this issue is recent, so prevent it from being purged
			gen0.remove(error);
			// has the user previously ignored this error?
			boolean ignored = this.ignoredProblems.contains(error);
			if (!ignored) {
				if (CATEGORY_META.equals(category) // if this is a meta error,
													// do not attempt to add it
						) 
															
				{
					return false;
				}
				else if(!this.bagofProblems.add(error))
				{
					// is this error
					// already listed?
					// if yes, do not add any quick fixes, but tell the swing window that this error should still be displayed
					return true;
				}
				createQuickfixes(error);
				return true;
			}
			return false;
		} else {
			return false;
		}

	}

	private void createQuickfixes(final IvanErrorInstance error) {
		IvanErrorsTaskPaneContainer tpc = this;
		JXTaskPane tsk = mypanes.get(error.Category);

		tsk.setName(error.Category);
		tpc.add(tsk);

		ApplicationActionMap map = Application.getInstance().getContext()
				.getActionMap(tsk);

		List<javax.swing.Action> myQuickfixesForThisError = createAvailableQuickfixes(error);

		for (Action act : myQuickfixesForThisError) {
			Component component = tsk.add(act);
			error.addComponent(component);
			map.put(act.getValue(QF_NAME), act);
		}

	}

	/**
	 * This method looks up available quickfixes in the quickfix map and creates
	 * appropriate actions
	 * 
	 * @param error
	 * @return
	 */
	protected List<javax.swing.Action> createAvailableQuickfixes(
			final IvanErrorInstance error) {

		String ref = StringUtils.abbreviate(
				StringUtils.join(error.Reference, ", "), 22);
		// this list will contain the availale fixes
		List<javax.swing.Action> myQuickfixesForThisError = new ArrayList<Action>();

		/** SELECT action is available, too */
		if (!error.Category.equals(CATEGORY_META)) {
			// the description to display
			String nametemplate = "Select text for {0},{1} ''{2}''";
			String displayDescription = MessageFormat.format(nametemplate,
					error.Codepoints.get(0).x, error.Codepoints.get(0).y, ref);

			String name = "qf-select";
			AbstractQuickfix myAction = new QuickfixSelectIssue(displayDescription, error, this);
			myAction.setNameTemplate(nametemplate);
			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, name);
			myQuickfixesForThisError.add(myAction);			
		}
		
		/* The IGNORE action is almost always available */
		if (!error.Category.equals(CATEGORY_META)) {
			// the description to display
			String nametemplate = "Ignore problem in {0},{1} ''{2}''";
			String displayDescription = MessageFormat.format(nametemplate,
					error.Codepoints.get(0).x, error.Codepoints.get(0).y, ref);

			AbstractQuickfix myAction = new QuickfixIgnoreProblem(this,
					displayDescription, error);
			myAction.setNameTemplate(nametemplate);

			// make the error retrievable
			myAction.putValue(QF_ERROR, error);
			// set the shorthand notation for this qf
			myAction.putValue(QF_NAME, "qf-ignore");
			myQuickfixesForThisError.add(myAction);			
		}

		return myQuickfixesForThisError;
	}

	/**
	 * This method inserts a new problem into the <code>bagofProblems</code>. It
	 * also retrieves appropriate quick fixes. Problems are not added, if the
	 * user previously ignored them.
	 * 
	 * @param category
	 *            Problem category (heading)
	 * @param errormsg
	 *            User-readable error message with advice
	 * @param codepoint
	 *            Single point in the document that should be highlighted to the
	 *            user
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg,
			CodePoint codepoint) {
		return createProblem(category, errormsg,
				Arrays.asList(new CodePoint[] { codepoint }), null);
	}

	/**
	 * This method inserts a new problem into the <code>bagofProblems</code>. It
	 * also retrieves appropriate quick fixes. Problems are not added, if the
	 * user previously ignored them.
	 * 
	 * @param category
	 *            Problem category (heading)
	 * @param errormsg
	 *            User-readable error message with advice
	 * @param i
	 *            Code point coordinate number one
	 * @param j
	 *            Code point coordinate number two
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, int i, int j) {
		return createProblem(category, errormsg, new CodePoint(i, j));
	}

	/**
	 * This method inserts a new problem into the <code>bagofProblems</code>. It
	 * also retrieves appropriate quick fixes. Problems are not added, if the
	 * user previously ignored them.
	 * 
	 * @param category
	 *            Problem category (heading)
	 * @param errormsg
	 *            User-readable error message with advice
	 * @param i
	 *            Code point coordinate number one
	 * @param j
	 *            Code point coordinate number two
	 * @param references
	 *            Relating text (like specific names in the document)
	 * @return TRUE if the problem was inserted, otherwise FALSE
	 */
	public boolean createProblem(String category, String errormsg, int i,
			int j, String[] references) {
		return createProblem(category, errormsg,
				Arrays.asList(new CodePoint[] { new CodePoint(i, j) }),
				references);
	}

	/**
	 * Unified "toString" method for quick fix actions. (Because it seems that
	 * ActionX.toString() can't share an implementation.)
	 * 
	 * @return ActionX.toString()
	 */
	protected String qfActionPrinter(Action action) {
		IvanErrorInstance err = (IvanErrorInstance) action.getValue(QF_ERROR);
		String qf_shorthand = (String) action.getValue(QF_NAME);

		// in case something goes wrong
		if (err == null) {
			log.warning("IvanErrorType instance was not set.");
			return super.toString();
		}

		StringBuilder outstr = new StringBuilder();
		for (CodePoint cp : err.Codepoints) {
			outstr.append(cp.x + "," + cp.y);
			outstr.append("|");
		}
		outstr.deleteCharAt(outstr.length() - 1);
		outstr.append("  ");
		outstr.append(qf_shorthand);

		if (err.Problem != null) {
			outstr.append("\t");
			outstr.append("(" + err.Problem + ")");
			outstr.append("\n");
		}

		return outstr.toString();
	}

	/**
	 * This method sets the editor window to allow this component direct
	 * modification of user text.
	 * 
	 * @param txtEditor
	 */
	public void setEditor(JTextComponent txtEditor) {
		this.txtEditor = txtEditor;
	}

	/**
	 * Removes categories and "problems" which have not been updated recently.
	 * More precisely, it maintains a list of old generations (gen0) and removes all
	 * display things which are not a member of the recent generation (which is not being tracked).
	 */
	public void purge() {
		log.info("purging");
		log.log(Level.INFO, String.format(
				"Generation 0: %d, all problems %d, and ignored problems %d ",
				gen0.size(), bagofProblems.size(), ignoredProblems.size()));
		for (IvanErrorInstance error : gen0) {
			// TODO: remove an error
			for (Component co : error.Components) {
				co.getParent().remove(co);
				log.fine("Component removed from " + error.Category);
			}
			error.Components.clear();
			bagofProblems.remove(error);
			ignoredProblems.remove(error);
		}
		// throw em away
		gen0.clear();
		// add survivors
		gen0.addAll(bagofProblems);
		gen0.addAll(ignoredProblems);
		this.updateUI();
	}

	/**
	 * removes squiggly markers from the text
	 */
	public void removeHighlights() {
		Highlighter hl = txtEditor.getHighlighter();
		for (Highlight h : hl.getHighlights()) {
			if(h.getPainter() instanceof SquiggleUnderlineHighlightPainter)
			{
				hl.removeHighlight(h);
			}
		}
	}

	/** Resets this panel to a "clean" state
	 * 
	 */
	public void clear() {
		String[] keys = new String[]{};
		keys = mypanes.keySet().toArray(keys);
		
		for (String cat : keys) {
			if(!cat.equals(CATEGORY_META))
			{
				this.remove(mypanes.get(cat));
				mypanes.remove(cat);
			}
		}
		
		this.bagofProblems.clear();
		this.gen0.clear();
		this.ignoredProblems.clear();

	}

}
