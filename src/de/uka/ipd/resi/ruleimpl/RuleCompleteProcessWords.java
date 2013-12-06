package de.uka.ipd.resi.ruleimpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphCompleteProcessWords;
import de.uka.ipd.resi.ontologyinterface.OntologyCompleteProcessWords;

/**
 * Rule which gives advice for completing uncompletely specified process words.
 * 
 * @author Torben Brumm
 */
public class RuleCompleteProcessWords extends Rule {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RuleCompleteProcessWords.class);
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#apply(de.uka.ipd.specificationimprover.Graph,
	 * de.uka.ipd.specificationimprover.Ontology)
	 */
	@Override
	public void apply(final Graph g, final Ontology o) throws WordNotFoundException, NotConnectedException,
			SyntaxException {
		RuleCompleteProcessWords.logger.debug("[>>>]apply( g = {}, o = {} )", g, o);
		if (g.supports(this) && o.supports(this)) {
			o.clearConstantCache();
			final OntologyCompleteProcessWords o2 = (OntologyCompleteProcessWords) o;
			final GraphCompleteProcessWords g2 = (GraphCompleteProcessWords) g;
			// Alle Verben holen
			final List<List<VerbFrame>> verbs = g2.getProcessWords();
			for (final List<VerbFrame> verb : verbs) {
				boolean frameForOntologyExists = false;
				final List<VerbFrame> suggestionVerbframes = new ArrayList<VerbFrame>();
				VerbFrame frame = null;
				Word verbAsWord = null;
				try {
					for (final VerbFrame currentVerbFrame : verb) {
						if (currentVerbFrame.getOntologyName() != null
								&& currentVerbFrame.getOntologyName().equals(o.getName())) {
							verbAsWord = currentVerbFrame.getWord();
							// show and update
							final List<VerbFrame> tempList = new ArrayList<VerbFrame>();
							tempList.add(currentVerbFrame);
							frame = Application.getInstance().selectVerbFrame(verbAsWord,
									o2.getProcessWordArgs(verbAsWord), tempList, o.getName());
							frameForOntologyExists = true;
						}
						else if (currentVerbFrame.getOntologyName() == null) {
							suggestionVerbframes.add(currentVerbFrame);
						}
					}
					if (!frameForOntologyExists) {
						verbAsWord = verb.get(0).getWord();
						frame = Application.getInstance().selectVerbFrame(verbAsWord,
								o2.getProcessWordArgs(verbAsWord), suggestionVerbframes, o.getName());
					}
					if (frame != null) {
						g2.updateVerbFrame(frame);
					}
				}
				catch (final WordNotFoundException e) {
					// TODO for now we just ignore it so we can continue with the next verb
				}
				finally {
					// if we have new information for the graph, we write it back
					if (verbAsWord != null && verbAsWord.isDirty()) {
						g.updateWordAttributes(verbAsWord);
					}
				}
				
			}
		}
		RuleCompleteProcessWords.logger.debug("[<<<]apply()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Gives advice for completing uncompletely specified process words.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getGraphInterface()
	 */
	@Override
	public String getGraphInterface() {
		return "GraphCompleteProcessWords";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getName()
	 */
	@Override
	public String getName() {
		return "CompleteProcessWords";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getOntologyInterface()
	 */
	@Override
	public String getOntologyInterface() {
		return "OntologyCompleteProcessWords";
	}
	
}
