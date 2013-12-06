package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencyc.api.CycAccess;
import org.opencyc.api.CycApiException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.kit.ipd.alicenlp.ivan.components.SshConnector;

/** These tests assert cyc availability (and possibly function)
 * 
 * @author Jonny
 *
 */
@SuppressWarnings("static-method")
public class CycTest {

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
		
		SshConnector.initializeCycConnections();
	}
	
	/** This class tears down the open ssh connections
	 * 
	 */
	@AfterClass
	public static void closePorts() {
		SshConnector.disconnect();
	}
	
}
