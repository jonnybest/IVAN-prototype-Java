package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/** This action ignores all currently displayed problems and clears the panel 
 * 
 * @author Jonny
 *
 */
final class IgnoreAllMetaAction extends AbstractAction {
	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;

	IgnoreAllMetaAction(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name) {
		super(name);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		this.ivanErrorsTaskPaneContainer.log.info("Ignoring all currently displayed errors");

		// for each panel...
		this.ivanErrorsTaskPaneContainer.ignoredProblems.addAll(this.ivanErrorsTaskPaneContainer.bagofProblems);
		for (IvanErrorInstance error : this.ivanErrorsTaskPaneContainer.bagofProblems) {				
			for (Component comp : error.Components) {
				comp.getParent().remove(comp);
			}
			error.Components.clear();
		}
		this.ivanErrorsTaskPaneContainer.bagofProblems.clear();
		this.ivanErrorsTaskPaneContainer.updateUI();
	}

	@Override
	public String toString() {
		String qf_shorthand = (String) getValue(IvanErrorsTaskPaneContainer.QF_NAME);
		
		StringBuilder outstr = new StringBuilder();
		
		//outstr.append("\t");	    			
		outstr.append(qf_shorthand);
		
		return outstr.toString();
	}
}