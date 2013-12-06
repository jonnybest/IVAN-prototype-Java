package de.uka.ipd.resi;

import javatools.datatypes.Pair;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.recaacommons.Sense;
import de.uka.ipd.resi.exceptions.NotConnectedException;
import de.uka.ipd.resi.exceptions.SyntaxException;
import de.uka.ipd.resi.exceptions.WordNotFoundException;
import de.uka.ipd.resi.gui.DeterminerSelector;
import de.uka.ipd.resi.gui.GraphAdder;
import de.uka.ipd.resi.gui.NominalizationMarker;
import de.uka.ipd.resi.gui.OntologyAdder;
import de.uka.ipd.resi.gui.RuleAdder;
import de.uka.ipd.resi.gui.SpecificationViewer;
import de.uka.ipd.resi.gui.VerbFrameSelector;
import de.uka.ipd.resi.gui.WordSenseSelector;
import de.uka.ipd.resi.gui.YesNoSelector;
import de.uka.ipd.resi.gui.DeterminerSelector.DeterminerSelectorListener;
import de.uka.ipd.resi.gui.GraphAdder.GraphAdderListener;
import de.uka.ipd.resi.gui.NominalizationMarker.NominalizationMarkerListener;
import de.uka.ipd.resi.gui.OntologyAdder.OntologyAdderListener;
import de.uka.ipd.resi.gui.RuleAdder.RuleAdderListener;
import de.uka.ipd.resi.gui.VerbFrameSelector.VerbFrameSelectorListener;
import de.uka.ipd.resi.gui.WordSenseSelector.WordSenseSelectorListener;
import de.uka.ipd.resi.gui.YesNoSelector.YesNoSelectorListener;
import de.uka.ipd.resi.ontologyimpl.StanfordPOSTagger;
import de.uka.ipd.resi.ontologyimpl.WordNetOntology;
import de.uka.ipd.resi.ontologyinterface.OntologyBaseFormTag;
import de.uka.ipd.resi.ontologyinterface.OntologyPOSTag;
import edu.kit.ipd.alicenlp.ivan.components.SshConnector;

public class Application {
	
	/**
	 * Server address of the Cyc server. TODO make configurable
	 */
	public static final String CYC_SERVER_ADDRESS = "localhost";
	
	/**
	 * Port of the Cyc server. TODO make configurable
	 */
	public static final int CYC_SERVER_PORT = 3600;
	
	/**
	 * Single instance of the application.
	 */
	private static Application instance;
	
	/**
	 * Directory where the tagger models reside. For English, the bidirectional taggers is slightly more accurate, but
	 * tags more slowly; choose the appropriate tagger based on your speed/performance needs. TODO make configurable
	 */
	private static final String STANFORDPOSTAGGER_MODEL_DIR = "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/"; // "lib/POS-models/";
	
	/**
	 * Trained on WSJ sections 0-18 using a bidirectional architecture and includes word shape features. Penn tagset.
	 * Performance: 97.18% correct on WSJ 19-21 (89.30% correct on unknown words) TODO make configurable
	 */
	private static final String STANFORDPOSTAGGER_TAGGER_1 = "wsj-0-18-bidirectional-distsim.tagger"; // "bidirectional-wsj-0-18.tagger";
	
	/**
	 * Trained on WSJ sections 0-18 using the left3words architectures and includes word shape features. Penn tagset.
	 * Performance: 96.97% correct on WSJ 19-21 (89.03% correct on unknown words) TODO make configurable
	 */
	private static final String STANFORDPOSTAGGER_TAGGER_2 = "left3words-wsj-0-18.tagger";
	
	/**
	 * Trained on WSJ sections 0-18 using the left3words architectures and includes word shape and distributional
	 * similarity features.# Penn tagset. Performance: 96.99% correct on WSJ 19-21 (89.77% correct on unknown words)
	 * TODO make configurable
	 */
	private static final String STANFORDPOSTAGGER_TAGGER_3 = "left3words-distsim-wsj-0-18.tagger";
	
	/**
	 * Path to a local WordNet database. TODO make configurable
	 */
	public static String WORDNET_DICT_PATH = "C:\\Programme\\WordNet\\2.1\\dict";
	
	/**
	 * Server Address of the WordNetServer. TODO make configurable
	 */
	public static String WORDNET_SERVER_ADDRESS = "localhost";
	
	/**
	 * Port of the WordNetServer. TODO make configurable
	 */
	public static int WORDNET_SERVER_PORT = 4711;
	
