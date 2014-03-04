/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.event.ActionEvent;

import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/** Not an actual fix, but an action which allow the user to select the issue text inside text area. 
 * 
 * @author Jonny
 *
 */
public class QuickfixSelectIssue extends AbstractQuickfix {

	/** Creates a new quickfix object for selecting issue text in textfield
	 * @param name
	 * @param error
	 * @param txtEditor
	 */
	public QuickfixSelectIssue(String name, IvanErrorInstance error,
			JTextComponent txtEditor) {
		super(name, error, txtEditor, true);
		
	}

	/** Interface method
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int dot = getIssue().getDot();
		int mark = getIssue().getMark();
		Caret car = super.txtEditor.getCaret();
		car.setDot(mark);
		car.moveDot(dot);
	}

}
