package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;

/** This action restores all previously ignored problems
 * 
 * @author Jonny
 *
 */
final class RestoreAllMetaAction extends AbstractAction {
	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;

	RestoreAllMetaAction(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name) {
		super(name);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//String name = (String) getValue(SHORT_DESCRIPTION);
		this.ivanErrorsTaskPaneContainer.log.info("Restoring error display");
		// TODO: implement RestoreAllMetaAction
		this.ivanErrorsTaskPaneContainer.ignoredProblems.clear();
		SwingWindow.processText();
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