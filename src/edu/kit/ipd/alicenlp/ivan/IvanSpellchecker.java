/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.xml.sax.SAXException;

import edu.stanford.nlp.parser.lexparser.GermanUnknownWordModel;

/**
 * @author Jonny
 * 
 */
public class IvanSpellchecker extends SwingWorker<List<RuleMatch>, Object> {
	String text;

	private static JLanguageTool langTool;
	
	/** Every event needs to know which text to check
	 * 
	 * @param document
	 */
	public IvanSpellchecker(String document) {
		text = document;
	}

	/** Spell check event entry method
	 * 
	 */
	@Override
	protected List<RuleMatch> doInBackground() throws IOException, ParserConfigurationException, SAXException {
		List<RuleMatch> matches = check(text);
		return matches;
	}

	/** This method is static and synchronized so that only one spell checking run may 
	 * be active at any given time. Otherwise you will get races.
	 * 
	 * @param text
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private static synchronized List<RuleMatch> check(String text) throws IOException, ParserConfigurationException, SAXException {
		if (langTool == null) {
			langTool = new MultiThreadedJLanguageTool(new AmericanEnglish(), Language.getLanguageForName("German"));
			langTool.activateDefaultFalseFriendRules();
			
		}
		JLanguageTool languageTool = langTool;
		List<RuleMatch> matches = languageTool.check(text, true, ParagraphHandling.NORMAL);
		return matches;
	}

}
