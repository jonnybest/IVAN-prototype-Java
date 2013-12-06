package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.VerbFrame.VerbArgument;
import de.uka.ipd.resi.gui.MarkEditor.MarkEditorListener;

/**
 * Dialog for selecting a VerbFrame.
 * 
 * @author Torben Brumm
 */
public class VerbFrameSelector {
	
	/**
	 * Listener that is fired when a VerbFrame is selected in a VerbFrameSelector and the dialog is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface VerbFrameSelectorListener {
		
		/**
		 * Fired when the dialog is closed (returns the selected VerbFrame on "ok" and null on "cancel").
		 * 
		 * @param verbFrame Selected VerbFrame.
		 */
		public void onSelect(VerbFrame verbFrame);
	}
	
	/**
	 * VerbFrame that is returned to the listener on dialog close.
	 */
	private VerbFrame returnValue = null;
	
	/**
	 * Mark for the VerbFrame.
	 */
	private Mark tempMark = null;
	
	/**
	 * Constructor. Opens the dialog.
	 * 
	 * @param parentShell Parent shell this modal dialog is centered on.
	 * @param verb Verb the frame shall be selected for.
	 * @param verbFrames VerbFrames to select from.
	 * @param suggestionVerbFrames VerbFrames that contain suggestions for the selection.
	 * @param ontologyName Name of the ontology the VerbFrames correspond to.
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public VerbFrameSelector(final Shell parentShell, final Word verb, final java.util.List<VerbFrame> verbFrames,
			final java.util.List<VerbFrame> suggestionVerbFrames, final String ontologyName,
			final VerbFrameSelectorListener listener) {
		final Display display = parentShell.getDisplay();
		
		// if there is already a mark, use it
		for (final VerbFrame suggestionFrame : suggestionVerbFrames) {
			if (suggestionFrame.getMark() != null) {
				this.tempMark = suggestionFrame.getMark();
			}
		}
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Complete Process Word in Ontology " + ontologyName);
		final GridLayout layout = new GridLayout(3, true);
		dialog.setLayout(layout);
		
		// we use tool tips on this dialog
		final ToolTipHandler toolTipHandler = new ToolTipHandler(dialog);
		
		// print the sentence
		final SentenceLabel sentenceText = new SentenceLabel(verb, dialog, SWT.WRAP);
		sentenceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		final Combo combo;
		final Combo[][] wordCombos;
		if (verbFrames.size() == 0) {
			final Text text = new Text(dialog, SWT.WRAP);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			text.setEditable(false);
			text.setText("No possible match found for " + verb.getPlainWord());
			combo = null;
			wordCombos = null;
		}
		else {
			combo = new Combo(dialog, SWT.READ_ONLY);
			
			final Composite stackComposite = new Composite(dialog, SWT.NONE);
			stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			final StackLayout stackLayout = new StackLayout();
			stackComposite.setLayout(stackLayout);
			
			final Composite[] verbFrameComposites = new Composite[verbFrames.size() + 1];
			wordCombos = new Combo[verbFrames.size() + 1][];
			int counter = 0;
			
			for (final VerbFrame verbFrame : verbFrames) {
				int i = 2;
				String stringToAdd = verb.getOntologyConstant(ontologyName);
				boolean inserted = false;
				while (!inserted) {
					boolean alreadyInserted = false;
					for (final String currentItem : combo.getItems()) {
						if (currentItem.equals(stringToAdd)) {
							alreadyInserted = true;
							break;
						}
					}
					if (alreadyInserted) {
						stringToAdd = verb.getOntologyConstant(ontologyName) + " " + i;
						i++;
					}
					else {
						combo.add(stringToAdd);
						inserted = true;
					}
				}
				
				verbFrameComposites[counter] = new Composite(stackComposite, SWT.NONE);
				verbFrameComposites[counter].setLayout(new GridLayout(3, true));
				
				wordCombos[counter] = new Combo[verbFrame.getArguments().size()];
				int counter2 = 0;
				for (final VerbArgument verbArgument : verbFrame.getArguments()) {
					final Label syntacticRole = new Label(verbFrameComposites[counter], SWT.NONE);
					syntacticRole.setText(verbArgument.getSyntacticRole());
					final Label semanticRole = new Label(verbFrameComposites[counter], SWT.NONE);
					semanticRole.setText(verbArgument.getSemanticRole().getSense());
					
					// hover for description
					semanticRole.setData(ToolTipHandler.TOOLTIP_DATA_KEY, verbArgument.getSemanticRole()
							.getDescription());
					toolTipHandler.activateToolTip(semanticRole);
					
					Word wordToSelect = verbArgument.getWord();
					
					// select the word suggested by the suggestion frames (overwrites suggestions from the ontology
					// itself
					// TODO up to now it works like Cyc even for graphs
					// TODO support for more than one suggestion (right now it uses the one it found last)
					for (final VerbFrame suggestionFrame : suggestionVerbFrames) {
						for (final VerbArgument suggestionArgument : suggestionFrame.getArguments()) {
							if (suggestionArgument.getSyntacticRole().equals(verbArgument.getSyntacticRole())) {
								wordToSelect = suggestionArgument.getWord();
							}
						}
					}
					
					wordCombos[counter][counter2] = new Combo(verbFrameComposites[counter], SWT.READ_ONLY);
					int indexToSelect = -1;
					int selectCounter = 0;
					for (final Word possibleArgument : verb.getSentence().getWords()) {
						wordCombos[counter][counter2].add(possibleArgument.getPlainWord());
						if (wordToSelect != null && wordToSelect.getId().equals(possibleArgument.getId())) {
							indexToSelect = selectCounter;
						}
						selectCounter++;
					}
					if (indexToSelect == -1) {
						// No suggestion => Select "none"
						indexToSelect = selectCounter;
					}
					wordCombos[counter][counter2].add("None of the above");
					
					// select the right one
					wordCombos[counter][counter2].select(indexToSelect);
					counter2++;
				}
				verbFrameComposites[counter].pack();
				counter++;
			}
			
			combo.add("None of the above");
			combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			verbFrameComposites[counter] = new Composite(stackComposite, SWT.NONE);
			
			combo.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
					// not needed
				}
				
				@Override
				public void widgetSelected(final SelectionEvent e) {
					stackLayout.topControl = verbFrameComposites[combo.getSelectionIndex()];
					stackComposite.layout();
				}
				
			});
			
			stackLayout.topControl = verbFrameComposites[0];
			combo.select(0);
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
					if (combo == null) {
						// no selection possible, so VerbFrame stays null
					}
					else {
						final int selectionIndex = combo.getSelectionIndex();
						if (selectionIndex == combo.getItemCount() - 1) {
							// no match found by user => return empty frame (not null as we need to delete any old
							// information)
							VerbFrameSelector.this.returnValue = new VerbFrame(ontologyName, verb);
						}
						else {
							final VerbFrame verbFrame = verbFrames.get(selectionIndex);
							int counter = 0;
							for (final VerbArgument argument : verbFrame.getArguments()) {
								final int selectionIndex2 = wordCombos[combo.getSelectionIndex()][counter]
										.getSelectionIndex();
								if (selectionIndex2 == wordCombos[combo.getSelectionIndex()][counter].getItemCount() - 1) {
									// no match found by user
									argument.setWord(null);
								}
								else {
									argument.setWord(verb.getSentence().getWords().get(selectionIndex2));
								}
								counter++;
							}
							
							VerbFrameSelector.this.returnValue = verbFrame;
						}
						// store the mark for the frame
						VerbFrameSelector.this.returnValue.setMark(VerbFrameSelector.this.tempMark);
					}
					dialog.close();
				}
				else if (event.widget == buttonCancel) {
					// close the dialog and return null
					dialog.close();
				}
				else if (event.widget == buttonMark) {
					// add a mark to this frame (or edit an existing one)
					// the rule will set the according mark if necessary
					new MarkEditor(dialog, VerbFrameSelector.this.tempMark, new MarkEditorListener() {
						
						@Override
						public void onEditFinish(final Mark mark) {
							VerbFrameSelector.this.tempMark = mark;
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
		
		dialog.setMinimumSize(400, 130);
		dialog.pack();
		Application.getInstance().centerShell(dialog, parentShell.getBounds());
		dialog.open();
		
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		// dialog is closed now so we call the according listener
		listener.onSelect(this.returnValue);
	}
	
}
