package de.uka.ipd.resi.gui;

import org.eclipse.swt.widgets.Composite;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.ontologyimpl.ConceptNetOntology;
import de.uka.ipd.resi.ontologyimpl.CycOntology;
import de.uka.ipd.resi.ontologyimpl.WordNetOntology;
import de.uka.ipd.resi.ontologyimpl.YagoOntology;

/**
 * Composite for adding Ontologies to the SpecificationImprover.
 * 
 * @author Torben Brumm
 */
public class OntologyAdder extends Adder {
	
	/**
	 * Listener that is fired when an Ontology is added.
	 * 
	 * @author Torben Brumm
	 */
	public interface OntologyAdderListener {
		
		/**
		 * Fired when a new Ontology is added.
		 * 
		 * @param ontology Ontology to add.
		 * @param ontologyName Name of the Ontology to add.
		 * @throws NullPointerException
		 */
		public void onAdd(Ontology ontology, String ontologyName) throws NullPointerException;
		
		/**
		 * Fired before a new Ontology is added.
		 * 
		 * @param ontologyName Name of the Ontology to add.
		 */
		public void onBeforeAdd(String ontologyName);
	}
	
	/**
	 * Listener that is called when an Ontology is added.
	 */
	private final OntologyAdderListener listener;
	
	/**
	 * Constructor.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 * @param listener Listener that is called when an Ontology is added.
	 */
	public OntologyAdder(final Composite parent, final int style, final OntologyAdderListener listener) {
		super(parent, style);
		this.listener = listener;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getItemName()
	 */
	@Override
	protected String getItemName() {
		return "Ontologies";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getPossibleItems()
	 */
	@Override
	protected String[] getPossibleItems() {
		return new String[] { "ResearchCyc", "WordNet", "ConceptNet", "YAGO"
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#onAdd(int)
	 */
	@Override
	protected String onAdd(final int selectedItemIndex) throws NullPointerException {
		final String ontologyName = this.getPossibleItems()[selectedItemIndex];
		this.listener.onBeforeAdd(ontologyName);
		Ontology o = null;
		try {
			switch (selectedItemIndex) {
				case 0:
					o = new CycOntology(Application.CYC_SERVER_ADDRESS, Application.CYC_SERVER_PORT);
					break;
				case 1:
					o = new WordNetOntology(Application.WORDNET_SERVER_ADDRESS, Application.WORDNET_SERVER_PORT);
					break;
				case 2:
					o = new ConceptNetOntology();
					break;
				case 3:
					o = new YagoOntology();
					break;
				default:
					// do nothing
			}
			this.listener.onAdd(o, ontologyName);
		}
		catch (final NotConnectedException ex) {
			Application.getInstance().handleException(ex);
			throw new NullPointerException();
		}
		return ontologyName;
	}
	
}
