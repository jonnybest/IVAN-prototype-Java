package de.uka.ipd.resi.ontologyimpl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opencyc.api.CycAccess;
import org.opencyc.api.CycApiException;
import org.opencyc.cycobject.CycConstant;
import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycSymbol;
import org.opencyc.cycobject.CycVariable;
import org.opencyc.cycobject.DefaultCycObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Ontology;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.VerbFrame.VerbArgument;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.ontologyinterface.OntologyCheckDeterminers;
import de.uka.ipd.resi.ontologyinterface.OntologyCheckForNominalization;
import de.uka.ipd.resi.ontologyinterface.OntologySimilarMeaning;
import de.uka.ipd.resi.ontologyinterface.OntologyCompleteProcessWords;
import de.uka.ipd.resi.ontologyinterface.OntologyAvoidAmbiguousWords;

/**
 * Ontology which connects to a Cyc-Server.
 * 
 * @author Torben Brumm
 */
public class CycOntology extends Ontology implements OntologySimilarMeaning, OntologyCompleteProcessWords,
		OntologyAvoidAmbiguousWords, OntologyCheckForNominalization, OntologyCheckDeterminers {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CycOntology.class);
	
	/**
	 * CycAccess object which does all the work.
	 */
	private final CycAccess cycAccess;
	
	/**
	 * Constructor which connects to a Cyc server.
	 * 
	 * @param serverAddress Address of the server to connect to.
	 * @param serverPort Server port to connect to.
	 * @throws NotConnectedException
	 */
	public CycOntology(final String serverAddress, final int serverPort) throws NotConnectedException {
		CycOntology.logger.debug("[>>>]CycOntology( serverAddress = {}, serverPort = {} )", serverAddress, serverPort);
		try {
			this.cycAccess = new CycAccess(serverAddress, serverPort);
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final CycApiException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		
		/*
		 * To activate Cyc log messages: this.cycAccess.traceOn();
		 */
		CycOntology.logger.info("Connected to Cyc server at {}:{}", serverAddress, serverPort);
		CycOntology.logger.debug("[<<<]CycOntology()");
	}
	
	@Override
	public void assureOntologyConstantIsSelected(final Word word) throws WordNotFoundException, NotConnectedException {
		CycOntology.logger.debug("[>>>]assureOntologyConstantIsSelected( word = {} )", word);
		this.getConstantForWord(word);
		CycOntology.logger.debug("[<<<]assureOntologyConstantIsSelected()");
	}
	
	/**
	 * Returns a CycConstant for the given word. If it is not set in the word, look it up and set it first.
	 * 
	 * @param word Word to get the constant for.
	 * @return CycConstant representing the word.
	 * @throws WordNotFoundException
	 * @throws NotConnectedException
	 */
	@SuppressWarnings("unchecked")
	private CycConstant getConstantForWord(final Word word) throws WordNotFoundException, NotConnectedException {
		CycOntology.logger.debug("[>>>]getConstantForWord( word = {} )", word);
		CycConstant constant = null;
		try {
			if (word.getOntologyConstant(this.getName()) == null) {
				final Iterator<Object> listIterator = this.cycAccess.getDenotsOfString(
						word.getBaseFormIfPresent().replaceAll("_", "")).iterator();
				final ArrayList<Sense> senses = new ArrayList<Sense>();
				final HashMap<String, CycConstant> senseMap = new HashMap<String, CycConstant>();
				while (listIterator.hasNext()) {
					final Object obj = listIterator.next();
					if (obj instanceof CycConstant) {
						final CycConstant tempConstant = (CycConstant) obj;
						String description = "";
						try {
							description = this.cycAccess.getComment(tempConstant);
						}
						catch (final Exception e) {
							// we ignore exceptions...descriptions are not mandatory after all
						}
						senses.add(new Sense(tempConstant.getName(), description));
						senseMap.put(tempConstant.getName(), tempConstant);
					}
				}
				
				this.selectOntologyConstant(word, senses.toArray(new Sense[0]));
				
				constant = senseMap.get(word.getOntologyConstant(this.getName()));
			}
			else {
				constant = this.cycAccess.getKnownConstantByName(word.getOntologyConstant(this.getName()));
			}
			
			if (constant == null) {
				throw new WordNotFoundException(this, word.toString());
			}
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final CycApiException e) {
			throw new WordNotFoundException(this, word.toString());
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		CycOntology.logger.debug("[<<<]getConstantForWord(): {}", constant);
		return constant;
	}
	
	@Override
	protected String getConstantToStoreFromSense(final Sense sense) {
		return sense.getSense();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DeterminerInfo getDeterminerInfoForWord(final Word word) throws WordNotFoundException, NotConnectedException {
		CycOntology.logger.debug("[>>>]getDeterminerInfoForWord( word = {} )", word);
		DeterminerInfo returnValue = null;
		
		CycConstant constant = null;
		
		try {
			constant = this.getConstantForWord(word);
		}
		catch (final WordNotFoundException e) {
			// ignore as we do not need a constant in every scenario
		}
		
		// build constant name (first letter upper case, rest lower case, suffix "-TheWord")
		final String cycTheWordConstantName = word.getBaseFormIfPresent().substring(0, 1).toUpperCase()
				+ word.getBaseFormIfPresent().substring(1).toLowerCase() + "-TheWord";
		
		try {
			// initialize constants
			CycConstant theWord;
			try {
				theWord = this.cycAccess.getKnownConstantByName(cycTheWordConstantName);
			}
			catch (final CycApiException e) {
				throw new WordNotFoundException(this, cycTheWordConstantName);
			}
			final CycConstant denotation = this.cycAccess.getKnownConstantByName("denotation");
			final CycConstant mt = this.cycAccess.getKnownConstantByName("GeneralEnglishMt");
			
			// initialize variables
			final CycVariable determinerType = new CycVariable("?DeterminerType");
			final CycVariable senseCounter = new CycVariable("?SenseCounter");
			final CycVariable constantOrNumberValue = new CycVariable("?ConstantOrNumberValue");
			
			// query ontology
			final CycList gaf = CycList.makeCycList(denotation, theWord, determinerType);
			gaf.add(senseCounter);
			gaf.add(constantOrNumberValue);
			final CycList variables = CycList.makeCycList(determinerType, senseCounter, constantOrNumberValue);
			final CycList response = this.cycAccess.queryVariables(variables, gaf, mt, null);
			if (response != null) {
				for (final Object singleResponseObject : response) {
					final CycList singleResponse = (CycList) singleResponseObject;
					System.out.println("XXXX:" + singleResponse);
					if (singleResponse.first() instanceof CycConstant) {
						final CycConstant determinerConstant = (CycConstant) singleResponse.first();
						if (constant != null && singleResponse.third() instanceof CycConstant
								&& ((CycConstant) singleResponse.third()).equals(constant)) {
							if (determinerConstant.getName().equals("Determiner-Definite")) {
								returnValue = new DeterminerInfo();
								returnValue.setIsDefiniteArticle();
							}
							else if (determinerConstant.getName().equals("Determiner-Indefinite")) {
								returnValue = new DeterminerInfo();
								returnValue.setIsIndefiniteArticle();
							}
							else if (determinerConstant.getName().equals("Determiner-Central")) {
								returnValue = new DeterminerInfo();
								if (constant.getName().equals("No-NLAttr")) {
									returnValue.setRange(0, 0);
								}
								else if (constant.getName().equals("Each-NLAttr")
										|| constant.getName().equals("Every-NLAttr")) {
									returnValue.setRange(DeterminerInfo.INFINITY, DeterminerInfo.INFINITY);
								}
								else {
									returnValue.setRange(0, DeterminerInfo.INFINITY);
								}
							}
						}
						else if (determinerConstant.getName().equals("Number-SP")) {
							final int numberIntValue = (Integer) singleResponse.third();
							returnValue = new DeterminerInfo();
							returnValue.setRange(numberIntValue, numberIntValue);
						}
						if (returnValue != null) {
							// if we found a match, we stop looking
							break;
						}
					}
				}
			}
			
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final CycApiException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		CycOntology.logger.debug("[<<<]getDeterminerInfoForWord(): {}", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Ontology#getName()
	 */
	@Override
	public String getName() {
		return "Cyc";
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.ontologyinterface.OntologyCompleteProcessWords#getProcessWordArgs(de.uka.ipd
	 * .specificationimprover.Word)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<VerbFrame> getProcessWordArgs(final Word processWord) throws WordNotFoundException,
			NotConnectedException {
		CycOntology.logger.debug("[>>>]getProcessWordArgs( processWord = {} )", processWord);
		final ArrayList<VerbFrame> returnValue = new ArrayList<VerbFrame>();
		try {
			final CycConstant verb;
			// build constant name (first letter upper case, rest lower case, suffix "-TheWord")
			final String cycConstantName = processWord.getBaseFormIfPresent().substring(0, 1).toUpperCase()
					+ processWord.getBaseFormIfPresent().substring(1).toLowerCase() + "-TheWord";
			try {
				verb = this.cycAccess.getKnownConstantByName(cycConstantName);
			}
			catch (final CycApiException e) {
				throw new WordNotFoundException(this, cycConstantName);
			}
			
			final CycConstant verbSemTrans = this.cycAccess.getKnownConstantByName("verbSemTrans");
			final CycVariable sense = new CycVariable("?Sense");
			final CycVariable frameType = new CycVariable("?FrameType");
			final CycVariable frame = new CycVariable("?Frame");
			final CycConstant mt = this.cycAccess.getKnownConstantByName("GeneralEnglishMt");
			
			final CycList requestGaf = CycList.makeCycList(verbSemTrans, verb);
			requestGaf.add(sense);
			requestGaf.add(frameType);
			requestGaf.add(frame);
			
			final CycList variables = CycList.makeCycList(sense, frameType, frame);
			final CycList response = this.cycAccess.queryVariables(variables, requestGaf, mt, null);
			
			if (response != null) {
				for (final Object possibleMeaningObject : response) {
					final CycList possibleMeaning = (CycList) possibleMeaningObject;
					final CycList possibleFrame = (CycList) possibleMeaning.third();
					final VerbFrame verbFrame = new VerbFrame(this.getName(), processWord);
					String frameSense = "";
					for (final Object sub : possibleFrame) {
						// ignore conjunctions and symbols
						if (sub instanceof CycConstant || sub instanceof CycSymbol) {
							continue;
						}
						final CycList ab = (CycList) sub;
						
						if (ab.first().toString().equals("isa")) {
							// sense of this frame
							frameSense = ab.third().toString();
						}
						else {
							// it's an argument
							final CycConstant semanticConstant = (CycConstant) ab.first();
							
							// try to get a description for the semantic role
							String semanticDescription = "";
							try {
								semanticDescription = this.cycAccess.getComment(semanticConstant);
							}
							catch (final Exception e) {
								// ignore the exception, description is not mandatory
							}
							
							final VerbArgument verbArgument = new VerbArgument(new Sense(semanticConstant.getName(),
									semanticDescription), ab.third().toString().substring(1));
							
							// find a suggestion from the sentence
							final CycList generalisations = this.cycAccess.getArg2Isas(semanticConstant);
							if (generalisations.size() > 0) {
								if (generalisations.size() > 1) {
									CycOntology.logger.info("More than one Arg2Isa for {} found", semanticConstant
											.toString());
								}
								final CycConstant generalisation = (CycConstant) generalisations.first();
								// TODO support for more than one suggestion (right now it uses the one it found last)
								for (final Word wordInSentence : processWord.getSentence().getWords()) {
									try {
										if (this.cycAccess.isGenlOf(generalisation, this
												.getConstantForWord(wordInSentence))) {
											verbArgument.setWord(wordInSentence);
										}
									}
									catch (final WordNotFoundException e) {
										// ignore (no constant => no suggestion)
									}
								}
							}
							
							verbFrame.getArguments().add(verbArgument);
						}
					}
					// only add frames with correct sense
					if (frameSense.equals(this.getConstantForWord(processWord).toString())) {
						returnValue.add(verbFrame);
					}
				}
				
			}
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		CycOntology.logger.debug("[<<<]getProcessWordArgs(): {}", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.ontologyinterface.OntologyCheckForNominalization#getProcessWordForNominalization
	 * (de.uka.ipd.specificationimprover.Word)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getProcessWordForNominalization(final Word possibleNominalization)
			throws NotConnectedException, WordNotFoundException {
		CycOntology.logger.debug("[<<<]getProcessWordForNominalization( possibleNominalization = {} )",
				possibleNominalization);
		
		final List<String> returnValue = new ArrayList<String>();
		
		try {
			// initialize constants
			final CycConstant singular = this.cycAccess.getKnownConstantByName("singular");
			final CycConstant massNumber = this.cycAccess.getKnownConstantByName("massNumber");
			final CycConstant infinitive = this.cycAccess.getKnownConstantByName("infinitive");
			final CycConstant denotation = this.cycAccess.getKnownConstantByName("denotation");
			final CycConstant verb = this.cycAccess.getKnownConstantByName("Verb");
			final CycConstant mt = this.cycAccess.getKnownConstantByName("GeneralEnglishMt");
			
			// initialize variables
			final CycVariable theWord = new CycVariable("?TheWord");
			final CycVariable senseCounter = new CycVariable("?SenseCounter");
			
			// ask for count nouns
			final CycList requestSingularGaf = CycList.makeCycList(singular, theWord, possibleNominalization
					.getBaseFormIfPresent());
			final CycList variable = CycList.makeCycList(theWord);
			final CycList responseSingular = this.cycAccess.queryVariables(variable, requestSingularGaf, mt, null);
			
			// ask for mass nouns (same variable)
			final CycList requestMassNumerGaf = CycList.makeCycList(massNumber, theWord, possibleNominalization
					.getBaseFormIfPresent());
			final CycList responseMassNumberGaf = this.cycAccess
					.queryVariables(variable, requestMassNumerGaf, mt, null);
			
			// check all found words for validity
			requestSingularGaf.appendElements(responseMassNumberGaf);
			
			for (final Object possibleTheWord : responseSingular) {
				if (((CycList) possibleTheWord).first() instanceof CycConstant) {
					// we only need constants
					final CycConstant theWordConstant = (CycConstant) ((CycList) possibleTheWord).first();
					
					// ask if it exists a verb with the same sense
					final CycList requestGafCheckForSense = CycList.makeCycList(denotation, theWordConstant, verb);
					requestGafCheckForSense.add(senseCounter);
					requestGafCheckForSense.add(this.getConstantForWord(possibleNominalization));
					final CycList variablesForCheckForSense = CycList.makeCycList(senseCounter);
					final CycList responseCheckForSense = this.cycAccess.queryVariables(variablesForCheckForSense,
							requestGafCheckForSense, mt, null);
					
					if (responseCheckForSense.size() > 0) {
						// get the infinitive
						for (final Object possibleInfinitive : this.cycAccess.getArg2s(infinitive, theWordConstant, mt)) {
							final String possibleProcessWord = possibleInfinitive.toString();
							// if not already added, add it
							if (!returnValue.contains(possibleProcessWord)) {
								returnValue.add(possibleProcessWord);
							}
						}
					}
					
				}
			}
			
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final CycApiException e) {
			throw new WordNotFoundException(this, possibleNominalization.toString());
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		CycOntology.logger.debug("[<<<]getProcessWordForNominalization(): {}", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.ontologyinterface.OntologyCompareNouns#getSimilarity(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public float getSimilarity(final Word noun1, final Word noun2) throws WordNotFoundException, NotConnectedException {
		CycOntology.logger.debug("[>>>]getSimilarity( noun1 = {}, noun2 = {} )", noun1, noun2);
		float returnValue = 0;
		try {
			final CycConstant cycConstant1 = this.getConstantForWord(noun1);
			final CycConstant cycConstant2 = this.getConstantForWord(noun2);
			if (this.cycAccess.isGenlOf(cycConstant1, cycConstant2)) {
				// TODO return more precise value
				returnValue = 1;
			}
			else if (this.cycAccess.isGenlOf(cycConstant2, cycConstant1)) {
				returnValue = -1;
			}
		}
		catch (final UnknownHostException e) {
			throw new NotConnectedException(this, e);
		}
		catch (final CycApiException e) {
			throw new WordNotFoundException(this, noun1.toString() + " & " + noun2.toString());
		}
		catch (final IOException e) {
			throw new NotConnectedException(this, e);
		}
		CycOntology.logger.debug("[<<<]getSimilarity(): {}", returnValue);
		return returnValue;
	}
	
}
