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
package org.opennms.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ThrowableAnticipatorTest extends TestCase {
    private ThrowableAnticipator m_anticipator;
    private Throwable m_throwable = new Throwable("our test throwable");

    public ThrowableAnticipatorTest() {
    }

    protected void setUp() throws Exception {
        m_anticipator = new ThrowableAnticipator();
    }

    protected void tearDown() throws Exception {
        m_anticipator.verifyAnticipated();
    }
    
    public void testConstructor() throws Exception {
        setUp();
    }
    
    public void testAnticipate() {
        m_anticipator.anticipate(m_throwable);
        m_anticipator.reset();
    }
    
    public void testThrowableReceivedVoid() {
        try {
            m_anticipator.throwableReceived(null);
        } catch (IllegalArgumentException e) {
            if ("Throwable must not be null".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected IllegalArgumentException: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected IllegalArgumentException.");
    }
    
    public void testThrowableReceivedVoidMessage() {
        try {
            m_anticipator.throwableReceived(new Exception());
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Exception".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testThrowableReceivedIgnoreMessage() {
        m_anticipator.anticipate(new Exception(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_anticipator.throwableReceived(new Exception("something random"));
        } catch (AssertionFailedError e) {
            fail("Received unexpected AssertionFailedError: " + e);
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
    }
    public void testThrowableReceived() {
        m_anticipator.anticipate(m_throwable);
        m_anticipator.throwableReceived(m_throwable);
    }
    
    public void testThrowableReceivedNotAnticipated() {
        try {
            m_anticipator.throwableReceived(m_throwable);
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Throwable: our test throwable".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testThrowableReceivedNotAnticipatedCheckCause() {
        try {
            m_anticipator.throwableReceived(m_throwable);
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Throwable: our test throwable".equals(e.getMessage())) {
                if (e.getCause() == null) {
                    fail("No cause throwable on received exception.");
                }
                assertEquals(m_throwable.getMessage(), e.getCause().getMessage());
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testSetFailFast() {
        assertTrue(m_anticipator.isFailFast());
        m_anticipator.setFailFast(false);
        assertFalse(m_anticipator.isFailFast());
    }
    
    public void testSetFailFastWithUnanticipated() {
        assertTrue(m_anticipator.isFailFast());
        m_anticipator.setFailFast(false);
        m_anticipator.throwableReceived(new Throwable("this should be unanticipated"));
        
        try {
            m_anticipator.setFailFast(true);
        } catch (AssertionFailedError e) {
            if (e.getMessage().startsWith("failFast is being changed from false to true and unanticipated exceptions have been received:")) {
                m_anticipator.reset();
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }

    public void testReset() {
        m_anticipator.setFailFast(false);
        m_anticipator.anticipate(m_throwable);
        m_anticipator.anticipate(new Throwable("something else"));
        m_anticipator.throwableReceived(m_throwable);
        m_anticipator.throwableReceived(new Throwable("yet another thing"));
        m_anticipator.reset();
    }
    
    public void testVerifyAnticipated() {
        m_anticipator.verifyAnticipated();
    }
    
}
