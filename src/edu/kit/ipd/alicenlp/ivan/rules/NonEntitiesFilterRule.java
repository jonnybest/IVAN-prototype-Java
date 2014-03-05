package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.search.ListSearchable;

/**
 * This rule classifies well-known entites into categories, so that entities
 * which do not correspond to a model may be ignored in the discourse model.
 * 
 * @author Jonny
 * 
 */
public class NonEntitiesFilterRule {

	/**
	 * The entity type indicates what role the entity has with regard to the
	 * discourse model and scene.
	 * 
	 * @author Jonny
	 * 
	 */
	public static enum EntityType {
		/**
		 * Entity corresponds to a model. examples: a car, a horse, a house.
		 * Also: default.
		 * 
		 */
		MODEL,
		/**
		 * Entity refers to something on screen but might not be a model.
		 * examples: the ground, the sky, green, brown, blonde
		 * 
		 */
		DISPLAYABLE,
		/**
		 * Entity does not appear visible in the scene. example: time
		 * 
		 */
		NONDISPLAYABLE
	}

	// well known words
	// background, foreground, ground (1,3,5,7,9,10,11), sky, grass, time,
	// while,
	/**
	 * display words
	 */
	static final String[] displaybase = { "foreground#n#1", "background#n#1", "background#n#2",
			"background#n#8", "ground#n#1", "ground#n#3", "ground#n#5",
			"ground#n#7", "ground#n#9", "ground#n#10", "ground#n#11", "sky#n",
			"grass#n#1", "clothes#n#1" };	
	static final HashSet<Synset> displaysenses = new HashSet<Synset>();
	static final HashSet<String> displaywords = new HashSet<>();
	
	/** non-displayable words
	 * 
	 */
	static final String[] nondisplaybase = {"time", "while"};
	static final HashSet<Synset> nondisplaysenses = new HashSet<Synset>();
	static final HashSet<String> nondisplaywords = new HashSet<>();

	/**
	 * The result of this operation
	 */
	private EntityType result;
	private static Dictionary dictionary;

	/**
	 * Create a new rule
	 * @throws JWNLException 
	 */
	public NonEntitiesFilterRule() throws JWNLException {
		// prepare setup
		if (dictionary == null)
			dictionary = BaseRule.setupWordNet();
		// retrieve synsets for our words
		prepareWordList(displaybase, displaysenses, displaywords);
		prepareWordList(nondisplaybase, nondisplaysenses, nondisplaywords);
	}

	/**
	 * @param words
	 * @param senses
	 * @param base
	 * @throws JWNLException
	 */
	private static void prepareWordList(String[] base, HashSet<Synset> senses, HashSet<String> words) throws JWNLException {
		// nothing to do here
		if(words.size() > 0)
			return;
		// retrieve all synsets
		for (String word : base) {
			List<Synset> synset = getAllSynsets(word);
			if(synset == null)
				throw new JWNLException("WordNET did not contain " + word);
			senses.addAll(synset);
		}
		// retrieve all synonyms
		for (Synset syn : senses) {
			for (Word w : syn.getWords()) {
				words.add(w.getLemma());
			}
		}
	}

	private static List<Synset> getAllSynsets(String entity) throws JWNLException {
		Synset lemma;
		String[] parts = StringUtils.split(entity, "#");
		int num = parts.length;
		String word = parts[0];
		POS pos = num > 1 ? POS.getPOSForKey(parts[1]) : POS.NOUN;
		int index = num > 2 ? Integer.parseInt(parts[2]) - 1 : -1;

		IndexWord iword = dictionary.lookupIndexWord(pos, word);
		if(iword == null)
			return null;
		List<Synset> syns = iword.getSenses();

		if (index >= 0 && syns.size() > index) {
			lemma = syns.get(index);
			return Arrays.asList(lemma);
		}
		else {
			return syns;
		}
	}

	/**
	 * 
	 * @param entity
	 *            A string describing the entity. 
	 * @return TRUE if this entity is well-known
	 *            
	 * @throws JWNLException
	 */
	public boolean apply(String entity) throws JWNLException {
		if(displaywords.contains(entity))
		{
			result = EntityType.DISPLAYABLE;
			return true;
		}
		else if(nondisplaywords.contains(entity))
		{
			result = EntityType.NONDISPLAYABLE;
			return true;
		}
		else {
			result = EntityType.MODEL;
			return false;
		}
	}

	@SuppressWarnings("unused")
	private static Synset getSynset(String entity) throws JWNLException {
		return getAllSynsets(entity).get(0);
	}

	/**
	 * retrieve the last classification result
	 * 
	 * @return
	 */
	public EntityType getResult() {
		return result;
	}
}
