package de.uka.ipd.resi.gui;

import org.eclipse.swt.widgets.Composite;

import de.uka.ipd.resi.Rule;
import de.uka.ipd.resi.ruleimpl.RuleCheckDeterminers;
import de.uka.ipd.resi.ruleimpl.RuleCheckForNominalization;
import de.uka.ipd.resi.ruleimpl.RuleSimilarMeaning;
import de.uka.ipd.resi.ruleimpl.RuleCompleteProcessWords;
import de.uka.ipd.resi.ruleimpl.RuleAvoidAmbiguousWords;

/**
 * Composite for adding Rules to the SpecificationImprover.
 * 
 * @author Torben Brumm
 */
public class RuleAdder extends Adder {
	
	/**
	 * Listener that is fired when a Rule is added.
	 * 
	 * @author Torben Brumm
	 */
	public interface RuleAdderListener {
		
		/**
		 * Fired when a new Rule is added.
		 * 
		 * @param rule Rule to add.
		 * @param ruleName Name of the Rule to add.
		 * @throws NullPointerException
		 */
		public void onAdd(Rule rule, String ruleName) throws NullPointerException;
		
		/**
		 * Fired before a new Rule is added.
		 * 
		 * @param ontologyName Name of the Rule to add.
		 */
		public void onBeforeAdd(String ruleName);
	}
	
	/**
	 * Listener that is called when a Rule is added.
	 */
	private final RuleAdderListener listener;
	
	/**
	 * Constructor.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 * @param listener Listener that is called when a Rule is added.
	 */
	public RuleAdder(final Composite parent, final int style, final RuleAdderListener listener) {
		super(parent, style);
		this.listener = listener;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getItemName()
	 */
	@Override
	protected String getItemName() {
		return "Rules";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#getPossibleItems()
	 */
	@Override
	protected String[] getPossibleItems() {
		return new String[] { "AvoidAmbiguousWords", "CheckForNominalizations", "CompleteProcessWords", "SimilarMeaning",
				"CheckDeterminers"
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.specificationimprover.gui.Adder#onAdd(int)
	 */
	@Override
	protected String onAdd(final int selectedItemIndex) {
		final String ruleName = this.getPossibleItems()[selectedItemIndex];
		this.listener.onBeforeAdd(ruleName);
		Rule r = null;
		switch (selectedItemIndex) {
			case 0:
				r = new RuleAvoidAmbiguousWords();
				break;
			case 1:
				r = new RuleCheckForNominalization();
				break;
			case 2:
				r = new RuleCompleteProcessWords();
				break;
			case 3:
				r = new RuleSimilarMeaning();
				break;
			case 4:
				r = new RuleCheckDeterminers();
				break;
			default:
				// do nothing
		}
		this.listener.onAdd(r, ruleName);
		return ruleName;
	}
	
}
