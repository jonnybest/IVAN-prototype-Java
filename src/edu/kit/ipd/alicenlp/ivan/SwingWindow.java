package edu.kit.ipd.alicenlp.ivan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class SwingWindow {

	private org.joda.time.DateTime stopwatch;
	private JFrame frame;
	private LineNumbersTextPane txtEditor;
	StyleContext sc = new StyleContext();
	// final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
	private Style SetupStyle;
	protected AttributeSet DefaultStyle;
	private JTextPane emitterTextPane;

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
		/*
		 * Recognise modal sentences
		 */
		StaticDynamicClassifier myclassifier = StaticDynamicClassifier
				.getInstance();
		DeclarationPositionFinder mydeclarationfinder = DeclarationPositionFinder.getInstance();
		String lines = text;
		/*
		 * String[] modalVerbs = {"can", "could", "may", "might", "must",
		 * "shall", "should", "will", "would", "have to", "has to", "had to",
		 * "need"}; for (String string : modalVerbs) { if
		 * (lines.contains(string)) { System.out.println("Found bad word: " +
		 * string + "."); } }
		 */
		/* tag with pos tags */

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
			if (depgraph.size() < 3) {
				continue;
			}
			StaticDynamicClassifier.Classification sentencetype = myclassifier.classifySentence(root, sentence);
			
			// color the sentence according to classification 
			switch (sentencetype) {
			case SetupDescription:
				//tell(depgraph.toString());
				markText(root.beginPosition(), root.endPosition());
				//DeclarationPositionFinder.DeclarationQuadruple decl = mydeclarationfinder.findAll(root, sentence);
				System.out.println(mydeclarationfinder.hasLocation(sentence));
				break;
			case ErrorDescription:
				// emit error
				break;
			case ActionDescription:
				// fallthrough to default
			default:				
				break;
			}
			
			
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
