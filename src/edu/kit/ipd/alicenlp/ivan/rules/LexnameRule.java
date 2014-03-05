package edu.kit.ipd.alicenlp.ivan.rules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/** Implements a classification based on wordnet lexical files. 
 * 
 * @author Jonny
 *
 */
public class LexnameRule implements ISentenceRule {

	Logger log = Logger.getLogger(getClass().getName());
	
	private static Dictionary dictionary;

	private Classification result;

	/**
	 * Create a new rule
	 */
	public LexnameRule() {
		// prepare setup
		if(dictionary == null)
			dictionary = BaseRule.setupWordNet();
	}

	@Override
	public boolean apply(CoreMap Sentence) throws JWNLException {
				
		Classification k = classify(Sentence);
		result = k;
		return k != null;
	}

	private Classification classify(CoreMap sentence) throws JWNLException {

		IndexedWord root = sentence.get(
				CollapsedCCProcessedDependenciesAnnotation.class)
				.getFirstRoot();
		
		/**
		 * Old style classification follows.
		 * 
		 */
		// TODO: always return SETUP when root is not a verb. Reason: if nouns
		// or adjectives are more important than the verb, there is no action or
		// event
		// short classification fix for broken sentences (wrong copula)
		// hint1: root is no verb
		if (!BaseRule.isPOSFamily(root, "VB")) {
			// hint 2: there is only one verb
			List<CoreLabel> verbs1 = new ArrayList<CoreLabel>();
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (CoreLabel word1 : labels) {
				if (BaseRule.isPOSFamily(word1, "VB")) {
					verbs1.add(word1);
				}
			}
			List<CoreLabel> verbs = verbs1;
			if (verbs.size() == 1) {
				String word = verbs.get(0).toString();
				// hint 3: the only verb is "to be"
				IndexWord wnetlemma = dictionary
						.lookupIndexWord(POS.VERB, word);
				IndexWord tobe = dictionary.getIndexWord(POS.VERB, "be");
				if (tobe.equals(wnetlemma)) {
					// ex: "Henry, Liv and Paddy are dogs."
					return Classification.SetupDescription;
				}
			}
		}

		// normal classification rules follow:
		SemanticGraph graph = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		// log.info(graph.toString());
		String word = expandVerb(root, graph);
		// classify by grammatical construction
		// classify by lexical file num
		IndexWord wnetw = dictionary.lookupIndexWord(POS.VERB, word);
		if (wnetw != null) {
			wnetw.sortSenses();
			List<Synset> senses = wnetw.getSenses();
			Synset mcs = senses.get(0); // most common sense
			long lexnum = mcs.getLexFileNum();

			if (lexnum == 42) {
				// stative
				// TODO: make sure this actually refers to a state; not a
				// changing
				// state
				return Classification.SetupDescription;
			} else if (senses.size() > 1 && senses.get(1).getLexFileNum() == 42) {
				return Classification.SetupDescription;
			} else if (lexnum == 36) // verb.creation
			{
				// ex: "The roof of the shed is painted blue, like the sky."
				boolean passive = BaseRule.isPassive(root, graph);
				if (passive) {
					if (!BaseRule.hasAgent(root, graph)) {
						// ex: The roof is painted by the father.
						return Classification.ActionDescription;
					}
					return Classification.SetupDescription;
				}
			}
		} else {
			log.warning( "WordNET did not recognise this verb.");
			Span errorspan = new Span(
					root.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
					root.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
			IvanErrorMessage err = new IvanErrorMessage(
					IvanErrorType.WORDNET,
					errorspan,
					"The word '"
							+ word
							+ "' is not properly recognised and may cause problems.");
			sentence.set(IvanAnnotations.ErrorMessageAnnotation.class, err);
			return Classification.ErrorDescription;
		}
		
		return null;
	}


	/**
	 * Finds whole word to multi-word verbs like phrasal verbs
	 * 
	 * @param graph
	 *            The sentence this word occurs in
	 * @param word
	 *            The word to find parts for
	 * @return The whole verb (in base form) as it exists in WordNet
	 * @throws JWNLException
	 */
	protected String expandVerb(IndexedWord word, SemanticGraph graph)
			throws JWNLException {
		String lemma = word.lemma();
		if (BaseRule.hasParticle(word, graph)) {
			String particle = null;
			particle = BaseRule.getParticle(word, graph).word();
			// System.err.println(particle);
			String combinedword = lemma + " " + particle;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;
			}
		} else if (BaseRule.hasPrepMod(word, graph)) {
			String prepmod = null;
			prepmod = BaseRule.getPrepMod(word, graph).word();
			// System.err.println(prepmod);
			String combinedword = lemma + " " + prepmod;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;
			}
		} else if (BaseRule.hasDirectObjectNP(word, graph)) {
			String dirobstr = null;
			IndexedWord direObj = null;
			direObj = BaseRule.getDirectObject(word, graph);
			CoreLabel det = BaseRule.getDeterminer(direObj, graph);
			if (det != null) {
				dirobstr = det.word() + " " + direObj.word();
			} else {
				dirobstr = direObj.word();
			}
			// System.err.println(direObj);
			String combinedword = lemma + " " + dirobstr;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;
			}
		}
		return lemma;
	}


	protected Boolean hasWordNetEntry(String verb) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, verb);
		if (word == null) {
			word = dictionary.lookupIndexWord(POS.VERB, verb);
			if (word == null || !word.getLemma().equals(verb)) {
				// skip
				//System.err.println("-- Cannot find word \"" + verb + "\" in WordNet dictionary."); //$NON-NLS-1$ //$NON-NLS-2$
				// System.err.println();
				return false;
			} else
				return true;
		} else
			return true;
	}

	public Classification getResult() {
		return result;
	}
}
