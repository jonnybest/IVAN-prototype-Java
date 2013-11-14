package edu.kit.ipd.alicenlp.ivan;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

public class jtstandtest {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					jtstandtest window = new jtstandtest();
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
	public jtstandtest() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final TextEditorPane editor = new TextEditorPane();
		editor.addKeyListener(new KeyAdapter() {
			private JLanguageTool langTool;
			private int limit = 8;
			
			public JLanguageTool getLanguageTool()
			{
				 try
				 {
					 if(langTool == null)
					 {
						 langTool = new JLanguageTool(new AmericanEnglish());
						 langTool.activateDefaultPatternRules();
					 }
					 return langTool;
				 }
				 catch (IOException e)
				 {
					 return null;
				 }
			}
			
			@Override
			synchronized public void keyTyped(KeyEvent arg0) {
				if (canCheckSpelling(arg0)) {
					try {
						//				List<RuleMatch> matches = langTool.check("A sentence " +
						//				    "with a error in the Hitchhiker's Guide tot he Galaxy");
						List<RuleMatch> matches = getLanguageTool().check(editor
								.getText(), true, ParagraphHandling.ONLYNONPARA);
						for (RuleMatch match : matches) {
							System.out.println("Potential error at line "
									+ match.getLine() + ", column "
									+ match.getColumn() + ": "
									+ match.getMessage());
							//					  System.out.println("Suggested correction: " +
							//					      match.getSuggestedReplacements());
							markSpellingError(match.getFromPos(),
									match.getToPos());
							break;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			private boolean canCheckSpelling(KeyEvent event) {
				if(limit  == 0)
				{
					limit  = 8;
					return true;
				}
				else {
					limit --;
					return false;
				}
			}

			private void markSpellingError(int beginPosition, int endPosition) {
				int length = endPosition - beginPosition;
				RSyntaxDocument doc = (RSyntaxDocument) editor.getDocument();
//				try {
//					// System.out.println(txtEditor.getText(beginPosition, length));
//					System.out.println(doc.getText(beginPosition, length));
//				} catch (BadLocationException e) {
//					System.err.println("Bad location: " + beginPosition + " "
//							+ endPosition);
//				}
//
//				doc.setCharacterAttributes(beginPosition, length,
//						doc.getStyle("spellingerror"), true);
				SquiggleUnderlineHighlightPainter sqpainter = new SquiggleUnderlineHighlightPainter(Color.RED);
				try {
					editor.getHighlighter().addHighlight(beginPosition, endPosition, sqpainter);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				nop();
				
			}

			private void nop() {
				// TODO Auto-generated method stub
				
			}
		});
		frame.getContentPane().add(editor, BorderLayout.CENTER);
	}

}
