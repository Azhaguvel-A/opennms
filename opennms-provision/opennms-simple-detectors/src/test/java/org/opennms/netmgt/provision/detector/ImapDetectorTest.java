/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class ImapDetectorTest {
    private SimpleDetector m_detector;
    private SimpleServer m_server; 
    
    
    @Before
    public void setUp() throws Exception{
        
        m_detector = new ImapDetector();
        m_detector.setServiceName("Imap");
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws Exception{
        
    }
    
    @Test
    public void testServerSuccess() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), shutdownServer("* BYE\r\nONMSCAPSD OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailUnexpectedBanner() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
            }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailUnexpectedLogoutResponse() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), singleLineRequest("* NOT OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
}
