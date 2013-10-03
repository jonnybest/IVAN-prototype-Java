package edu.kit.ipd.alicenlp.ivan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LineNumbersTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.sf.extjwnl.dictionary.Dictionary;

import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.Locale;

public class SwingWindow {

	private org.joda.time.DateTime stopwatch;
	private JFrame frame;
	private LineNumbersTextPane txtEditor;
	StyleContext sc = new StyleContext();
	// final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
	private Style SetupStyle;
	protected AttributeSet DefaultStyle;
	private JTextPane emitterTextPane;
	private Set<EntityInfo> problemSetMissingLocation;
	private Set<EntityInfo> problemSetMissingDirection;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingWindow window = new SwingWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setLocale(Locale.ENGLISH);
		frame.setTitle("¶v – Input & Verify AliceNLP");
		frame.setBounds(100, 100, 612, 511);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		

		txtEditor = new LineNumbersTextPane();
		txtEditor.setDisplayLineNumbers(true);
		// txtEditor.setDocument(doc);

		txtEditor.setText("In publishing and graphic design, lorem ipsum is a \n"
				+ "placeholder text (filler text) commonly used to demonstrate \n"
				+ "the graphic elements of a document or visual presentation, \n"
				+ "such as font, typography, and layout, by removing the \n"
				+ "distraction of meaningful content.");

		addStylesToDocument((StyledDocument) txtEditor.getDocument());		

		frame.getContentPane().add(txtEditor.getContainerWithLines(), BorderLayout.CENTER);
		
		JXTaskPaneContainer containerTaskPanel = new JXTaskPaneContainer();
		JXTaskPane starterPain = new JXTaskPane();
		starterPain.setTitle("Serious problems ");
		starterPain.add(new Label("None."));
		containerTaskPanel.add(starterPain);
		frame.getContentPane().add(containerTaskPanel, BorderLayout.EAST);
		
		emitterTextPane = new JTextPane();
		emitterTextPane.setText("Hello World!");
		//emitterTextPane.setPreferredSize(new Dimension(10, 40));
		emitterTextPane.setEditable(false);
		JScrollPane emitterScrollPane = new JScrollPane(emitterTextPane);		
		frame.getContentPane().add(emitterScrollPane, BorderLayout.SOUTH);

		// the emitter and the TaskPane have something to work on, so set up the linguistics stuff
		setupFeedback();
		