	/**
	 * Singleton getter.
	 * 
	 * @return the instance
	 */
	public static Application getInstance() {
		if (Application.instance == null) {
			Application.instance = new Application();
		}
		return Application.instance;
	}
	
	/**
	 * Starts the application.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		Application.getInstance().run();
	}
	
	/**
	 * ComboBox for selecting the BaseFormTagger.
	 */
	private Combo baseFormTaggerCombo;
	
	/**
	 * Button for clearing everything and restarting with a fresh SpecificationImprover.
	 */
	private Button clearButton;
	
	/**
	 * SWT-Display.
	 */
	private final Display display;
	
	/**
	 * Button for exporting the current content of the graph to a GXL file.
	 */
	private Button exportButton;
	
	/**
	 * GraphAdder for adding Graphs to the SpecificationImprover.
	 */
	private final GraphAdder graphAdder;
	
	/**
	 * Button for applying all selected rules to all selected graphs with all selected ontologies.
	 */
	private Button improveButton;
	
	/**
	 * Log output.
	 */
	private final Text log;
	
	/**
	 * OntologyAdder for adding Ontologies to the SpecificationImprover.
	 */
	private final OntologyAdder ontologyAdder;
	
	/**
	 * ComboBox for selecting the POSTagger.
	 */
	private Combo posTaggerCombo;
	
	/**
	 * Button for preannotating the added graphs with base forms and POS tags.
	 */
	private Button preannotateButton;
	
	/**
	 * Button for printing all sentences in the added graphs to the log output.
	 */
	private Button printSentencesButton;
	
	/**
	 * RuleAdder for adding Rules to the SpecificationImprover.
	 */
	private final RuleAdder ruleAdder;
	
	/**
	 * SWT-Shell.
	 */
	private final Shell shell;
	
	/**
	 * SpecificationViewer for displaying the current specification.
	 */
	private final SpecificationViewer specificationViewer;
	
	/**
	 * SpecificationImprover which does all the work.
	 */
	private SpecificationImprover specImprover = new SpecificationImprover();
	
	/**
	 * Field that stores temporarily values for dialogs.
	 */
	private Object tempDialogReturnValue;
	
