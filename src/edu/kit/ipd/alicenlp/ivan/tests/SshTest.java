/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program enables you to connect to sshd server and get the shell prompt.
 *   $ CLASSPATH=.:../build javac Shell.java 
 *   $ CLASSPATH=.:../build java Shell
 * You will be asked username, hostname and passwd. 
 * If everything works fine, you will get the shell prompt. Output may
 * be ugly because of lacks of terminal-emulation, but you can issue commands.
 *
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jcraft.jsch.*;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

/**
 * This class tests SSH connections.
 * 
 * @author Jonny
 * 
 */
@SuppressWarnings("static-method")
public class SshTest {

	/**
	 * Let's figure out how to establish a connection and a SOCKS forwarding
	 * daemon!
	 * @throws Exception 
	 */
	@Test
	public final void connectTest() throws Exception {

		String myhost = "best@i41vm-automodel.ipd.kit.edu";
		String mypw = "hunter2";

		String[] arg = { myhost };

		JSch jsch = new JSch();

		// jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

		String host = null;
		if (arg.length > 0) {
			host = arg[0];
		} else {
			host = JOptionPane.showInputDialog("Enter username@hostname",
					System.getProperty("user.name") + "@localhost");
		}
		String user = host.substring(0, host.indexOf('@'));
		host = host.substring(host.indexOf('@') + 1);

		Session session = jsch.getSession(user, host, 22);

		String passwd = mypw; // JOptionPane.showInputDialog("Enter password");
		session.setPassword(passwd);

		UserInfo ui = new TestCaseUserInfo() {

			@Override
			public boolean promptYesNo(String str) {
				// fall through
				return true;
			}

			@Override
			public boolean promptPassword(String message) {
				return false;
			}

			// public void showMessage(String message) {
			// JOptionPane.showMessageDialog(null, message);
			// }
			//
			// public boolean promptYesNo(String message) {
			// Object[] options = { "yes", "no" };
			// int foo = JOptionPane.showOptionDialog(null, message,
			// "Warning", JOptionPane.DEFAULT_OPTION,
			// JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			// return foo == 0;
			// }

			// If password is not given before the invocation of
			// Session#connect(),
			// implement also following methods,
			// * UserInfo#getPassword(),
			// * UserInfo#promptPassword(String message) and
			// * UIKeyboardInteractive#promptKeyboardInteractive()

		};

		session.setUserInfo(ui);

		// It must not be recommended, but if you want to skip host-key check,
		// invoke following,
		session.setConfig("StrictHostKeyChecking", "no");

		System.out.println("config done. connecting to server...");

		// session.connect();
		session.connect(10000);// (30000); // making a connection with timeout.
		System.out.println("session is online");

		System.out.println("assigning port fw");
		// https://i41vm-automodel.ipd.kit.edu:3602/cgi-bin/cyccgi/cg?cb-start
		// is now at https://localhost:3602/cgi-bin/cyccgi/cg?cb-start
		int assinged_port = session.setPortForwardingL(0,
				"i41vm-automodel.ipd.kit.edu", 3602);

		System.out.println("local port is now " + assinged_port);
		
		System.out.println("fetching data");
		
		System.out.println("try 2");
		String thing = executeGet("http://localhost:" + assinged_port
				+ "/cgi-bin/cyccgi/cg?cb-start");
		System.out.println(thing);

		if(thing.length() < 500)
			fail("answer probably too short");
		
		session.disconnect();

		System.out.println("done");
		// this test fails if an exception occurs along the way
	}

	/**
	 * This class decides how to get information from the user.
	 * 
	 * @author Jonny
	 * 
	 */
	public static class TestCaseUserInfo implements UserInfo,
			UIKeyboardInteractive {
		public String getPassword() {
			return passwd;
		}

		public boolean promptYesNo(String str) {
			Object[] options = { "yes", "no" };
			int foo = JOptionPane.showOptionDialog(null, str, "Warning",
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			return foo == 0;
		}

		String passwd;
		JTextField passwordField = (JTextField) new JPasswordField(20);

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			Object[] ob = { passwordField };
			int result = JOptionPane.showConfirmDialog(null, ob, message,
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				passwd = passwordField.getText();
				return true;
			} else {
				return false;
			}
		}

		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}

		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0);
		private Container panel;

		public String[] promptKeyboardInteractive(String destination,
				String name, String instruction, String[] prompt, boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for (int i = 0; i < prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]), gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if (echo[i]) {
					texts[i] = new JTextField(20);
				} else {
					texts[i] = new JPasswordField(20);
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}

			if (JOptionPane.showConfirmDialog(null, panel, destination + ": "
					+ name, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
				String[] response = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					response[i] = texts[i].getText();
				}
				return response;
			} else {
				return null; // cancel
			}
		}
	}

	/** Fetches a website from the given URL.
	 * 
	 * @param targetURL
	 * @return the website as a string
	 * @throws IOException
	 */
	public static String executeGet(String targetURL) throws IOException {
		URL url;
		HttpURLConnection connection = null;

		// Create connection
		url = new URL(targetURL);
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		// connection.setRequestProperty("Content-Type",
		// "application/x-www-form-urlencoded");
		//
		// connection.setRequestProperty("Content-Length", "" +
		// Integer.toString(urlParameters.getBytes().length));
		// connection.setRequestProperty("Content-Language", "en-US");

		connection.setUseCaches(false);
		connection.setDoInput(true);
//		connection.setDoOutput(true);
		connection.setReadTimeout(15000); // wait 15 seconds
		
		System.out.println("connection prepared. now sending");
		// Send request
//		 DataOutputStream wr = new DataOutputStream (
//		 connection.getOutputStream ());
////		 wr.writeBytes (urlParameters);
//		 wr.flush ();
//		 wr.close ();

		// Get Response
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}

		connection.disconnect();
		rd.close();
		return response.toString();
	}
}