		txtEditor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (arg0.getKeyChar() == '.' || arg0.getKeyChar() == '\n') {
					JXEditorPane editor = (JXEditorPane) arg0.getSource();
					try {
						startStopWatch();
						processText(editor.getText());
						stopAndPrintStopWatch();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		// LineWrapEditorKit mykit = (LineWrapEditorKit)
		// txtEditor.getEditorKit();
		// mykit.setWrap(true);

		refreshLineNumbersFont();
	}

	/** Prepares the problems list for the task pane. 
	 * If I decide to do inital stuff with the taskpane and the emitter, that code goes here as well.
	 */
	private void setupFeedback() {
		// prepare a list for the problems
		this.problemSetMissingDirection = new HashSet<EntityInfo>();
		this.problemSetMissingLocation = new HashSet<EntityInfo>();
	}

	/**
	 * 
	 */
	private void refreshLineNumbersFont() {
		Font font = txtEditor.getFont();
		txtEditor.setFont(font);
	}

	protected void addStylesToDocument(StyledDocument doc) {
		// Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		DefaultStyle = def;

		SetupStyle = doc.addStyle("setup", def);
		StyleConstants.setBackground(SetupStyle, Color.CYAN);
		StyleConstants.setBold(SetupStyle, true);
		// StyleConstants.setFontFamily(SetupStyle, "Times New Roman");

		Style action = doc.addStyle("action", SetupStyle);
		StyleConstants.setBackground(action, Color.ORANGE);

		Style event = doc.addStyle("event", SetupStyle);
		StyleConstants.setBackground(event, Color.MAGENTA);

		Style time = doc.addStyle("time", SetupStyle);
		StyleConstants.setBackground(time, Color.YELLOW);

		/*
		 * Style s = doc.addStyle("italic", regular);
		 * StyleConstants.setItalic(s, true);
		 */

	}

	/**
	 * @param text
	 * @throws Exception
	 */
	private void processText(String text) throws Exception {
		clearStyles();
		
		/** Until I come up with something better, I need to scrub the state of everything each time the analysis runs 
		 */
		// FIXME: This part is a setup for memory leaks. 
		problemSetMissingDirection.clear();
		problemSetMissingLocation.clear();
		DeclarationPositionFinder mydeclarationfinder = DeclarationPositionFinder.getInstance();
		mydeclarationfinder.reset(); // this component is stateful, so we have to reset it
		
		// Get an instance of the classifier for setup sentences
		StaticDynamicClassifier myclassifier = StaticDynamicClassifier
				.getInstance();
		String lines = text;
		/*
		 * String[] modalVerbs = {"can", "could", "may", "might", "must",
		 * "shall", "should", "will", "would", "have to", "has to", "had to",
		 * "need"}; for (String string : modalVerbs) { if
		 * (lines.contains(string)) { System.out.println("Found bad word: " +
		 * string + "."); } }
		 */
		/* tag with pos tags */

		Dictionary dictionary = null;
		try {
			dictionary = myclassifier.getDictionary();
		} 
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StanfordCoreNLP pipeline = null;
		try {
			pipeline = myclassifier.getPipeline();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Annotation mydoc = new Annotation(lines);
		pipeline.annotate(mydoc);
		java.util.List<CoreMap> sentences = mydoc
				.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentences
			SemanticGraph depgraph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			if (depgraph.getRoots().isEmpty()) {
				continue;
			}
			IndexedWord root = depgraph.getFirstRoot();
			// probably not a good idea. Counter-example of a valid sentence with 2 words: Henry appears.
//			if (depgraph.size() < 3) {
//				continue;
//			}
			if (depgraph.isEmpty()) {
				continue;
			}
			
			/*** Requirement 1: Check subject for entity or name and extract name, position and direction if possible
			 * 		Also, save name in name list and save position and direction in EntityInfo.
			 */
			// TODO: implement problem1
			// 1. check for names
			List<String> names = mydeclarationfinder.recogniseNames(sentence); // recognises named und unnamed entities in this sentence
			// 2. are they declared already?
			boolean everythingdeclared = mydeclarationfinder.isDeclared(names);
			// -- for each name: 
			for (String n : names) {					
				// 		4. while we're at it (iterating), check if there is any info missing for this name
				/*
				 * I have two options: 1. retrieve the entityinfo, check for missing data, display a warning to the user
				 * or 2. retrieve entityinfo, check for missing data, store the entityinfo in a list corresponding with the
				 * problem type and later call a method for each problem type which displays a compact list of missing things.
				 * I chose 2.
				 */
				List<EntityInfo> declarednames = mydeclarationfinder.getCurrentState().get(n);
				
				if (declarednames == null) {
					// there are no declarations with this name (at all)
					// TODO: does this sentence qualify as a declaration? if yes, declare now and try to get declared names again. if not, skip these.
					List<EntityInfo> decls = mydeclarationfinder.getDeclarations(sentence);
					if (decls.size() > 0) {
						mydeclarationfinder.getCurrentState().addAll(decls);
						declarednames = decls;
					}
					else {
						continue;
					}
				}
				for (EntityInfo infoOnName : declarednames) {
					
					if (!infoOnName.hasLocation()) {
						// try to get a loc from the current sentence. if not, add to problems. if yes, remove from problems
						EntityInfo moreinfo = mydeclarationfinder.getLocation(sentence);
						if (moreinfo == null || !moreinfo.hasLocation()) {
							// Bad! This sentence contains no location info and we are still missing location info
							problemSetMissingLocation.add(infoOnName);
						}
						else {
							// fixme: the info may not relate to the proper name
							assert infoOnName.getEntity().equals(moreinfo.getEntity());
							
							// good! this sentence contains info, so lets merge the two
							infoOnName.setLocation(moreinfo.getLocation());
							// and remove the problem entry if we had one
							@SuppressWarnings("unused")
							boolean success = problemSetMissingLocation.remove(infoOnName);
						}
					}
					if (!infoOnName.hasDirection()) 
					{
						// try to get a dir from the current sentence. if not, add to problems. if yes, remove from problems
						EntityInfo moreinfo = mydeclarationfinder.getDirection(sentence);
						if (moreinfo == null || !moreinfo.hasDirection()) {
							// Bad! This sentence contains no location info and we are still missing location info
							problemSetMissingDirection.add(infoOnName);
						}
						else {
							// good! this sentence contains info, so lets merge the two
							infoOnName.setDirection(moreinfo.getDirection());
							// and remove the problem entry if we had one
							@SuppressWarnings("unused")
							boolean success = problemSetMissingDirection.remove(infoOnName);
						}						
					}
				}
				//		5. create a display for the missing info?
				// see above
			}
			
			/*** Requirement 2: Classify sentence into Setup descriptions and non-setup descriptions  
			 */
			StaticDynamicClassifier.Classification sentencetype = myclassifier.classifySentence(root, sentence);
			if (mydeclarationfinder.hasLocation(sentence)) {
				EntityInfo loc = mydeclarationfinder.getLocation(sentence);
				tell("There's a location in \"" + sentence.get(TextAnnotation.class));
				System.out.println("The location \""+ loc +"\" was found in \"" + sentence.get(TextAnnotation.class));
			}
			
			// color the sentence according to classification 
			switch (sentencetype) {
			case SetupDescription:
				//tell(depgraph.toString());
				markText(root.beginPosition(), root.endPosition());
				//DeclarationPositionFinder.DeclarationQuadruple decl = mydeclarationfinder.findAll(root, sentence);
				break;
			case ErrorDescription:
				// emit error
				break;
			case ActionDescription:
				// fallthrough to default
			default:				
				break;
			}
			
			/*** Requirement 3: Check this sentence for co-reference
			 */
			
			// a CoreLabel is a CoreMap with additional token-specific labels			
//			for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
//				// this is the text of the token
//				// String word = item.get(TextAnnotation.class);
//				// this is the POS tag of the token
//				String pos = item.get(PartOfSpeechAnnotation.class);
//				if (pos.equals("VBG")) {
//					tell("\""+ item.getString(TextAnnotation.class) + "\" is a gerund.");
//					markText(item.beginPosition(), item.endPosition());
//				}
//				// this is the NER label of the token
//				// String ne = item.get(NamedEntityTagAnnotation.class);
//
//				// System.out.println(word);
//			}
		}
		refreshLineNumbersFont();
		diplayWarnings();
	}

	private void diplayWarnings() {
		// TODO This method manipulates the right-hand actionpanel for displaying errors and warnings
		// create a missing-stuff display
		
		for (EntityInfo info : problemSetMissingLocation) {
			// TODO: add this entity to the missinglocation problem
		}		
		
		for (EntityInfo info : problemSetMissingDirection) {
			// TODO: add this entity to the missingdirection problem
		}
	}

	private void tell(String output) {
		this.emitterTextPane.setText(output);
	}

	private void clearStyles() {
		StyledDocument doc = (StyledDocument) txtEditor.getDocument();
		doc.setCharacterAttributes(0, doc.getLength(), DefaultStyle, true);
	}

	private void markText(int beginPosition, int endPosition) {
		int length = endPosition - beginPosition;
		StyledDocument doc = (StyledDocument) txtEditor.getDocument();
		try {
			// System.out.println(txtEditor.getText(beginPosition, length));
			System.out.println(doc.getText(beginPosition, length));
		} catch (BadLocationException e) {
			System.err.println("Bad location: " + beginPosition + " "
					+ endPosition);
		}

		doc.setCharacterAttributes(beginPosition, length,
				doc.getStyle("action"), true);
		nop();
	}

	private void nop() {
	}

	private void stopAndPrintStopWatch() {
		// TODO Auto-generated method stub
		org.joda.time.DateTime now = org.joda.time.DateTime.now();
		long diff = now.getMillis() - stopwatch.getMillis();
		System.err.println("Stopwatch: " + (diff) + " ms.");
	}

	private void startStopWatch() {
		stopwatch = org.joda.time.DateTime.now();
	}

}
