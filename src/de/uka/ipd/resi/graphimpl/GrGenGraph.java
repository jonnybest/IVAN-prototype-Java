package de.uka.ipd.resi.graphimpl;

import ikvm.lang.CIL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cli.System.Collections.IEnumerator;
import cli.de.unika.ipd.grGen.lgsp.LGSPBackend;
import cli.de.unika.ipd.grGen.lgsp.LGSPEdge;
import cli.de.unika.ipd.grGen.lgsp.LGSPGraph;
import cli.de.unika.ipd.grGen.lgsp.LGSPNode;
import cli.de.unika.ipd.grGen.libGr.EdgeType;
import cli.de.unika.ipd.grGen.libGr.IEdge;
import cli.de.unika.ipd.grGen.libGr.INode;
import cli.de.unika.ipd.grGen.libGr.NodeType;
import cli.de.unika.ipd.grGen.libGr.Porter;
import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.DeterminerInfo;
import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Mark;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.VerbFrame.VerbArgument;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphCheckDeterminers;
import de.uka.ipd.resi.graphinterface.GraphCheckForNominalization;
import de.uka.ipd.resi.graphinterface.GraphSimilarMeaning;
import de.uka.ipd.resi.graphinterface.GraphCompleteProcessWords;
import de.uka.ipd.resi.graphinterface.GraphAvoidAmbiguousWords;

/**
 * Graph which imports a GXL-File and uses the SALE model.
 * 
 * @author Torben Brumm
 */
