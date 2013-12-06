package de.uka.ipd.resi.ruleimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphCheckDeterminers;
import de.uka.ipd.resi.ontologyinterface.OntologyCheckDeterminers;

/**
 * Rule which checks all determiners and asks if correctly used.
 * 
 * @author Torben Brumm
 */
public class RuleCheckDeterminers extends Rule {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RuleCheckDeterminers.class);
	
	@Override
	public void apply(final Graph g, final Ontology o) throws WordNotFoundException, SyntaxException,
			NotConnectedException {
		RuleCheckDeterminers.logger.debug("[>>>]apply( g = {}, o = {} )", g, o);
		if (g.supports(this) && o.supports(this)) {
			o.clearConstantCache();
			final OntologyCheckDeterminers o2 = (OntologyCheckDeterminers) o;
			final GraphCheckDeterminers g2 = (GraphCheckDeterminers) g;
			for (final Word word : g2.getAllDeterminers()) {
				
				final DeterminerInfo graphDeterminer = g2.getDeterminerInfoForWord(word);
				
				DeterminerInfo ontologyDeterminer = null;
				try {
					ontologyDeterminer = o2.getDeterminerInfoForWord(word);
				}
				catch (final WordNotFoundException e) {
					// word not found => no suggestion available
				}
				finally {
					// if we have new information for the graph, we write it back
					if (word != null && word.isDirty()) {
						g.updateWordAttributes(word);
					}
				}
				
				final DeterminerInfo newDeterminer = Application.getInstance().selectDeterminer(word, graphDeterminer,
						ontologyDeterminer, o.getName());
				g2.setDeterminerInfoForWord(word, newDeterminer);
			}
		}
		RuleCheckDeterminers.logger.debug("[<<<]apply()");
	}
	
	@Override
	public String getDescription() {
		return "Checks for wrongly used determiners.";
	}
	
	@Override
	public String getGraphInterface() {
		return "GraphCheckDeterminers";
	}
	
	@Override
	public String getName() {
		return "CheckDeterminers";
	}
	
	@Override
	public String getOntologyInterface() {
		return "OntologyCheckDeterminers";
	}
	
}
