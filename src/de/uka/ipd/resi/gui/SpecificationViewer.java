package de.uka.ipd.resi.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.gui.SentenceEditor.SentenceEditorListener;

/**
 * Composite which displays the current specification and opens a sentence editor on double click.
 * 
 * @author Torben Brumm
 */
public class SpecificationViewer extends Composite {
	
	/**
	 * The currently shown Labels.
	 */
	private final ArrayList<Label> sentenceLabels = new ArrayList<Label>();
	
	/**
	 * Constructor.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 */
	public SpecificationViewer(final Composite parent, final int style) {
		super(parent, style);
		this.setLayout(new RowLayout(SWT.VERTICAL));
	}
	
	/**
	 * Updates the Viewer with the given sentences. Discards all old sentences.
	 * 
	 * @param sentences Sentences to show.
	 */
	public void update(final Sentence[] sentences) {
		// remove old sentences
		for (final Label label : this.sentenceLabels) {
			label.dispose();
		}
		this.sentenceLabels.clear();
		
		// add new ones
		for (final Sentence sentence : sentences) {
			final Label tempLabel = new Label(this, SWT.WRAP);
			tempLabel.setText(sentence.getPlainSentence());
			final RowData tempLayoutData = new RowData();
			tempLayoutData.width = 220;
			tempLabel.setLayoutData(tempLayoutData);
			
			// add double click listener for editor
			tempLabel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseDoubleClick(final MouseEvent e) {
					new SentenceEditor(SpecificationViewer.this.getShell(), sentence, new SentenceEditorListener() {
						
						@Override
						public void onEditFinish(final Sentence sentence) {
							if (sentence != null) {
								try {
									final Graph g = sentence.getGraph();
									for (final Word word : sentence.getWords()) {
										if (word.isDirty()) {
											g.updateWordAttributes(word);
										}
									}
								}
								catch (final WordNotFoundException e) {
									Application.getInstance().handleException(e);
								}
							}
							try {
								Application.getInstance().updateSpecificationView();
							}
							catch (final SyntaxException e) {
								Application.getInstance().handleException(e);
							}
							
						}
						
					});
				}
				
				@Override
				public void mouseDown(final MouseEvent e) {
					// not needed
				}
				
				@Override
				public void mouseUp(final MouseEvent e) {
					// not needed
				}
				
			});
			this.sentenceLabels.add(tempLabel);
		}
		
		// new layout
		this.layout();
		this.pack();
	}
	
}
