import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JTextPane;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import java.awt.Label;
import java.awt.TextField;


public class SwingWindow {

	private JFrame frame;

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
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JTextPane txtpnnn = new JTextPane();
		txtpnnn.setEnabled(false);
		txtpnnn.setText("1\r\n2\r\n3\r\n4\r\n5");
		panel.add(txtpnnn);
		
		JTextPane txtpnInPublishingAnd = new JTextPane();
		txtpnInPublishingAnd.setText("In publishing and graphic design, lorem ipsum is a placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, and layout, by removing the distraction of meaningful content.");
		panel.add(txtpnInPublishingAnd);
		
		JLabel list = new JLabel();
		list.setText("platzhalter f\u00FCr jxtaskpane");
		panel.add(list);
		
		JTextPane txtpnHi = new JTextPane();
		txtpnHi.setText("hi");
		frame.getContentPane().add(txtpnHi);
	}

}
