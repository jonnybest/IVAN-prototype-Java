package de.uka.ipd.resi.ruleimpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphCheckForNominalization;
import de.uka.ipd.resi.ontologyinterface.OntologyCheckForNominalization;

/**
 * Rule which checks for nominalizations and proposes verbs for replacing them.
 * 
 * @author Torben Brumm
 */
public class RuleCheckForNominalization extends Rule {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RuleCheckForNominalization.class);
	
	@Override
	public void apply(final Graph g, final Ontology o) throws SyntaxException, NotConnectedException,
			WordNotFoundException {
		RuleCheckForNominalization.logger.debug("[>>>]apply( g = {}, o = {} )", g, o);
		if (g.supports(this) && o.supports(this)) {
			o.clearConstantCache();
			final OntologyCheckForNominalization o2 = (OntologyCheckForNominalization) o;
			final GraphCheckForNominalization g2 = (GraphCheckForNominalization) g;
			for (final Word word : g2.getAllNouns()) {
				List<String> possibleProcessWords;
				try {
					possibleProcessWords = o2.getProcessWordForNominalization(word);
				}
				catch (final WordNotFoundException e) {
					// word not found => skip it
					continue;
				}
				finally {
					// if we have new information for the graph, we write it back
					if (word != null && word.isDirty()) {
						g.updateWordAttributes(word);
					}
				}
				if (possibleProcessWords != null && possibleProcessWords.size() > 0) {
					final Mark mark = Application.getInstance().showNominalization(word, possibleProcessWords,
							g2.getNominalizationMark(word));
					g2.setNominalizationMark(word, mark);
				}
			}
		}
		RuleCheckForNominalization.logger.debug("[<<<]apply()");
	}
	
	@Override
	public String getDescription() {
		return "Checks for nominalizations and proposes replacements.";
	}
	
	@Override
	public String getGraphInterface() {
		return "GraphCheckForNominalization";
	}
	
	@Override
	public String getName() {
		return "CheckForNominalization";
	}
	
	@Override
	public String getOntologyInterface() {
		return "OntologyCheckForNominalization";
	}
	
}
