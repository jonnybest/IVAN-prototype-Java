package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** This action implements a quick fix: it adds a location
 * 
 * @author Jonny
 *
 */
final class AddLocationAction extends Quickfix {
	/**
	 * 
	 */
	private final IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer;
	final private IvanErrorInstance myerror;
	private List<String> stubs = new ArrayList<String>();

	AddLocationAction(IvanErrorsTaskPaneContainer ivanErrorsTaskPaneContainer, String name, IvanErrorInstance error2) {
		super(name, ivanErrorsTaskPaneContainer.txtEditor);
		this.ivanErrorsTaskPaneContainer = ivanErrorsTaskPaneContainer;
		this.myerror = error2;
		stubs.addAll(Arrays.asList(new String[]{
			" is in the left front.",
			" is in the right front.",
			" is in the background to the left."}));
		
		installCaret(error2.Codepoints.get(0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//String name = (String) getValue(SHORT_DESCRIPTION);
		log.info("I'm adding a location.");
		this.ivanErrorsTaskPaneContainer.insertSentenceStub(myerror, stubs, " is in the …. ", "in the …");
		log.info("This action's error is " + getValue(IvanErrorsTaskPaneContainer.QF_ERROR));
	}


	@Override
	public String toString() {
		return this.ivanErrorsTaskPaneContainer.qfActionPrinter(this);
	}
}