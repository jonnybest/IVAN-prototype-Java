package edu.kit.ipd.alicenlp.ivan.components;

import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import edu.kit.ipd.alicenlp.ivan.data.CodePoint;

/** Template class for installing caret tracking
 * 
 * @author Jonny
 *
 */
public abstract class Quickfix extends AbstractAction {

	protected Caret sentence;
	private JTextComponent txtEditor;
	final Logger log = Logger.getLogger(getClass().getName());

	/** This action implements an abstract quick fix.
	 * 
	 * @param name
	 * @param txtEditor 
	 */
	public Quickfix(String name, JTextComponent txtEditor) {
		super(name);
		this.txtEditor = txtEditor;
	}

	/**
	 * @param sentence
	 */
	public void installCaret(CodePoint sentence) {
		// The caret will track the positions across the users' editings. 
		this.sentence = new DefaultCaret();
		this.sentence.install(txtEditor);
		this.sentence.setVisible(false);
		this.sentence.setDot(sentence.x);
		this.sentence.moveDot(sentence.y);
		log.info("Installed Caret for " + sentence);
	}

}
