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
// Modifications:
//
// 2008 Jan 26: Use Spring to setup objects. - dj@opennms.org
// 2008 Jan 08: Dependency inject EventConfDao. - dj@opennms.org
// 2007 Dec 25: Use the new EventConfigurationManager.loadConfiguration(File). - dj@opennms.org
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
package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.PropertySettingTestSuite;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class TrapHandlerTest extends AbstractDependencyInjectionSpringContextTests {
    public static TestSuite suite() {
        Class testClass = TrapHandlerTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }

    private Trapd m_trapd = null;

    private EventAnticipator m_anticipator;

    private MockEventIpcManager m_eventMgr;

    private InetAddress m_localhost = null;

    private int m_snmpTrapPort = 10000;

    private boolean m_doStop = false;

    private static final String m_ip = "127.0.0.1";
    
    private static final long m_nodeId = 1;

    private MockTrapdIpMgr m_trapdIpMgr;

    private TrapQueueProcessor m_processor;
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/applicationContext-trapDaemon.xml",
                "classpath:org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml",
        };
    }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        super.setDirty();
        
        MockLogAppender.setupLogging();

        m_anticipator = new EventAnticipator();
        m_eventMgr.setEventAnticipator(m_anticipator);

        m_localhost = InetAddress.getByName(m_ip);
        
        m_trapdIpMgr.clearKnownIpsMap();
        m_trapdIpMgr.setNodeId(m_ip, m_nodeId);

        m_trapd.start();
        m_doStop = true;
    }

    public void finishUp() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        m_eventMgr.finishProcessingEvents();
        m_anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Override
    public void onTearDown() throws Exception {
        if (m_trapd != null && m_doStop) {
            m_trapd.stop();
            m_trapd = null;
        }
        
        super.onTearDown();
    }

    public void testV1TrapNoNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v1",
                null, 6, 1);
    }

    public void testV2TrapNoNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    public void testV1TrapNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                "v1", null, 6, 1);
    }

    public void testV2TrapNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v1", ".1.3.6.1.2.1.15.7", 6, 1);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMatch()
    throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v2c", ".1.3.6.1.2.1.15.7", 6, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbinds()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.4.2404", valueFactory.getInt32(3));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.5.2404", valueFactory.getInt32(2));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.6.2404", valueFactory.getInt32(5));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.3.0.2404", valueFactory.getOctetString("http://a.b.c.d/cgi/fDetail?index=2404".getBytes()));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/HP/traps/hpicfFaultFinderTrap",
                "v1", ".1.3.6.1.4.1.11.2.14.12.1", 6, 5, varbinds);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificAndMatchWithVarbinds()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.4.2404", valueFactory.getInt32(3));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.5.2404", valueFactory.getInt32(2));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.6.2404", valueFactory.getInt32(5));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.3.0.2404", valueFactory.getOctetString("http://a.b.c.d/cgi/fDetail?index=2404".getBytes()));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/HP/traps/hpicfFaultFinderTrap",
                "v2c", ".1.3.6.1.4.1.11.2.14.12.1", 6, 5, varbinds);
    }


    // FIXME: these exist to provide testing for the new Textual Convention feature
    // See EventConfDataTest for the other part of this testing
    public void FIXMEtestV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50}));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);
    }

    // FIXME: these exist to provide testing for the new Textual Convention feature
    public void FIXMEtestV2EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        byte[] macAddr = new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50};

        String encoded = new String(Base64.encodeBase64(macAddr));
        byte[] decodeBytes = Base64.decodeBase64(encoded.toCharArray());

        assertByteArrayEquals(macAddr, decodeBytes);

        // XXX: this is a problem.. putting the bytes into a string and taking them
        // back out.. does not produce the same results
        String decoded = new String(macAddr);
        byte[] roundTripMacAddr = decoded.getBytes();

        assertByteArrayEquals(macAddr, roundTripMacAddr);


        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(macAddr));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v2c", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);
    }

    private void assertByteArrayEquals(byte[] macAddr, byte[] bytes) {
        assertEquals("expect length: "+macAddr.length, macAddr.length, bytes.length);
        for (int i = 0; i < macAddr.length; i++) {
            assertEquals("Expected byte "+i+" to match", macAddr[i], bytes[i]);
        }
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMatchWithZero()
    throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v2c", ".1.3.6.1.2.1.15.7.0", 6, 1);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v2c",
                ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                ".1.3.6.1.2.1.15.7", 5, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                ".1.3.6.1.2.1.15.7", 6, 50);
    }

    public void testV1GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0);
    }

    public void testV2GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }

    public void testV1TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV2TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV1TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                "v1", null, 6, 1);
    }

    public void testV2TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    public void testNodeGainedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        m_processor.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    public void testInterfaceReparentedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        m_processor.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    public void testInterfaceDeletedModifiesIpMgr() throws Exception {
        long nodeId = 0;
        m_processor.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.INTERFACE_DELETED_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        anticipateEvent("uei.opennms.org/internal/discovery/newSuspect",
                m_ip, nodeId);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    public Event anticipateEvent(String uei) {
        return anticipateEvent(uei, m_ip, m_nodeId);
    }

    public Event anticipateEvent(String uei, String ip, long nodeId) {
        Event event = new Event();
        event.setInterface(ip);
        event.setNodeid(nodeId);
        event.setUei(uei);
        m_anticipator.anticipateEvent(event);
        return event;
    }

    public void anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
            String event,
            String version, String enterprise,
            int generic, int specific) throws Exception {
        m_processor.setNewSuspect(newSuspectOnTrap);

        if (newSuspectOnTrap) {
            // Note: the nodeId will be zero because the node is not known
            anticipateEvent("uei.opennms.org/internal/discovery/newSuspect",
                    m_ip, 0);
        }

        if (event != null) {
            if (nodeKnown) {
                anticipateEvent(event);
            } else {
                /*
                 * If the node is unknown, the nodeId on the trap event
                 * will be zero.
                 */
                anticipateEvent(event, m_ip, 0);
            }
        }

        sendTrap(version, enterprise, generic, specific);

        finishUp();
    }


    public void anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
            String event,
            String version, String enterprise,
            int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        m_processor.setNewSuspect(newSuspectOnTrap);

        if (newSuspectOnTrap) {
            // Note: the nodeId will be zero because the node is not known
            anticipateEvent("uei.opennms.org/internal/discovery/newSuspect",
                    m_ip, 0);
        }

        if (event != null) {
            if (nodeKnown) {
                anticipateEvent(event);
            } else {
                /*
                 * If the node is unknown, the nodeId on the trap event
                 * will be zero.
                 */
                anticipateEvent(event, m_ip, 0);
            }
        }

        sendTrap(version, enterprise, generic, specific, varbinds);

        finishUp();
    }

    public void sendTrap(String version, String enterprise, int generic,
            int specific) throws Exception {
        if (enterprise == null) {
            enterprise = ".0.0";
        }

        if (version.equals("v1")) {
            sendV1Trap(enterprise, generic, specific);
        } else if (version.equals("v2c")) {
            sendV2Trap(enterprise, specific);
        } else {
            throw new Exception("unsupported SNMP version for test: "
                    + version);
        }
    }

    private void sendTrap(String version, String enterprise, int generic, 
            int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        if (enterprise == null) {
            enterprise = ".0.0";
        }

        if (version.equals("v1")) {
            sendV1Trap(enterprise, generic, specific, varbinds);
        } else if (version.equals("v2c")) {
            sendV2Trap(enterprise, specific, varbinds);
        } else {
            throw new Exception("unsupported SNMP version for test: "
                    + version);
        }
    }

    public void sendV1Trap(String enterprise, int generic, int specific)
    throws Exception {
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(enterprise));
        pdu.setGeneric(generic);
        pdu.setSpecific(specific);
        pdu.setTimeStamp(0);
        pdu.setAgentAddress(m_localhost);

        pdu.send(m_localhost.getHostAddress(), m_snmpTrapPort, "public");
    }

    public void sendV1Trap(String enterprise, int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds)
    throws Exception {
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(enterprise));
        pdu.setGeneric(generic);
        pdu.setSpecific(specific);
        pdu.setTimeStamp(0);
        pdu.setAgentAddress(m_localhost);
        Iterator it = varbinds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            pdu.addVarBind(SnmpObjId.get((String) pairs.getKey()), (SnmpValue) pairs.getValue());
        }
        pdu.send(m_localhost.getHostAddress(), m_snmpTrapPort, "public");
    }


    public void sendV2Trap(String enterprise, int specific) throws Exception {
        SnmpObjId enterpriseId = SnmpObjId.get(enterprise);
        boolean isGeneric = false;
        SnmpObjId trapOID;
        if (SnmpObjId.get(".1.3.6.1.6.3.1.1.5").isPrefixOf(enterpriseId)) {
            isGeneric = true;
            trapOID = enterpriseId;
        } else {
            trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(specific));
            // XXX or should it be this
            // trap OID = enterprise + ".0." + specific;
        }

        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"),
                SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(trapOID));
        if (isGeneric) {
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"),
                    SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        }

        pdu.send(m_localhost.getHostAddress(), m_snmpTrapPort, "public");
    }

    public void sendV2Trap(String enterprise, int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        SnmpObjId enterpriseId = SnmpObjId.get(enterprise);
        boolean isGeneric = false;
        SnmpObjId trapOID;
        if (SnmpObjId.get(".1.3.6.1.6.3.1.1.5").isPrefixOf(enterpriseId)) {
            isGeneric = true;
            trapOID = enterpriseId;
        } else {
            trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(specific));
            // XXX or should it be this
            // trap OID = enterprise + ".0." + specific;
        }

        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"),
                SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(trapOID));
        if (isGeneric) {
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"),
                    SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        }
        for (Map.Entry<String, SnmpValue> entry : varbinds.entrySet()) {
            pdu.addVarBind(SnmpObjId.get(entry.getKey()), entry.getValue());
        }

        pdu.send(m_localhost.getHostAddress(), m_snmpTrapPort, "public");
    }

    public Trapd getDaemon() {
        return m_trapd;
    }

    public void setDaemon(Trapd trapd) {
        m_trapd = trapd;
    }

    public MockTrapdIpMgr getTrapdIpMgr() {
        return m_trapdIpMgr;
    }

    public void setTrapdIpMgr(MockTrapdIpMgr trapdIpMgr) {
        m_trapdIpMgr = trapdIpMgr;
    }

    public TrapQueueProcessor getProcessor() {
        return m_processor;
    }

    public void setProcessor(TrapQueueProcessor processor) {
        m_processor = processor;
    }

    public MockEventIpcManager getEventMgr() {
        return m_eventMgr;
    }

    public void setEventMgr(MockEventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
}
