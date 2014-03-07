package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;

import org.jdesktop.swingx.JXTaskPane;

/** This action ignores a single problem
 * 
 * @author Jonny
 *
 */
final class QuickfixIgnoreProblem extends AbstractQuickfix {

	QuickfixIgnoreProblem(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name, IvanErrorInstance error) {
		super(name, error, ivanErrorsTaskPaneContainer, true);
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		removeError(Error);
		
		// get this category panel
		JXTaskPane panel = this.getIvanErrorsTaskPaneContainer().mypanes.get(Error.Category);
		// update visuals
		panel.updateUI();
		// save this problem as "ignored" 
		this.getIvanErrorsTaskPaneContainer().ignoredProblems.add(Error);
		// remove it from the problems which are currently of concern
		this.getIvanErrorsTaskPaneContainer().bagofProblems.remove(Error);
		log.info("This action's error is " + getValue(IvanErrorsTaskPaneContainer.QF_ERROR));
		log.info(getIvanErrorsTaskPaneContainer().toString());
	}

	@Override
	public String toString() {
		return this.getIvanErrorsTaskPaneContainer().qfActionPrinter(this);
	}
}