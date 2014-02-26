package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import edu.kit.ipd.alicenlp.ivan.SwingWindow;

/** This action invokes the pipeline and checks the sentence
 * 
 * @author Jonny
 *
 */
final class CheckSentencesMetaAction extends AbstractAction {
	Logger log = Logger.getLogger(getClass().getName());
	
	CheckSentencesMetaAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//String name = (String) getValue(SHORT_DESCRIPTION);
		log.info("Running pipeline");
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