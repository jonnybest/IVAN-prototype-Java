package edu.kit.ipd.alicenlp.ivan;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.StyleContext;

import opennlp.tools.util.StringUtil;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXBusyLabel;
import org.languagetool.rules.RuleMatch;

import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.components.IvanDiscourseModelPrinter;
import edu.kit.ipd.alicenlp.ivan.components.IvanErrorsTaskPaneContainer;
import edu.kit.ipd.alicenlp.ivan.data.CodePoint;
import edu.kit.ipd.alicenlp.ivan.data.DiscourseModel;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.SentenceClassificationAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorType;
import edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

/**
 * This is the main class if IVAN. It creates the user interface and manages all
 * the other components.
 * 
 * @author Jonny
 * 
 */
public class SwingWindow {

	@SuppressWarnings("serial")
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	private static final String ERROR_MISSING_A_DIRECTION = "These entities are missing a direction. Where or what do they face?";
	private static final String ERROR_ENTITIES_ARE_MISSING_A_LOCATION = "These entities are missing a location. Where do they stand in the scene?";
	private static final String ERROR_STYLE = "This error means that something in the text does not fit well.";
	private static final String ERROR_WORD_IS_MISSING_IN_OUR_DICTIONARY = "This error means that the analyzer tried to process this sentence and could not proceed, because a word is missing in our dictionary. It would be best, if you could come up with a different word here.";
	private static final String ERROR_ENTITIES_SHARE_A_SYNONYM = "This error means that two distinct entities share a synonym. Try giving names to the characters and things to resolve this issue.";
	private static final String ERROR_UNUSUAL_STRUCTURE = "IVAN could not properly analyze this sentence, because of it's unusual structure. Maybe try a shorter sentence instead?";

	private static final String ERROR_NAME_COULD_NOT_BE_RESOLVED_TO_AN_ENTITY = "This error means that a pronoun (or maybe a name) could not be resolved to an entity.";
	protected static final String PROPERTIES_ANNOTATORS = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, declarations, sdclassifier";
	protected static final String DEFAULT_TEXT = ""; 
//	= "The ground is covered with grass, the sky is blue. \n"
//			+ "In the background on the left hand side there is a PalmTree. \n"
//			+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. \n"
//			+ "Right to the mailbox there is a Frog facing east. \n"
//			+ "In front of the Bunny there is a Broccoli. \n"
//			+ "In the foreground on the right hand side there is a Bunny facing southwest. \n"
//			+ "The Bunny turns to face the Broccoli. \n"
//			+ "The Bunny hops three times to the Broccoli. \n"
//			+ "The Bunny eats the Broccoli. \n"
//			+ "The Bunny turns to face the Frog. \n"
//			+ "The Bunny taps his foot twice. \n"
//			+ "The Frog ribbits. The Frog turns to face northeast. \n"
//			+ "The frog hops three times to northeast. \n"
//			+ "The Bunny turns to face the Mailbox. \n"
//			+ "The Bunny hops three times to the Mailbox. \n"
//			+ "The Bunny opens the Mailbox. \n"
//			+ "The Bunny looks in the Mailbox and at the same time the Frog turns to face the Bunny. \n"
//			+ "The Frog hops two times to the Bunny. \n"
//			+ "The Frog disappears. A short time passes.";

	private static final String DOCUMENT_TXT = "document.txt";
	private static final String PANEL_TXT = "panel.txt";
	private static Logger log = Logger.getLogger(SwingWindow.class.getName());
	private static SwingWindow instance;

	private static void checkout(String file2ref) {
		GitManager.checkout(file2ref, null);
	}

