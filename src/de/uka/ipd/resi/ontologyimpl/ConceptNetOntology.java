package de.uka.ipd.resi.ontologyimpl;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcProxy;
import redstone.xmlrpc.XmlRpcStruct;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;

/**
 * TODO Translate comments when finished. TODO insert log TODO make configurable
 * 
 * @author Torben Brumm
 */
public class ConceptNetOntology extends Ontology implements OntologySimilarMeaning {
	
	/**
	 * Schnittstelle, ueber die auf die Serverfunktionen zugegriffen werden kann.
	 */
	private static interface ConceptNet {
		
		public XmlRpcArray getFwdRelations(String word, String type, int minimumScore) throws XmlRpcFault;
		
		public XmlRpcArray getRevRelations(String word, String type, int minimumScore) throws XmlRpcFault;
	}
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConceptNetOntology.class);
	
	/**
	 * Maximale Suchtiefe bei Tiefensuche.
	 */
	private static final int MAX_SEARCH_DEPTH = 3;
	
	/**
	 * Minimale Qualitaet ("Score") der Relationen.
	 */
	private static final int MINIMUM_SCORE = 3;
	
	/**
	 * Adresse des ConceptNet-Servers.
	 */
	private static String SERVER_ADDRESS = "127.0.0.1";
	
	/**
	 * Pfad auf dem ConceptNet-Server.
	 */
	private static String SERVER_PATH = "";
	
	/**
	 * Port des ConceptNet-Servers.
	 */
	private static int SERVER_PORT = 9854;
	
	/**
	 * Protokoll des ConceptNet-Servers.
	 */
	private static String SERVER_PROTOCOL = "http";
	
	/**
	 * XMLRPC-Proxy (/-Client), der die Serverfunktionen bereitstellt
	 */
	private ConceptNet client;
	
	private int searchDepth;
	
	/**
	 * Konstruktor, der den ConceptNet-Server-Zugangg initialisiert.
	 */
	public ConceptNetOntology() {
		try {
			this.client = (ConceptNet) XmlRpcProxy.createProxy(new URL(ConceptNetOntology.SERVER_PROTOCOL,
					ConceptNetOntology.SERVER_ADDRESS, ConceptNetOntology.SERVER_PORT, ConceptNetOntology.SERVER_PATH),
					new Class[] { ConceptNet.class
					}, true);
		}
		catch (final MalformedURLException e) {
			// TODO Exception-Handling
			e.printStackTrace();
		}
	}
	
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
		return "ConceptNet";
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyCompareNouns#getSimilarity(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public synchronized float getSimilarity(final Word noun1, final Word noun2) throws WordNotFoundException {
		// TODO Was ist mit mehrdeutigen Woertern?
		ConceptNetOntology.logger.debug("[>>>]getSimilarity( noun1 = {}, noun2 = {} )", noun1, noun2);
		float returnValue = 0;
		final float probability1 = this.hypernymProbability(noun1.getBaseFormIfPresent(), noun2.getBaseFormIfPresent());
		final float probability2 = this.hypernymProbability(noun2.getBaseFormIfPresent(), noun1.getBaseFormIfPresent());
		if (probability1 > probability2) {
			returnValue = probability1;
		}
		else {
			returnValue = (-1) * probability2;
		}
		ConceptNetOntology.logger.debug("[<<<]getSimilarity(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Liefert die Wahrscheinlichkeit (auf eine Skala von 0 bis 1), dass ein Wort ein Hyperonym eines anderen ist (ueber
	 * die Relation 'IsA').
	 * 
	 * @param possibleHypernym moegliches Hyperonym.
	 * @param possibleHyponym moegliches Hyponym
	 * @return Wahrscheinlichkeit (0 bis 1)
	 * @throws WordNotFoundException
	 */
	private synchronized float hypernymProbability(final String possibleHypernym, final String possibleHyponym)
			throws WordNotFoundException {
		ConceptNetOntology.logger.debug("[>>>]hypernymProbability( possibleHypernym = {}, possibleHyponym = {} )",
				possibleHypernym, possibleHyponym);
		if (possibleHypernym.equals(possibleHyponym)) {
			return 1;
		}
		float combinedProbability = 0;
		int scoreSum = 0;
		this.searchDepth++;
		if (this.searchDepth < ConceptNetOntology.MAX_SEARCH_DEPTH) {
			try {
				for (final Object relationObject : this.client.getFwdRelations(possibleHyponym, "IsA",
						ConceptNetOntology.MINIMUM_SCORE)) {
					final XmlRpcStruct relationStruct = (XmlRpcStruct) relationObject;
					final int score = relationStruct.getInteger("score");
					final String newPossibleHyponym = relationStruct.getStruct("stem2").getString("name");
					// Je hoeher eine Relation bewertet wurde, desto groesser ist ihr Einfluss
					scoreSum += score;
					// Nur weiter forschen, wenn noch nichts gefunden wurde (oder ein besserer Treffer vorliegt)
					if (combinedProbability == 0 || possibleHypernym.equals(newPossibleHyponym)) {
						combinedProbability += score * this.hypernymProbability(possibleHypernym, newPossibleHyponym);
					}
				}
			}
			catch (final XmlRpcFault e) {
				// Fehler bei Abfrage => Objekt gibt es nicht
				this.searchDepth--;
				
				// Wenn es die erste Anfrage ist, finden wir den begriff gar nicht, also Exception
				if (this.searchDepth == 0) {
					throw new WordNotFoundException(this, possibleHyponym);
				}
				
				// sonst Sackgasse => 0 zurueckgeben
				return 0;
			}
		}
		this.searchDepth--;
		if (scoreSum == 0) {
			return 0;
		}
		return combinedProbability / scoreSum;
	}
	
}
