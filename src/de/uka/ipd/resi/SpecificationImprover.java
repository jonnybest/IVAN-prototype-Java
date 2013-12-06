 package de.uka.ipd.resi;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphimpl.GrGenGraph;
import de.uka.ipd.resi.ontologyinterface.OntologyBaseFormTag;
import de.uka.ipd.resi.ontologyinterface.OntologyPOSTag;

/**
 * The center of the application.
 * 
 * @author Torben Brumm
 */
public class SpecificationImprover {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SpecificationImprover.class);
	
	/**
	 * "Ontology" (in fact it is a tagger) for adding base forms to words.
	 */
	private OntologyBaseFormTag baseFormTagger;
	
	/**
	 * List of graphs on which the rules are applied.
	 */
	private final Set<Graph> graphs = new HashSet<Graph>();
	
	/**
	 * List of ontologies whoch help applying the rules.
	 */
	private final Set<Ontology> ontologies = new HashSet<Ontology>();
	
	/**
	 * "Ontology" (in fact it is a tagger) for adding Penn Treebank tags to words.
	 */
	private OntologyPOSTag posTagger;
	
	/**
	 * List of rules which are applied.
	 */
	private final Set<Rule> rules = new HashSet<Rule>();
	
	/**
	 * Adds a graph to apply the rules to it.
	 * 
	 * @param g Graph to add.
	 */
	public void addGraph(final Graph g) {
		this.graphs.add(g);
	}
	
	/**
	 * Adds an ontology to use it while applying the rules.
	 * 
	 * @param o Ontology to add.
	 */
	public void addOntology(final Ontology o) {
		this.ontologies.add(o);
	}
	
	/**
	 * Add a rule to apply.
	 * 
	 * @param r Rule to add.
	 */
	public void addRule(final Rule r) {
		this.rules.add(r);
	}
	
	/**
	 * Writes the graphs into the specified file.
	 * 
	 * @param filename Filename.
	 */
	public void exportToFile(final String filename) {
		SpecificationImprover.logger.debug("[>>>]exportToFile( filename = {} )", filename);
		// TODO just a simple solution for now
		for (final Graph g : this.graphs) {
			if (g instanceof GrGenGraph) {
				((GrGenGraph) g).exportToFile(filename);
			}
		}
		SpecificationImprover.logger.debug("[<<<]exportToFile()");
	}
	
	/**
	 * Return all sentences of the specification (from all graphs).
	 * 
	 * @return All sentences from the graphs.
	 * @throws SyntaxException
	 */
	public Sentence[] getCurrentSpecification() throws SyntaxException {
		SpecificationImprover.logger.debug("[>>>]getCurrentSpecification()");
		Sentence[] returnValue = new Sentence[0];
		for (final Graph g : this.graphs) {
			final Sentence[] oldReturnArray = returnValue;
			final Sentence[] sentencesToAdd = g.getSentences();
			returnValue = new Sentence[oldReturnArray.length + sentencesToAdd.length];
			for (int i = 0; i < oldReturnArray.length; i++) {
				returnValue[i] = oldReturnArray[i];
			}
			for (int i = 0; i < sentencesToAdd.length; i++) {
				returnValue[i + oldReturnArray.length] = sentencesToAdd[i];
			}
		}
		SpecificationImprover.logger.debug("[<<<]getCurrentSpecification(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Are there any Graphs added to this Improver?
	 * 
	 * @return Graphs added?
	 */
	public boolean hasGraphs() {
		return !this.graphs.isEmpty();
	}
	
	/**
	 * Are there any Ontologies added to this Improver?
	 * 
	 * @return Ontologies added?
	 */
	public boolean hasOntologies() {
		return !this.ontologies.isEmpty();
	}
	
	/**
	 * Are there any Rules added to this Improver?
	 * 
	 * @return Rules added?
	 */
	public boolean hasRules() {
		return !this.rules.isEmpty();
	}
	
	/**
	 * Main function: Applies all rules to all graphs.
	 * 
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 * @throws SyntaxException
	 */
	public void improve() throws WordNotFoundException, SyntaxException, NotConnectedException {
		SpecificationImprover.logger.debug("[>>>]improve()");
		for (final Graph g : this.graphs) {
			for (final Rule r : this.rules) {
				SpecificationImprover.logger.info("Regel: " + r.getDescription());
				for (final Ontology o : this.ontologies) {
					r.apply(g, o);
				}
			}
		}
		SpecificationImprover.logger.debug("[<<<]improve()");
	}
	
	/**
	 * Preannotates the words of the graphs with base forms and Penn Treebank tags. Needed for better results.
	 * 
	 * @throws WordNotFoundException
	 * @throws SyntaxException
	 */
	public void preAnnotateGraphs() throws WordNotFoundException, SyntaxException {
		SpecificationImprover.logger.debug("[>>>]preAnnotateGraphs()");
		for (final Graph g : this.graphs) {
			for (final Sentence sentence : g.getSentences()) {
				if (this.posTagger != null) {
					this.posTagger.tagSentenceWithPOS(sentence);
				}
				if (this.baseFormTagger != null) {
					this.baseFormTagger.tagSentenceWithBaseForm(sentence);
				}
				for (final Word word : sentence.getWords()) {
					if (word.isDirty()) {
						g.updateWordAttributes(word);
					}
				}
			}
		}
		SpecificationImprover.logger.debug("[<<<]preAnnotateGraphs()");
	}
	
	/**
	 * Sets the "ontology" (in fact it is a tagger) for adding base forms to words.
	 * 
	 * @param baseFormTagger Base form tagger to use.
	 */
	public void setBaseFormTagger(final OntologyBaseFormTag baseFormTagger) {
		this.baseFormTagger = baseFormTagger;
	}
	
	/**
	 * Sets the "ontology" (in fact it is a tagger) for adding Penn Treebank tags to words.
	 * 
	 * @param posTagger POS tagger to use.
	 */
	public void setPOSTagger(final OntologyPOSTag posTagger) {
		this.posTagger = posTagger;
	}
}
