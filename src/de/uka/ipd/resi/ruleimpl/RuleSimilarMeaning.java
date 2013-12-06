package de.uka.ipd.resi.ruleimpl;

import java.util.ArrayList;
import java.util.Set;

import javatools.datatypes.Pair;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.Application;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphSimilarMeaning;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;

/**
 * Rule whoch checks nouns for their similarity and replaces them if needed.
 * 
 * @author Torben Brumm
 */
public class RuleSimilarMeaning extends Rule {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RuleSimilarMeaning.class);
	
	/**
	 * Threshold value. If a returned value from the ontology is greater than this value, the user should be asked if
	 * the graph should be altered.
	 */
	private static final float threshold = .2f;
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#apply(de.uka.ipd.specificationimprover.Graph,
	 * de.uka.ipd.specificationimprover.Ontology)
	 */
	@Override
	public void apply(final Graph g, final Ontology o) throws WordNotFoundException, SyntaxException,
			NotConnectedException {
		RuleSimilarMeaning.logger.debug("[>>>]apply( g = {}, o = {} )", g, o);
		if (g.supports(this) && o.supports(this)) {
			o.clearConstantCache();
			final OntologySimilarMeaning o2 = (OntologySimilarMeaning) o;
			final GraphSimilarMeaning g2 = (GraphSimilarMeaning) g;
			final Set<Word> nouns = g2.getAllNouns();
			final ArrayList<Pair<Word, String>> proposedReplacements = new ArrayList<Pair<Word, String>>();
			final ArrayList<Pair<String, Word>> automaticReplacements = new ArrayList<Pair<String, Word>>();
			final ArrayList<Pair<String, String>> replacementsToSkip = new ArrayList<Pair<String, String>>();
			for (final Word noun1 : nouns) {
				for (final Word noun2 : nouns) {
					if (noun2.compareToIgnoreCase(noun1) > 0) {
						if (automaticReplacements.contains(new Pair<String, Word>(noun1.getBaseFormIfPresent(), noun2))) {
							g2.replaceNoun(noun1, noun2);
						}
						else if (automaticReplacements.contains(new Pair<String, Word>(noun2.getBaseFormIfPresent(),
								noun1))) {
							g2.replaceNoun(noun2, noun1);
						}
						try {
							final float similarity;
							try {
								similarity = o2.getSimilarity(noun1, noun2);
							}
							finally {
								// write possible changes to graph no matter what
								if (noun1.isDirty()) {
									g.updateWordAttributes(noun1);
								}
								if (noun2.isDirty()) {
									g.updateWordAttributes(noun2);
								}
							}
							if (Math.abs(similarity) > RuleSimilarMeaning.threshold) {
								Word oldNoun = noun2;
								Word newNoun = noun1;
								if (similarity < 0) {
									// switch nouns
									oldNoun = noun1;
									newNoun = noun2;
								}
								
								final Pair<String, String> skipReplacement = new Pair<String, String>(oldNoun
										.getBaseFormIfPresent(), noun2.getBaseFormIfPresent());
								if (!replacementsToSkip.contains(skipReplacement)) {
									final Pair<Word, String> replacementToPropose = new Pair<Word, String>(oldNoun,
											newNoun.getBaseFormIfPresent());
									if (!proposedReplacements.contains(replacementToPropose)) {
										proposedReplacements.add(replacementToPropose);
										final Pair<Boolean, Boolean> userDecision = Application.getInstance()
												.checkReplaceNoun(oldNoun, newNoun, Math.abs(similarity), o.getName());
										if (userDecision.first) {
											g2.replaceNoun(oldNoun, newNoun);
											if (userDecision.second) {
												automaticReplacements.add(new Pair<String, Word>(oldNoun
														.getBaseFormIfPresent(), newNoun));
											}
										}
										else if (userDecision.second) {
											replacementsToSkip.add(skipReplacement);
										}
									}
								}
							}
						}
						catch (final WordNotFoundException e) {
							// word not found => do nothing
						}
					}
				}
				
			}
		}
		RuleSimilarMeaning.logger.debug("[<<<]apply()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Checks nouns for their similarity.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getGraphInterface()
	 */
	@Override
	public String getGraphInterface() {
		return "GraphSimilarMeaning";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getName()
	 */
	@Override
	public String getName() {
		return "SimilarMeaning";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Rule#getOntologyInterface()
	 */
	@Override
	public String getOntologyInterface() {
		return "OntologySimilarMeaning";
	}
	
}
