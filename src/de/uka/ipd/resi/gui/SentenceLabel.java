package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import de.uka.ipd.resi.Word;

/**
 * Label which prints a sentence of a word with the given word highlighted.
 * 
 * @author Torben Brumm
 */
public class SentenceLabel extends StyledText {
	
	/**
	 * Constructor.
	 * 
	 * @param word Word to highlight (includes the sentence to print).
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 */
	public SentenceLabel(final Word word, final Composite parent, final int style) {
		super(parent, style);
		this.setEditable(false);
		this.setBackground(parent.getBackground());
		this.setText(word.getSentence().getPlainSentence());
		
		final StyleRange styleRange = new StyleRange();
		styleRange.start = this.getStartIndexInSentenceForWord(word);
		styleRange.length = word.getPlainWord().length();
		styleRange.fontStyle = SWT.BOLD;
		styleRange.foreground = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		this.setStyleRange(styleRange);
	}
	
	/**
	 * Returns the char index in the sentence the word starts at.
	 * 
	 * @param word Word to find.
	 * @return Index.
	 */
	private int getStartIndexInSentenceForWord(final Word word) {
		int counter = 0;
		for (final Word currentWord : word.getSentence().getWords()) {
			if (currentWord.getId().equals(word.getId())) {
				return counter;
			}
			counter += currentWord.getPlainWord().length();
			if (!(currentWord.getPlainWord().equals(",") || currentWord.getPlainWord().equals(":") || currentWord
					.getPlainWord().equals(";"))) {
				// insert space after this word (yes, this way ",", ":" and ";" have a space before, but not after
				// them...wrong, but doesn't matter for the counter
				counter++;
			}
		}
		return counter;
	}
	
}
