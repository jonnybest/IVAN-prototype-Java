import java.util.Properties;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class MainWindow extends ApplicationWindow {
	private Text txtn;
	private Text text_1;

	/**
	 * Create the application window.
	 */
	public MainWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		{
			txtn = new Text(container, SWT.BORDER | SWT.MULTI);
			txtn.setText("1\r\n2");
			txtn.setEditable(false);
			txtn.setBounds(10, 10, 38, 390);
		}
		StyledText styledText = new StyledText(container, SWT.BORDER | SWT.WRAP);
		styledText.addModifyListener(new ModifyListener() {
			/**
			 * This is what happens when the text in the center area gets modified
			 */
			public void modifyText(ModifyEvent arg0) {
				/* Update line numbers in txtn area 
				 */
				StyledText text = (StyledText) arg0.getSource();
				String numbers = "";
				for (int i = 0; i < text.getLineCount(); i++) {
					numbers += i + "\n";
				}
				txtn.setText(numbers);
				
				processText(text);				
			}

			/**
			 * @param text
			 */
			private void processText(StyledText text) {
				/* Recognise modal sentences 
				 */
				String lines = text.getText();
				String[] modalVerbs = {"can", "could", "may", "might", "must", "shall", "should", "will", "would", "have to", "has to", "had to", "need"};
				for (String string : modalVerbs) {
					if (lines.contains(string)) {
						System.out.println("Found bad word: " + string + ".");						
					}
				}
				/* tag with pos tags */
				Properties props = new Properties();
			    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
			    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			    Annotation mydoc = new Annotation(text.getText());
			    pipeline.annotate(mydoc);
			    java.util.List<CoreMap> sentences = mydoc.get(SentencesAnnotation.class);
			    
			    /*
			    for(CoreMap sentence: sentences)
			    {
			    	// traversing the words in the current sentence
			    	// a CoreLabel is a CoreMap with additional token-specific labels
			    	for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
			    		// this is the text of the token
			            //String word = item.get(TextAnnotation.class);
			            // this is the POS tag of the token
			            //String pos = item.get(PartOfSpeechAnnotation.class);
			            // this is the NER label of the token
			            //String ne = item.get(NamedEntityTagAnnotation.class);
			            
			            //System.out.println(word);
					}
			    }
			    */
			    
			}
		});
		styledText.setText("My sample text styled.\nLorem ipsum dolor sit amet,");
		styledText.setBounds(54, 10, 513, 390);
		{
			text_1 = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			text_1.setBounds(54, 406, 723, 155);
		}
		
		List list = new List(container, SWT.BORDER);
		list.setItems(new String[] {"Please ignore.", "Find a reason.", "Can't get no satisfaction."});
		list.setBounds(573, 10, 204, 390);
		styledText.setLineBackground(0, 1, this.getShell().getDisplay().getSystemColor(SWT.COLOR_GREEN));

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			MainWindow window = new MainWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("VAN - Verify AliceNLP prototype");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(803, 680);
	}
}