public class GrGenGraph extends Graph implements GraphSimilarMeaning, GraphCompleteProcessWords,
		GraphAvoidAmbiguousWords, GraphCheckForNominalization, GraphCheckDeterminers {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(GrGenGraph.class);
	
	/**
	 * Sense: AG.
	 */
	private static final Sense SENSE_AG = new Sense("AG", "");
	
	/**
	 * Sense: PAT.
	 */
	private static final Sense SENSE_PAT = new Sense("PAT", "");
	
	/**
	 * Delimiter which separates two elements of an "array" in a string field.
	 */
	private static final String STRING_DELIMITER = "::";
	
	/**
	 * String which replaces occurrences of the string delimiter in strings in the corresponding "array".
	 */
	private static final String STRING_DELIMITER_IN_STRING = ":..:";
	
	/**
	 * Edge: ROLE-ACT.
	 */
	private final EdgeType edgeTypeAct;
	
	/**
	 * Edge: ROLE-AG
	 */
	private final EdgeType edgeTypeAg;
	
	/**
	 * Edge: MARK_EDGE-DETERMINER_MARK.
	 */
	private final EdgeType edgeTypeDeterminerMark;
	
	/**
	 * Edge: ROLE-EQROLE.
	 */
	private final EdgeType edgeTypeEqrole;
	
	/**
	 * Edge: HAS_ARGUMENT.
	 */
	private final EdgeType edgeTypeHasArgument;
	
	/**
	 * Edge: HAS_ATTRIBUTE.
	 */
	private final EdgeType edgeTypeHasAttribute;
	
	/**
	 * Edge: NEXT.
	 */
	private final EdgeType edgeTypeNext;
	
	/**
	 * Edge: MARK_EDGE-NOMINALIZATION_MARK.
	 */
	private final EdgeType edgeTypeNominalizationMark;
	
	/**
	 * Edge: ROLE-PAT.
	 */
	private final EdgeType edgeTypePat;
	
	/**
	 * Edge: ROLE.
	 */
	private final EdgeType edgeTypeRole;
	
	/**
	 * Edge: SUPERPHRASE_CLOSURE.
	 */
	private final EdgeType edgeTypeSuperPhraseClosure;
	
	/**
	 * Edge: MARK_EDGE-VERB_MARK.
	 */
	private final EdgeType edgeTypeVerbMark;
	
	/**
	 * Edge: MARK_EDGE-WORD_MARK.
	 */
	private final EdgeType edgeTypeWordMark;
	
	/**
	 * Graph object which contains all the information.
	 */
	private final LGSPGraph graph;
	
	/**
	 * Node: CONSTITUENT.
	 */
	private final NodeType nodeTypeConstituent;
	
	/**
	 * Node: MARK.
	 */
	private final NodeType nodeTypeMark;
	
	/**
	 * Node: MARK-NULL_MARK.
	 */
	private final NodeType nodeTypeNullMark;
	
	/**
	 * Node: CONSTITUENT-PHRASE.
	 */
	private final NodeType nodeTypePhrase;
	
	/**
	 * Node: CONSTITUENT-PROPERTY.
	 */
	private final NodeType nodeTypeProperty;
	
	/**
	 * Node: CONSTITUENT-SET.
	 */
	private final NodeType nodeTypeSet;
	
	/**
	 * Constructor for a graph from a GXL file.
	 * 
	 * @param filename Name of the GXL file.
	 */
	public GrGenGraph(final String filename) {
		GrGenGraph.logger.debug("[>>>]GrGenGraph( filename = {} )", filename);
		this.graph = (LGSPGraph) Porter.Import(filename, "sale.gm", new LGSPBackend());
		
		// initialize node types
		this.nodeTypePhrase = this.graph.GetNodeType("PHRASE");
		this.nodeTypeConstituent = this.graph.GetNodeType("CONSTITUENT");
		this.nodeTypeSet = this.graph.GetNodeType("SET");
		this.nodeTypeProperty = this.graph.GetNodeType("PROPERTY");
		this.nodeTypeMark = this.graph.GetNodeType("MARK");
		this.nodeTypeNullMark = this.graph.GetNodeType("NULL_MARK");
		
		// initialize edge types
		this.edgeTypeNext = this.graph.GetEdgeType("NEXT");
		this.edgeTypeRole = this.graph.GetEdgeType("ROLE");
		this.edgeTypePat = this.graph.GetEdgeType("PAT");
		this.edgeTypeAct = this.graph.GetEdgeType("ACT");
		this.edgeTypeAg = this.graph.GetEdgeType("AG");
		this.edgeTypeEqrole = this.graph.GetEdgeType("EQROLE");
		this.edgeTypeHasAttribute = this.graph.GetEdgeType("HAS_ATTRIBUTE");
		this.edgeTypeHasArgument = this.graph.GetEdgeType("HAS_ARGUMENT");
		this.edgeTypeSuperPhraseClosure = this.graph.GetEdgeType("SUPERPHRASE_CLOSURE");
		this.edgeTypeVerbMark = this.graph.GetEdgeType("VERB_MARK");
		this.edgeTypeWordMark = this.graph.GetEdgeType("WORD_MARK");
		this.edgeTypeNominalizationMark = this.graph.GetEdgeType("NOMINALIZATION_MARK");
		this.edgeTypeDeterminerMark = this.graph.GetEdgeType("DETERMINER_MARK");
		
		// initialize variables for later retrieval
		final IEnumerator nodeEnumerator = this.graph.get_Nodes().GetEnumerator();
		while (nodeEnumerator.MoveNext()) {
			final LGSPNode currentNode = (LGSPNode) nodeEnumerator.get_Current();
			this.graph.SetVariableValue(this.graph.GetElementName(currentNode), currentNode);
		}
		GrGenGraph.logger.debug("[<<<]GrGenGraph()");
	}
	
	/**
	 * Builds a Sentence object from a phrase node and inserts it into the given list. Calls itself recursively with all
	 * following sentences.
	 * 
	 * @param sentenceList List of sentences to add the new sentences to.
	 * @param phraseNode Node with all information for the sentence and the info about following sentences.
	 * @throws SyntaxException
	 */
	private void addSentencesToListFromNode(final ArrayList<Sentence> sentenceList, final LGSPNode phraseNode)
			throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]addSentencesToListFromNode( sentenceList = {}, phraseNode = {} )", sentenceList,
				phraseNode);
		if (phraseNode == null) {
			// abort, no following sentence
			return;
		}
		final ArrayList<Word> wordList = new ArrayList<Word>();
		final LGSPNode nextPhrase = this.addWordsToListFromPhrase(wordList, phraseNode);
		if (wordList.size() > 0) {
			sentenceList.add(new Sentence(this, this.graph.GetElementName(phraseNode), (String) phraseNode
					.GetAttribute("PLAINTEXT"), wordList));
		}
		this.addSentencesToListFromNode(sentenceList, nextPhrase);
		GrGenGraph.logger.debug("[<<<]addSentencesToListFromNode()");
	}
	
	/**
	 * Adds Words from a phrase (or subphrase) and all its subphrases to a given list of words. Returns the next phrase
	 * in teh graph
	 * 
	 * @param wordList List of words to add new words to.
	 * @param phraseNode Node which contains the phrase.
	 * @return Next phrase in graph.
	 * @throws SyntaxException
	 */
	private LGSPNode addWordsToListFromPhrase(final ArrayList<Word> wordList, final LGSPNode phraseNode)
			throws SyntaxException {
		GrGenGraph.logger
				.debug("[>>>]addWordsToListFromPhrase( wordList = {}, phraseNode = {} )", wordList, phraseNode);
		// initialize lists
		LGSPNode nextPhrase = null;
		final HashMap<Integer, Word> wordMap = new HashMap<Integer, Word>();
		final HashMap<Integer, ArrayList<Word>> subphraseMap = new HashMap<Integer, ArrayList<Word>>();
		final ArrayList<ArrayList<Word>> setList = new ArrayList<ArrayList<Word>>();
		
		// handle subphrases
		final IEnumerator incomingEnumerator = phraseNode.get_Incoming().GetEnumerator();
		while (incomingEnumerator.MoveNext()) {
			final IEdge currentEdge = (IEdge) incomingEnumerator.get_Current();
			if (currentEdge.get_Type().IsA(this.edgeTypeSuperPhraseClosure)) {
				final LGSPNode subPhrase = (LGSPNode) currentEdge.get_Source();
				final ArrayList<Word> innerWordList = new ArrayList<Word>();
				if (this.addWordsToListFromPhrase(innerWordList, subPhrase) != null) {
					throw new SyntaxException(this, "Found a NEXT-edge in a subphrase");
				}
				subphraseMap.put(CIL.unbox_int(subPhrase.GetAttribute("POSITION")), innerWordList);
			}
		}
		
		final IEnumerator elementEnumerator = phraseNode.get_Outgoing().GetEnumerator();
		while (elementEnumerator.MoveNext()) {
			final IEdge currentEdge = (IEdge) elementEnumerator.get_Current();
			if (currentEdge.get_Type().IsA(this.edgeTypeNext)) {
				nextPhrase = (LGSPNode) currentEdge.get_Target();
			}
			else if (currentEdge.get_Type().IsA(this.edgeTypeEqrole)) {
				// TODO assertions
			}
			else {
				final LGSPNode currentWord = (LGSPNode) currentEdge.get_Target();
				Word word = null;
				
				if (currentWord.get_Type().IsA(this.nodeTypeSet)) {
					// sets (act like subphrases)
					final ArrayList<Word> innerWordList = new ArrayList<Word>();
					if (this.addWordsToListFromPhrase(innerWordList, currentWord) != null) {
						throw new SyntaxException(this, "Found a NEXT-edge in a set");
					}
					innerWordList.add(CIL.unbox_int(currentWord.GetAttribute("POSITION")), this.buildWordFromNode(
							currentWord, currentWord.get_Type().get_Name().substring(0,
									currentWord.get_Type().get_Name().length() - 4)));
					// XXX work around for compiler "error"
					setList.add(innerWordList);
				}
				
				// we handled subphrases, so we only need the other types
				else if (!currentWord.get_Type().IsA(this.nodeTypePhrase)) {
					word = this.buildWordFromNodeWithPlainTextAttribute(currentWord, "VALUE");
					wordMap.put(CIL.unbox_int(currentWord.GetAttribute("POSITION")), word);
				}
				
				// handle attributes
				final IEnumerator attributeNodeEnumerator = currentWord.get_Outgoing().GetEnumerator();
				while (attributeNodeEnumerator.MoveNext()) {
					final IEdge currentAttributeEdge = (IEdge) attributeNodeEnumerator.get_Current();
					if (currentAttributeEdge.get_Type().IsA(this.edgeTypeHasAttribute)) {
						final LGSPNode attribute = (LGSPNode) currentAttributeEdge.get_Target();
						Word attributeWord = null;
						
						// we handled subphrases, so we only need the other types
						if (!attribute.get_Type().IsA(this.nodeTypePhrase)) {
							attributeWord = this.buildWordFromNodeWithPlainTextAttribute(attribute, "NAME");
						}
						wordMap.put(CIL.unbox_int(attribute.GetAttribute("POSITION")), attributeWord);
					}
				}
			}
		}
		int setCounter = 0;
		// XXX work around for compiler "error" with sets
		final int setCorrector = (phraseNode.get_Type().IsA(this.nodeTypeSet)) ? 1 : 0;
		for (int i = 0; i < wordMap.size() + subphraseMap.size() + setList.size() + setCorrector; i++) {
			final Word currentWord = wordMap.get(i);
			if (currentWord == null) {
				
				if (subphraseMap.get(i) == null) {
					// XXX work around for compiler "error" with sets
					if (setList.size() > setCounter) {
						wordList.addAll(setList.get(setCounter));
						setCounter++;
					}
					else if (!phraseNode.get_Type().IsA(this.nodeTypeSet)) {
						// TODO error/exception??? or just ignore?
						System.out.println("Word not found!" + i);
					}
				}
				else {
					// handle subphrases differently
					wordList.addAll(subphraseMap.get(i));
				}
			}
			else {
				wordList.add(currentWord);
			}
		}
		GrGenGraph.logger.debug("[<<<]addWordsToListFromPhrase(): {}", nextPhrase);
		return nextPhrase;
	}
	
	/**
	 * Builds a Mark object from a node.
	 * 
	 * @param node Node with the information of the mark.
	 * @return Mark with all information from the node or null if it was no mark node.
	 */
	private Mark buildMarkFromNode(final LGSPNode node) {
		GrGenGraph.logger.debug("[>>>]buildMarkFromNode( node = {} ): {}", node);
		Mark mark;
		if (node.get_Type().IsA(this.nodeTypeMark) && !node.get_Type().IsA(this.nodeTypeNullMark)) {
			mark = new Mark();
			mark.setComment((String) node.GetAttribute("COMMENT"));
		}
		else {
			mark = null;
		}
		GrGenGraph.logger.debug("[<<<]buildMarkFromNode(): {}", mark);
		return mark;
	}
	
	/**
	 * Builds a Word object from a node.
	 * 
	 * @param node Node with the information about the word.
	 * @param plainWord Plain word.
	 * @return Word with all information from the node.
	 * @throws SyntaxException
	 */
	private Word buildWordFromNode(final LGSPNode node, final String plainWord) throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]buildWordFromNode( node = {}, plainWord = {} )", node, plainWord);
		final Word word = new Word(this.graph.GetElementName(node), plainWord);
		word.setBaseForm((String) node.GetAttribute("BASEFORM"));
		word.setPennTreebankTag((String) node.GetAttribute("POSTAG"));
		
		final String ontologyNamesString = ((String) node.GetAttribute("ONTOLOGY_NAMES"));
		final String ontologyConstantNamesString = ((String) node.GetAttribute("ONTOLOGY_CONSTANT_NAMES"));
		if (ontologyNamesString != null && ontologyConstantNamesString != null) {
			final String[] ontologyNames = ontologyNamesString.split(GrGenGraph.STRING_DELIMITER);
			final String[] ontologyConstantNames = ontologyConstantNamesString.split(GrGenGraph.STRING_DELIMITER, -1);
			
			if (ontologyNames.length != ontologyConstantNames.length) {
				throw new SyntaxException(this, word.getPlainWord() + ": Number of ontology names and constants differ");
			}
			for (int i = 0; i < ontologyNames.length; i++) {
				if (ontologyNames[i].length() != 0) {
					word.setOntologyConstant(ontologyNames[i].replaceAll(GrGenGraph.STRING_DELIMITER_IN_STRING,
							GrGenGraph.STRING_DELIMITER), ontologyConstantNames[i].replaceAll(
							GrGenGraph.STRING_DELIMITER_IN_STRING, GrGenGraph.STRING_DELIMITER));
				}
			}
		}
		
		final IEnumerator markEnumerator = node.GetCompatibleOutgoing(this.edgeTypeWordMark).GetEnumerator();
		while (markEnumerator.MoveNext()) {
			final LGSPEdge markEdge = (LGSPEdge) markEnumerator.get_Current();
			word.setMark(this.buildMarkFromNode(markEdge.target));
		}
		
		GrGenGraph.logger.debug("[<<<]buildWordFromNode(): {}", word);
		return word;
	}
	
	/**
	 * Builds a Word object from a node which has an attribute containing the plain word.
	 * 
	 * @param node Node with the information about the word.
	 * @param plainTextAttribute Name of the node attribute which represents the plain word.
	 * @return Word with all information from the node.
	 * @throws SyntaxException
	 */
	private Word buildWordFromNodeWithPlainTextAttribute(final LGSPNode node, final String plainTextAttribute)
			throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]buildWordFromNodeWithPlainTextAttribute( node = {}, plainTextAttribute = {} )",
				node, plainTextAttribute);
		final Word returnValue = this.buildWordFromNode(node, (String) node.GetAttribute(plainTextAttribute));
		GrGenGraph.logger.debug("[>>>]buildWordFromNodeWithPlainTextAttribute(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Writes the graph data into the given file (GXL-Format).
	 * 
	 * @param filename Filename
	 */
	public void exportToFile(final String filename) {
		GrGenGraph.logger.debug("[>>>]exportToFile( filename = {} )", filename);
		Porter.Export(this.graph, filename);
		GrGenGraph.logger.debug("[<<<]exportToFile()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.graphinterface.GraphCheckDeterminers#getAllDeterminers()
	 */
	@Override
	public Set<Word> getAllDeterminers() throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]getAllDeterminers()");
		final Set<Word> wordSet = new HashSet<Word>();
		final IEnumerator enumerator = this.graph.GetCompatibleNodes(this.nodeTypeConstituent).GetEnumerator();
		while (enumerator.MoveNext()) {
			final LGSPNode node = (LGSPNode) enumerator.get_Current();
			if (node.GetAttribute("POSTAG") != null
					&& (((String) node.GetAttribute("POSTAG")).equals("DT") || ((String) node.GetAttribute("POSTAG"))
							.equals("CD"))) {
				final Word word;
				if (node.InstanceOf(this.nodeTypeProperty)) {
					word = this.buildWordFromNodeWithPlainTextAttribute(node, "NAME");
				}
				else {
					word = this.buildWordFromNodeWithPlainTextAttribute(node, "VALUE");
				}
				try {
					word.setSentence(this.getSentenceForWord(word));
				}
				catch (final WordNotFoundException e) {
					// if the word is not found, we have an error in our graph
					throw new SyntaxException(this, "No sentence found for determiner " + word);
				}
				wordSet.add(word);
			}
		}
		GrGenGraph.logger.debug("[>>>]getAllDeterminers(): {}", wordSet);
		return wordSet;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.graphinterface.GraphCompareNouns#getAllNouns()
	 */
	@Override
	public Set<Word> getAllNouns() throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]getAllNouns()");
		final Set<Word> wordSet = new HashSet<Word>();
		final IEnumerator enumerator = this.graph.GetCompatibleNodes(this.nodeTypeConstituent).GetEnumerator();
		while (enumerator.MoveNext()) {
			final LGSPNode node = (LGSPNode) enumerator.get_Current();
			if (node.GetAttribute("POSTAG") != null && ((String) node.GetAttribute("POSTAG")).startsWith("N")) {
				final Word word = this.buildWordFromNodeWithPlainTextAttribute(node, "VALUE");
				try {
					word.setSentence(this.getSentenceForWord(word));
				}
				catch (final WordNotFoundException e) {
					// if the word is not found, we have an error in our graph
					throw new SyntaxException(this, "No sentence found for noun " + word);
				}
				wordSet.add(word);
			}
		}
		GrGenGraph.logger.debug("[>>>]getAllNouns(): {}", wordSet);
		return wordSet;
	}
	
	/**
	 * Returns all nodes of phrases the given source node od a role edge is a subphrase (or set) of including itself if
	 * it is a phrase.
	 * 
	 * @param node Node to start with.
	 * @param roleType Type of the role edge.
	 * @return List of phrase nodes.
	 */
	private List<LGSPNode> getAllPhraseNodesForRoleSourceNode(final LGSPNode node, final EdgeType roleType) {
		final ArrayList<LGSPNode> phraseNodes = new ArrayList<LGSPNode>();
		final LGSPNode currentNode = node;
		if (currentNode.get_Type().IsA(this.nodeTypePhrase)) {
			// add the node
			phraseNodes.add(currentNode);
			// add all superphrases
			final IEnumerator superPhraseEnumerator = currentNode
					.GetCompatibleOutgoing(this.edgeTypeSuperPhraseClosure).GetEnumerator();
			while (superPhraseEnumerator.MoveNext()) {
				phraseNodes.addAll(this.getAllPhraseNodesForRoleSourceNode((LGSPNode) ((IEdge) superPhraseEnumerator
						.get_Current()).get_Target(), roleType));
			}
		}
		else if (currentNode.get_Type().IsA(this.nodeTypeSet)) {
			// do not add the node
			// add all "superphrases" from the set
			final IEnumerator superPhraseEnumerator = currentNode.GetCompatibleIncoming(roleType).GetEnumerator();
			while (superPhraseEnumerator.MoveNext()) {
				phraseNodes.addAll(this.getAllPhraseNodesForRoleSourceNode((LGSPNode) ((IEdge) superPhraseEnumerator
						.get_Current()).get_Source(), roleType));
			}
		}
		return phraseNodes;
	}
	
	@Override
	public DeterminerInfo getDeterminerInfoForWord(final Word word) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]getDeterminerInfoForWord( word = {} )", word);
		final LGSPNode node = this.graph.GetNodeVarValue(word.getId());
		if (node == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		DeterminerInfo returnValue = null;
		final IEnumerator markEnumerator = node.GetCompatibleOutgoing(this.edgeTypeDeterminerMark).GetEnumerator();
		while (markEnumerator.MoveNext()) {
			final LGSPEdge markEdge = (LGSPEdge) markEnumerator.get_Current();
			returnValue = new DeterminerInfo();
			if (CIL.unbox_boolean(markEdge.GetAttribute("IS_DEFINITE_ARTICLE"))) {
				returnValue.setIsDefiniteArticle();
			}
			else if (CIL.unbox_boolean(markEdge.GetAttribute("IS_INDEFINITE_ARTICLE"))) {
				returnValue.setIsIndefiniteArticle();
			}
			else {
				returnValue.setRange(CIL.unbox_int(markEdge.GetAttribute("MIN_VALUE")), CIL.unbox_int(markEdge
						.GetAttribute("MAX_VALUE")));
			}
			// also includes the case that no mark is set
			returnValue.setMark(this.buildMarkFromNode(markEdge.target));
		}
		GrGenGraph.logger.debug("[<<<]getDeterminerInfoForWord(): {}", returnValue);
		return returnValue;
	}
	
	@Override
	public String getName() {
		return "GrGen";
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.graphinterface.GraphCheckForNominalization#getNominalizationMark(de.uka.ipd.
	 * specificationimprover.Word)
	 */
	@Override
	public Mark getNominalizationMark(final Word word) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]getNominalizationMark( word = {} )", word);
		final LGSPNode node = this.graph.GetNodeVarValue(word.getId());
		if (node == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		Mark returnValue = null;
		final IEnumerator markEnumerator = node.GetCompatibleOutgoing(this.edgeTypeNominalizationMark).GetEnumerator();
		while (markEnumerator.MoveNext()) {
			final LGSPEdge markEdge = (LGSPEdge) markEnumerator.get_Current();
			returnValue = this.buildMarkFromNode(markEdge.target);
		}
		GrGenGraph.logger.debug("[<<<]getNominalizationMark(): {}", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.graphinterface.GraphCompleteProcessWords#getProcessWords()
	 */
	@Override
	public List<List<VerbFrame>> getProcessWords() throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]getProcessWords()");
		final List<List<VerbFrame>> returnValue = new ArrayList<List<VerbFrame>>();
		final IEnumerator verbEnumerator = this.graph.GetExactEdges(this.edgeTypeAct).GetEnumerator();
		while (verbEnumerator.MoveNext()) {
			final LGSPEdge edge = (LGSPEdge) verbEnumerator.get_Current();
			final LGSPNode wordNode = (LGSPNode) edge.get_Target();
			
			if (wordNode.get_Type().IsA(this.nodeTypeSet)) {
				// ignore set roots ("AND", "OR" etc.)
				continue;
			}
			
			final Word word = this.buildWordFromNodeWithPlainTextAttribute(wordNode, "VALUE");
			try {
				word.setSentence(this.getSentenceForWord(word));
			}
			catch (final WordNotFoundException e) {
				// if the word is not found, we have an error in our graph
				throw new SyntaxException(this, "No sentence found for verb " + word);
			}
			
			// add VerbFrames
			final Map<String, VerbFrame> verbFrames = new HashMap<String, VerbFrame>();
			final IEnumerator verbArgumentEnumerator = wordNode.GetCompatibleOutgoing(this.edgeTypeHasArgument)
					.GetEnumerator();
			while (verbArgumentEnumerator.MoveNext()) {
				final LGSPEdge argumentEdge = (LGSPEdge) verbArgumentEnumerator.get_Current();
				final VerbArgument argument = new VerbArgument(new Sense((String) argumentEdge
						.GetAttribute("SEMANTIC_ROLE"), ""), (String) argumentEdge.GetAttribute("SYNTACTIC_ROLE"));
				argument.setWord(this.buildWordFromNodeWithPlainTextAttribute(argumentEdge.target, "VALUE"));
				
				final String ontologyName = (String) argumentEdge.GetAttribute("ONTOLOGY_NAME");
				if (verbFrames.get(ontologyName) == null) {
					verbFrames.put(ontologyName, new VerbFrame(ontologyName, word));
				}
				
				verbFrames.get(ontologyName).getArguments().add(argument);
			}
			
			final List<VerbFrame> list = new ArrayList<VerbFrame>();
			list.addAll(verbFrames.values());
			
			// add a verb frame with suggestions
			final VerbFrame suggestionsVerbFrame = new VerbFrame(null, word);
			final String edgeSetId = (String) edge.GetAttribute("SETID");
			
			for (final LGSPNode phraseNode : this.getAllPhraseNodesForRoleSourceNode(edge.source, this.edgeTypeAct)) {
				final IEnumerator possibleArgumentsEnumerator = phraseNode.GetCompatibleOutgoing(this.edgeTypeRole)
						.GetEnumerator();
				while (possibleArgumentsEnumerator.MoveNext()) {
					final LGSPEdge possibleArgumentEdge = (LGSPEdge) possibleArgumentsEnumerator.get_Current();
					final String possibleArgumentEdgeSetId = (String) possibleArgumentEdge.GetAttribute("SETID");
					if (edgeSetId == null || edgeSetId.equals("") || possibleArgumentEdgeSetId == null
							|| possibleArgumentEdgeSetId.equals("") || edgeSetId.equals(possibleArgumentEdgeSetId)) {
						if (possibleArgumentEdge.InstanceOf(this.edgeTypeAg)) {
							final VerbArgument suggestionsVerbArgument = new VerbArgument(GrGenGraph.SENSE_AG,
									"SUBJECT");
							suggestionsVerbArgument.setWord(this.buildWordFromNodeWithPlainTextAttribute(
									possibleArgumentEdge.target, "VALUE"));
							suggestionsVerbFrame.getArguments().add(suggestionsVerbArgument);
						}
						else if (possibleArgumentEdge.InstanceOf(this.edgeTypePat)) {
							final VerbArgument suggestionsVerbArgument = new VerbArgument(GrGenGraph.SENSE_PAT,
									"OBJECT");
							suggestionsVerbArgument.setWord(this.buildWordFromNodeWithPlainTextAttribute(
									possibleArgumentEdge.target, "VALUE"));
							suggestionsVerbFrame.getArguments().add(suggestionsVerbArgument);
						}
					}
				}
			}
			list.add(suggestionsVerbFrame);
			
			// get mark for verb frames
			Mark mark = null;
			final IEnumerator markEnumerator = wordNode.GetCompatibleOutgoing(this.edgeTypeVerbMark).GetEnumerator();
			while (markEnumerator.MoveNext()) {
				final LGSPEdge markEdge = (LGSPEdge) markEnumerator.get_Current();
				mark = this.buildMarkFromNode(markEdge.target);
			}
			if (mark != null) {
				for (final VerbFrame currentFrame : list) {
					currentFrame.setMark(mark);
				}
			}
			
			returnValue.add(list);
		}
		GrGenGraph.logger.debug("[>>>]getProcessWords(): {}", returnValue);
		return returnValue;
	}
	
	/**
	 * Returns the Sentence the given word is part of.
	 * 
	 * @param word Wanted Word.
	 * @return Sentence the word is in.
	 * @throws WordNotFoundException
	 * @throws SyntaxException
	 */
	private Sentence getSentenceForWord(final Word word) throws WordNotFoundException, SyntaxException {
		GrGenGraph.logger.debug("[>>>]getSentenceForWord( word = {} )", word);
		// TODO optimize?
		for (final Sentence possibleSentence : this.getSentences()) {
			for (final Word possibleWord : possibleSentence.getWords()) {
				if (possibleWord.getId().equals(word.getId())) {
					GrGenGraph.logger.debug("[<<<]getSentenceForWord(): {}", possibleSentence);
					return possibleSentence;
				}
			}
		}
		throw new WordNotFoundException(this, word.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Graph#getSentences()
	 */
	@Override
	public Sentence[] getSentences() throws SyntaxException {
		GrGenGraph.logger.debug("[>>>]getSentences()");
		final ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();
		
		// Constituents holen
		final IEnumerator constituentEnumerator = this.graph.GetExactNodes(this.nodeTypeConstituent).GetEnumerator();
		while (constituentEnumerator.MoveNext()) {
			final INode root = (INode) constituentEnumerator.get_Current();
			
			// fuer jeden Root-Node den ersten Satz holen (der dann ueber next-Kanten mit den folgenden verbunden ist)
			final IEnumerator nextEnumerator = root.GetExactOutgoing(this.edgeTypeNext).GetEnumerator();
			while (nextEnumerator.MoveNext()) {
				final LGSPNode currentPhrase = (LGSPNode) ((IEdge) nextEnumerator.get_Current()).get_Target();
				this.addSentencesToListFromNode(sentenceList, currentPhrase);
			}
			
		}
		final Sentence[] returnValue = sentenceList.toArray(new Sentence[sentenceList.size()]);
		GrGenGraph.logger.debug("[<<<]getSentences(): {}", returnValue);
		return returnValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.graphinterface.GraphCompareNouns#replaceNoun(de.uka.ipd.specificationimprover
	 * .Word, de.uka.ipd.specificationimprover.Word)
	 */
	@Override
	public void replaceNoun(final Word oldNoun, final Word newNoun) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]replaceNoun( oldNoun = {}, newNoun = {} )", oldNoun, newNoun);
		// base form is certainly correct, even if null
		oldNoun.setBaseForm(newNoun.getBaseForm());
		if (oldNoun.getPennTreebankTag().equals(newNoun.getPennTreebankTag())) {
			// if the tag is the same, we can just the given form
			oldNoun.setPlainWord(newNoun.getPlainWord());
		}
		else if (oldNoun.getPennTreebankTag().endsWith("S")) {
			// we need plural, so we create it by adding an s
			// TODO improve (e.g. with WordNet)
			oldNoun.setPlainWord(newNoun.getBaseFormIfPresent() + "s");
		}
		else {
			// we need singular, should be the base form
			oldNoun.setPlainWord(newNoun.getBaseFormIfPresent());
		}
		this.updateWordAttributes(oldNoun);
		GrGenGraph.logger.debug("[<<<]replaceNoun()");
	}
	
	@Override
	public void setDeterminerInfoForWord(final Word word, final DeterminerInfo determinerInfo)
			throws WordNotFoundException {
		GrGenGraph.logger.debug("[<<<]setDeterminerInfoForWord( word = {}, determinerInfo = {})", word, determinerInfo);
		final LGSPNode node = this.graph.GetNodeVarValue(word.getId());
		if (node == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		
		// first we update a possible comment
		this.updateMarkForNode(node, (determinerInfo == null ? null : determinerInfo.getMark()),
				this.edgeTypeDeterminerMark);
		
		if (determinerInfo != null) {
			// assure that we have a correct edge to add our info to
			final IEnumerator markEnumerator = node.GetCompatibleOutgoing(this.edgeTypeDeterminerMark).GetEnumerator();
			LGSPEdge markEdge = null;
			while (markEnumerator.MoveNext()) {
				markEdge = (LGSPEdge) markEnumerator.get_Current();
			}
			if (markEdge == null) {
				// we need to add one as we have no mark comment
				final LGSPNode newMarkNode = this.graph.AddNode(this.nodeTypeNullMark);
				markEdge = this.graph.AddEdge(this.edgeTypeDeterminerMark, node, newMarkNode);
			}
			
			// update attributes
			markEdge.SetAttribute("IS_DEFINITE_ARTICLE", CIL.box_boolean(determinerInfo.isDefiniteArticle()));
			markEdge.SetAttribute("IS_INDEFINITE_ARTICLE", CIL.box_boolean(determinerInfo.isIndefiniteArticle()));
			try {
				markEdge.SetAttribute("MIN_VALUE", CIL.box_int(determinerInfo.getMinValue()));
				markEdge.SetAttribute("MAX_VALUE", CIL.box_int(determinerInfo.getMaxValue()));
			}
			catch (final IllegalStateException e) {
				// do nothing, we just can't set the min and max value (not needed)
			}
		}
		GrGenGraph.logger.debug("[<<<]setDeterminerInfoForWord()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.uka.ipd.specificationimprover.graphinterface.GraphCheckForNominalization#setNominalizationMark(de.uka.ipd.
	 * specificationimprover.Word, de.uka.ipd.specificationimprover.Mark)
	 */
	@Override
	public void setNominalizationMark(final Word word, final Mark mark) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]setNominalizationMark( word = {}, mark = {} )", word, mark);
		final LGSPNode node = this.graph.GetNodeVarValue(word.getId());
		if (node == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		this.updateMarkForNode(node, mark, this.edgeTypeNominalizationMark);
		GrGenGraph.logger.debug("[<<<]setNominalizationMark()");
	}
	
	/**
	 * Updates (or removes/adds) a mark for a given node.
	 * 
	 * @param node Node with the Mark.
	 * @param mark Mark with the new values (or null if the mark shall be removed).
	 * @param edgeType Type of the edge that connect the node with the mark.
	 */
	private void updateMarkForNode(final LGSPNode node, final Mark mark, final EdgeType edgeType) {
		GrGenGraph.logger.debug("[<<<]updateMarkForNode( node = {}, mark = {}, edgeType = {})", new Object[] { node,
				mark, edgeType
		});
		// first remove a possible old mark edge that corresponds to the node
		final IEnumerator markEnumerator = node.GetCompatibleOutgoing(edgeType).GetEnumerator();
		while (markEnumerator.MoveNext()) {
			final LGSPEdge markEdge = (LGSPEdge) markEnumerator.get_Current();
			final INode markNode = markEdge.get_Target();
			this.graph.Remove(markEdge);
			this.graph.Remove(markNode);
		}
		
		// add new edge and node if needed
		if (mark != null) {
			final LGSPNode newMarkNode = this.graph.AddNode(this.nodeTypeMark);
			newMarkNode.SetAttribute("COMMENT", mark.getComment());
			
			this.graph.AddEdge(edgeType, node, newMarkNode);
		}
		GrGenGraph.logger.debug("[<<<]updateMarkForNode()");
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.uka.ipd.specificationimprover.graphinterface.GraphCompleteProcessWords#updateVerbFrame(de.uka.ipd.
	 * specificationimprover.VerbFrame)
	 */
	@Override
	public void updateVerbFrame(final VerbFrame verbFrame) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]updateVerbFrame( verbFrame = {} )", verbFrame);
		// if the word needs an update, do it
		if (verbFrame.getWord().isDirty()) {
			this.updateWordAttributes(verbFrame.getWord());
		}
		final LGSPNode wordNode = this.graph.GetNodeVarValue(verbFrame.getWord().getId());
		if (wordNode == null) {
			throw new WordNotFoundException(this, verbFrame.getWord().toString());
		}
		
		// update arguments
		
		// first remove all old edges that correspond to the VerbFrame (simpler than updating them)
		final IEnumerator verbArgumentEnumerator = wordNode.GetCompatibleOutgoing(this.edgeTypeHasArgument)
				.GetEnumerator();
		while (verbArgumentEnumerator.MoveNext()) {
			final LGSPEdge argumentEdge = (LGSPEdge) verbArgumentEnumerator.get_Current();
			if (verbFrame.getOntologyName().equals(argumentEdge.GetAttribute("ONTOLOGY_NAME"))) {
				this.graph.Remove(argumentEdge);
			}
		}
		
		// add new edges
		for (final VerbArgument argument : verbFrame.getArguments()) {
			
			if (argument.getWord() != null) {
				
				// if the argument needs an update, do it
				if (argument.getWord().isDirty()) {
					this.updateWordAttributes(argument.getWord());
				}
				
				final LGSPNode argumentNode = this.graph.GetNodeVarValue(argument.getWord().getId());
				if (argumentNode == null) {
					throw new WordNotFoundException(this, argument.getWord().toString());
				}
				
				final LGSPEdge newArgumentEdge = this.graph.AddEdge(this.edgeTypeHasArgument, wordNode, argumentNode);
				newArgumentEdge.SetAttribute("ONTOLOGY_NAME", verbFrame.getOntologyName());
				newArgumentEdge.SetAttribute("SEMANTIC_ROLE", argument.getSemanticRole().getSense());
				newArgumentEdge.SetAttribute("SYNTACTIC_ROLE", argument.getSyntacticRole());
			}
		}
		
		// update mark
		this.updateMarkForNode(wordNode, verbFrame.getMark(), this.edgeTypeVerbMark);
		
		GrGenGraph.logger.debug("[<<<]updateVerbFrame()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.Graph#updateWordAttribute(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public void updateWordAttributes(final Word word) throws WordNotFoundException {
		GrGenGraph.logger.debug("[>>>]updateWordAttributes( word = {} )", word);
		final LGSPNode node = this.graph.GetNodeVarValue(word.getId());
		if (node == null) {
			throw new WordNotFoundException(this, word.toString());
		}
		if (node.get_Type().IsA(this.nodeTypeProperty)) {
			node.SetAttribute("NAME", word.getPlainWord());
		}
		else {
			node.SetAttribute("VALUE", word.getPlainWord());
		}
		node.SetAttribute("BASEFORM", word.getBaseForm());
		node.SetAttribute("POSTAG", word.getPennTreebankTag());
		
		String ontologyNames = "";
		String ontologyConstantNames = "";
		for (final Entry<String, String> mapEntry : word.getOntologyConstants().entrySet()) {
			ontologyNames += mapEntry.getKey().replaceAll(GrGenGraph.STRING_DELIMITER,
					GrGenGraph.STRING_DELIMITER_IN_STRING)
					+ GrGenGraph.STRING_DELIMITER;
			ontologyConstantNames += mapEntry.getValue().replaceAll(GrGenGraph.STRING_DELIMITER,
					GrGenGraph.STRING_DELIMITER_IN_STRING)
					+ GrGenGraph.STRING_DELIMITER;
		}
		if (ontologyNames.length() > 0) {
			// cut off last delimiter
			ontologyNames = ontologyNames.substring(0, ontologyNames.length() - GrGenGraph.STRING_DELIMITER.length());
			ontologyConstantNames = ontologyConstantNames.substring(0, ontologyConstantNames.length()
					- GrGenGraph.STRING_DELIMITER.length());
		}
		node.SetAttribute("ONTOLOGY_NAMES", ontologyNames);
		node.SetAttribute("ONTOLOGY_CONSTANT_NAMES", ontologyConstantNames);
		
		// update mark
		this.updateMarkForNode(node, word.getMark(), this.edgeTypeWordMark);
		
		word.setDirty(false);
		GrGenGraph.logger.debug("[<<<]updateWordAttributes()");
	}
	
}
