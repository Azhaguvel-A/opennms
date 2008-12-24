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
// 2008 Feb 09: Remove warnings. - dj@opennms.org
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
package org.opennms.netmgt.translator;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jmock.cglib.MockObjectTestCase;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

public class EventTranslatorTest extends MockObjectTestCase {
    
    /* TODO for PassiveSTatusKeeper
     add reason mapper for status reason
     
     be able to create an event with translated values
     - determine new event values based on config
     - assign computed values to new event
     - copy over (or not) untranslated attributes
     
     make sure we can translate uei if desired
     
     modify passive status config to handle specific event with specific parms
     
     
     */


    private EventTranslator m_translator;
    private String m_passiveStatusConfiguration = getStandardConfig();
    private MockEventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_network;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;
    private EventTranslatorConfigFactory m_config;

    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();

        createMockNetwork();
        createMockDb();
        createAnticipators();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);

        Reader rdr = new StringReader(m_passiveStatusConfiguration);
        m_config = new EventTranslatorConfigFactory(rdr, m_db);
        EventTranslatorConfigFactory.setInstance(m_config);
        
        m_translator = EventTranslator.getInstance();
        m_translator.setEventManager(m_eventMgr);
        m_translator.setConfig(EventTranslatorConfigFactory.getInstance());
        m_translator.setDataSource(m_db);
        
        m_translator.init();
        m_translator.start();
        
    }

    protected void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_translator.stop();
        sleep(200);
        MockLogAppender.assertNoWarningsOrGreater();
        DataSourceFactory.setInstance(null);
        m_db.drop();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
        super.tearDown();
    }
    

    private void createAnticipators() {
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
    }

    private void createMockDb() throws Exception {
        m_db = new MockDatabase();
        m_db.populate(m_network);
        DataSourceFactory.setInstance(m_db);
    }

    private void createMockNetwork() {
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addNode(3, "Firewall");
        m_network.addInterface("192.168.1.4");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.5");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addNode(100, "localhost");
        m_network.addInterface("127.0.0.1");
        m_network.addService("PSV");
        m_network.addService("PSV2");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
    
    
    public void testSubElementString() throws Exception {
    	m_passiveStatusConfiguration = getSqlSubValueString();
    	tearDown();
    	setUp();
    	testTranslateEvent();
        
    }
    
    public void testSubElementLong() throws Exception {
    	m_passiveStatusConfiguration = getSqlSubValueLong();
    	tearDown();
    	setUp();
    	testTranslateEvent();
    }
    
    
    public void testIsTranslationEvent() throws Exception {
        // test non matching uei match fails
        Event pse = createTestEvent("someOtherUei", "Router", "192.168.1.1", "ICMP", "Down");
        assertFalse(m_config.isTranslationEvent(pse));
        
        // test matchin uei succeeds
        Event te = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        assertTrue(m_config.isTranslationEvent(te));
        
        // test null parms fails
        Event teWithNullParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNullParms.setParms(null);
        assertFalse(m_config.isTranslationEvent(teWithNullParms));
        
        // test empty  parm list fails
        Event teWithNoParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        Parms parms = teWithNoParms.getParms();
        parms.removeAllParm();
        assertFalse(m_config.isTranslationEvent(teWithNoParms));

        // test missing a parm fails
        Event teWithWrongParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        Parms p = teWithWrongParms.getParms();
        p.getParm(2).setParmName("unmatching"); // change the name for the third parm so it fails to match
        assertFalse(m_config.isTranslationEvent(teWithWrongParms));

        // that a matching parm value succeeds
        Event te2 = createTestEvent("translationTest", "Router", "xxx192.168.1.1xxx", "ICMP", "Down");
        assertTrue(m_config.isTranslationEvent(te2));
        
        // that a matching parm value succeeds
        Event te3 = createTestEvent("translationTest", "Router", "xxx192.168.1.2", "ICMP", "Down");
        assertFalse(m_config.isTranslationEvent(te3));
    }
    
    public void testTranslateEvent() throws MarshalException, ValidationException {
    	
    		//printNodeInfo();

    		// test non matching uei match fails
        Event pse = createTestEvent("someOtherUei", "Router", "192.168.1.1", "ICMP", "Down");
        assertTrue(m_config.translateEvent(pse).isEmpty());
        
        // test matchin uei succeeds
        Event te = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "This node is way Down!");
        List translatedEvents = m_config.translateEvent(te);
		assertNotNull(translatedEvents);
		assertEquals(1, translatedEvents.size());
        validateTranslatedEvent((Event)translatedEvents.get(0));

        // test null parms fails
        Event teWithNullParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNullParms.setParms(null);
        assertTrue(m_config.translateEvent(teWithNullParms).isEmpty());
        
        // test empty  parm list fails
        Event teWithNoParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        Parms parms = teWithNoParms.getParms();
        parms.removeAllParm();
        assertTrue(m_config.translateEvent(teWithNoParms).isEmpty());

        // test missing a parm fails
        Event teWithWrongParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        Parms p = teWithWrongParms.getParms();
        p.getParm(2).setParmName("unmatching"); // change the name for the third parm so it fails to match
        assertTrue(m_config.translateEvent(teWithWrongParms).isEmpty());

        // that a matching parm value succeeds
        Event te2 = createTestEvent("translationTest", "Router", "xxx192.168.1.1xxx", "ICMP", "Down");
        assertNotNull(m_config.translateEvent(te2));
		assertEquals(1, translatedEvents.size());
        validateTranslatedEvent((Event)translatedEvents.get(0));
        
        // that a matching parm value succeeds
        Event te3 = createTestEvent("translationTest", "Router", "xxx192.168.1.2", "ICMP", "Down");
        assertTrue(m_config.translateEvent(te3).isEmpty());
    }

