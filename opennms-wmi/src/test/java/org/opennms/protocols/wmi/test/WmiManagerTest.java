//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi.test;

import static org.easymock.EasyMock.*;
import org.opennms.protocols.wmi.IWmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.opennms.protocols.wmi.test.stubs.OnmsWbemObjectSetBiosStub;
import org.opennms.protocols.wmi.test.stubs.OnmsWbemObjectBiosStub;
import org.opennms.protocols.wmi.test.stubs.OnmsWbemPropSetBiosStub;
import org.opennms.protocols.wmi.test.stubs.OnmsWbemPropBiosStub;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;

import junit.framework.TestCase;

import java.util.List;

/**
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * 
 */
public class WmiManagerTest extends TestCase {
	private IWmiClient m_WmiMock;

	/*
	 * Create a placeholder mock object. We will reset() this in each test
	 * so that we can reuse it.
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Create a mock client to use.
		m_WmiMock = createMock(IWmiClient.class);
	}

	/*
	 * Tear down simply resets the mock object.
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		reset(m_WmiMock);
	}

	/**
	 * Test that the isValidMatchType object works for the expected values of
	 * "all", "none", "some" and "one" but does not work for other arbitrary values.
	 * 
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiManager#isValidMatchType(java.lang.String)}.
	 */
	public final void testIsValidMatchType() {
		assertTrue(WmiManager.isValidMatchType("all"));
		assertTrue(WmiManager.isValidMatchType("none"));
		assertTrue(WmiManager.isValidMatchType("some"));
		assertTrue(WmiManager.isValidMatchType("one"));

		assertFalse(WmiManager.isValidMatchType("thisisntavalidtype"));
	}

	/**
	 * Test that the isValidOpType only returns true for valid operation types such
	 * as "EQ", "GT", "LT" and "NEQ" but not for arbitrary strings.
	 * 
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiManager#isValidOpType(java.lang.String)}.
	 */
	public final void testIsValidOpType() {
		assertTrue(WmiManager.isValidOpType("EQ"));
		assertTrue(WmiManager.isValidOpType("GT"));
		assertTrue(WmiManager.isValidOpType("LT"));
		assertTrue(WmiManager.isValidOpType("NEQ"));

		assertFalse(WmiManager.isValidOpType("MADEUP"));
	}

