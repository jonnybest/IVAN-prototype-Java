package de.uka.ipd.resi.ontologyimpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javatools.database.Database;
import javatools.database.PostgresDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yago.queryprocessing.QueryProcessor;
import yago.queryprocessing.QueryProcessor.InvalidTripleException;
import yago.queryprocessing.QueryProcessor.Template;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;

/**
 * TODO Translate comments when finished. TODO insert log TODO make configurable
 * 
 * @author Torben Brumm
 */
public class YagoOntology extends Ontology implements OntologySimilarMeaning {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(YagoOntology.class);
	
	/**
	 * Maximale Suchtiefe, fuer die Ergebnisse geliefert werden soll.
	 */
	private static final int MAX_SEARCH_DEPTH = 2;
	
	/**
	 * Adresse des YAGO-Servers.
	 */
	private static String SERVER_ADDRESS = "127.0.0.1";
	
	/**
	 * Datenbank auf dem YAGO-Server.
	 */
	private static final String SERVER_DATABASE = "yago";
	
	/**
	 * Passwort fuer den YAGO-Server.
	 */
	private static final String SERVER_PASSWORD = "yago";
	
	/**
	 * Port des YAGO-Servers.
	 */
	private static int SERVER_PORT = 5432;
	
	/**
	 * Benutzername fuer den YAGO-Server.
	 */
	private static final String SERVER_USER = "yago";
	
