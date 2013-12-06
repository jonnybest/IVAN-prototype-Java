package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.gui.MarkEditor.MarkEditorListener;

/**
 * Dialog for selecting a WordSense.
 * 
 * @author Torben Brumm
 */
public class DeterminerSelector {
	
	/**
	 * Listener that is fired when the dialog is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface DeterminerSelectorListener {
		
		/**
		 * Fired when the dialog is closed (returns the edited DeterminerInfo on "ok" (null if it was deleted) and the
		 * original DeterminerInfo on "cancel").
		 * 
		 * @param determinerInfo Further info about the determiner.
		 */
		public void onEditFinish(DeterminerInfo determinerInfo);
	}
	
	/**
	 * DeterminerInfo that is returned to the listener on dialog close.
	 */
	private DeterminerInfo determinerInfo;
	
	/**
	 * Mark for the DeterminerInfo.
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
	public DeterminerSelector(final Shell parentShell, final Word word, final DeterminerInfo currentDeterminerInfo,
			final DeterminerInfo ontologyDeterminerInfo, final String ontologyName,
			final DeterminerSelectorListener listener) {
		final Display display = parentShell.getDisplay();
		// initialize return values
		this.determinerInfo = currentDeterminerInfo;
		this.tempMark = (this.determinerInfo == null ? null : this.determinerInfo.getMark());
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Select Determiner in Ontology " + ontologyName);
		final GridLayout layout = new GridLayout(3, true);
		dialog.setLayout(layout);
		
		// print the sentence
		final SentenceLabel sentenceText = new SentenceLabel(word, dialog, SWT.WRAP);
		sentenceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		final Combo combo = new Combo(dialog, SWT.READ_ONLY);
		
		combo.add("Definite Article (needs referenced object)");
		combo.add("Indefinite Article (only in definitions)");
		combo.add("Quantifier (specify further)");
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		final Listener onlyDigitsListener = new Listener() {
			
			public void handleEvent(final Event e) {
				final String string = e.text;
				final char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		};
		
		final Composite quantifierComposite = new Composite(dialog, SWT.NONE);
		quantifierComposite.setLayout(new GridLayout(2, false));
		quantifierComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		final Button exactButton = new Button(quantifierComposite, SWT.RADIO);
		exactButton.setText("Exactly");
		final Text exactValue = new Text(quantifierComposite, SWT.SINGLE | SWT.BORDER);
		exactValue.setText("0");
		exactValue.addListener(SWT.Verify, onlyDigitsListener);
		
		final Button minimumButton = new Button(quantifierComposite, SWT.RADIO);
		minimumButton.setText("Minimum");
		final Text minimumValue = new Text(quantifierComposite, SWT.SINGLE | SWT.BORDER);
		minimumValue.setText("0");
		minimumValue.addListener(SWT.Verify, onlyDigitsListener);
		
		final Button maximumButton = new Button(quantifierComposite, SWT.RADIO);
		maximumButton.setText("Maximum");
		final Text maximumValue = new Text(quantifierComposite, SWT.SINGLE | SWT.BORDER);
		maximumValue.setText("0");
		maximumValue.addListener(SWT.Verify, onlyDigitsListener);
		
		final Button fromToButton = new Button(quantifierComposite, SWT.RADIO);
		fromToButton.setText("From");
		final Composite fromToComposite = new Composite(quantifierComposite, SWT.NONE);
		fromToComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		final Text fromToMinValue = new Text(fromToComposite, SWT.SINGLE | SWT.BORDER);
		fromToMinValue.setText("0");
		fromToMinValue.addListener(SWT.Verify, onlyDigitsListener);
		final Label fromToToLabel = new Label(fromToComposite, SWT.CENTER);
		fromToToLabel.setText("to");
		final Text fromToMaxValue = new Text(fromToComposite, SWT.SINGLE | SWT.BORDER);
		fromToMaxValue.setText("0");
		fromToMaxValue.addListener(SWT.Verify, onlyDigitsListener);
		
		final Button noneButton = new Button(quantifierComposite, SWT.RADIO);
		noneButton.setText("None (without exception) [0]");
		noneButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Button allButton = new Button(quantifierComposite, SWT.RADIO);
		allButton.setText("All (without exception)");
		allButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Button arbitraryButton = new Button(quantifierComposite, SWT.RADIO);
		arbitraryButton.setText("Arbitrary amount [0..*]");
		arbitraryButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		combo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// not needed
			}
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final int selectionIndex = combo.getSelectionIndex();
				if (selectionIndex == 2) {
					quantifierComposite.setVisible(true);
				}
				else {
					quantifierComposite.setVisible(false);
				}
			}
			
		});
		
		DeterminerInfo initializingDeterminerInfo = new DeterminerInfo();
		initializingDeterminerInfo.setRange(0, DeterminerInfo.INFINITY);
		
		if (currentDeterminerInfo != null) {
			initializingDeterminerInfo = currentDeterminerInfo;
		}
		else if (ontologyDeterminerInfo != null) {
			initializingDeterminerInfo = ontologyDeterminerInfo;
		}
		
		if (initializingDeterminerInfo.isDefiniteArticle()) {
			combo.select(0);
			quantifierComposite.setVisible(false);
		}
		else if (initializingDeterminerInfo.isIndefiniteArticle()) {
			combo.select(1);
			quantifierComposite.setVisible(false);
		}
		else {
			combo.select(2);
			if (initializingDeterminerInfo.getMinValue() == DeterminerInfo.INFINITY) {
				allButton.setSelection(true);
			}
			else if (initializingDeterminerInfo.getMaxValue() == DeterminerInfo.INFINITY) {
				if (initializingDeterminerInfo.getMinValue() == 0) {
					arbitraryButton.setSelection(true);
				}
				else {
					minimumButton.setSelection(true);
					minimumValue.setText(String.valueOf(initializingDeterminerInfo.getMinValue()));
				}
			}
			else if (initializingDeterminerInfo.getMinValue() == initializingDeterminerInfo.getMaxValue()) {
				if (initializingDeterminerInfo.getMinValue() == 0) {
					noneButton.setSelection(true);
				}
				else {
					exactButton.setSelection(true);
					exactValue.setText(String.valueOf(initializingDeterminerInfo.getMinValue()));
				}
			}
			else if (initializingDeterminerInfo.getMinValue() == 0) {
				maximumButton.setSelection(true);
				maximumValue.setText(String.valueOf(initializingDeterminerInfo.getMaxValue()));
			}
			else {
				fromToButton.setSelection(true);
				fromToMinValue.setText(String.valueOf(initializingDeterminerInfo.getMinValue()));
				fromToMaxValue.setText(String.valueOf(initializingDeterminerInfo.getMaxValue()));
			}
		}
		
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
					DeterminerSelector.this.determinerInfo = new DeterminerInfo();
					switch (combo.getSelectionIndex()) {
						case 0:
							DeterminerSelector.this.determinerInfo.setIsDefiniteArticle();
							break;
						
						case 1:
							DeterminerSelector.this.determinerInfo.setIsIndefiniteArticle();
							break;
						
						case 2:
							if (exactButton.getSelection()) {
								final int value = Integer.parseInt(exactValue.getText());
								DeterminerSelector.this.determinerInfo.setRange(value, value);
							}
							else if (minimumButton.getSelection()) {
								DeterminerSelector.this.determinerInfo.setRange(Integer
										.parseInt(minimumValue.getText()), DeterminerInfo.INFINITY);
							}
							else if (maximumButton.getSelection()) {
								DeterminerSelector.this.determinerInfo.setRange(0, Integer.parseInt(maximumValue
										.getText()));
							}
							else if (fromToButton.getSelection()) {
								DeterminerSelector.this.determinerInfo.setRange(Integer.parseInt(fromToMinValue
										.getText()), Integer.parseInt(fromToMaxValue.getText()));
							}
							else if (noneButton.getSelection()) {
								DeterminerSelector.this.determinerInfo.setRange(0, 0);
							}
							else if (allButton.getSelection()) {
								DeterminerSelector.this.determinerInfo.setRange(DeterminerInfo.INFINITY,
										DeterminerInfo.INFINITY);
							}
							else {
								DeterminerSelector.this.determinerInfo.setRange(0, DeterminerInfo.INFINITY);
							}
							break;
						
					}
					
					// set mark for word
					DeterminerSelector.this.determinerInfo.setMark(DeterminerSelector.this.tempMark);
					dialog.close();
				}
				else if (event.widget == buttonCancel) {
					// close the dialog and return null
					dialog.close();
				}
				else if (event.widget == buttonMark) {
					// add a mark to this determiner (or edit an existing one)
					new MarkEditor(dialog, DeterminerSelector.this.tempMark, new MarkEditorListener() {
						
						@Override
						public void onEditFinish(final Mark mark) {
							DeterminerSelector.this.tempMark = mark;
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
		listener.onEditFinish(this.determinerInfo);
	}
}
