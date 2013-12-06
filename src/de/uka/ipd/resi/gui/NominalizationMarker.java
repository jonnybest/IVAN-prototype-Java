package de.uka.ipd.resi.gui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Word;

/**
 * Dialog for marking a nominalization.
 * 
 * @author Torben Brumm
 */
public class NominalizationMarker {
	
	/**
	 * Listener that is fired when editing a nominalization mark is finished (or the mark was deleted) and the dialog is
	 * closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface NominalizationMarkerListener {
		
		/**
		 * Fired when the dialog is closed (returns the edited Mark on "ok", the original mark on "cancel" and null on
		 * "delete").
		 * 
		 * @param mark New Mark.
		 */
		public void onEditFinish(Mark mark);
	}
	
	/**
	 * Mark that is returned to the listener on dialog close.
	 */
	private Mark returnValue;
	
	/**
	 * Constructor. Opens the dialog.
	 * 
	 * @param parentShell Parent shell this modal dialog is centered on.
	 * @param word Possible nominalization.
	 * @param possibleProcessWords List of verbs that could be used instead of the nominalization.
	 * @param currentMark Mark that is currently set for this nominalization.
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public NominalizationMarker(final Shell parentShell, final Word word, final List<String> possibleProcessWords,
			final Mark currentMark, final NominalizationMarkerListener listener) {
		this.returnValue = currentMark;
		final Display display = parentShell.getDisplay();
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Add Nominalization Mark");
		final GridLayout layout = new GridLayout(3, true);
		dialog.setLayout(layout);
		
		// print the sentence
		final SentenceLabel sentenceText = new SentenceLabel(word, dialog, SWT.WRAP);
		sentenceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		// build String of verbs
		String verbString = possibleProcessWords.get(0);
		for (int i = 1; i < possibleProcessWords.size(); i++) {
			verbString += ", " + possibleProcessWords.get(i);
		}
		
		// comment editor
		final Text comment = new Text(dialog, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		final GridData commentLayoutdata = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		commentLayoutdata.widthHint = 250;
		comment.setLayoutData(commentLayoutdata);
		if (currentMark != null && currentMark.getComment() != null) {
			comment.setText(currentMark.getComment());
		}
		else {
			comment.setText("Nominalization. Use verb " + verbString + " instead.");
		}
		
		// buttons
		final Button buttonRemoveMark = new Button(dialog, SWT.PUSH);
		buttonRemoveMark.setText("Remove Mark");
		if (currentMark == null) {
			buttonRemoveMark.setEnabled(false);
		}
		buttonRemoveMark.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonOK = new Button(dialog, SWT.PUSH);
		buttonOK.setText("OK");
		buttonOK.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonCancel = new Button(dialog, SWT.PUSH);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Listener buttonListener = new Listener() {
			
			public void handleEvent(final Event event) {
				if (event.widget == buttonOK) {
					// store comment for mark
					if (NominalizationMarker.this.returnValue == null) {
						NominalizationMarker.this.returnValue = new Mark();
					}
					NominalizationMarker.this.returnValue.setComment(comment.getText());
				}
				else if (event.widget == buttonRemoveMark) {
					// remove the current mark
					NominalizationMarker.this.returnValue = null;
				}
				// we close the dialog for every button
				dialog.close();
			}
		};
		
		buttonRemoveMark.addListener(SWT.Selection, buttonListener);
		buttonOK.addListener(SWT.Selection, buttonListener);
		buttonCancel.addListener(SWT.Selection, buttonListener);
		
		dialog.setMinimumSize(200, 150);
		dialog.pack();
		Application.getInstance().centerShell(dialog, parentShell.getBounds());
		dialog.open();
		
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		// dialog is closed now so we call the according listener
		listener.onEditFinish(this.returnValue);
	}
	
}
