package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.gui.MarkEditor.MarkEditorListener;

/**
 * Dialog for selecting a WordSense.
 * 
 * @author Torben Brumm
 */
public class WordSenseSelector {
	
	/**
	 * Listener that is fired when a Sense is selected in a WordSenseSelector and the dialog is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface WordSenseSelectorListener {
		
		/**
		 * Fired when the dialog is closed (returns the selected values on "ok" and null/false on "cancel").
		 * 
		 * @param verbFrame Selected VerbFrame.
		 */
		public void onSelect(Sense sense, boolean useForAllOccurrences);
	}
	
	/**
	 * Sense that is returned to the listener on dialog close.
	 */
	private de.uka.ipd.recaacommons.Sense returnSense = null;
	
	/**
	 * Boolean value that indicates if the selected Sense shall be used for all occurrences of the word and that is
	 * returned to the listener on dialog close.
	 */
	private boolean returnUseForAllOccurrences;
	
	/**
	 * Mark for the Word.
	 */
	private Mark tempMark;
	
	/**
	 * Constructor. Opens the dialog.
	 * 
	 * @param parentShell Parent shell this modal dialog is centered on.
	 * @param word Word to select the sense for.
	 * @param senses Possible senses.
	 * @param ontologyName Name of the ontology the senses derive from.
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public WordSenseSelector(final Shell parentShell, final Word word, final Sense[] senses, final String ontologyName,
			final WordSenseSelectorListener listener) {
		final Display display = parentShell.getDisplay();
		// initialize mark value
		this.tempMark = word.getMark();
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Select Sense in Ontology " + ontologyName);
		final GridLayout layout = new GridLayout(3, true);
		dialog.setLayout(layout);
		
		// print the sentence
		final SentenceLabel sentenceText = new SentenceLabel(word, dialog, SWT.WRAP);
		sentenceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		final Combo combo;
		if (senses.length == 0) {
			final Text text = new Text(dialog, SWT.WRAP);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			text.setEditable(false);
			text.setText("No possible match found for " + word.getPlainWord());
			combo = null;
		}
		else {
			combo = new Combo(dialog, SWT.READ_ONLY);
			for (final Sense sense : senses) {
				combo.add(sense.getSense());
			}
			combo.add("None of the above");
			combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			
			final Text senseDescription = new Text(dialog, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			final GridData descriptionLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
			descriptionLayoutData.widthHint = 200;
			descriptionLayoutData.heightHint = 70;
			senseDescription.setLayoutData(descriptionLayoutData);
			senseDescription.setEditable(false);
			
			combo.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
					// not needed
				}
				
				@Override
				public void widgetSelected(final SelectionEvent e) {
					final int selectionIndex = combo.getSelectionIndex();
					if (selectionIndex == combo.getItemCount() - 1) {
						senseDescription.setText("");
					}
					else {
						senseDescription.setText(senses[selectionIndex].getDescription());
					}
				}
				
			});
			
			combo.select(0);
			if (senses.length > 0) {
				senseDescription.setText(senses[0].getDescription());
			}
			
		}
		final Button checkBox = new Button(dialog, SWT.CHECK);
		checkBox.setText("Use Selection for all occurrences of " + word.getPlainWord());
		checkBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		final Button buttonMark = new Button(dialog, SWT.PUSH);
		if (this.tempMark == null) {
			buttonMark.setText("Add Mark");
		}
		else {
			buttonMark.setText("Edit Mark");
		}
		buttonMark.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonOK = new Button(dialog, SWT.PUSH);
		buttonOK.setText("OK");
		buttonOK.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonCancel = new Button(dialog, SWT.PUSH);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Listener buttonListener = new Listener() {
			
			public void handleEvent(final Event event) {
				if (event.widget == buttonOK) {
					WordSenseSelector.this.returnUseForAllOccurrences = checkBox.getSelection();
					if (combo == null) {
						// no selection possible
						WordSenseSelector.this.returnSense = Sense.NO_SENSE_FOUND;
					}
					else {
						final int selectionIndex = combo.getSelectionIndex();
						if (selectionIndex == combo.getItemCount() - 1) {
							// no match found by user
							WordSenseSelector.this.returnSense = Sense.NO_SENSE_FOUND;
						}
						else {
							WordSenseSelector.this.returnSense = senses[selectionIndex];
						}
					}
					
					// set mark for word
					word.setMark(WordSenseSelector.this.tempMark);
					dialog.close();
				}
				else if (event.widget == buttonCancel) {
					// close the dialog and return null
					dialog.close();
				}
				else if (event.widget == buttonMark) {
					// add a mark to this frame (or edit an existing one)
					// the rule will set the according mark if necessary
					new MarkEditor(dialog, WordSenseSelector.this.tempMark, new MarkEditorListener() {
						
						@Override
						public void onEditFinish(final Mark mark) {
							WordSenseSelector.this.tempMark = mark;
							if (mark == null) {
								buttonMark.setText("Add Mark");
							}
							else {
								buttonMark.setText("Edit Mark");
							}
						}
						
					});
				}
			}
		};
		
		buttonMark.addListener(SWT.Selection, buttonListener);
		buttonOK.addListener(SWT.Selection, buttonListener);
		buttonCancel.addListener(SWT.Selection, buttonListener);
		
		if (combo != null) {
			combo.setFocus();
		}
		
		dialog.setMinimumSize(250, 130);
		dialog.pack();
		Application.getInstance().centerShell(dialog, parentShell.getBounds());
		dialog.open();
		
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		// dialog is closed now so we call the according listener
		listener.onSelect(this.returnSense, this.returnUseForAllOccurrences);
	}
}
