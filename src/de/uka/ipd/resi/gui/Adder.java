package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * Abstract Composite for adding items (Rules/Ontologies/Graphs) to the SpecificationImprover.
 * 
 * @author Torben Brumm
 */
public abstract class Adder extends Composite {
	
	/**
	 * Items that are already added.
	 */
	private final List addedItemsList;
	
	/**
	 * Items that can be added.
	 */
	private final List possibleItemsList;
	
	/**
	 * Constructor.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 */
	public Adder(final Composite parent, final int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, true));
		
		final Label possibleRulesListLabel = new Label(this, SWT.CENTER);
		possibleRulesListLabel.setText("Possible " + this.getItemName());
		possibleRulesListLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER
				| GridData.VERTICAL_ALIGN_END));
		
		final Label addedRulesListLabel = new Label(this, SWT.CENTER);
		addedRulesListLabel.setText("Added " + this.getItemName());
		addedRulesListLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_END));
		
		this.possibleItemsList = new List(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		for (final String possibleItem : this.getPossibleItems()) {
			this.possibleItemsList.add(possibleItem);
		}
		this.possibleItemsList.setLayoutData(new GridData(150, 150));
		this.possibleItemsList.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				try {
					final String addedName = Adder.this.onAdd(Adder.this.possibleItemsList.getFocusIndex());
					Adder.this.addedItemsList.add(addedName);
				}
				catch (final NullPointerException ex) {
					// do nothing as we have not added the item to the list
				}
			}
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// not needed
			}
			
		});
		
		this.addedItemsList = new List(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		this.addedItemsList.setLayoutData(new GridData(150, 150));
	}
	
	/**
	 * Returns the name of the items that can be added with this list (in plural).
	 * 
	 * @return Name of the items (plural).
	 */
	protected abstract String getItemName();
	
	/**
	 * Returns an array of Strings that are added to the "possible" list.
	 * 
	 * @return Possible Items.
	 */
	protected abstract String[] getPossibleItems();
	
	/**
	 * Called when an item shall be added. Throws a NullPointerException if there was no item to add (in this case no
	 * new item is added to the list).
	 * 
	 * @param selectedItemIndex Index of the item that shall be added.
	 * @throws NullPointerException
	 */
	protected abstract String onAdd(int selectedItemIndex) throws NullPointerException;
	
	/**
	 * Removes all items from the list of added items.
	 */
	public void removeAddedItems() {
		this.addedItemsList.removeAll();
	}
	
}