	/**
	 * Makes sure that any given name is converted into a valid ref name. git
	 * imposes the following rules on how references are named:
	 * 
	 * 1. They can include slash / for hierarchical (directory) grouping, but no
	 * slash-separated component can begin with a dot ..
	 * 
	 * 2. They must contain at least one /. This enforces the presence of a
	 * category like heads/, tags/ etc. but the actual names are not restricted.
	 * 
	 * 3. They cannot have two consecutive dots .. anywhere.
	 * 
	 * 4. They cannot have ASCII control characters (i.e. bytes whose values are
	 * lower than \040, or \177 DEL), space, tilde ~, caret ^, colon :,
	 * question-mark ?, asterisk *, or open bracket [ anywhere.
	 * 
	 * 5. They cannot end with a slash / nor a dot ..
	 * 
	 * 6. They cannot end with the sequence .lock.
	 * 
	 * 7. They cannot contain a sequence @{.
	 * 
	 * 8. They cannot contain a \.
	 * 
	 * @param file
	 * @return
	 */

	public static String file2ref(String file) {
		String ofile = file, // "old file"
		nfile = file; // "new file"
		do {
			ofile = nfile; // "new" is the new "old"

			// 3. no double dots
			nfile = ofile.replace("..", ".");

		} while (!ofile.equals(nfile));

		// work with a char array
		char[] f = nfile.toCharArray();
		// 4. no low values
		for (int i = 0; i < f.length; i++) {
			if (f[i] < 41 || f[i] > 176) {
				f[i] = 'o';
			}
			// log.info(Arrays.toString(f));
		}

		// space, tilde ~, caret ^, colon :, question-mark ?, asterisk *, or
		// open bracket [ anywhere.
		nfile = Arrays.toString(f).replace("[", "").replace("]", "")
				// not a violation, but it's produced by Arrays.toString
				.replace(", ", "")
				// also a byproduct of the array print
				.replace(" ", "o").replace("^", "o").replace(":", "o")
				.replace("?", "o").replace("*", "o").replace(".lock", "ooooo") // 6.
																				// They
																				// cannot
																				// end
																				// with
																				// the
																				// sequence
																				// .lock.
				.replace("@{", "oo") // 7. They cannot contain a sequence @{.
		;
		return nfile;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            not currently used
	 * @throws IOException
	 * @throws SecurityException
	 */
	public static void main(String[] args) {
		try {
			Logger global = Logger.getLogger("");

			global.getHandlers();

			Handler fh = new FileHandler("ivan.log");
			fh.setFormatter(new java.util.logging.SimpleFormatter());
			fh.setLevel(Level.CONFIG);
			global.addHandler(fh);

		} catch (SecurityException | IOException e1) {
			e1.printStackTrace();
			System.err
					.println("Warning: Failed to initialize java.util.logging. Logging is disabled.");
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingWindow window = new SwingWindow();
					window.frmvanInput.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method invokes the pipeline from other components.
	 */
	public static void processText() {
		try {
			instance.processText(instance.txtEditor.getText());
		} catch (Exception e) {
			System.err
					.println("The caller tried to process this text and caused an exception.");
			e.printStackTrace();
		}
	}

	/**
	 * This method prepares the git repository and tracking files.
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	private static void setupTracking() throws IOException, GitAPIException {
		File dir = new File(GitManager.TRACKINGPATH);
		dir.mkdir();

		String documentpath = GitManager.TRACKINGPATH + DOCUMENT_TXT;
		File what = new File(documentpath);
		what.createNewFile();
		
		String panelpath = GitManager.TRACKINGPATH + PANEL_TXT;
		File pf = new File(panelpath);
		pf.createNewFile();

		GitManager.safeInit();
	}

	private static void tag(String tagname) {
		edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.tag(tagname);
	}

	private org.joda.time.DateTime stopwatch;
	private JFrame frmvanInput;
	/**
	 * editor panel
	 */
	private TextEditorPane txtEditor;
	/**
	 * errors panel
	 */
	private IvanErrorsTaskPaneContainer containerTaskPanel;
	StyleContext sc = new StyleContext();
	protected AttributeSet DefaultStyle;
	/**
	 * This is the text pane for writing user output.
	 */
	private JTextPane emitterTextPane;
	private JXBusyLabel busyLabel;
	private JButton btnSaveCheck;

	private Component horizontalGlue;

	private String currentFileName = null;

	private JMenuBar menuBar;

	protected boolean isSpellingOkay;

	private List<RuleMatch> spellingErrors = new ArrayList<>();

	private JScrollPane errorScrollPane;

	private JLabel coords;

	/**
	 * Create the application.
	 */
	public SwingWindow() {
		instance = this;

		initialize();
	}

	private void clearStyles() {
		txtEditor.getHighlighter().removeAllHighlights();
		log.info("SwingWindow.clearStyles()");
	}

	/**
	 * This method performs a commit to the local git repository
	 * 
	 * @throws IOException
	 */
	private void commit(String file) throws IOException {
		String branch = file2ref(file);
		String basepath = edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.TRACKINGPATH;
		FileWriter out = null;
		try {
			out = new FileWriter(basepath + DOCUMENT_TXT);
			out.write(txtEditor.getText());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
		edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.commit(branch);
	}

	/**
	 * A utility method for creating categories by mapping from an IvanErrorType
	 * 
	 * @param type
	 * @param description
	 * @return The category that was assigned for this error.
	 */
	public String createCategory(IvanErrorType type) {
		String defaultcategory = "misc";
		String category;
		String description = "";

		switch (type) {
		case COREFERENCE:
			category = IvanErrorsTaskPaneContainer.CATEGORY_AMBIGOUS;
			description = ERROR_NAME_COULD_NOT_BE_RESOLVED_TO_AN_ENTITY;
			break;
		case GRAPH:
			category = IvanErrorsTaskPaneContainer.CATEGORY_GRAMMAR;
			description = ERROR_UNUSUAL_STRUCTURE;
			break;
		case SYNONYMS:
			category = "names";
			description = ERROR_ENTITIES_SHARE_A_SYNONYM;
			break;
		case WORDNET:
			category = "dictionary";
			description = ERROR_WORD_IS_MISSING_IN_OUR_DICTIONARY;
		case STYLE:
			category = "style";
			description = ERROR_STYLE;
			break;
		case DIRECTION:
			category = IvanErrorsTaskPaneContainer.CATEGORY_DIRECTION;
			description = ERROR_MISSING_A_DIRECTION;
			break;
		case LOCATION:
			category = IvanErrorsTaskPaneContainer.CATEGORY_LOCATION;
			description = ERROR_ENTITIES_ARE_MISSING_A_LOCATION;
			break;
		default:
			category = defaultcategory;
			description = "Other errors:";
			break;
		}

		containerTaskPanel.createCategory(category, description);
		return category;
	}

	/**
	 * This method initializes the pipeline and the spell checker
	 * 
	 */
	private void delayedInit() {
		final SwingWorker<Object, Object> task = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				// delay
				Thread.sleep(500);
				// init spell checker
				IvanPipeline.prepare();
				return null;
			}
		};
		task.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("state".equals(evt.getPropertyName()) && task.isDone()) {
					busyLabel.setBusy(false);
					log.info("Pipeline is ready.");
				} else {
					busyLabel.setBusy(true);
				}
			}
		});

