//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 09: Organize imports. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.test.mock.MockLogAppender;

public class PollOutagesConfigManagerTest extends TestCase {

    private PollOutagesConfigManager m_manager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PollOutagesConfigManagerTest.class);
    }

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
        
        String xml = "<?xml version=\"1.0\"?>\n" + 
                "<outages>\n" + 
                "   <outage name=\"one\" type=\"weekly\">\n" + 
                "       <time day=\"sunday\" begins=\"12:30:00\" ends=\"12:45:00\"/>\n" + 
                "       <time day=\"sunday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"monday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"tuesday\" begins=\"13:00:00\" ends=\"14:45:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "       <interface address=\"192.168.0.36\"/>\n" + 
                "       <interface address=\"192.168.0.38\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"two\" type=\"monthly\">\n" + 
                "       <time day=\"1\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"21:30:00\" ends=\"21:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <interface address=\"192.168.100.254\"/>\n" + 
                "       <interface address=\"192.168.101.254\"/>\n" + 
                "       <interface address=\"192.168.102.254\"/>\n" + 
                "       <interface address=\"192.168.103.254\"/>\n" + 
                "       <interface address=\"192.168.104.254\"/>\n" + 
                "       <interface address=\"192.168.105.254\"/>\n" + 
                "       <interface address=\"192.168.106.254\"/>\n" + 
                "       <interface address=\"192.168.107.254\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"three\" type=\"specific\">\n" + 
                "       <time begins=\"21-Feb-2005 05:30:00\" ends=\"21-Feb-2005 15:00:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "   </outage>\n" + 
                "</outages>\n";
        
        StringReader rdr = new StringReader(xml);
        
        m_manager = new PollOutagesConfigManager() {

            protected void saveXML(String xmlString) throws IOException, MarshalException, ValidationException {}
            public void update() throws IOException, MarshalException, ValidationException {}

            
        };
        
        m_manager.setConfig((Outages) Unmarshaller.unmarshal(Outages.class, rdr));

            
        
        
    }

    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    private long getTime(String timeString) throws ParseException {
        Date date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(timeString);
        return date.getTime();
        
    }
    
    public void testIsTimeInOutageWeekly() throws Exception {

        assertTrue(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "two"));
        assertTrue(m_manager.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "three"));
        
        assertTrue(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "three"));
        
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "one"));
        assertTrue(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "three"));
        
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "one"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "two"));
        assertFalse(m_manager.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "three"));
        
        
    }


}
