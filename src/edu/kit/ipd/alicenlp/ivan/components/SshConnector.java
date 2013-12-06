/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import java.util.List;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import edu.kit.ipd.alicenlp.ivan.tests.SshTest.TestCaseUserInfo;

/** This class establishes all the necessary SSH connections and port mappings for IVAN  
 * 
 * @author Jonny
 *
 */
public class SshConnector {
	
	static Session RecaaSession;

	/** Maps a port to research cyc at IPD
	 * 
	 * @param remote
	 * @return
	 * @throws JSchException
	 */
	private static Session mapPortToCycWithSsh(int remote) throws JSchException {
		Session session = establishIpdSession();
	
		System.out.println("assigning port fw");
		// https://i41vm-automodel.ipd.kit.edu:3602/cgi-bin/cyccgi/cg?cb-start
		// is now at https://localhost:3602/cgi-bin/cyccgi/cg?cb-start
		int assinged_port = session.setPortForwardingL(remote,
				"i41vm-automodel.ipd.kit.edu", remote);
	
		System.out.println("local port is now " + assinged_port);
		return session;
	}

	/**
	 * @return
	 * @throws JSchException
	 */
	private static Session establishIpdSession() throws JSchException {
		if(RecaaSession != null && RecaaSession.isConnected())
			return RecaaSession;
		
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
		};
	
		session.setUserInfo(ui);
	
		// It must not be recommended, but if you want to skip host-key check,
		// invoke following,
		session.setConfig("StrictHostKeyChecking", "no");
	
		System.out.println("config done. connecting to server...");
	
		// session.connect();
		session.connect(10000);// (30000); // making a connection with timeout.
		System.out.println("session is online");
		return session;
	}

	/** Disconnects everything and closes ports
	 * 
	 */
	public static void disconnect()
	{
		if(RecaaSession != null)
			RecaaSession.disconnect();
	}

	/** establishes the mappings required for cyc
	 * 
	 * @throws JSchException
	 */
	public static void initializeCycConnections() throws JSchException {
		
		mapPortToCycWithSsh(3600);
		mapPortToCycWithSsh(3601);
		mapPortToCycWithSsh(3602);				
		mapPortToCycWithSsh(3614);
		mapPortToCycWithSsh(3615);		
	}
}
