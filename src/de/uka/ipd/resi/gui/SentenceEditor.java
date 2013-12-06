package de.uka.ipd.resi.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.gui.MarkEditor.MarkEditorListener;

/**
 * Editor for editing attributes of words.
 * 
 * @author Torben Brumm
 */
public class SentenceEditor {
	
	/**
	 * Listener that is fired when the dialog is closed.
	 * 
	 * @author Torben Brumm
	 */
	public interface SentenceEditorListener {
		
		/**
		 * Fired when the dialog is closed (returns the edited sentence on "ok" and null on "cancel").
		 * 
		 * @param sentence Edited Sentence.
		 */
		public void onEditFinish(Sentence sentence);
	}
	
	/**
	 * Column for the word's base form.
	 */
	private static final int BASEFORM_COLUMN = 1;
	
	/**
	 * Column for the word's mark.
	 */
	private static final int MARK_COLUMN = 3;
	
	/**
	 * Column for the word's POS tag.
	 */
	private static final int POSTAG_COLUMN = 2;
	
	/**
	 * Sentence to return on dialog close.
	 */
	private Sentence returnSentence = null;
	
	/**
	 * Constructor. Opens the dialog.
	 * 
	 * @param parentShell Parent shell this modal dialog is centered on.
	 * @param sentence Sentence to edit.
	 * @param listener Listener that is called when the dialog is closed.
	 */
	public SentenceEditor(final Shell parentShell, final Sentence sentence, final SentenceEditorListener listener) {
		final Display display = parentShell.getDisplay();
		
		final Shell dialog = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText("Edit Sentence");
		final GridLayout layout = new GridLayout(2, true);
		dialog.setLayout(layout);
		
		final Table table = new Table(dialog, SWT.NONE);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		// prepare columns
		final TableColumn wordColumn = new TableColumn(table, SWT.NONE);
		wordColumn.setWidth(100);
		wordColumn.setText("Word");
		
		final TableColumn baseFormColumn = new TableColumn(table, SWT.NONE, SentenceEditor.BASEFORM_COLUMN);
		baseFormColumn.setWidth(100);
		baseFormColumn.setText("Base Form");
		
		final TableColumn posTagColumn = new TableColumn(table, SWT.NONE, SentenceEditor.POSTAG_COLUMN);
		posTagColumn.setWidth(100);
		posTagColumn.setText("POS Tag");
		
		final TableColumn markColumn = new TableColumn(table, SWT.NONE, SentenceEditor.MARK_COLUMN);
		markColumn.setWidth(100);
		markColumn.setText("Mark");
		
		// insert data and needed ontology columns
		int columnCounter = 4;
		final Map<String, Integer> ontologyColumnsMap = new HashMap<String, Integer>();
		for (final Word word : sentence.getWords()) {
			final TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { word.getPlainWord(), word.getBaseForm(), word.getPennTreebankTag(),
					(word.getMark() == null) ? null : word.getMark().getComment()
			});
			
			for (final Entry<String, String> entry : word.getOntologyConstants().entrySet()) {
				if (ontologyColumnsMap.get(entry.getKey()) == null) {
					// add column
					final TableColumn ontologyColumn = new TableColumn(table, SWT.NONE);
					ontologyColumn.setWidth(150);
					ontologyColumn.setText("Ontology Constant (" + entry.getKey() + ")");
					ontologyColumnsMap.put(entry.getKey(), columnCounter);
					columnCounter++;
				}
				item.setText(ontologyColumnsMap.get(entry.getKey()), entry.getValue());
			}
			
		}
		
		// initialize editor
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		table.addMouseListener(new MouseListener() {
			
			public void mouseDoubleClick(final MouseEvent event) {
				final Rectangle clientArea = table.getClientArea();
				final Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount()) {
					boolean visible = false;
					final TableItem item = table.getItem(index);
					for (int i = 1; i < table.getColumnCount(); i++) {
						final Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final Word word = sentence.getWords().get(index);
							final int column = i;
							if (column == SentenceEditor.BASEFORM_COLUMN || column == SentenceEditor.POSTAG_COLUMN) {
								final Text text = new Text(table, SWT.NONE);
								final Listener textListener = new Listener() {
									
									public void handleEvent(final Event e) {
										switch (e.type) {
											case SWT.FocusOut:
												if (column == SentenceEditor.BASEFORM_COLUMN) {
													word.setBaseForm(text.getText());
												}
												else {
													word.setPennTreebankTag(text.getText());
												}
												word.setDirty(true);
												item.setText(column, text.getText());
												text.dispose();
												break;
											case SWT.Traverse:
												switch (e.detail) {
													case SWT.TRAVERSE_RETURN:
														if (column == SentenceEditor.BASEFORM_COLUMN) {
															word.setBaseForm(text.getText());
														}
														else {
															word.setPennTreebankTag(text.getText());
														}
														word.setDirty(true);
														item.setText(column, text.getText());
														// FALL THROUGH
													case SWT.TRAVERSE_ESCAPE:
														text.dispose();
														e.doit = false;
												}
												break;
										}
									}
								};
								text.addListener(SWT.FocusOut, textListener);
								text.addListener(SWT.Traverse, textListener);
								editor.setEditor(text, item, i);
								text.setText(item.getText(i));
								text.selectAll();
								text.setFocus();
								return;
							}
							else if (column == SentenceEditor.MARK_COLUMN) {
								new MarkEditor(dialog, word.getMark(), new MarkEditorListener() {
									
									@Override
									public void onEditFinish(final Mark mark) {
										word.setMark(mark);
										word.setDirty(true);
										item.setText(column, (word.getMark() == null) ? "" : word.getMark()
												.getComment());
									}
									
								});
							}
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible) {
						return;
					}
					index++;
				}
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
		
		// buttons
		final Button buttonOK = new Button(dialog, SWT.PUSH);
		buttonOK.setText("OK");
		buttonOK.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button buttonCancel = new Button(dialog, SWT.PUSH);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Listener buttonListener = new Listener() {
			
			public void handleEvent(final Event event) {
				if (event.widget == buttonOK) {
					SentenceEditor.this.returnSentence = sentence;
					dialog.close();
				}
				else if (event.widget == buttonCancel) {
					// close the dialog and return null
					dialog.close();
				}
			}
		};
		
		buttonOK.addListener(SWT.Selection, buttonListener);
		buttonCancel.addListener(SWT.Selection, buttonListener);
		
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
		listener.onEditFinish(this.returnSentence);
		
	}
}
