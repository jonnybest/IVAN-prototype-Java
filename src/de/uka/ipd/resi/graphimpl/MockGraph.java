package de.uka.ipd.resi.graphimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.ipd.resi.Graph;
import de.uka.ipd.resi.Sentence;
import de.uka.ipd.resi.VerbFrame;
import de.uka.ipd.resi.Word;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.graphinterface.GraphSimilarMeaning;
import de.uka.ipd.resi.graphinterface.GraphCompleteProcessWords;

public class MockGraph extends Graph implements GraphSimilarMeaning, GraphCompleteProcessWords {
	
	@Override
	public Set<Word> getAllNouns() {
		final Set<Word> set = new HashSet<Word>();
		// set.add("chancellor");
		// set.add("Angela Merkel");
		// set.add("mammal");
		// set.add("german shepherd");
		// set.add("client");
		// set.add("flap");
		// set.add("person");
		// set.add("human");
		// set.add("male");
		return set;
	}
	
	@Override
	public String getName() {
		return "Mock";
	}
	
	@Override
	public List<List<VerbFrame>> getProcessWords() {
		final ArrayList<Word> list = new ArrayList<Word>();
		final Word word = new Word("id", "listens");
		word.setBaseForm("listen");
		word.setPennTreebankTag("VXXXX");
		list.add(word);
		return null;
	}
	
	@Override
	public Sentence[] getSentences() {
		final String sentence = "The WHOIS_client makes a text_request to the WHOIS_server then the WHOIS_server replies with text_content";
		final String[] wordArray = sentence.split(" ");
		final ArrayList<Word> wordList = new ArrayList<Word>();
		for (final String word : wordArray) {
			wordList.add(new Word("id:" + word, word));
		}
		final Sentence a = new Sentence(this, "a", sentence, wordList);
		return new Sentence[] { a
		};
	}
	
	@Override
	public void replaceNoun(final Word oldNoun, final Word newNoun) {
		System.out.println(oldNoun + " wurde mit " + newNoun + " ersetzt");
	}
	
	@Override
	public void updateVerbFrame(final VerbFrame verbFrame) {
	}
	
	@Override
	public void updateWordAttributes(final Word word) throws WordNotFoundException {
	}
	
}
