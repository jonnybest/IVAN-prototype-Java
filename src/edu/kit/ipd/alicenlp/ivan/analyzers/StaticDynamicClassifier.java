package edu.kit.ipd.alicenlp.ivan.analyzers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.extjwnl.JWNLException;
import edu.kit.ipd.alicenlp.ivan.IvanException;
import edu.kit.ipd.alicenlp.ivan.data.DiscourseModel;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.DocumentErrorAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.kit.ipd.alicenlp.ivan.rules.DirectSpeechRule;
import edu.kit.ipd.alicenlp.ivan.rules.EntitiesSynonymsErrorRule;
import edu.kit.ipd.alicenlp.ivan.rules.ErrorRule;
import edu.kit.ipd.alicenlp.ivan.rules.EventRule;
import edu.kit.ipd.alicenlp.ivan.rules.FirstMentionRule;
import edu.kit.ipd.alicenlp.ivan.rules.IncompleteEntitiesErrorRule;
import edu.kit.ipd.alicenlp.ivan.rules.LexnameRule;
import edu.kit.ipd.alicenlp.ivan.rules.TimeRule;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class decides whether a sentence pertains to the initial state of the
 * scene before runtime (SETUP) or to changes to the scene during runtime
 * (ACTION, EVENT, TIME). It also finds sentence-based issues and document-based
 * issues (ERROR).
 * 
 * @author Jonny
 * 
 */
public class StaticDynamicClassifier extends IvanAnalyzer {
	private Logger log = Logger.getLogger("StaticDynamicClassifier");

	/**
	 * This is the main method for classification. It classifies a single
	 * sentence. It may perform better, if the Annotations from
	 * DeclarationPositionFinder are present.
	 * 
	 * @param sentence
	 * @return
	 * @throws JWNLException
	 */
	public Classification classifySentenceAnnotation(CoreMap sentence)
			throws JWNLException {

		// before we really get started, we check for errors
		if (sentence.get(CollapsedCCProcessedDependenciesAnnotation.class)
				.getRoots().isEmpty())
			return Classification.ErrorDescription;


		// this is the default result, if nothing else matches
		Classification defaultclass = Classification.ActionDescription;

		/**
		 * This is a new style of classifying. Simply create a rule to check and
		 * call "apply". The apply method is responsible for producing the
		 * result and all we have to do is to decide what to do with the result.
		 * In most cases, we simply want to annotate.
		 */
		// is sentence entirely direct speech? if so, we ignore it completely
		if(new DirectSpeechRule().apply(sentence))
			return Classification.Speech;
		
		// does this sentence container an error?
		ErrorRule checkError = new ErrorRule();
		if (checkError.apply(sentence)) {
			log .info("bad sentence found");
			sentence.set(IvanAnnotations.ErrorMessageAnnotation.class,
					checkError.getErrorMessage());
			return Classification.ErrorDescription;
		}

		// After we checked for errors, we can continue checking for already classified sentences:
		if (sentence.get(IvanAnnotations.SentenceClassificationAnnotation.class) != null)
			return sentence.get(IvanAnnotations.SentenceClassificationAnnotation.class);

		// does this sentence contain an event?
		EventRule checkevent = new EventRule();
		// yes!
		if (checkevent.apply(sentence)) {
			log.info("Event found");
			// since we only support one classification, return the
			// classification instantly
			return Classification.EventDescription;
		}

		// does this sentence explicitly reference time or duration?
		TimeRule checkTime = new TimeRule();
		// yes!
		if (checkTime.apply(sentence)) {
			System.out.print("Time reference found");
			return Classification.TimeDescription;
		}

		// does this sentence describe the initial state of the scene?
		LexnameRule lexnameRule = new LexnameRule();
		if(lexnameRule.apply(sentence))
		{
			log.info("Lexname rule found: " + lexnameRule.getResult().name());
			return lexnameRule.getResult(); // most likely "SETUP"
		}

		return defaultclass;
	}

