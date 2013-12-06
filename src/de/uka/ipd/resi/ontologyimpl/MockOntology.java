package de.uka.ipd.resi.ontologyimpl;

import de.uka.ipd.resi.Ontology;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;

public class MockOntology extends Ontology implements OntologySimilarMeaning {
	
	@Override
	protected String getConstantToStoreFromSense(final Sense sense) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Ontology#getName()
	 */
	@Override
	public String getName() {
		return "Mock";
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyCompareNouns#getSimilarity(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public float getSimilarity(final Word noun1, final Word noun2) {
		if (noun1.getBaseFormIfPresent().equalsIgnoreCase(noun2.getBaseFormIfPresent())) {
			return 1;
		}
		return 0;
	}
	
}
