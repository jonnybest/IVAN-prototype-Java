package de.uka.ipd.resi.exceptions;

import de.uka.ipd.resi.Ontology;

/**
 * Exception which is thrown when an ontology can't be connected to.
 * 
 * @author Torben Brumm
 */
public class NotConnectedException extends Exception {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param o Ontology which can't be connected to.
	 * @param reason Exception which causes the connect to fail.
	 */
	public NotConnectedException(final Ontology o, final Throwable reason) {
		super(o.getName() + " ontology " + o.toString() + ": " + reason);
	}
}