	/**
	 * Test a standard client connect.
	 * 
	 * Test method for {@link org.opennms.protocols.wmi.WmiManager#init()}.
     *
     * @throws WmiException if there are any problems with the WmiManager
	 */
	public final void testInit() throws WmiException {
		// Set up WMI mock client.
		m_WmiMock.connect("127.0.0.1", "Administrator", "password");
		replay(m_WmiMock);

		// Create a manager.
		WmiManager wmiManager = new WmiManager("127.0.0.1", "Administrator",
				"password");
		// Initialize
		wmiManager.init(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test that the WMI manager properly handles invalid host exceptions
	 * from the WMI client.
	 * 
	 * Test method for {@link org.opennms.protocols.wmi.WmiManager#init()}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testInitBadHostname() throws WmiException {
		// Set up WMI mock client.
		// 1) Expect a call to connect() with a bad hostname.
		// 2) Throw a new WmiException indictating a bad hostname.
		m_WmiMock.connect("bad-hostname", "Administrator", "password");
		expectLastCall()
				.andThrow(
						new WmiException(
								"Unknown host 'bad-hostname'. Failed to connect to WMI agent."));
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("bad-hostname",
					"Administrator", "password");
			// Initialize
			wmiManager.init(m_WmiMock);

		} catch (WmiException e) {
			assertTrue("Exception missing message: Unknown host: " + e, e
					.getMessage().contains("Unknown host"));
		}
		verify(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test that the WMI manager handles invalid username or password exceptions
	 * properly from the WMI client.
	 * 
	 * Test method for {@link org.opennms.protocols.wmi.WmiManager#init()}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testInitBadUserPass() throws WmiException {
		// Set up WMI mock client.
		// 1) Expect a call to connect() with a bad hostname.
		// 2) Throw a new WmiException indictating a user or password.
		m_WmiMock.connect("127.0.0.1", "Administrator", "wrongpassword");
		expectLastCall()
				.andThrow(
						new WmiException(
								"Failed to connect to host '127.0.0.1': The attempted logon is invalid. This is either due to a bad username or authentication information. [0xC000006D]"));
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("127.0.0.1",
					"Administrator", "wrongpassword");
			// Initialize
			wmiManager.init(m_WmiMock);

		} catch (WmiException e) {
			assertTrue(
					"Exception missing message: The attempted logon is invalid: "
							+ e, e.getMessage().contains(
							"The attempted logon is invalid"));
		}
		verify(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test that a normal, standard close functions properly.
	 * 
	 * Test method for {@link org.opennms.protocols.wmi.WmiManager#close()}.
     *
     * @throws WmiException if there is unexpected behavior.
	 */
	public final void testClose() throws WmiException {
		// Set up WMI mock client.
		// 1) Expect a call to connect()
		// 2) Expect a call to disconnect()
		m_WmiMock.connect("127.0.0.1", "Administrator", "password");
		m_WmiMock.disconnect();
		replay(m_WmiMock);

		// Create a manager.
		WmiManager wmiManager = new WmiManager("127.0.0.1", "Administrator",
				"password");
		// Initialize
		wmiManager.init(m_WmiMock);
		// Disconnect
		wmiManager.close();

		verify(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test an attempt to close the client before the client has been
	 * initialized. This should throw a new exception stating that WmiClient
	 * hasn't been properly initialized.
	 * 
	 * Test method for {@link org.opennms.protocols.wmi.WmiManager#close()}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testCloseWithInvalidSession() throws WmiException {
		// Set up WMI mock client.
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("127.0.0.1",
					"Administrator", "password");
			// Disconnect without initializing/connecting.
			wmiManager.close();

		} catch (WmiException e) {
			assertTrue(
					"Exception missing message: WmiClient was not initialized: "
							+ e, e.getMessage().contains(
							"WmiClient was not initialized"));
		}

		verify(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test the performOp method with an invalid WMI class and valid WMI object.
	 * 
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiManager#performOp(org.opennms.protocols.wmi.WmiParams)}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testPerformOpInvalidClass() throws WmiException {
		// Create parameter holder.
		WmiParams params = new WmiParams("2/12/2004 00:00:00", "EQ",
				"Win32_BISO", "ReleaseDate");
		// Set up WMI mock client.
		// 1) Expect a call to connect() with a bad hostname.
		// 2) Throw a new WmiException indictating a bad hostname.
		m_WmiMock.connect("127.0.0.1", "Administrator", "password");
        m_WmiMock.performInstanceOf("Win32_BISO");
        
        expectLastCall()
				.andThrow(
						new WmiException(
								"Failed to perform WMI operation: Exception occurred.  [0x80020009] ==> Message from Server: SWbemServicesEx Invalid class"));
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("127.0.0.1",
					"Administrator", "password");

			// Initialize
			wmiManager.init(m_WmiMock);

			// Perform an operation.
			wmiManager.performOp(params);

		} catch (WmiException e) {
			assertTrue(
					"Exception missing message: SWbemServicesEx Invalid class: "
							+ e, e.getMessage().contains(
							"SWbemServicesEx Invalid class"));
		}

		verify(m_WmiMock);

		reset(m_WmiMock);
	}

	/**
	 * Test the performOp method with an valid WMI class and invalid WMI object.
	 * 
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiManager#performOp(org.opennms.protocols.wmi.WmiParams)}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testPerformOpInvalidObject() throws WmiException {

		//
		// Create parameter holder.
		WmiParams params = new WmiParams("2/12/2004 00:00:00", "EQ",
				"Win32_BIOS", "RelDate");
		// Set up WMI mock client.
		// 1) Expect a call to connect() with a bad hostname.
		// 2) Throw a new WmiException indictating a bad hostname.
        OnmsWbemObjectSet wos = new OnmsWbemObjectSetBiosStub(
                new OnmsWbemObjectBiosStub(
                        new OnmsWbemPropSetBiosStub(
                                new OnmsWbemPropBiosStub()
                        )
                )
        );
        
        m_WmiMock.connect("127.0.0.1", "Administrator", "password");
        expect(m_WmiMock.performInstanceOf("Win32_BIOS")).andReturn(wos);
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("127.0.0.1", "Administrator", "password");

			// Initialize
			wmiManager.init(m_WmiMock);

			// Perform an operation.
			wmiManager.performOp(params);

		} catch (WmiException e) {
			assertTrue("Exception missing message: Unknown name: " + e, e
					.getMessage().contains("Unknown name"));
		}

		verify(m_WmiMock);

		reset(m_WmiMock);
	}

    	/**
	 * Test the performOp method with a valid WMI class and valid WMI object.
	 *
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiManager#performOp(org.opennms.protocols.wmi.WmiParams)}.
     *
     * @throws WmiException if there is a problem with the mock object.
	 */
	public final void testPerformOpValidObject() throws WmiException {

		//
		// Create parameter holder.
		WmiParams params = new WmiParams("2/12/2004 00:00:00", "EQ",
				"Win32_BIOS", "ReleaseDate");
		// Set up WMI mock client.
		// 1) Expect a call to connect() with a bad hostname.
		// 2) Throw a new WmiException indictating a bad hostname.
        OnmsWbemObjectSet wos = new OnmsWbemObjectSetBiosStub(
                new OnmsWbemObjectBiosStub(
                        new OnmsWbemPropSetBiosStub(
                                new OnmsWbemPropBiosStub()
                        )
                )
        );

        m_WmiMock.connect("127.0.0.1", "Administrator", "password");
        expect(m_WmiMock.performInstanceOf("Win32_BIOS")).andReturn(wos);
		replay(m_WmiMock);

		try {
			// Create a manager.
			WmiManager wmiManager = new WmiManager("127.0.0.1", "Administrator", "password");

			// Initialize
			wmiManager.init(m_WmiMock);

			// Perform an operation.
			WmiResult res = wmiManager.performOp(params);
            //assertTrue(res)

        } catch (WmiException e) {
			//assertTrue("Exception missing message: Unknown name: " + e, e
			//		.getMessage().contains("Unknown name"));
		}

		verify(m_WmiMock);

		reset(m_WmiMock);
	}

}
