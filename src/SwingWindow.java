import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import org.eclipse.swt.widgets.DateTime;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.joda.time.DateTimeField;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class SwingWindow {

	private org.joda.time.DateTime stopwatch;
	private JXFrame frame;
	private JXEditorPane txtEditor;
	private JTextPane txtLineNumbers;

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
		frame = new JXFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		txtLineNumbers = new JTextPane();
		txtLineNumbers.setEnabled(false);
		txtLineNumbers.setText("1\r\n2\r\n3\r\n4\r\n5");
		panel.add(txtLineNumbers);
		
		txtEditor = new JXEditorPane();
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
			
			private void stopAndPrintStopWatch() {
				// TODO Auto-generated method stub
				org.joda.time.DateTime now = org.joda.time.DateTime.now();
				long diff = now.getMillis() - stopwatch.getMillis();
				System.err.println("Stopwatch: " + (diff) + " ms.");
			}

			private void startStopWatch() {
				stopwatch = org.joda.time.DateTime.now();
			}

			/**
			 * @param text
			 * @throws Exception 
			 */
			private void processText(String text) throws Exception {				
				/* Recognise modal sentences 
				 */
				StaticDynamicClassifier myclassifier = StaticDynamicClassifier.getInstance();
				String lines = text;
				/*String[] modalVerbs = {"can", "could", "may", "might", "must", "shall", "should", "will", "would", "have to", "has to", "had to", "need"};
				for (String string : modalVerbs) {
					if (lines.contains(string)) {
						System.out.println("Found bad word: " + string + ".");						
					}
				}*/
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
			    java.util.List<CoreMap> sentences = mydoc.get(SentencesAnnotation.class);
			    
			    
			    for(CoreMap sentence: sentences)
			    {
			    	// traversing the words in the current sentence
			    	// a CoreLabel is a CoreMap with additional token-specific labels
			    	for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
			    		// this is the text of the token
			            //String word = item.get(TextAnnotation.class);
			            // this is the POS tag of the token
			            String pos = item.get(PartOfSpeechAnnotation.class);
			            if (pos.equals("VBG")) {
							System.out.println(item.getString(TextAnnotation.class) + " is a gerund.");
							markText(item.beginPosition(), item.endPosition());
						}
			            // this is the NER label of the token
			            //String ne = item.get(NamedEntityTagAnnotation.class);
			            
			            //System.out.println(word);
					}
			    }
			    
			    
			}

			private void markText(int beginPosition, int endPosition) {
				StyledDocument styled = (StyledDocument) txtEditor.getDocument(); 
				styled.
				
			}
		});
		txtEditor.setText("In publishing and graphic design, lorem ipsum is a placeholder text (filler text) commonly "
				+ "used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, "
				+ "and layout, by removing the distraction of meaningful content.");
		
		panel.add(txtEditor);
		
		JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();		
		JXTaskPane taskPane = new JXTaskPane();
		JXLabel taskLabel = new JXLabel("Nothing to do right now.");
		taskPane.add(taskLabel);
		taskContainer.add(taskPane);
		panel.add(taskContainer);
		
		JTextPane txtpnHi = new JTextPane();
		txtpnHi.setEditable(false);
		txtpnHi.setText("...");
		frame.getContentPane().add(txtpnHi);
	}

}
