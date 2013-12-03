package edu.kit.ipd.alicenlp.ivan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.StyleContext;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXBusyLabel;
import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder;
import edu.kit.ipd.alicenlp.ivan.analyzers.IvanAnalyzer.Classification;
import edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier;
import edu.kit.ipd.alicenlp.ivan.components.IvanErrorsTaskPaneContainer;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations;
import edu.kit.ipd.alicenlp.ivan.data.IvanAnnotations.IvanEntitiesAnnotation;
import edu.kit.ipd.alicenlp.ivan.data.IvanErrorMessage;
import edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

/** This is the main class if IVAN. It creates the user interface and manages all the other components. 
 * 
 * @author Jonny
 *
 */
public class SwingWindow {

	private static final String DOCUMENT_TXT = "document.txt";
	private static SwingWindow instance;
	private org.joda.time.DateTime stopwatch;
	private JFrame frmvanInput;
	/**
	 *  editor panel
	 */
	private TextEditorPane txtEditor;
	/**
	 *  errors panel
	 */
	private IvanErrorsTaskPaneContainer containerTaskPanel;

	StyleContext sc = new StyleContext();
	protected AttributeSet DefaultStyle;
	/**
	 * This is the text pane for writing user output.
	 */
	private JTextPane emitterTextPane;
	
	/** This set contains problematic entity information regarding location.
	 * 
	 */
	private Set<EntityInfo> problemSetMissingLocation;
	/** This set contains entity information which is missing a direction
	 * 
	 */
	private Set<EntityInfo> problemSetMissingDirection;
	private JXBusyLabel busyLabel;
	private JButton btnSaveCheck;
	private Component horizontalGlue;
	private String currentFileName = null;
	private JMenuBar menuBar;
	private StanfordCoreNLP mypipeline;
	
	/**
	 * This is the central pipeline which classifies text. This should never be directly accessed. Use getPipeline() instead.
	 */
	private static StanfordCoreNLP stanfordCentralPipeline;

