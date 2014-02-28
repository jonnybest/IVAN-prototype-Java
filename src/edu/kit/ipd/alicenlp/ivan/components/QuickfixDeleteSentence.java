package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

/** This action implements a quick fix: it deletes the offending sentence
 * 
 * @author Jonny
 *
 */
final class QuickfixDeleteSentence extends AbstractQuickfix {
	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;
	// the underlying issue for this quick fix
	private IvanErrorInstance Error;

	QuickfixDeleteSentence(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name, IvanErrorInstance error) {
		super(name, error, ivanErrorsTaskPaneContainer.txtEditor);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
		// Save "position" of the offending text inside editor frame.
		// TODO: make a convention to put the whole sentence into the last bucket of the codepoints.
		CodePoint sentence = error.Codepoints.get(error.Codepoints.size()-1);			
//		installCaret(sentence);
		this.Error = error;
	}

	@Override
	public void actionPerformed(ActionEvent e) {			
		
		// obtain positions
		this.ivanErrorsTaskPaneContainer.txtEditor.getCaret().setDot(getSentence().getDot());
		this.ivanErrorsTaskPaneContainer.txtEditor.getCaret().moveDot(getSentence().getMark());
		
		// delete it. this is undoable
		this.ivanErrorsTaskPaneContainer.txtEditor.replaceSelection("");
		// get the focus so user can start editing right away
		this.ivanErrorsTaskPaneContainer.txtEditor.requestFocusInWindow();
		log.info("This action's error is " + getValue(IvanErrorsTaskPaneContainer.QF_ERROR));
		
		removeError(Error);
	}

	@Override
	public String toString() {
		return this.ivanErrorsTaskPaneContainer.qfActionPrinter(this);
	}
}