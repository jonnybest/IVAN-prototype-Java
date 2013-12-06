package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencyc.api.CycAccess;
import org.opencyc.api.CycApiException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import edu.kit.ipd.alicenlp.ivan.tests.SshTest.TestCaseUserInfo;

/** These tests assert cyc availability (and possibly function)
 * 
 * @author Jonny
 *
 */
@SuppressWarnings("static-method")
public class CycTest {
	
	static List<Session> sessions = new LinkedList<>();

	/** A quick check whether cyc is reachable.
	 * 
	 * @throws UnknownHostException
	 * @throws CycApiException
	 * @throws IOException
	 * @throws JSchException
	 */
	@Test
	public final void connectToCycTest() throws UnknownHostException, CycApiException, IOException, JSchException {
		
		CycAccess cyc = new CycAccess("localhost", 3600);
		
		assertNotNull("cyc connection failed", cyc);
		System.out.println(cyc.isOpenCyc() ? "connected to opencyc" : "connected to cyc");
		
		cyc.close();
	}


	/** Maps ports for cyc (opens ssh connections) 
	 * 
	 * @throws JSchException
	 */
	@BeforeClass
	public static void mapCycPorts() throws JSchException {
		
		Session ses;
		ses = mapPortToCycWithSsh(3600);
		sessions.add(ses);
		
		ses = mapPortToCycWithSsh(3601);
		sessions.add(ses);
		
		ses = mapPortToCycWithSsh(3602);
		sessions.add(ses);
		
		ses = mapPortToCycWithSsh(3614);
		sessions.add(ses);
		
		ses = mapPortToCycWithSsh(3615);
		sessions.add(ses);
	}
	
	/** This class tears down the open ssh connections
	 * 
	 */
	@AfterClass
	public static void closePorts() {
		for (Session ses : sessions) {
			ses.disconnect();
		}
	}

	/** Maps a port to research cyc at IPD
	 * 
	 * @param remote
	 * @return
	 * @throws JSchException
	 */
	private static Session mapPortToCycWithSsh(int remote) throws JSchException {
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

		System.out.println("assigning port fw");
		// https://i41vm-automodel.ipd.kit.edu:3602/cgi-bin/cyccgi/cg?cb-start
		// is now at https://localhost:3602/cgi-bin/cyccgi/cg?cb-start
		int assinged_port = session.setPortForwardingL(remote,
				"i41vm-automodel.ipd.kit.edu", remote);

		System.out.println("local port is now " + assinged_port);
		return session;
	}
	
}
