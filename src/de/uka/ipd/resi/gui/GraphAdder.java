package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.graphimpl.GrGenGraph;

/**
 * Composite for adding Graphs to the SpecificationImprover.
 * 
 * @author Torben Brumm
 */
public class GraphAdder extends Adder {
	
	/**
	 * Listener that is fired when a Graph is added.
	 * 
	 * @author Torben Brumm
	 */
	public interface GraphAdderListener {
		
		/**
		 * Fired when a new Graph is added.
		 * 
		 * @param graph Graph to add.
		 * @param graphName Name of the Graph to add.
		 * @throws NullPointerException
		 */
		public void onAdd(Graph graph, String graphName) throws NullPointerException;
		
		/**
		 * Fired before a new Graph is added.
		 * 
		 * @param graphName Name of the Graph to add.
		 */
		public void onBeforeAdd(String graphName);
	}
	
	/**
	 * Listener for this Adder.
	 */
	private final GraphAdderListener listener;
	
	/**
	 * Constructor.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 * @param listener Listener that is called when a Graph is added.
	 */
	public GraphAdder(final Composite parent, final int style, final GraphAdderListener listener) {
		super(parent, style);
		this.listener = listener;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getItemName()
	 */
	@Override
	protected String getItemName() {
		return "Graphs";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getPossibleItems()
	 */
	@Override
	protected String[] getPossibleItems() {
		return new String[] { "GrGen"
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#onAdd(int)
	 */
	@Override
	protected String onAdd(final int selectedItemIndex) throws NullPointerException {
		String graphName = this.getPossibleItems()[selectedItemIndex];
		this.listener.onBeforeAdd(graphName);
		Graph g = null;
		switch (selectedItemIndex) {
			case 0:
				final FileDialog fd = new FileDialog(GraphAdder.this.getShell(), SWT.OPEN);
				fd.setText("Open File");
				final String[] filterExt = { "*.gxl", "*.*"
				};
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				if (selected != null) {
					g = new GrGenGraph(selected);
					graphName += " (" + selected + ")";
				}
				break;
			
			default:
				// do nothing
		}
		GraphAdder.this.listener.onAdd(g, graphName);
		return graphName;
	}
	
}