		task.execute();
		new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				// delay
				Thread.sleep(2000);
				// init spell checker
				IvanSpellchecker.prepare();
				return null;
			}
		}.execute();

		busyLabel.setBusy(true);
	}

	/**
	 * Do inital stuff, create a UI with a frame and all that
	 */
	@SuppressWarnings("serial")
	// This class is surely not getting serialized
	private void initialize() {
		frmvanInput = new JFrame();
		frmvanInput
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								SwingWindow.class
										.getResource("/edu/kit/ipd/alicenlp/ivan/resources/ivan2.png")));
		frmvanInput.setLocale(Locale.ENGLISH);
		frmvanInput.setTitle("¶van – Input & Verify AliceNLP");
		frmvanInput.setBounds(100, 100, 980, 670);
		frmvanInput.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel container = new JPanel(new GridBagLayout());
		frmvanInput.setContentPane(container);

		/**
		 * Initialize the contents of the frame, the EDITOR
		 */
		txtEditor = new TextEditorPane();
		// txtEditor.setDisplayLineNumbers(true);
		// txtEditor.setDocument(doc);

		txtEditor.setText(DEFAULT_TEXT);

		/**
		 * Here is where I build the EMITTER panel
		 */
		emitterTextPane = new JTextPane();
		emitterTextPane.setText("Hello World!");
		// emitterTextPane.setPreferredSize(new Dimension(10, 40));
		emitterTextPane.setEditable(false);
		JScrollPane emitterScrollPane = new JScrollPane(emitterTextPane);

		/*
		 * Here is where I build the MENU
		 */
		menuBar = new JMenuBar();
		JMenu filemenu = new JMenu("File ");

		/** Allows the user to LOAD a document into the editor */
		final Action actionLoad = new SwingAction() {
			/**
			 * Loads a document with a jfilechooser dialog
			 */
			public void actionPerformed(ActionEvent e) {
				JFileChooser loadChooser = new JFileChooser();
				loadChooser.setFileFilter(new FileNameExtensionFilter(
						"Text file", "txt"));
				File file = null;
				int showOpenDialog = loadChooser.showOpenDialog(txtEditor);
				switch (showOpenDialog) {
				case JFileChooser.APPROVE_OPTION:
					currentFileName = loadChooser.getSelectedFile()
							.getAbsolutePath();
					file = loadChooser.getSelectedFile();
					break;
				default: // nothing to do
					return;
				}
				load(txtEditor, file);
			}
		};
		actionLoad.putValue(Action.NAME, "Load…"); // set the name
		actionLoad
				.putValue(Action.SHORT_DESCRIPTION, "Open a file for editing");

		filemenu.add(actionLoad);
		menuBar.add(filemenu);

		// this is an action for saving the file (no checking)
		final Action saveAction = new SwingAction() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					save(txtEditor);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		saveAction.putValue(Action.NAME, "Save"); // set the name
		saveAction.putValue(Action.SHORT_DESCRIPTION, "Saves the file");

		filemenu.add(saveAction);

		// this is an action for SAVE AS...
		final Action saveAsAction = new SwingAction() {
			public void actionPerformed(ActionEvent arg0) {
				// remember old document name for later
				String tmpfilename = currentFileName;
				// delete the current document name so the save file dialog will
				// pop up
				currentFileName = null;
				// save things
				boolean saved = false;
				try {
					saved = save(txtEditor);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// if the save was not successful, restore the old document name
				if (!saved) {
					currentFileName = tmpfilename;
				}
			}
		};
		saveAsAction.putValue(Action.NAME, "Save as…"); // set the name
		saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Saves to a new file");

		filemenu.add(saveAsAction);

		// this is an action for EXIT...
		final Action exitAction = new SwingAction() {
			public void actionPerformed(ActionEvent arg0) {
				Application.getInstance().exit();
			}
		};
		exitAction.putValue(Action.NAME, "Exit"); // set the name
		exitAction.putValue(Action.SHORT_DESCRIPTION, "Closes the program");

		filemenu.add(exitAction);

		// create an EDIT menu with UNDO, CUT, PASTE and the like
		JMenu editMenu = new JMenu("Edit ");

		final Action undoAction = /* undoAction */new SwingAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// txtEditor.doCommand("undo", this);
				txtEditor.undoLastAction();
			}
		};
		undoAction.putValue(Action.NAME, "Undo (Ctrl-Z)");
		undoAction.putValue(Action.SHORT_DESCRIPTION,
				"Reverts your last action");
		editMenu.add(undoAction);

		final Action redoAction = /* redoAction */new SwingAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// txtEditor.doCommand("redo", this);
				txtEditor.redoLastAction();
			}
		};
		redoAction.putValue(Action.NAME, "Redo (Ctrl-Y)");
		redoAction.putValue(Action.SHORT_DESCRIPTION, "Restores the last undo");
		editMenu.add(redoAction);

		Action copy = txtEditor.getActionMap().get("copy");
		Action paste = txtEditor.getActionMap().get("paste");
		Action cut = txtEditor.getActionMap().get("cut");
		editMenu.add(copy); // FIXME: not enabled in menu :(
		editMenu.add(cut); // FIXME: not enabled in menu :(
		editMenu.add(paste);

		// put the edit menu on the menu bar
		menuBar.add(editMenu);

		// this button triggers a run of the analyzers
		btnSaveCheck = new JButton("Save and Check (Ctrl-S)");
		menuBar.add(btnSaveCheck);

		final Action saveCheckAction = new SwingAction() {
			public void actionPerformed(ActionEvent arg0) {
				boolean saved = false;
				try {
					saved = save(txtEditor);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (!saved) {
					return;
				}
				try {
					startStopWatch();
					processText(txtEditor.getText());
					stopAndPrintStopWatch();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		saveCheckAction.putValue(Action.NAME, "Save and check"); // set the name
		saveCheckAction.putValue(Action.SHORT_DESCRIPTION,
				"Saves the file and runs analysis");

		// setup up the CTRL-S hotkey for running save-and-check from within the
		// editor area
		InputMap map = txtEditor
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		map.put(KeyStroke.getKeyStroke("control S"), saveCheckAction);

		btnSaveCheck.addActionListener(saveCheckAction);

		// set up a caretlister to update coordinates
		txtEditor.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if (e.getDot() == e.getMark()) {
					coords.setText(String.format("[%d]", e.getMark()));
				} else {
					coords.setText(String.format("[%d,%d]", e.getMark(),
							e.getDot()));
				}
			}
		});

		// this glue pushes the spinner to the right
		horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		coords = new JLabel("[…]");
		menuBar.add(coords);
		menuBar.add(Box.createHorizontalStrut(6));

		// this spinner tells the user that analysis is currently running
		busyLabel = new JXBusyLabel();
		menuBar.add(busyLabel);
		busyLabel.setHorizontalAlignment(SwingConstants.TRAILING);

		/*
		 * Here is where I build the TASK panel
		 */
		// creating the actual taskpanel
		containerTaskPanel = new IvanErrorsTaskPaneContainer(txtEditor);

		containerTaskPanel.createCategory("meta", null);
		containerTaskPanel.createProblem("meta", null, 0, 0);

		// the emitter and the TaskPane have something to work on, so set up the
		// linguistics stuff
		setupFeedback();

		// prepare git tracking
		try {
			setupTracking();
		} catch (IOException | GitAPIException e1) {
			e1.printStackTrace();
		}

		delayedInit();

		/**
		 * LAYOUTS
		 */
		final Container contentPane = frmvanInput.getContentPane();

		// the menu bar is on top, stretches all the way right
		GridBagConstraints menuBarLayout = new GridBagConstraints();
		// top left
		menuBarLayout.anchor = GridBagConstraints.FIRST_LINE_START; // push to
																	// the top
																	// left
		menuBarLayout.gridx = 0;
		menuBarLayout.gridy = 0;
		// manage width
		menuBarLayout.gridwidth = 2; // stretch
		menuBarLayout.fill = GridBagConstraints.HORIZONTAL; // fill all x-space
		menuBarLayout.weightx = 0.5; // a weight > 0 allows for resizing
		// add
		contentPane.add(menuBar, menuBarLayout);

		// the text field is under the menu on the left and shares a row with
		// the errors panel/scroll pane
		GridBagConstraints textfieldLayout = new GridBagConstraints();
		// middle left
		textfieldLayout.anchor = GridBagConstraints.FIRST_LINE_START; // push to
																		// the
																		// top
																		// left
		textfieldLayout.gridx = 0; // col left
		textfieldLayout.gridy = 1; // row center
		// manage stretching
		textfieldLayout.weightx = 0.5; // resizable
		textfieldLayout.weighty = 0.5; // resizable
		textfieldLayout.fill = GridBagConstraints.BOTH;
		// add
		contentPane.add(txtEditor, textfieldLayout);

		errorScrollPane = new JScrollPane(containerTaskPanel);
		// prevent indefinite shrinking
		errorScrollPane.setMinimumSize(new Dimension(222, 127));
		errorScrollPane.setMaximumSize(new Dimension(222, 3000));

		GridBagConstraints scrollpanelLayout = new GridBagConstraints();
		// middle, right
		scrollpanelLayout.anchor = GridBagConstraints.FIRST_LINE_END;
		scrollpanelLayout.gridx = 1; // right row
		scrollpanelLayout.gridy = 1; // center column
		// stretch to fill
		scrollpanelLayout.weightx = 0.5; // resizable
		scrollpanelLayout.weighty = 0.5; // resizable
		scrollpanelLayout.fill = GridBagConstraints.BOTH;
		// add our controls to the visible world
		contentPane.add(errorScrollPane, scrollpanelLayout);

		// the emitter panel has the bottom row to itself
		GridBagConstraints emitterpanelLayout = new GridBagConstraints();
		// bottom left
		emitterpanelLayout.anchor = GridBagConstraints.LAST_LINE_START;
		emitterpanelLayout.gridx = 0; // col left
		emitterpanelLayout.gridy = 2; // row bottom
		// stretch width
		emitterpanelLayout.weightx = 0.5;
		emitterpanelLayout.gridwidth = 2;
		// manage height:
		emitterpanelLayout.weighty = 0.1;
		emitterpanelLayout.fill = GridBagConstraints.BOTH;
		// add
		contentPane.add(emitterScrollPane, emitterpanelLayout);

	}

	/**
	 * Loads a page into the editor
	 * 
	 * @param editor
	 * @param file
	 */
	public void load(TextEditorPane editor, File file) {
		try {
			editor.load(FileLocation.create(file), "utf-8");
			checkout(file2ref(file.getName()));
			tag("load." + file2ref(file.getName()));
			this.currentFileName = file.getPath();
		} catch (IOException e) {
			// catch block in case load fails
			e.printStackTrace();
			log.info("SwingWindow.load()");
		}
	}

	private void markIvanError(int beginPosition, int endPosition)
			throws BadLocationException {
		// create a painter for lines
		SquiggleUnderlineHighlightPainter sqpainter = new SquiggleUnderlineHighlightPainter(
				Color.RED);
		// paint the highlights
		Object tag = txtEditor.getHighlighter().addHighlight(beginPosition, endPosition,
				sqpainter);
		log.info(tag.toString());
	}

	void markSpelling() {
		for (RuleMatch match : spellingErrors) {
			log.info("Potential error at line " + match.getLine() + ", column "
					+ match.getColumn() + ": " + match.getMessage());
			log.info("Rule: " + match.getRule().getId());

			try {
				markSpellingError(match.getFromPos(), match.getToPos());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}

	private void markSpellingError(int beginPosition, int endPosition)
			throws BadLocationException {
		// create a painter for lines
		SquiggleUnderlineHighlightPainter sqpainter = new SquiggleUnderlineHighlightPainter(
				Color.GREEN);
		// paint the highlights
		txtEditor.getHighlighter().addHighlight(beginPosition, endPosition,
				sqpainter);
	}

	private void markText(int beginPosition, int endPosition, Color color) {

		DefaultHighlightPainter sqpainter = new DefaultHighlightPainter(color);
		try {
			txtEditor.getHighlighter().addHighlight(beginPosition, endPosition,
					sqpainter);
		} catch (BadLocationException e) {
			e.printStackTrace();
			log.info("SwingWindow.markText()");
		}
	}

	/**
	 * Runs a text analysis and manages user visible feedback
	 * 
	 * @param text
	 * @throws Exception
	 */
	private void processText(String text) {

		final IvanSpellchecker spellchecker = new IvanSpellchecker(text);
		
		final IvanPipeline task = new IvanPipeline(text);
		task.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("state".equals(evt.getPropertyName()) && task.isDone()) {
					log.info("done");

					Annotation doc;
					try {
						doc = task.get();
					} catch (InterruptedException | ExecutionException e1) {
						log.warning(e1.toString());
						e1.printStackTrace();
						return;
					}

					try {
						emitterTextPane.setText(""); // remove old tells
//						DiscourseModel entities = doc.get(IvanAnnotations.IvanEntitiesAnnotation.class);
//						tell(new IvanDiscourseModelPrinter(entities).toString());
						updateDocumentMarkers(doc);
						updateSentenceMarkers(doc);
						tracePanel();
						commit(currentFileName != null ? currentFileName : "");
					} catch (IvanException e) {
						log.warning(e.toString());
						e.printStackTrace();
					} catch (BadLocationException e) {
						log.warning(e.toString());
						e.printStackTrace();
					}
					/**
					 * Print state to emitter panel
					 */
					// retrieve recognition results
					// DiscourseModel entitiesState =
					// doc.get(IvanEntitiesAnnotation.class);
					// RecognitionStatePrinter emitterwriter = new
					// RecognitionStatePrinter(entitiesState);
					// tell(emitterwriter.toString());
 catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					spellchecker.execute();
					
				}
			}
		});

		task.execute();
		// this.busyLabel.setVisible(true);
		this.busyLabel.setBusy(true);

		// prepare the text with our pipeline
		// Annotation doc = task.get();

		
		spellchecker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("state".equals(evt.getPropertyName()) && task.isDone()) {
					try {
						spellingErrors = spellchecker.get();
						markSpelling();
						busyLabel.setBusy(false);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	protected void tracePanel() throws IOException {
		
		OpenOption[] oo = {java.nio.file.StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE};
		log.info(containerTaskPanel.toString());
		Files.write(Paths.get("tracking/panel.txt"), this.containerTaskPanel.toString().getBytes(StandardCharsets.UTF_8), oo);
	}

	/**
	 * Reloads the page, even if it is already being displayed
	 * 
	 * @param editor
	 *            destination container
	 * @param file
	 *            file to load
	 */
	public void reload(TextEditorPane editor, File file) {
		Document doc = editor.getDocument();
		doc.putProperty(Document.StreamDescriptionProperty, null);
		load(editor, file);
	}

	/**
	 * Saves the current document
	 * 
	 * @param editor
	 *            the component which contains the text
	 * @return
	 * @throws IOException
	 */
	protected boolean save(TextEditorPane editor) throws IOException {
		File outputfile = null;
		if (this.currentFileName == null) {
			JFileChooser jfchooser = new JFileChooser();
			jfchooser.setFileFilter(new FileNameExtensionFilter("Text file",
					"txt"));
			int file = jfchooser.showSaveDialog(editor);
			switch (file) {
			case JFileChooser.APPROVE_OPTION:
				outputfile = jfchooser.getSelectedFile();
				this.currentFileName = outputfile.getAbsolutePath();
				break;

			default: // I can't do anything with the other options
				return false;
			}
		} else {
			outputfile = new File(currentFileName);
		}
		FileWriter out = null;
		try {
			out = new FileWriter(currentFileName);
			out.write(editor.getText());
		} catch (IOException e) {
			// I have no clue what to do here
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				// okay, even close() throws? That's messed up.
			}
		}
		
		return true;
	}

	/**
	 * Prepares the problems list for the task pane. If I decide to do inital
	 * stuff with the taskpane and the emitter, that code goes here as well.
	 */
	private void setupFeedback() {
		// tell the errors panel where our editor is so it can apply quick fixes
		this.containerTaskPanel.setEditor(this.txtEditor);
	}

	private void startStopWatch() {
		stopwatch = org.joda.time.DateTime.now();
	}

	private void stopAndPrintStopWatch() {
		org.joda.time.DateTime now = org.joda.time.DateTime.now();
		long diff = now.getMillis() - stopwatch.getMillis();
		System.err.println("Stopwatch: " + (diff) + " ms.");
	}

	private void tell(String output) {
		String seperator = this.emitterTextPane.getText().length() > 0 ? "\n" : "";
		this.emitterTextPane.setText(this.emitterTextPane.getText() + seperator + output);
	}

	/**
	 * @param doc
	 * @throws IvanException
	 * @throws BadLocationException
	 */
	public void updateDocumentMarkers(Annotation doc) throws IvanException,
			BadLocationException {

		// fetch errors
		List<IvanErrorMessage> errors = doc
				.get(IvanAnnotations.DocumentErrorAnnotation.class);
		// if errors exist in the document, display them
		if (errors != null) {
			// process document-wide errors
			for (IvanErrorMessage documenterror : errors) {
				tell(documenterror.toString());
				String category = createCategory(documenterror.getType());
				boolean showError = this.containerTaskPanel.createProblem(category, documenterror,
						null);
				if(showError){
					int length = documenterror.getSpan().end() - documenterror.getSpan().start();
					log.info("Showing error for this word: "+ txtEditor.getText(documenterror.getSpan().start(), length));
					markIvanError(documenterror.getSpan().start(), documenterror.getSpan().end());
				}
			}

			// clear leftover errors from last run which may have been fixed by
			// now
			this.containerTaskPanel.purge();
		}
		if (errors != null)
			log.log(Level.INFO, "Document wide errors: " + errors);
	}

	/**
	 * @param doc
	 * @throws BadLocationException
	 */
	public void updateSentenceMarkers(Annotation doc)
			throws BadLocationException {
		// clear all previous markers
		clearStyles();

		// retrieve the sentences
		List<CoreMap> listsentences = doc.get(SentencesAnnotation.class);

		for (CoreMap sentence : listsentences) {
			// traversing the words in the current sentences
			SemanticGraph depgraph = sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);
			if (depgraph.getRoots().isEmpty()) {
				continue;
			}
			/***
			 * Requirement 2: Classify sentence into Setup descriptions and
			 * non-setup descriptions
			 */
			StaticDynamicClassifier.Classification sentencetype = sentence
					.get(SentenceClassificationAnnotation.class);

			// get root for coloring
			IndexedWord root = depgraph.getFirstRoot();
			if (depgraph.isEmpty()) {
				continue;
			}
			// retrieve the error message
			IvanErrorMessage err = sentence
					.get(IvanAnnotations.ErrorMessageAnnotation.class);
			// if any error is present, show that instead of the usual cues
			if (err != null) {
				// make sure we show the error message
				sentencetype = Classification.ErrorDescription;
			}
			// color the sentence according to classification
			switch (sentencetype) {
			case SetupDescription:
				// tell(depgraph.toString());
				markText(root.beginPosition(), root.endPosition(), new Color(
						0xB3C4FF));
				// DeclarationPositionFinder.DeclarationQuadruple decl =
				// mydeclarationfinder.findAll(root, sentence);
				break;
			case ErrorDescription:
				log.info("Error in text found: " + err + "; sentence: "
						+ sentence.toString());
				if (err == null)
					break;
				// create the error category in the panel on the right hand side
				String category = createCategory(err.getType());
				// create an error message inside the panel on the right hand
				// side
				boolean showError = this.containerTaskPanel
						.createProblem(
								category,
								err,
								new CodePoint(
										sentence.get(CharacterOffsetBeginAnnotation.class),
										sentence.get(CharacterOffsetEndAnnotation.class)));
				// highlight the text at the error's location
				if(showError){
					tell(err.toString());
					markIvanError(err.getSpan().start(), err.getSpan().end());
				}
				break;
			case EventDescription:
				markText(root.beginPosition(), root.endPosition(), new Color(
						0xBF4889));
				break;
			case TimeDescription:
				markText(root.beginPosition(), root.endPosition(), new Color(
						0x7E17ED));
				break;
			case ActionDescription:
				markText(root.beginPosition(), root.endPosition(), new Color(
						0xFFC4B3));
				break;
			default:
				break;
			}
		}
	}
}
