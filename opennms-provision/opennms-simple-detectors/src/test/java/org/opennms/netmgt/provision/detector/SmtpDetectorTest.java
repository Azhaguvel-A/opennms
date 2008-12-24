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

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


/**
 * @author Donald Desloge
 *
 */
public class SmtpDetectorTest {
    
    private SmtpDetector m_detector;
    private SimpleServer m_server;
    
    @Before
    public void setUp() throws Exception {
        m_server = getServer();
        m_server.init();
        m_server.startServer();
        
        m_detector = new SmtpDetector();
        m_detector.init();
        m_detector.setPort(m_server.getLocalPort());
    }
    
    @After
    public void tearDown() throws IOException {
        m_server.stopServer();
    }
    
    @Test
    public void testDetectorFailWrongCodeExpectedMultilineRequest() throws Exception {
        SimpleServer tempServer = new SimpleServer() {
            
            public void onInit() {
                String[] multiLine = {"600 First line"};
                
                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };
        
        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(tempServer.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailIncompleteMultilineResponseFromServer() throws Exception {
        SimpleServer tempServer = new SimpleServer() {
            
            public void onInit() {
                String[] multiLine = {"250-First line", "400-Bogus second line"};
                
                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };
        
        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(tempServer.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailBogusSecondLine() throws Exception {
        SimpleServer tempServer = new SimpleServer() {
            
            public void onInit() {
                String[] multiLine = {"250-First line", "400-Bogus second line", "250 Requested mail action completed"};
                
                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };
        
        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(tempServer.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongTypeOfBanner() throws Exception {
        SimpleServer tempServer = new SimpleServer() {
            
            public void onInit() {
                setBanner("Bogus");
            }
        };
        
        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());
        
        assertFalse(m_detector.isServiceDetected(tempServer.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailServerStopped() throws IOException {
        m_server.stopServer();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongPort() {
        m_detector.setPort(1);
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSucess() {
        assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    private SimpleServer getServer() {
        return new SimpleServer() {
             
            public void onInit() {
                String[] multiLine = {"250-First line", "250-Second line", "250 Requested mail action completed"};
                
                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };
    }
}
