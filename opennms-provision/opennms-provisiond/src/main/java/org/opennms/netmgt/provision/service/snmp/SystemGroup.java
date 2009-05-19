//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.provision.service.snmp;


import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.operations.ScanResource;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * <P>
 * SystemGroup holds the system group properties It implements the SnmpHandler
 * to receive notifications when a reply is received/error occurs in the
 * SnmpSession used to send requests/receive replies.
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class SystemGroup extends AggregateTracker {

    //
    // Lookup strings for specific table entries
    //
    public final static String SYS_OBJECTID_ALIAS = "sysObjectID";
    private static final String SYS_OBJECTID = ".1.3.6.1.2.1.1.2";

    public final static String SYS_UPTIME_ALIAS = "sysUptime";
    private static final String SYS_UPTIME = ".1.3.6.1.2.1.1.3";

    public final static String SYS_NAME_ALIAS = "sysName";
    private static final String SYS_NAME = ".1.3.6.1.2.1.1.5";

    public final static String SYS_DESCR_ALIAS = "sysDescr";
    private static final String SYS_DESCR = ".1.3.6.1.2.1.1.1";

    public final static String SYS_LOCATION_ALIAS = "sysLocation";
    private static final String SYS_LOCATION = ".1.3.6.1.2.1.1.6";

    public final static String SYS_CONTACT_ALIAS = "sysContact";
    private static final String SYS_CONTACT = ".1.3.6.1.2.1.1.4";
    
    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    public static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        // Changed array size from 7 to 6 because we are no longer going after
        // sysServices...sysServices is not currently being used and it causes
        // the entire SystemGroup collection to fail on at least one version
        // of Linux where it does not exist in the SNMP agent.
        //
        ms_elemList = new NamedSnmpVar[6];
        int ndx = 0;

        /**
         * <P>
         * A description of the remote entity. For example this may include
         * hardware, operating system, and various version information. This
         * should be a US-ASCII display string.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_DESCR_ALIAS, SYS_DESCR);

        /**
         * <P>
         * The vendor's authoritative identification of the network management
         * subsystem. This can often be used to identify the vendor, and often
         * times the specific vendor's hardware platform.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, SYS_OBJECTID_ALIAS, SYS_OBJECTID);

        /**
         * <P>
         * The time since the network management portion of the system was last
         * initialized. This will be in 1/100th of a second increments.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, SYS_UPTIME_ALIAS, SYS_UPTIME);

        /**
         * <P>
         * The identification and contact information for the person that is
         * managing this node. While the contact information is often used to
         * store contact information about the person managing the node, it is a
         * free form US-ASCII field that may contain additional information
         * depending on the environment.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_CONTACT_ALIAS, SYS_CONTACT);

        /**
         * <P>
         * The administratively assigned name for this particular node. This may
         * often be the same as the hostname, but it can differ depending on the
         * site's implementation.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_NAME_ALIAS, SYS_NAME);

        /**
         * <P>
         * The physical location of the node. This field, like many others, is a
         * free formed US-ASCII field that can contain any type of location
         * string. Some sites might actually use a special encoding that
         * designates the state, city, building, floor, and room that contains
         * the equipment.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_LOCATION_ALIAS, SYS_LOCATION);

        /**
         * <P>
         * A value that indicates the set of services that this entity provides.
         * This is a bit encode integer that allows the management entity to
         * determine if the agent supports the following standards
         * </P>
         * 
         * <UL>
         * <LI>physical (e.g. repeaters)</LI>
         * <LI>datalink/subnetwork (e.g. bridges)</LI>
         * <LI>internet (e.g. routers)</LI>
         * <LI>end-to-end (e.g. IP hosts)</LI>
         * <LI>applications (e.g. mail relays)</LI>
         * </UL>
         * 
         * <P>
         * To get more information about the encoding see Page 123 of "SNMP,
         * SNMPv2, SNMPv3 and RMON 1 and 2 3rd Ed." by William Stallings [ISBN
         * 0-201-48534-6]
         * </P>
         */
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
        // "sysServices", ".1.3.6.1.2.1.1.7");
    }

    /**
     * <P>
     * The SYSTEM_OID is the object identifier that represents the root of the
     * system information in the MIB forest. Each of the system elements can be
     * retrieved by adding their specific index to the string, and an additional
     * Zero(0) to signify the single instance item.
     * </P>
     */
    public static final String SYSTEM_OID = ".1.3.6.1.2.1.1";

    private final SnmpStore m_store;
    private final InetAddress m_address;
    
    /**
     * <P>
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</em> using the notifyAll() method.
     * </P>
     * @param address TODO
     * 
     */
    public SystemGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList); 
    }
    
    public String getSysName() {
        return m_store.getDisplayString(SYS_NAME);
    }

    public String getSysObjectID() {
        return m_store.getObjectID(SYS_OBJECTID);
    }

    public String getSysDescr() {
        return m_store.getDisplayString(SYS_DESCR);
    }

    public String getSysLocation() {
        return m_store.getDisplayString(SYS_LOCATION);
    }

    public String getSysContact() {
        return m_store.getDisplayString(SYS_CONTACT);
    }

    @Override
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    @Override
    protected void reportGenErr(String msg) {
        log().warn("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    @Override
    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * @param sr
     */
    public void updateSnmpDataForResource(ScanResource sr) {
        if (!failed()) {
            sr.setAttribute("sysContact", getSysContact());
            sr.setAttribute("sysDescription", getSysDescr());
            sr.setAttribute("sysLocation", getSysLocation());
            sr.setAttribute("sysObjectId", getSysObjectID());
        }
    }

    /**
     * @param node
     */
    public void updateSnmpDataForNode(OnmsNode node) {
        ScanResource sr = new ScanResource("SNMP");
        sr.setNode(node);
        updateSnmpDataForResource(sr);
    }

}
