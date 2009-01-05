/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class XmlrpcAnticipatorTest extends TestCase {
    private static final int PORT = 9000;
    
    private XmlrpcAnticipator m_anticipator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_anticipator = new XmlrpcAnticipator(PORT);
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_anticipator != null) {
            m_anticipator.shutdown();
        }
        
        super.tearDown();
    }

    /**
     * See if we have any bugs with starting and stopping an anticipator.
     *   
     * @throws IOException
     */
    public void testSetupAndTearDown() {
        // do nothing, let setUp and tearDown do th work
    }

    
    /**
     * See if we have any bugs with starting and stopping two anticipators back to back.
     *   
     * @throws IOException
     */
    public void testSetupTwice() throws IOException {
        // It's already been set up in setUp(), so just shutdown
        m_anticipator.shutdown();

        m_anticipator = new XmlrpcAnticipator(PORT);
        // Let tearDown() do the shutdown
    }
    
    public void testGoodAnticipation() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("foo", "bar");
        
        
        Vector<Object> v2 = new Vector<Object>();
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        v2.add(t2);
        t2.put("foo", "bar");
        
        m_anticipator.anticipateCall("howCheesyIsIt", v);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + PORT);
        client.execute("howCheesyIsIt", v2);
        
        m_anticipator.verifyAnticipated();
    }
    
    public void testAnticipatedNotSeen() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("foo", "bar");
        
        
        Vector<Object> v2 = new Vector<Object>();
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        v2.add(t2);
        t2.put("foo", "baz");
        
        m_anticipator.anticipateCall("howCheesyIsIt", v);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + PORT);
        client.execute("howCheesyIsIt", v2);

        boolean sawException = false;
        try {
            m_anticipator.verifyAnticipated();
        } catch (AssertionFailedError e) {
            // good, we were expecting this
            sawException = true;
        }
        
        if (!sawException) {
            fail("Did not receive an expected AssertionFailedError when calling verifyAnticipated() on the anticipator");
        }
    }
    
    public void testIgnoreDescriptionInsideHashtable() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("description", "cheesy");
        t.put("something other than description", "hello");
        
        
        Vector<Object> v2 = new Vector<Object>();
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        v2.add(t2);
        t2.put("description", "cheesiest");
        t2.put("something other than description", "hello");
        
        m_anticipator.anticipateCall("howCheesyIsIt", v);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + PORT);
        client.execute("howCheesyIsIt", v2);
        
        m_anticipator.verifyAnticipated();
    }
}
