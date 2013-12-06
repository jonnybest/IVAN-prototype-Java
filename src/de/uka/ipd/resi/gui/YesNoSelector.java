package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Word;


/**
 * Dialog for answering a yes/no-question.
 * 
 * @author Torben Brumm
 */
public class YesNoSelector {
	
	/**
	 * Listener that is fired when the dialog is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface YesNoSelectorListener {
		
		/**
		 * Fired when the dialog is closed (returns true on "yes" and false on "no" and the according value for "Use for all ocurrences?").
		 * 
		 * @param yes Yes?.
		 * @param useForAllOccurrences Shall this selection be used for all occurrences of the word?
		 */
		public void onSelect(boolean yes,  boolean useForAllOccurrences);
	}
	
	/**
	 * Boolean that is returned to the listener on dialog close.
	 */
	private boolean returnValueYesNo = false;
	
	/**
	 * Shall this selection be used for all occurrences of the word?
	 */
	private boolean returnUseForAllOccurrences = false;
	
	/**
	 * Constructor. Opens the dialog.
	 * 
	 * @param parentShell Parent shell this modal dialog is centered on.
	 * @param title Title for this dialog.
	 * @param word Word to answer the yes/no-question for.
	 * @param question Question to ask
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public YesNoSelector(final Shell parentShell, final String title, final Word word, final String question, final YesNoSelectorListener listener) {
		final Display display = parentShell.getDisplay();
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText(title);
		final GridLayout layout = new GridLayout(2, true);
		dialog.setLayout(layout);
		
		// print the sentence
		final SentenceLabel sentenceText = new SentenceLabel(word, dialog, SWT.WRAP);
		sentenceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Label questionLabel = new Label(dialog, SWT.WRAP);
		questionLabel.setText(question);
		questionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Button checkBox = new Button(dialog, SWT.CHECK);
		checkBox.setText("Use Selection for all occurrences of " + word.getPlainWord());
		checkBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Button buttonYes = new Button(dialog, SWT.PUSH);
		buttonYes.setText("Yes");
		buttonYes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonNo = new Button(dialog, SWT.PUSH);
		buttonNo.setText("No");
		buttonNo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Listener buttonListener = new Listener() {
			
			public void handleEvent(final Event event) {
				if (event.widget == buttonYes) {
					// close the dialog and return true
					YesNoSelector.this.returnValueYesNo = true;
					YesNoSelector.this.returnUseForAllOccurrences = checkBox.getSelection();
					dialog.close();
				}
				else if (event.widget == buttonNo) {
					// close the dialog and return false
					YesNoSelector.this.returnUseForAllOccurrences = checkBox.getSelection();
					dialog.close();
				}
			}
		};
		
		buttonYes.addListener(SWT.Selection, buttonListener);
		buttonNo.addListener(SWT.Selection, buttonListener);
		
		dialog.pack();
		Application.getInstance().centerShell(dialog, parentShell.getBounds());
		dialog.open();
		
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		// dialog is closed now so we call the according listener
		listener.onSelect(returnValueYesNo, returnUseForAllOccurrences);
	}
}