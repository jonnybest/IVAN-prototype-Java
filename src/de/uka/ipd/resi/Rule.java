package de.uka.ipd.resi;

import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;

/**
 * @author Torben Brumm
 */
public abstract class Rule {
	
	/**
	 * Applies the rule to the given graph with information from the given ontology.
	 * 
	 * @param g Graph on which the rule is applied.
	 * @param o Ontology which helps applying the rule.
	 * @throws WordNotFoundException
	 * @throws SyntaxException
	 * @throws NotConnectedException
	 */
	public abstract void apply(Graph g, Ontology o) throws WordNotFoundException, SyntaxException,
			NotConnectedException;
	
	/**
	 * Returns a description of this rule.
	 * 
	 * @return Description of this rule.
	 */
	public abstract String getDescription();
	
	/**
	 * Returns the name of the graph interface which provides the functions this rule needs.
	 * 
	 * @return Interface name.
	 */
	public abstract String getGraphInterface();
	
	/**
	 * Returns the type of the rule.
	 * 
	 * @return Name of rule type.
	 */
	public abstract String getName();
	
	/**
	 * Returns the name of the ontology interface which provides the functions this rule needs.
	 * 
	 * @return Interface name.
	 */
	public abstract String getOntologyInterface();
	
}
