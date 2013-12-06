package de.uka.ipd.resi.gui;

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

/**
 * Dialog for editing a Mark.
 * 
 * @author Torben Brumm
 */
public class MarkEditor {
	
	/**
	 * Listener that is fired when editing a Mark is finished (or the Mark was deleted) in a MarkEditor and the dialog
	 * is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface MarkEditorListener {
		
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
	 * @param mark Mark to edit, null if a new Mark shall be created.
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public MarkEditor(final Shell parentShell, final Mark mark, final MarkEditorListener listener) {
		this.returnValue = mark;
		final Display display = parentShell.getDisplay();
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Edit Comment for Mark");
		final GridLayout layout = new GridLayout(3, true);
		dialog.setLayout(layout);
		
		final Text comment = new Text(dialog, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		final GridData commentLayoutdata = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		commentLayoutdata.widthHint = 250;
		comment.setLayoutData(commentLayoutdata);
		if (mark != null && mark.getComment() != null) {
			comment.setText(mark.getComment());
		}
		
		final Button buttonRemoveMark = new Button(dialog, SWT.PUSH);
		buttonRemoveMark.setText("Remove Mark");
		if (mark == null) {
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
					if (MarkEditor.this.returnValue == null) {
						MarkEditor.this.returnValue = new Mark();
					}
					MarkEditor.this.returnValue.setComment(comment.getText());
				}
				else if (event.widget == buttonRemoveMark) {
					// remove the current mark
					MarkEditor.this.returnValue = null;
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
