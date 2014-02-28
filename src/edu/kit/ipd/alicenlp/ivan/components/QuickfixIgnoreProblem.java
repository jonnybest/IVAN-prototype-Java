package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.jdesktop.swingx.JXTaskPane;

/** This action ignores a single problem
 * 
 * @author Jonny
 *
 */
final class QuickfixIgnoreProblem extends AbstractQuickfix {

	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;
	private final IvanErrorsTaskPaneContainer tp;

	QuickfixIgnoreProblem(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name, IvanErrorInstance error,
			IvanErrorsTaskPaneContainer tp) {
		super(name, error, ivanErrorsTaskPaneContainer.txtEditor, false);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
		this.tp = tp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// only remove pertaining components 
		for (Component co : Error.Components) {
			co.getParent().remove(co);
		}
		Error.Components.clear(); // only you can prevent memory leaks :)
		
		// get this category panel
		JXTaskPane panel = this.ivanErrorsTaskPaneContainer.mypanes.get(Error.Category);
		// update visuals
		panel.updateUI();
		// save this problem as "ignored" 
		this.ivanErrorsTaskPaneContainer.ignoredProblems.add(Error);
		// remove it from the problems which are currently of concern
		this.ivanErrorsTaskPaneContainer.bagofProblems.remove(Error);
		log.info("This action's error is " + getValue(IvanErrorsTaskPaneContainer.QF_ERROR));
		log.info(tp.toString());
	}

	@Override
	public String toString() {
		return this.ivanErrorsTaskPaneContainer.qfActionPrinter(this);
	}
}