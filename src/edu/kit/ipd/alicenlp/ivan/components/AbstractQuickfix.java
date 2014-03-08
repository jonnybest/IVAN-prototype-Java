package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

/** Template class for installing caret tracking
 * 
 * @author Jonny
 *
 */
public abstract class AbstractQuickfix extends AbstractAction {

	protected IvanErrorInstance Error;
	
	protected JTextComponent txtEditor;
	final Logger log = Logger.getLogger(getClass().getName());
	private List<Caret> carets = new LinkedList<Caret>();
	private final AbstractQuickfix _instance;

	private String nameTemplate;

	/**
	 * 
	 */
	protected final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;

	/** This action implements an abstract quick fix.
	 * 
	 * @param name
	 * @param error 
	 * @param container where this issue resides 
	 */
	public AbstractQuickfix(String name, IvanErrorInstance error, IvanErrorsTaskPaneContainer container) {
		this(name, error, container, true);
	}

	/** Creates a new quick fix.
	 * 
	 * @param name The human-reable display text of this quick fix 
	 * @param error The relating error
	 * @param container The taskPanel container where this action is going to reside
	 * @param installQuickfix If TRUE, this quick fix needs to track text inside the text, which means that we install a Caret for each Codepooint. 
	 */
	public AbstractQuickfix(final String name, final IvanErrorInstance error,
			final IvanErrorsTaskPaneContainer container, final boolean installQuickfix) {
		super(name);
		this.ivanErrorsTaskPaneContainer = container;
		this.txtEditor = container.txtEditor;
		this.Error = error;
		if(installQuickfix && error.Codepoints.size() > 0)
		{
			for (CodePoint cp : error.Codepoints) {
				if(cp.length() > 0)
					carets.add(installCaret(cp));
			}
		}
		_instance = this;
	}

	/**
	 * @param codep
	 * @return 
	 */
	protected Caret installCaret(CodePoint codep) {
		// The caret will track the positions across the users' editings. 
		final DefaultCaret place = new DefaultCaret();		
		place.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		place.install(txtEditor);
		// remove mouse listeners from component, since we don't want the mouse to modify our caret
		txtEditor.removeMouseListener(place);
		txtEditor.removeMouseMotionListener(place);
		txtEditor.removeFocusListener(place);
		// configure visibility and set proper positions
		place.setVisible(false);
		place.setSelectionVisible(false);
		place.setDot(codep.y);
		place.moveDot(codep.x);
		log.info("Installed Caret for " + codep);
		place.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				log.fine(e.getSource() + " at " + e.toString());
				// when the caret changes, update the display
				updateName();
			}
		});
		return place;
	}

	protected void updateName() {
		int mark = this.getIssue().getMark();
		int dot = this.getIssue().getDot();
		String nm = MessageFormat.format(getNameTemplate(), mark < dot ? mark : dot, mark > dot ? mark : dot, getRef());
//		if (mark == dot) {
//			log.warning("This caret folded into singularity: " + nm);
//			log.fine("temporary singular state ignored");
//		}
		for (Object co: Error.Components) {
//			System.out.println(co.getClass());
			
		}
		if(mark==dot)
			this.setEnabled(false);
		else 
			this.setEnabled(true);
		_instance.putValue(NAME, nm);
		
	}

	private String getRef() {
		return StringUtils.abbreviate(
				StringUtils.join(Error.Reference, ", "), 22);
	}

	private String getNameTemplate() {
		return nameTemplate;
	}

	/** 
	 * Removes this error from the user interface and uninstalls any carets that this quick fix had. 
	 * @param Error The error to remove. We actually only remove its components from the GUI.
	 */
	protected void removeError(IvanErrorInstance Error) {
		// this caret is now useless and can be removed
		uninstallCarets(txtEditor);
		
		// assume this issue to be fixed and remove this error's components
		for (Component co : Error.Components) {
			co.getParent().remove(co);
		}
		Error.Components.clear();
	}


	private void uninstallCarets(JTextComponent txtEditor2) {
		for (Caret c : carets) {
			c.deinstall(txtEditor2);
		}		
		carets.clear();
	}

	/** This applies quick fixes which require insertion in the editor panel.
	 * 
	 * @param markThisPart 
	 * 
	 */
	void insertSentenceStub(IvanErrorInstance myerror, List<String> stubs, String defaultStub, String markThisPart) {
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

	protected Caret getSentence(){
		if(carets.size()==0)
			return null;
		return carets.get(carets.size()-1);
	}
	
	protected Caret getIssue(){
		if(carets.size()==0)
			return null;
		return carets.get(0);
	}

	/** A template for the MesssageFormat.format() function with up three arguments: StartPos, EndPos, Reference
	 * @param nametemplate
	 */
	public void setNameTemplate(String nametemplate) {
		nameTemplate = nametemplate;
	}

	/**
	 * @return the ivanErrorsTaskPaneContainer
	 */
	protected IvanErrorsTaskPaneContainer getIvanErrorsTaskPaneContainer() {
		return ivanErrorsTaskPaneContainer;
	}
}
