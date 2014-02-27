package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class QuickfixAddDirection extends AbstractQuickfix {
	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;
	private List<String> stubs = new ArrayList<String>();		

	QuickfixAddDirection(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name, IvanErrorInstance error) {
		super(name, ivanErrorsTaskPaneContainer.txtEditor);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
		Error = error;
		stubs.addAll(Arrays.asList(new String[]{
				" is facing the camera.",
				" is facing front.",
				" is turned to the right."}));
		
		installCaret(error.Codepoints.get(0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//String name = (String) getValue(SHORT_DESCRIPTION);
		log.info("I'm adding a direction.");
		insertSentenceStub(Error, stubs, " is facing ….", "…");
		log.info("This action's error is " + getValue(IvanErrorsTaskPaneContainer.QF_ERROR));
	}

	@Override
	public String toString() {
		return this.ivanErrorsTaskPaneContainer.qfActionPrinter(this);
	}
}