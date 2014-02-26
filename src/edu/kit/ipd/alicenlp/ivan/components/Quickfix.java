package edu.kit.ipd.alicenlp.ivan.components;

import java.awt.Component;
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

	protected IvanErrorInstance Error;
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
	 * @param codep
	 * @return 
	 */
	public Caret installCaret(CodePoint codep) {
		// if there's already a caret present, use this
		if(Error != null && Error.StandardCaret != null)
		{
			Caret car = Error.StandardCaret;
			// codepoints and markers need to agree
			if(codep.x == car.getDot() && codep.y == car.getMark())
			{
				log.info("Skipped");
				return Error.StandardCaret;
			}
		}
		
		// The caret will track the positions across the users' editings. 
		DefaultCaret place = new DefaultCaret();
		place.install(txtEditor);
		place.setVisible(false);
		place.setDot(codep.x);
		place.moveDot(codep.y);
		log.info("Installed Caret for " + codep);
		this.sentence = place;
		return place;
	}

	protected void removeError(IvanErrorInstance Error) {
		// this caret is now useless and can be removed
		sentence.deinstall(txtEditor);
		
		// assume this issue to be fixed and remove this error's components
		for (Component co : Error.Components) {
			co.getParent().remove(co);
		}
		Error.Components.clear();
	}

}