//	private void printNodeInfo() {
//		RowProcessor rp = new RowProcessor() {
//			public void processRow(ResultSet rs) throws SQLException {
//				System.err.println("nodeid: "+rs.getString("nodeid")+", nodeLabel: "+rs.getString("nodeLabel")+" ipaddr: "+rs.getString("ipaddr"));
//			}
//		};
//		
//		Querier q = new Querier(m_db, "select node.nodeid as nodeid, node.nodeLabel as nodeLabel, ipinterface.ipaddr as ipaddr from node, ipinterface where node.nodeid = ipinterface.nodeid and node.nodeLabel = 'Router' and ipinterface.ipaddr = '192.168.1.1' and ipinterface.isManaged != 'D' ", rp);
//		q.execute();
//	}

	private void validateTranslatedEvent(Event event) {
		assertEquals(m_translator.getName(), event.getSource());
		assertEquals(3L, event.getNodeid());
		assertEquals("www.opennms.org", event.getHost());
        assertEquals("a generated event", event.getDescr());
        assertEquals("192.168.1.1", event.getInterface());
        assertEquals("Switch", EventUtils.getParm(event, "nodeLabel"));
        assertEquals("PSV", event.getService());
        assertEquals("Down", EventUtils.getParm(event, "passiveStatus"));
	}
    
    public void testUEIList() {
    		List ueis = m_config.getUEIList();
    		assertEquals(1, ueis.size());
    		assertTrue(ueis.contains("uei.opennms.org/services/translationTest"));
    }

    private Event createTestEvent(String type, String nodeLabel, String ipAddr, String serviceName, String status) {
		Parms parms = new Parms();

        if(nodeLabel != null) parms.addParm(buildParm(EventConstants.PARM_PASSIVE_NODE_LABEL, nodeLabel));
        if(ipAddr != null) parms.addParm(buildParm(EventConstants.PARM_PASSIVE_IPADDR, ipAddr));
        if(serviceName != null) parms.addParm(buildParm(EventConstants.PARM_PASSIVE_SERVICE_NAME, serviceName));
        if(status != null) parms.addParm(buildParm(EventConstants.PARM_PASSIVE_SERVICE_STATUS, status));

		return createEventWithParms("uei.opennms.org/services/"+type, parms);
	}

    private Event createEventWithParms(String uei, Parms parms) {
		Event e = MockEventUtil.createEvent("Automation", uei);
		e.setHost("localhost");
        
        e.setParms(parms);
        Logmsg logmsg = new Logmsg();
        logmsg.setContent("Testing Passive Status Keeper with down status");
        e.setLogmsg(logmsg);
        return e;
	}
    
    
    
    private Parm buildParm(String parmName, String parmValue) {
        Value v = new Value();
        v.setContent(parmValue);
        Parm p = new Parm();
        p.setParmName(parmName);
        p.setValue(v);
        return p;
    }
    
    
    private String getSqlSubValueLong() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D' and to_number(?, '999999') = 9999 \" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "				<value type=\"field\" name=\"nodeid\" result=\"9999\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</event-translator-configuration>\n" + 
        "";
    }
    private String getSqlSubValueString() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D' and ? = 'test' \" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "				<value type=\"field\" name=\"host\" result=\"test\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</event-translator-configuration>\n" + 
        "";
    }
    
    
    
    private String getStandardConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</event-translator-configuration>\n" + 
        "";
    }
    
}
