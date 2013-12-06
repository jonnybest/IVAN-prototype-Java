package de.uka.ipd.resi.ruleimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.ontologyinterface.OntologyAvoidAmbiguousWords;

/**
 * Rule which assures that all words get an ontology constant assigned (if there is found one) to avoid later
 * questioning and to find ambiguous words.
 * 
 * @author Torben Brumm
 */
public class RuleAvoidAmbiguousWords extends Rule {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RuleAvoidAmbiguousWords.class);
	
	@Override
	public void apply(final Graph g, final Ontology o) throws SyntaxException, NotConnectedException,
			WordNotFoundException {
		RuleAvoidAmbiguousWords.logger.debug("[>>>]apply( g = {}, o = {} )", g, o);
		if (g.supports(this) && o.supports(this)) {
			RuleAvoidAmbiguousWords.logger.debug("1");
			
			o.clearConstantCache();
			RuleAvoidAmbiguousWords.logger.debug("2");
			for (final Sentence sentence : g.getSentences()) {
				RuleAvoidAmbiguousWords.logger.debug("3");
				for (final Word word : sentence.getWords()) {
					RuleAvoidAmbiguousWords.logger.debug("4");
					final OntologyAvoidAmbiguousWords o2 = (OntologyAvoidAmbiguousWords) o;
					try {
						o2.assureOntologyConstantIsSelected(word);
					}
					catch (final WordNotFoundException e) {
						RuleAvoidAmbiguousWords.logger.debug("5");
						// TODO for now we just ignore it so we can continue with the next word
					}
					finally {
						// if we have new information for the graph, we write it back
						if (word.isDirty()) {
							g.updateWordAttributes(word);
						}
					}
				}
			}
		}
		RuleAvoidAmbiguousWords.logger.debug("[<<<]apply()");
	}
	
	@Override
	public String getDescription() {
		return "Assures that all words get a matching ontology constant if it exists to remove ambiguity.";
	}
	
	@Override
	public String getGraphInterface() {
		return "GraphAvoidAmbiguousWords";
	}
	
	@Override
	public String getName() {
		return "AvoidAmbiguousWords";
	}
	
	@Override
	public String getOntologyInterface() {
		return "OntologyAvoidAmbiguousWords";
	}
	
}