	/**
	 * This is the constructor for the Stanford Pipeline Interface
	 * 
	 * @param name
	 * @param properties
	 */
	public StaticDynamicClassifier(String name, Properties properties) {
		// do not assign any instace, because I fear multiple annotators may be
		// interfering with each other if the references escape pipeline context
	}

	/**
	 * This is the simple constructor for a standalone classifier
	 */
	public StaticDynamicClassifier() {
	}

	@Override
	public void annotate(Annotation annotation) {
		// before dealing with each individual sentence, run a document-wide
		// classification
		FirstMentionRule rule = new FirstMentionRule();
		try {
			// pre-tag the sentences based on state data
			rule.apply(annotation, true);
		} catch (IvanException e1) {
			log.severe(e1.toString());
			e1.printStackTrace();
		}
		// annotate each sentence separately
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			// process
			try {
				Classification sentenceclass = classifySentenceAnnotation(sentence);

				// this is the proper way to annotate
				sentence.set(
						IvanAnnotations.SentenceClassificationAnnotation.class,
						sentenceclass);

				// check for initial state consistency
			} catch (JWNLException e) {
				// no classification for this sentence then :(
				log.warning( "Error while classifying sentences."+ e);
			} catch (NullPointerException | java.lang.AssertionError e) {
				log.warning( "Error while classifying sentences."+ e);
				Span range = Span.fromValues(
						sentence.get(CharacterOffsetBeginAnnotation.class),
						sentence.get(CharacterOffsetEndAnnotation.class));
				IvanErrorMessage error = new IvanErrorMessage(
						IvanErrorType.UNKNOWN, range,
						"Processing this sentence caused an exception.");

				sentence.set(IvanAnnotations.ErrorMessageAnnotation.class,
						error);
				sentence.set(
						IvanAnnotations.SentenceClassificationAnnotation.class,
						Classification.ErrorDescription);
			}
		}
		try {
			// run classifier
			classifyDocument(annotation);
			// check recognition state for missing entries
			IncompleteEntitiesErrorRule staterule = new IncompleteEntitiesErrorRule(
					annotation
							.get(IvanAnnotations.IvanEntitiesAnnotation.class));
			// run check
			if (staterule.apply(annotation)) {
				// save results
				if (annotation
						.get(IvanAnnotations.DocumentErrorAnnotation.class) != null) {
					annotation.get(
							IvanAnnotations.DocumentErrorAnnotation.class)
							.addAll(staterule.getErrorMessages());
				} else {
					annotation.set(
							IvanAnnotations.DocumentErrorAnnotation.class,
							staterule.getErrorMessages());
				}
			}
		} catch (JWNLException e) {
			log.warning( e.toString());
			e.printStackTrace();
		}
	}

	/** This method performs document-wide rule checking
	 * 
	 * @param annotation
	 * @throws JWNLException
	 */
	private static void classifyDocument(Annotation annotation)
			throws JWNLException {
		// document-wide error checking
		List<IvanErrorMessage> errors = annotation
				.get(DocumentErrorAnnotation.class);
		if (errors == null)
			errors = new ArrayList<IvanErrorMessage>();

		// lets check the entities for consistency.
		DiscourseModel entities = annotation.get(IvanEntitiesAnnotation.class);
		if (entities == null)
			return; // no entities - nothing to do

		// 1. distinct entities should not be synonymous to each other
		EntitiesSynonymsErrorRule rule = new EntitiesSynonymsErrorRule(entities);
		if (rule.apply(annotation, true)) {
			errors.add(rule.getErrorMessage());
			annotation.set(DocumentErrorAnnotation.class, errors);
		}
	}

	@Override
	public Set<Requirement> requirementsSatisfied() {
		Set<Requirement> isatisfy = new HashSet<Annotator.Requirement>();
		isatisfy.add(SETUP_CLASSIFICATION);
		isatisfy.add(ACTION_CLASSIFICATION);
		return isatisfy;
	}

	@Override
	public Set<Requirement> requires() {
		Set<Requirement> myreqs = new HashSet<Annotator.Requirement>();
		myreqs.addAll(TOKENIZE_SSPLIT_POS_LEMMA);
		// myreqs.add(PARSE_REQUIREMENT);
		return myreqs;
	}
}