	/**
	 * Launch the application.
	 * @param args not currently used
	 */
	public static void main(String[] args) {
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
	 * Create the application.
	 */
	public SwingWindow() {
		instance = this;

		initialize();
	}

	/**
	 * Do inital stuff, create a UI with a frame and all that
	 */
	@SuppressWarnings("serial") // This class is surely not getting serialized
	private void initialize() {
		frmvanInput = new JFrame();
		frmvanInput.setLocale(Locale.ENGLISH);
		frmvanInput.setTitle("¶van – Input & Verify AliceNLP");
		frmvanInput.setBounds(100, 100, 612, 511);
		frmvanInput.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		

		/**
		 * Initialize the contents of the frame, the EDITOR
		 */
		txtEditor = new TextEditorPane();
//		txtEditor.setDisplayLineNumbers(true);
		// txtEditor.setDocument(doc);

		txtEditor.setText("The ground is covered with grass, the sky is blue. \n"
				+ "In the background on the left hand side there is a PalmTree. \n"
				+ "In the foreground on the left hand side there is a closed Mailbox facing southeast. \n"
				+ "Right to the mailbox there is a Frog facing east. In the foreground on the right hand side there is a Bunny facing southwest. \n"
				+ "In front of the Bunny there is a Broccoli. \n"
				+ "The Bunny turns to face the Broccoli. \n"
				+ "The Bunny hops three times to the Broccoli. \n"
				+ "The Bunny eats the Broccoli. \n"
				+ "The Bunny turns to face the Frog. \n"
				+ "The Bunny taps his foot twice. \n"
				+ "The Frog ribbits. The Frog turns to face northeast. \n"
				+ "The frog hops three times to northeast. \n"
				+ "The Bunny turns to face the Mailbox. \n"
				+ "The Bunny hops three times to the Mailbox. \n"
				+ "The Bunny opens the Mailbox. \n"
				+ "The Bunny looks in the Mailbox and at the same time the Frog turns to face the Bunny. \n"
				+ "The Frog hops two times to the Bunny. \n"
				+ "The Frog ribbits.");

		frmvanInput.getContentPane().add(txtEditor, BorderLayout.CENTER);

		txtEditor.addKeyListener(new KeyAdapter() {
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
			
			private boolean canCheckSpelling(KeyEvent event) {
				return false;
//				if(limit  == 0)
//				{
//					limit  = 8;
//					return true;
//				}
//				else {
//					limit --;
//					return false;
//				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {		
				if (canCheckSpelling(arg0)) {
					try {
						//				List<RuleMatch> matches = langTool.check("A sentence " +
						//				    "with a error in the Hitchhiker's Guide tot he Galaxy");
						List<RuleMatch> matches = getLanguageTool().check(txtEditor
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
						e.printStackTrace();
					}
				}
				if (arg0.getKeyChar() == '.' || arg0.getKeyChar() == '\n') {
					
					try {
						startStopWatch();
						processText(txtEditor.getText());
						stopAndPrintStopWatch();
					} catch (Exception e) {
						e.printStackTrace();
						System.out
								.println("SwingWindow.initialize().new KeyAdapter() {...}.keyTyped()");
					}
				}
			}
		});
		/** END of the editor part */
		
		/**
		 * Here is where I build the EMITTER panel
		 */
		emitterTextPane = new JTextPane();
		emitterTextPane.setText("Hello World!");
		//emitterTextPane.setPreferredSize(new Dimension(10, 40));
		emitterTextPane.setEditable(false);
		JScrollPane emitterScrollPane = new JScrollPane(emitterTextPane);		
		frmvanInput.getContentPane().add(emitterScrollPane, BorderLayout.SOUTH);
		
		/*
		 * Here is where I build the MENU
		 */
		menuBar = new JMenuBar();
		JMenu filemenu = new JMenu("File ");
		
		/** Allows the user to LOAD a document into the editor */
		final Action actionLoad = new SwingAction()
		{			
			/**
			 * Loads a document with a jfilechooser dialog
			 */
			public void actionPerformed(ActionEvent e) {
				JFileChooser loadChooser = new JFileChooser();
				loadChooser.setFileFilter(new FileNameExtensionFilter("Text file", "txt"));
				File file = null;
				int showOpenDialog = loadChooser.showOpenDialog(txtEditor);				
				switch (showOpenDialog) {
				case JFileChooser.APPROVE_OPTION:
					currentFileName = loadChooser.getSelectedFile().getAbsolutePath();
					file = loadChooser.getSelectedFile();
					break;
				default: // nothing to do
					return;
				}				
				load(txtEditor, file);
			}
		};
		actionLoad.putValue(Action.NAME, "Load…"); // set the name
		actionLoad.putValue(Action.SHORT_DESCRIPTION, "Open a file for editing");
		
		filemenu.add(actionLoad);
		menuBar.add(filemenu);
		
		
		frmvanInput.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		
		// this is an action for saving the file (no checking)
		final Action saveAction = new SwingAction() {
			public void actionPerformed(ActionEvent arg0) {
				save(txtEditor);								
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
				// delete the current document name so the save file dialog will pop up
				currentFileName = null;
				// save things
				boolean saved = save(txtEditor);
				// if the save was not successful, restore the old document name
				if(!saved){
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
		
		final Action undoAction = /*undoAction*/ new SwingAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//txtEditor.doCommand("undo", this);
				txtEditor.undoLastAction();
			}
		};
		undoAction.putValue(Action.NAME, "Undo (Ctrl-Z)");
		undoAction.putValue(Action.SHORT_DESCRIPTION, "Reverts your last action");
		editMenu.add(undoAction);
		
		final Action redoAction = /*redoAction*/ new SwingAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//txtEditor.doCommand("redo", this);
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
				boolean saved = save(txtEditor);
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
		saveCheckAction.putValue(Action.SHORT_DESCRIPTION, "Saves the file and runs analysis");
		
		// setup up the CTRL-S hotkey for running save-and-check from within the editor area
		InputMap map = txtEditor.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		map.put(KeyStroke.getKeyStroke("control S"), saveCheckAction);
		
		btnSaveCheck.addActionListener(saveCheckAction);
		
		
		// this glue pushes the spinner to the right
		horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);
		
		// this spinner tells the user that analysis is currently running
		busyLabel = new JXBusyLabel();
		menuBar.add(busyLabel);
		busyLabel.setVisible(false);
		busyLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		/*
		 * Here is where I build the TASK panel
		 */
		// creating the actual taskpanel
		containerTaskPanel = new IvanErrorsTaskPaneContainer(txtEditor);
		// create us a scroll thing
		// wrap our panel into the scrollthing
		JScrollPane sp = new JScrollPane(containerTaskPanel);
		// add our controls to the visible world
		frmvanInput.getContentPane().add(sp, BorderLayout.EAST);
		
		// create some mock content
		containerTaskPanel.createCategory("effect", "Sentences without any effect.");
		containerTaskPanel.createProblem("effect", "I think there is a man in my bathroom.", 13,22);
		
		containerTaskPanel.createCategory("location", "These sentences contain incomplete descriptions. In this case, the location is missing.");
		containerTaskPanel.createProblem("location", "There is a cat looking north.", 25, 31, new String[] {"the cat"});
		
		containerTaskPanel.createCategory("direction", "Entities without a declared direction.");
		containerTaskPanel.createProblem("direction", "There is a boy and a girl.", 51,76, new String[]{"boy", "girl"});
		
		containerTaskPanel.createCategory("meta", null);
		containerTaskPanel.createProblem("meta", null, 0,0);
		
		System.out.println(containerTaskPanel);
		System.out.println();
		
		// the emitter and the TaskPane have something to work on, so set up the linguistics stuff
		setupFeedback();
		
		// prepare git tracking
		try {
			setupTracking();
		} catch (IOException | GitAPIException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Saves the current document
	 * 
	 * @param editor
	 *            the component which contains the text
	 * @return
	 */
	protected boolean save(TextEditorPane editor) {
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
				if(out != null)
					out.close();
			} catch (IOException e) {
				// okay, even close() throws? That's messed up.
			}
		}
		commit(outputfile.getName());
		return true;
	}
	
	/**
	 * This method performs a commit to the local git repository
	 */
	@SuppressWarnings("resource")
	private void commit(String file) {
		String branch = file2ref(file);
		String basepath = edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.TRACKINGPATH;
		FileWriter out;
		try {
			out = new FileWriter(basepath + DOCUMENT_TXT);
			out.write(txtEditor.getText());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.commit(branch);
	}

	/** Makes sure that any given name is converted into a valid ref name.
	 git imposes the following rules on how references are named:

    1. They can include slash / for hierarchical (directory) grouping, 
    	but no slash-separated component can begin with a dot .. 

    2. They must contain at least one /. This enforces the presence of a 
    	category like heads/, tags/ etc. but the actual names are not restricted. 

    3. They cannot have two consecutive dots .. anywhere. 

    4. They cannot have ASCII control characters (i.e. bytes whose values 
    	are lower than \040, or \177 DEL), space, tilde ~, caret ^, colon :, 
    	question-mark ?, asterisk *, or open bracket [ anywhere. 

    5. They cannot end with a slash / nor a dot .. 

    6. They cannot end with the sequence .lock. 

    7. They cannot contain a sequence @{. 

    8. They cannot contain a \. 
	 
	 * @param file
	 * @return
	 */

	public static String file2ref(String file) {
		String ofile = file, // "old file" 
				nfile = file; // "new file"
		do
		{
			ofile = nfile; // "new" is the new "old"
			
			// 3. no double dots
			nfile = ofile.replace("..", ".");

		}while(!ofile.equals(nfile));
		
		// work with a char array
		char[] f = nfile.toCharArray();
		// 4. no low values
		for(int i = 0; i < f.length; i++)
		{
			if(f[i] < 41 || f[i] > 176)
			{
				f[i] = 'o';
			}
//			System.out.println(Arrays.toString(f));
		}

		// space, tilde ~, caret ^, colon :, question-mark ?, asterisk *, or open bracket [ anywhere. 
		nfile = Arrays.toString(f)
				.replace("[", "")
				.replace("]", "") // not a violation, but it's produced by Arrays.toString
				.replace(", ", "") // also a byproduct of the array print
				.replace(" ", "o")
				.replace("^", "o")
				.replace(":", "o")
				.replace("?", "o")
				.replace("*", "o")
				.replace(".lock", "ooooo") // 6. They cannot end with the sequence .lock. 
				.replace("@{", "oo") // 7. They cannot contain a sequence @{. 
				;
		return nfile;
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
			System.out.println("SwingWindow.load()");
		}
	}

	private static void checkout(String file2ref) {
		GitManager.checkout(file2ref, null);
	}

	private static void tag(String tagname) {
		edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager.tag(tagname);
	}

	/**
	 * Reloads the page, even if it is already being displayed
	 * 
	 * @param editor destination container
	 * @param file file to load
	 */
	public void reload(TextEditorPane editor, File file) 
	{
		Document doc = editor.getDocument();
		doc.putProperty(Document.StreamDescriptionProperty, null);
		load(editor, file);
	}

	/**
	 * Prepares the problems list for the task pane. If I decide to do inital
	 * stuff with the taskpane and the emitter, that code goes here as well.
	 */
	private void setupFeedback() {
		// prepare a list for the problems
		this.problemSetMissingDirection = new HashSet<EntityInfo>();
		this.problemSetMissingLocation = new HashSet<EntityInfo>();

		// tell the errors panel where our editor is so it can apply quick fixes
		this.containerTaskPanel.setEditor(this.txtEditor);
	}

	/**
	 *  This method invokes the pipeline from other components.
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
	
	/** This method prepares the git repository and tracking files.
	 * @throws IOException 
	 * @throws GitAPIException 
	 * 
	 */
	private static void setupTracking() throws IOException, GitAPIException
	{
		File dir = new File(GitManager.TRACKINGPATH);
		dir.mkdir();
		
		String documentpath = GitManager.TRACKINGPATH + DOCUMENT_TXT;
		File what = new File(documentpath);
		what.createNewFile();
		
		GitManager.safeInit();
	}

	/**
	 * @param text
	 * @throws Exception
	 */
	private void processText(String text) throws Exception {
		clearStyles();

		/**
		 * Until I come up with something better, I need to scrub the state of
		 * everything each time the analysis runs
		 */
		// FIXME: This part is a setup for memory leaks.
		problemSetMissingDirection.clear();
		problemSetMissingLocation.clear();
		Annotation doc = annotateClassifications(text);

		List<IvanErrorMessage> errors = doc.get(IvanAnnotations.DocumentErrorAnnotation.class);
		if(errors != null){
			for (IvanErrorMessage docer : errors) {
				markIvanError(docer.getSpan().start(), docer.getSpan().end());
			}
		}
		
		java.util.List<CoreMap> listsentences = doc
				.get(SentencesAnnotation.class);
		
		InitialState entitiesState = doc.get(IvanEntitiesAnnotation.class); 
		
		for (CoreMap sentence : listsentences) {
			// traversing the words in the current sentences
			SemanticGraph depgraph = sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);
			if (depgraph.getRoots().isEmpty()) {
				continue;
			}


			/***
			 * Requirement 1: Check subject for entity or name and extract name,
			 * position and direction if possible Also, save name in name list
			 * and save position and direction in EntityInfo.
			 */
			// TODO: implement problem1
			// 1. check for names
			List<String> names = DeclarationPositionFinder
					.recogniseNames(sentence); // recognises named und unnamed
												// entities in this sentence
			// 2. are they declared already?

			// -- for each name:
			for (String n : names) {
				// 4. while we're at it (iterating), check if there is any info
				// missing for this name
				/*
				 * I have two options: 1. retrieve the entityinfo, check for
				 * missing data, display a warning to the user or 2. retrieve
				 * entityinfo, check for missing data, store the entityinfo in a
				 * list corresponding with the problem type and later call a
				 * method for each problem type which displays a compact list of
				 * missing things. I chose 2.
				 */
				List<EntityInfo> declarednames = entitiesState.get(n);

				for (EntityInfo infoOnName : declarednames) {

					if (!infoOnName.hasLocation()) {
						// try to get a loc from the current sentence. if not,
						// add to problems. if yes, remove from problems
						EntityInfo moreinfo = DeclarationPositionFinder
								.getLocation(sentence);
						if (moreinfo == null || !moreinfo.hasLocation()) {
							// Bad! This sentence contains no location info and
							// we are still missing location info
							problemSetMissingLocation.add(infoOnName);
						} else {
							// fixme: the info may not relate to the proper name
							assert infoOnName.getEntity().equals(
									moreinfo.getEntity());

							// good! this sentence contains info, so lets merge
							// the two
							infoOnName.setLocation(moreinfo.getLocation());
							// and remove the problem entry if we had one
							@SuppressWarnings("unused")
							boolean success = problemSetMissingLocation
									.remove(infoOnName);
						}
					}
					if (!infoOnName.hasDirection()) {
						// try to get a dir from the current sentence. if not,
						// add to problems. if yes, remove from problems
						EntityInfo moreinfo = DeclarationPositionFinder
								.getDirection(sentence);
						if (moreinfo == null || !moreinfo.hasDirection()) {
							// Bad! This sentence contains no location info and
							// we are still missing location info
							problemSetMissingDirection.add(infoOnName);
						} else {
							// good! this sentence contains info, so lets merge
							// the two
							infoOnName.setDirection(moreinfo.getDirection());
							// and remove the problem entry if we had one
							@SuppressWarnings("unused")
							boolean success = problemSetMissingDirection
									.remove(infoOnName);
						}
					}
				}
				// 5. create a display for the missing info?
				// see above
				// call a method which iterates over each problem type and displays errors
				refreshErrorsDisplay();
			}

			/***
			 * Requirement 2: Classify sentence into Setup descriptions and
			 * non-setup descriptions
			 */
			StaticDynamicClassifier.Classification sentencetype = sentence.get(Classification.class);
			
			// finding locations?
			if (DeclarationPositionFinder.hasLocation(sentence)) {
				EntityInfo loc = DeclarationPositionFinder
						.getLocation(sentence);
				tell("There's a location in \""
						+ sentence.get(TextAnnotation.class));
				System.out.println("The location \"" + loc
						+ "\" was found in \""
						+ sentence.get(TextAnnotation.class));
			}

			// get root for coloring
			IndexedWord root = depgraph.getFirstRoot();
			if (depgraph.isEmpty()) {
				continue;
			}
			// color the sentence according to classification
			switch (sentencetype) {
			case SetupDescription:
				// tell(depgraph.toString());
				markText(root.beginPosition(), root.endPosition(), new Color(0xB3C4FF));
				// DeclarationPositionFinder.DeclarationQuadruple decl =
				// mydeclarationfinder.findAll(root, sentence);
				break;
			case ErrorDescription:
				IvanErrorMessage err = sentence.get(IvanAnnotations.ErrorMessageAnnotation.class);
				markIvanError(err.getSpan().start(), err.getSpan().end());
				// emit error
				break;
			case ActionDescription:
				markText(root.beginPosition(), root.endPosition(), new Color(0xFFC4B3));
				// fallthrough to default
			default:
				break;
			}

			/***
			 * Requirement 3: Check this sentence for co-reference
			 */

			// a CoreLabel is a CoreMap with additional token-specific labels
			// for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
			// // this is the text of the token
			// // String word = item.get(TextAnnotation.class);
			// // this is the POS tag of the token
			// String pos = item.get(PartOfSpeechAnnotation.class);
			// if (pos.equals("VBG")) {
			// tell("\""+ item.getString(TextAnnotation.class) +
			// "\" is a gerund.");
			// markText(item.beginPosition(), item.endPosition());
			// }
			// // this is the NER label of the token
			// // String ne = item.get(NamedEntityTagAnnotation.class);
			//
			// // System.out.println(word);
			// }
		}

		diplayWarnings();
	}

	private void markIvanError(int beginPosition, int endPosition) {
		// TODO Auto-generated method stub
		SquiggleUnderlineHighlightPainter sqpainter = new SquiggleUnderlineHighlightPainter(Color.RED);
		try {
			txtEditor.getHighlighter().addHighlight(beginPosition, endPosition, sqpainter);
		} catch (BadLocationException e) {
			e.printStackTrace();
			Redwood.log(Redwood.ERR, e);
		}
	}

	private void refreshErrorsDisplay() {
		// TODO Auto-generated method stub
		
	}
	
	private void markSpellingError(int beginPosition, int endPosition) {
		SquiggleUnderlineHighlightPainter sqpainter = new SquiggleUnderlineHighlightPainter(Color.RED);
		try {
			txtEditor.getHighlighter().addHighlight(beginPosition, endPosition, sqpainter);
		} catch (BadLocationException e) {
			e.printStackTrace();
			System.out
					.println("SwingWindow.initialize().new KeyAdapter() {...}.markSpellingError()");
		}				
	}

	/**
	 * 
	 */
	protected StanfordCoreNLP setupCoreNLP() {
		StanfordCoreNLP pipeline;
		if (mypipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternativ: wsj-bidirectional
			try {
				props.put(
						"pos.model",
						"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// konfiguriere pipeline
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); //$NON-NLS-1$ //$NON-NLS-2$
			pipeline = new StanfordCoreNLP(props);
			mypipeline = pipeline;
		} else {
			pipeline = mypipeline;
		}
		return pipeline;
	}

	private void diplayWarnings() {
		// TODO This method manipulates the right-hand actionpanel for
		// displaying errors and warnings
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
		txtEditor.getHighlighter().removeAllHighlights();
		System.out.println("SwingWindow.clearStyles()");
	}

	private void markText(int beginPosition, int endPosition, Color color) {
		
		DefaultHighlightPainter sqpainter = new DefaultHighlightPainter(color);
		try {
			txtEditor.getHighlighter().addHighlight(beginPosition, endPosition, sqpainter);
		} catch (BadLocationException e) {			
			e.printStackTrace();
			System.out.println("SwingWindow.markText()");
		}
	}

	private void stopAndPrintStopWatch() {
		org.joda.time.DateTime now = org.joda.time.DateTime.now();
		long diff = now.getMillis() - stopwatch.getMillis();
		System.err.println("Stopwatch: " + (diff) + " ms.");
	}

	private void startStopWatch() {
		stopwatch = org.joda.time.DateTime.now();
	}

	@SuppressWarnings("serial")
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
		}
	}
	
	/**
	 * Annotates a document with our customized pipeline.
	 * 
	 * @param text
	 *            A text to process
	 * @return The annotated text
	 */
	public static Annotation annotateClassifications(String text) {
		Annotation doc = new Annotation(text);
		getPipeline().annotate(doc);
		return doc;
	}

	private static StanfordCoreNLP getPipeline() {
		if (stanfordCentralPipeline == null) {
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization,
			// NER, parsing, and coreference resolution
			Properties props = new Properties();
			// alternative: wsj-bidirectional
			try {
				props.put(
						"pos.model",
						"edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// adding our own annotator property
			props.put("customAnnotatorClass.sdclassifier",
					"edu.kit.ipd.alicenlp.ivan.analyzers.StaticDynamicClassifier");
			// adding our declaration finder
			props.put("customAnnotatorClass.declarations",
					"edu.kit.ipd.alicenlp.ivan.analyzers.DeclarationPositionFinder");
			// configure pipeline
			props.put(
					"annotators", "tokenize, ssplit, pos, lemma, ner, parse, declarations, sdclassifier"); //$NON-NLS-1$ //$NON-NLS-2$
			stanfordCentralPipeline = new StanfordCoreNLP(props);
		}
		
		return stanfordCentralPipeline;
	}
}