	/**
	 * Constructor for starting the application.
	 */
	private Application() {
		this.display = new Display();
		this.shell = new Shell(this.display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText("RESI");
		//this.shell.setImage(new Image(this.display, "Wheeled-tractor-32.png"));
		
		final GridLayout layout = new GridLayout(2, false);
		this.shell.setLayout(layout);
		
		// ***** Settings *****
		
		final Composite settingsComposite = new Composite(this.shell, 0);
		settingsComposite.setLayout(new GridLayout(2, true));
		settingsComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		
		// *** Graphs ***
		
		this.graphAdder = new GraphAdder(settingsComposite, SWT.BORDER, new GraphAdderListener() {
			
			@Override
			public void onAdd(final Graph graph, final String graphName) {
				if (graph != null) {
					Application.this.specImprover.addGraph(graph);
					Application.this.checkButtons();
					try {
						Application.this.updateSpecificationView();
					}
					catch (final SyntaxException ex) {
						Application.this.handleException(ex);
					}
					Application.this.log.append("\n\nGraph" + graphName + " added.");
				}
				else {
					Application.this.log.append("\n\nNo graph " + graphName + " could be added.");
					throw new NullPointerException();
				}
				
			}
			
			@Override
			public void onBeforeAdd(final String graphName) {
				Application.this.log.append("\n\nAdding graph " + graphName + "...");
			}
			
		});
		this.graphAdder.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		
		// *** Ontologies ***
		
		this.ontologyAdder = new OntologyAdder(settingsComposite, SWT.BORDER, new OntologyAdderListener() {
			
			@Override
			public void onAdd(final Ontology ontology, final String ontologyName) throws NullPointerException {
				if (ontology != null) {
					Application.this.specImprover.addOntology(ontology);
					Application.this.checkButtons();
					Application.this.log.append("\n\nOntology" + ontologyName + " added.");
				}
				else {
					Application.this.log.append("\n\nNo ontology " + ontologyName + " could be added.");
					throw new NullPointerException();
				}
				
			}
			
			@Override
			public void onBeforeAdd(final String ontologyName) {
				Application.this.log.append("\n\nAdding ontology " + ontologyName + "...");
			}
			
		});
		this.ontologyAdder.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		
		// *** Rules ***
		
		this.ruleAdder = new RuleAdder(settingsComposite, SWT.BORDER, new RuleAdderListener() {
			
			@Override
			public void onAdd(final Rule rule, final String ruleName) {
				if (rule != null) {
					Application.this.specImprover.addRule(rule);
					Application.this.checkButtons();
					Application.this.log.append("\n\nRule" + ruleName + " added.");
				}
				else {
					Application.this.log.append("\n\nNo rule " + ruleName + " could be added.");
					throw new NullPointerException();
				}
				
			}
			
			@Override
			public void onBeforeAdd(final String ruleName) {
				Application.this.log.append("\n\nAdding rule " + ruleName + "...");
			}
			
		});
		this.ruleAdder.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		
		// *** Tagger Selectors ***
		
		this.insertTaggerSelector(settingsComposite);
		
		// *****Specification *****
		
		final Composite specificationComposite = new Composite(this.shell, SWT.BORDER);
		specificationComposite.setLayout(new GridLayout(1, true));
		final GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		viewerData.widthHint = 250;
		specificationComposite.setLayoutData(viewerData);
		
		final Label specificationLabel = new Label(specificationComposite, SWT.CENTER);
		specificationLabel.setText("Current Specification");
		specificationLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		final ScrolledComposite specificationViewerScroll = new ScrolledComposite(specificationComposite, SWT.V_SCROLL);
		specificationViewerScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.specificationViewer = new SpecificationViewer(specificationViewerScroll, SWT.NONE);
		specificationViewerScroll.setContent(this.specificationViewer);
		this.specificationViewer.setSize(this.specificationViewer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// ***** Buttons *****
		
		final Composite buttonComposite = this.insertButtons(this.shell, 0);
		buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		// ***** Log *****
		
		this.log = new Text(this.shell, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		final GridData logLayoutdata = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		logLayoutdata.heightHint = 200;
		this.log.setLayoutData(logLayoutdata);
		this.log.setEditable(false);
		
		this.checkButtons();
		
		this.shell.pack();
	}
	
	/**
	 * Centers a Shell on a given rectangle.
	 * 
	 * @param shell Shell to center.
	 * @param parentRectangle Rectangle to center the Shell on.
	 */
	public void centerShell(final Shell shell, final Rectangle parentRectangle) {
		final Rectangle shellRectangle = shell.getBounds();
		shell.setLocation(parentRectangle.x + (parentRectangle.width - shellRectangle.width) / 2, parentRectangle.y
				+ (parentRectangle.height - shellRectangle.height) / 2);
	}
	
	/**
	 * Checks which buttons should be enabled.
	 */
	private void checkButtons() {
		if (this.specImprover.hasGraphs()) {
			this.printSentencesButton.setEnabled(true);
			this.exportButton.setEnabled(true);
		}
		else {
			this.printSentencesButton.setEnabled(false);
			this.exportButton.setEnabled(false);
		}
		
		if (this.specImprover.hasGraphs()
				&& ((this.posTaggerCombo.getSelectionIndex() > 0) || (this.baseFormTaggerCombo.getSelectionIndex() > 0))) {
			this.preannotateButton.setEnabled(true);
		}
		else {
			this.preannotateButton.setEnabled(false);
		}
		
		if (this.specImprover.hasGraphs() && this.specImprover.hasOntologies() && this.specImprover.hasRules()) {
			this.improveButton.setEnabled(true);
		}
		else {
			this.improveButton.setEnabled(false);
		}
		
		// "Clear" button always works
	}
	
	/**
	 * Handles any exception taht occurs in the GUI.
	 * 
	 * @param e Exception that shall be handled.
	 */
	public void handleException(final Exception e) {
		Application.this.log.append("\n\n" + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
	}
	
	/**
	 * Inserts all buttons into the given Composite on a new Composite.
	 * 
	 * @param composite Composite to insert into.
	 * @param style Style the Composite with the buttons shall have.
	 * @return Composite with the buttons in it.
	 */
	private Composite insertButtons(final Composite composite, final int style) {
		final Composite buttonComposite = new Composite(composite, style);
		buttonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		final Button logButton = new Button(buttonComposite, SWT.TOGGLE);
		logButton.setText("Hide Log");
		logButton.setSelection(true);
		logButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (logButton.getSelection()) {
					((GridData) Application.this.log.getLayoutData()).heightHint = 250;
					Application.this.log.setVisible(true);
					logButton.setText("Hide Log");
				}
				else {
					Application.this.log.setVisible(false);
					((GridData) Application.this.log.getLayoutData()).heightHint = 0;
					logButton.setText("Show Log");
				}
				Application.this.shell.pack();
			}
		});
		
		this.printSentencesButton = new Button(buttonComposite, SWT.PUSH);
		this.printSentencesButton.setText("Print Sentences");
		this.printSentencesButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					String outputString = "\n\nPrint Sentences:\n=============\n";
					for (final Sentence sentence : Application.this.specImprover.getCurrentSpecification()) {
						outputString += sentence.printCompleteSentence();
					}
					Application.this.log.append(outputString);
				}
				catch (final SyntaxException e1) {
					Application.this.handleException(e1);
				}
			}
		});
		
		this.preannotateButton = new Button(buttonComposite, SWT.PUSH);
		this.preannotateButton.setText("Preannotate Graphs");
		this.preannotateButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					Application.this.log.append("\n\nPreannotating graphs...");
					Application.this.specImprover.preAnnotateGraphs();
					Application.this.updateSpecificationView();
					Application.this.log.append("\n\nGraphs preannotated.");
				}
				catch (final WordNotFoundException e1) {
					Application.this.handleException(e1);
				}
				catch (final SyntaxException e1) {
					Application.this.handleException(e1);
				}
			}
		});
		
		this.improveButton = new Button(buttonComposite, SWT.PUSH);
		this.improveButton.setText("IMPROVE!");
		this.improveButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Application.this.log.append("\n\nImproving specification...");
				try {
					Application.this.specImprover.improve();
					Application.this.updateSpecificationView();
				}
				catch (final WordNotFoundException ex) {
					Application.this.handleException(ex);
				}
				catch (final SyntaxException ex) {
					Application.this.handleException(ex);
				}
				catch (final NotConnectedException ex) {
					Application.this.handleException(ex);
				}
				Application.this.log.append("\n\nSpecification improved.");
			}
		});
		
		this.clearButton = new Button(buttonComposite, SWT.PUSH);
		this.clearButton.setText("Clear");
		this.clearButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Application.this.log.append("\n\nClearing everything...");
				Application.this.specImprover = new SpecificationImprover();
				Application.this.graphAdder.removeAddedItems();
				Application.this.ontologyAdder.removeAddedItems();
				Application.this.ruleAdder.removeAddedItems();
				Application.this.posTaggerCombo.select(0);
				Application.this.baseFormTaggerCombo.select(0);
				Application.this.checkButtons();
				try {
					Application.this.updateSpecificationView();
				}
				catch (final SyntaxException ex) {
					Application.this.handleException(ex);
				}
				Application.this.log.append("\n\nEverything cleared.");
			}
		});
		
		this.exportButton = new Button(buttonComposite, SWT.PUSH);
		this.exportButton.setText("Export");
		this.exportButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Application.this.log.append("\n\nExporting graphs to file...");
				final FileDialog fd = new FileDialog(Application.this.shell, SWT.SAVE);
				fd.setText("Export to file");
				final String[] filterExt = { "*.gxl", "*.*"
				};
				fd.setFilterExtensions(filterExt);
				fd.setFileName("export.gxl");
				final String selected = fd.open();
				if (selected != null) {
					Application.this.specImprover.exportToFile(selected);
					Application.this.log.append("\n\nGraphs successfully exported.");
				}
				else {
					Application.this.log.append("\n\nExport cancelled.");
				}
			}
		});
		return buttonComposite;
	}
	
	/**
	 * Inserts the tagger selector component into the given Composite.
	 * 
	 * @param composite Composite to insert into.
	 */
	private void insertTaggerSelector(final Composite composite) {
		final Composite taggerComposite = new Composite(composite, 0);
		taggerComposite.setLayout(new GridLayout(2, false));
		taggerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label posTaggerComboLabel = new Label(taggerComposite, SWT.LEFT);
		posTaggerComboLabel.setText("POS tagger");
		
		this.posTaggerCombo = new Combo(taggerComposite, SWT.READ_ONLY);
		this.posTaggerCombo.setItems(new String[] { "None", "StanfordPOSTagger (bidirectional)",
				"StanfordPOSTagger (left3words)", "StanfordPOSTagger (left3words with dist. sim.)"
		});
		this.posTaggerCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		this.posTaggerCombo.select(0);
		this.posTaggerCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				OntologyPOSTag posTagger = null;
				final int selectedIndex = Application.this.posTaggerCombo.getSelectionIndex();
				String posTaggerString = Application.this.posTaggerCombo.getItem(selectedIndex);
				Application.this.log.append("\n\nSetting POS tagger " + posTaggerString + "...");
				try {
					switch (selectedIndex) {
						case 0:
							// do nothing, posTagger must be set to null
							break;
						
						case 1:
							posTagger = new StanfordPOSTagger(Application.STANFORDPOSTAGGER_MODEL_DIR
									+ Application.STANFORDPOSTAGGER_TAGGER_1);
							break;
						
						case 2:
							posTagger = new StanfordPOSTagger(Application.STANFORDPOSTAGGER_MODEL_DIR
									+ Application.STANFORDPOSTAGGER_TAGGER_2);
							break;
						
						case 3:
							posTagger = new StanfordPOSTagger(Application.STANFORDPOSTAGGER_MODEL_DIR
									+ Application.STANFORDPOSTAGGER_TAGGER_3);
							break;
						
						default:
							// do nothing
					}
				}
				catch (final NotConnectedException ex) {
					Application.this.handleException(ex);
					posTaggerString = "not";
				}
				Application.this.specImprover.setPOSTagger(posTagger);
				Application.this.checkButtons();
				Application.this.log.append("\n\nPOS tagger " + posTaggerString + " set.");
			}
			
		});
		
		final Label baseFormTaggerComboLabel = new Label(taggerComposite, SWT.LEFT);
		baseFormTaggerComboLabel.setText("Base form tagger");
		
		this.baseFormTaggerCombo = new Combo(taggerComposite, SWT.READ_ONLY);
		this.baseFormTaggerCombo.setItems(new String[] { "None", "WordNet"
		});
		this.baseFormTaggerCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		this.baseFormTaggerCombo.select(0);
		this.baseFormTaggerCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				OntologyBaseFormTag baseFormTagger = null;
				final int selectedIndex = Application.this.baseFormTaggerCombo.getSelectionIndex();
				String baseFormTaggerString = Application.this.baseFormTaggerCombo.getItem(selectedIndex);
				Application.this.log.append("\n\nSetting base form tagger " + baseFormTaggerString + "...");
				try {
					switch (selectedIndex) {
						case 0:
							// do nothing, baseFormTagger must be set to null
							break;
						
						case 1:
							baseFormTagger = new WordNetOntology(Application.WORDNET_SERVER_ADDRESS,
									Application.WORDNET_SERVER_PORT);
							break;
						
						default:
							// do nothing
					}
				}
				catch (final NotConnectedException ex) {
					Application.this.handleException(ex);
					baseFormTaggerString = "not";
				}
				Application.this.specImprover.setBaseFormTagger(baseFormTagger);
				Application.this.checkButtons();
				Application.this.log.append("\n\nBase form tagger " + baseFormTaggerString + " set.");
			}
			
		});
	}
	
	/**
	 * Runs the application (and exits it afterwards or on error).
	 */
	private void run() {
		this.centerShell(this.shell, this.display.getPrimaryMonitor().getClientArea());
		this.shell.open();
		try {
			// Jonny: establish SSH connection
			SshConnector.initializeRecaaConnections();
			// show interface
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch()) {
					this.display.sleep();
				}
			}
			// Jonny: close connections
			SshConnector.disconnect();
			this.display.dispose();
			System.exit(0);
		}
		catch (final Throwable e) {
			System.out.println("Error or Exception:");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public DeterminerInfo selectDeterminer(final Word word, final DeterminerInfo graphDeterminer,
			final DeterminerInfo ontologyDeterminer, final String ontologyName) {
		this.tempDialogReturnValue = null;
		new DeterminerSelector(this.shell, word, graphDeterminer, ontologyDeterminer, ontologyName,
				new DeterminerSelectorListener() {
					
					@Override
					public void onEditFinish(final DeterminerInfo determinerInfo) {
						Application.this.tempDialogReturnValue = determinerInfo;
					}
					
				});
		return (DeterminerInfo) this.tempDialogReturnValue;
	}
	
	/**
	 * Opens a dialog for selecting the correct VerbFrame from a List of VerbFrames. Returns the selected VerbFrame or
	 * null on cancel.
	 * 
	 * @param verb Verb the frame shall be selected for.
	 * @param verbFrames VerbFrames to select from.
	 * @param suggestionVerbFrames VerbFrames that contain suggestions for the selection.
	 * @param ontologyName Name of the ontology the VerbFrames correspond to.
	 * @return Selected VerbFrame or null.
	 */
	public VerbFrame selectVerbFrame(final Word verb, final java.util.List<VerbFrame> verbFrames,
			final java.util.List<VerbFrame> suggestionVerbFrames, final String ontologyName) {
		this.tempDialogReturnValue = null;
		new VerbFrameSelector(this.shell, verb, verbFrames, suggestionVerbFrames, ontologyName,
				new VerbFrameSelectorListener() {
					
					@Override
					public void onSelect(final VerbFrame verbFrame) {
						Application.this.tempDialogReturnValue = verbFrame;
					}
					
				});
		return (VerbFrame) this.tempDialogReturnValue;
	}
	
	/**
	 * Opens a dialog for selecting one of the given senses for a Word in the given ontology. The returned boolean
	 * indicates if the selected sense shall be used for all occurrences of the word. The returned sense is the selected
	 * sense (NO_SENSE_FOUND-Sense when there was no sense to select from or the user found no match) on pressing OK and
	 * null on pressing cancel or closing the dialog.
	 * 
	 * @param word Word to select the sense for.
	 * @param senses Possible senses.
	 * @param ontologyName Name of the ontology the senses derive from.
	 * @return Pair of bool and selected sense (or null).
	 */
	@SuppressWarnings("unchecked")
	public Pair<Boolean, Sense> selectWordSense(final Word word, final Sense[] senses, final String ontologyName) {
		this.tempDialogReturnValue = null;
		
		new WordSenseSelector(this.shell, word, senses, ontologyName, new WordSenseSelectorListener() {
			
			@Override
			public void onSelect(final Sense sense, final boolean useForAllOccurrences) {
				Application.this.tempDialogReturnValue = new Pair<Boolean, Sense>(useForAllOccurrences, sense);
			}
			
		});
		return (Pair<Boolean, Sense>) this.tempDialogReturnValue;
		
	}
	
	/**
	 * Opens a dialog for informing the user that he probably used a nominalization and informing him about process
	 * words that could be used instead. Returns a Mark if the given nominalization shall be marked or null if there
	 * should be no Mark.
	 * 
	 * @param word Word the possible process word shall be shown for.
	 * @param possibleProcessWords Process words that could be used instead.
	 * @param currentMark Current Mark for this nominalization.
	 * @return Mark to set or null for no mark.
	 */
	public Mark showNominalization(final Word word, final java.util.List<String> possibleProcessWords,
			final Mark currentMark) {
		this.tempDialogReturnValue = null;
		new NominalizationMarker(this.shell, word, possibleProcessWords, currentMark,
				new NominalizationMarkerListener() {
					
					@Override
					public void onEditFinish(final Mark mark) {
						Application.this.tempDialogReturnValue = mark;
					}
					
				});
		return (Mark) this.tempDialogReturnValue;
	}
	
	/**
	 * Updates the shown specification with the current data.
	 * 
	 * @throws SyntaxException
	 */
	public void updateSpecificationView() throws SyntaxException {
		this.specificationViewer.update(this.specImprover.getCurrentSpecification());
	}

	/**
	 * Asks the user if one noun shall be replaced by the other noun.
	 * 
	 * @param oldNoun Old noun that could be replaced.
	 * @param newNoun New noun that will replace the old noun if desired.
	 * @param ontologyConfidence Confidence of the that the raplacement is appropriate (on a scale from 0 to 1).
	 * @param ontologyName Name of the ontology that suggests the replacement.
	 * @return User decision: 1. Should the noun be replaced? 2. Should this selection be used for all occurrences of the noun?
	 */
	@SuppressWarnings("unchecked")
	public Pair<Boolean, Boolean> checkReplaceNoun(Word oldNoun, Word newNoun, float ontologyConfidence, String ontologyName) {
		this.tempDialogReturnValue = null;
		String title = "Replace Noun?";
		String question = "Shall '" + oldNoun.getPlainWord() + "' be replaced with '" + newNoun.getBaseFormIfPresent() + "'? " + ontologyName  + " suggests this replacement with a confidence of " + Math.round(ontologyConfidence*1000)/10 + "%.";
		new YesNoSelector(this.shell, title, oldNoun, question,
				new YesNoSelectorListener() {
					
					@Override
					public void onSelect(final boolean yes, final boolean useForAllOccurrences) {
						Application.this.tempDialogReturnValue = new Pair<Boolean, Boolean>(yes, useForAllOccurrences);
					}
					
				});
		return (Pair<Boolean, Boolean>) this.tempDialogReturnValue;
	}
	
}
