/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatBeginningRule;
import org.languagetool.rules.patterns.PatternRule;
import org.omg.CORBA.Environment;
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
		JLanguageTool languageTool = null;
		
		if (langTool == null) {
			languageTool = new MultiThreadedJLanguageTool(new AmericanEnglish(), Language.getLanguageForName("German"));
			// get and delete some obnoxious default rules
			List<Rule> things = languageTool.getAllActiveRules();
			//TODO remove CommaWhitespaceRule
			//TODO remove WordRepeatBeginningRule
			languageTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
			languageTool.disableRule("WHITESPACE_PUNCTUATION");
			languageTool.disableRule("ENGLISH_WORD_REPEAT_BEGINNING_RULE");
			
			// load my custom rule set
			System.out.println(JLanguageTool.getDataBroker().getResourceDir());
			URL grammarrules = ClassLoader.getSystemResource("edu/kit/ipd/alicenlp/ivan/resources/grammar.xml");
			List<PatternRule> loadPatternRules = languageTool.loadPatternRules(grammarrules.getPath());
			for (PatternRule patternRule : loadPatternRules) {
				languageTool.addRule(patternRule);
			}
			langTool = languageTool;
		}
		
		List<RuleMatch> matches = langTool.check(text, true, ParagraphHandling.NORMAL);
		return matches;
	}

}