	/**
	 * Konstruktor, der testweise eine Verbindung zur YAGO-Datenbank herstellt.
	 */
	public YagoOntology() {
		// Wir testen nur, ob wir eine Verbindung zur DB bekommen
		try {
			final Database db = this.getConnectedDB();
			db.close();
		}
		catch (final InstantiationException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final IllegalAccessException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final ClassNotFoundException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final SQLException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
	}
	
	/**
	 * Liefert eine Datenbank zurueck, die bereits verbunden ist. Diese Verbindung muss am ende wieder geschlossen
	 * werden.
	 * 
	 * @return verbundene Datenbank.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Database getConnectedDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException {
		return new PostgresDatabase(YagoOntology.SERVER_USER, YagoOntology.SERVER_PASSWORD,
				YagoOntology.SERVER_DATABASE, YagoOntology.SERVER_ADDRESS, YagoOntology.SERVER_PORT + "");
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
		return "YAGO";
	}
	
	/**
	 * Rueckgabewert, wenn bei Suchtiefe searchDepth ein Treffer aufgetreten ist.
	 * 
	 * @param searchDepth Suchtiefe.
	 * @return Wahrscheinlichkeitswert.
	 */
	private float getReturnValue(final int searchDepth) {
		// TODO evtl. ueberarbeiten
		return 1 - (searchDepth) * .05f;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyCompareNouns#getSimilarity(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public float getSimilarity(final Word noun1, final Word noun2) throws WordNotFoundException {
		YagoOntology.logger.debug("[>>>]getSimilarity( noun1 = {}, noun2 = {} )", noun1, noun2);
		float returnValue = 0;
		try {
			final Database db = this.getConnectedDB();
			final String yagoObject1 = this.getYagoObject(db, noun1.getBaseFormIfPresent());
			final String yagoObject2 = this.getYagoObject(db, noun2.getBaseFormIfPresent());
			if (yagoObject1.equals(yagoObject2)) {
				returnValue = this.getReturnValue(0);
			}
			else {
				for (int i = 1; i <= YagoOntology.MAX_SEARCH_DEPTH; i++) {
					if (this.isSubClassOf(db, yagoObject1, yagoObject2, i, "isA")) {
						returnValue = (-1) * this.getReturnValue(i);
						break;
					}
					if (this.isSubClassOf(db, yagoObject2, yagoObject1, i, "isA")) {
						returnValue = this.getReturnValue(i);
						break;
					}
					if (this.isSubClassOf(db, yagoObject1, yagoObject2, i, "type")) {
						returnValue = (-1) * this.getReturnValue(i);
						break;
					}
					if (this.isSubClassOf(db, yagoObject2, yagoObject1, i, "type")) {
						returnValue = this.getReturnValue(i);
						break;
					}
					if (this.isSubClassOf(db, yagoObject1, yagoObject2, i, null)) {
						returnValue = (-1) * this.getReturnValue(i);
						break;
					}
					if (this.isSubClassOf(db, yagoObject2, yagoObject1, i, null)) {
						returnValue = this.getReturnValue(i);
						break;
					}
				}
			}
			db.close();
		}
		catch (final InstantiationException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final IllegalAccessException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final ClassNotFoundException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final SQLException e1) {
			logger.error(e1.getMessage() + "\n" + e1.getStackTrace());
		}
		catch (final InvalidTripleException e) {
			logger.error(e.getMessage() + "\n" + e.getStackTrace());
		}
		YagoOntology.logger.debug("[<<<]getSimilarity(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Baut aus einem Wort das YAGO-Objekt, unter dem es in der Datenbank zu finden ist.
	 * 
	 * @param db Datenbankverbindung.
	 * @param word Wort, zu dem ein YAGO-Objekt gefunden werden soll.
	 * @return zugehoeriges YAGO-Objekt.
	 * @throws InvalidTripleException
	 * @throws WordNotFoundException
	 */
	private String getYagoObject(final Database db, final String word) throws InvalidTripleException,
			WordNotFoundException {
		YagoOntology.logger.debug("[>>>]getYagoObject(db = {}, word = {})", db, word);
		String returnValue;
		final ArrayList<Template> list = new ArrayList<Template>();
		list.add(new Template("?id0", "means", "\"" + word + "\"", "?object"));
		final Iterator<Map<String, String>> iterator = StaticQueryProcessor.solutions(list, db, 1).iterator();
		if (iterator.hasNext()) {
			returnValue = iterator.next().get("?object");
		}
		else {
			list.clear();
			list.add(new Template("?id0", "?relation", word, "?object"));
			if (StaticQueryProcessor.solutions(list, db, 1).size() > 0) {
				returnValue = word;
			}
			else {
				throw new WordNotFoundException(this, word);
			}
		}
		YagoOntology.logger.debug("[<<<]getYagoObject(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Ueberprueft, ob possibleSubClass eine Unterklasse von possibleSuperClass ist. Dabei ist die genaue Suchtiefe
	 * anzugeben, fuer die diese Bedingung gelten soll. Ueber die preCondition kann man die unterste Abfrage steuern, wenn
	 * noetig (z.B. x isA subClassOf subClassOf y).
	 * 
	 * @param db Datenbankverbindung, auf der die Abfrage stattfinden soll.
	 * @param possibleSubClass moegliche Unterklasse.
	 * @param possibleSuperClass moegliche Oberklasse.
	 * @param exactSearchDepth genaue Suchtiefe (Minimum 1).
	 * @param preCondition wenn die erste Abfrage anders sein soll, angeben, sonst null.
	 * @return Ist possibleSubClass eine Unterklasse von possibleSuperClass (bei der angegebenen Suchtiefe)?
	 * @throws InvalidTripleException
	 */
	private boolean isSubClassOf(final Database db, final String possibleSubClass, final String possibleSuperClass,
			final int exactSearchDepth, String preCondition) throws InvalidTripleException {
		YagoOntology.logger.debug(
				"[>>>]isSubClassOf(db = {}, noun1 = {}, noun2 = {}, exactSearchDepth = {}, preCondition = {})",
				new Object[] { db, possibleSubClass, possibleSuperClass, exactSearchDepth, preCondition
				});
		boolean returnValue = false;
		
		// nur weitermachen, wenn die Suchtiefe ueberhaupt groesser als 0 ist
		if (exactSearchDepth > 0) {
			
			// Initialisierungen
			final ArrayList<Template> list = new ArrayList<Template>();
			int templateId = 0;
			int variableId = 0;
			
			// Wenn keine Vorbedingung angegeben wurde, ist auch die erste Suchstufe "subClassOf"
			if (preCondition == null) {
				preCondition = "subClassOf";
			}
			
			// Bei Suchtiefe 1 brauchen wir nur ein Template
			if (exactSearchDepth == 1) {
				list.add(new Template("?id" + templateId, preCondition, possibleSubClass, possibleSuperClass));
			}
			
			// sonst Templateliste aufbauen
			else {
				
				// erstes Template mit dem ersten Wort und einer Variable
				list.add(new Template("?id" + templateId++, preCondition, possibleSubClass, "?x" + variableId));
				
				// je nach Suchtiefe weitere Templates mit Variablen einfuegen
				while (templateId < exactSearchDepth - 1) {
					list.add(new Template("?id" + templateId++, "subClassOf", "?x" + variableId, "?x" + ++variableId));
				}
				
				// letztes Template mit einer Variablen und dem zweiten Wort
				list.add(new Template("?id" + templateId, "subClassOf", "?x" + variableId, possibleSuperClass));
			}
			
			// Wenn ein Treffer da ist, true zurueckliefern
			if (StaticQueryProcessor.solutions(list, db, 1).size() > 0) {
				returnValue = true;
			}
		}
		YagoOntology.logger.debug("[<<<]isSubClassOf(): {}", returnValue);
		return returnValue;
	}
	
	
	/**
	 * Static wrapper for the query processor.
	 * @author Mathias
	 *
	 */
	private static class StaticQueryProcessor {
		private static QueryProcessor qp = new QueryProcessor();
		
		public static Set<Map<String, String>> solutions(ArrayList<Template> list, Database db, int i) throws InvalidTripleException {
			return qp.solutions(list, db, 1);
		}
		
	}
	
}
