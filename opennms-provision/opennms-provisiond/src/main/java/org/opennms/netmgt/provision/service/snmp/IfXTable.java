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
// IfXTable.java,v 1.1.1.1 2001/11/11 17:34:36 ben Exp
//

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * The IfXTable uses a SnmpSession to collect the entries in the remote agent's
 * interface extensions table. It implements the SnmpHandler to receive
 * notifications and handle errors associated with the data collection. Data is
 * collected using a series of GETNEXT PDU request to walk multiple parts of the
 * interface table at once. The number of SNMP packets should not exceed the
 * number of interface + 1, assuming no lost packets or error conditions occur.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc2233.txt">RFC2233 </A>
 */
public final class IfXTable extends SnmpTable<IfXTableEntry> {

    /**
     * <P>
     * Constructs an IfXTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param address TODO
     * @see IfXTableEntry
     */
    public IfXTable(InetAddress address) {
        this(address, null);
    }

    public IfXTable(InetAddress address, Set<SnmpInstId> ifIndices) {
        super(address, "ifXTable", IfXTableEntry.ms_elemList, ifIndices);
    }

    protected IfXTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfXTableEntry(inst.toInt());
    }
    
    public String getIfName(int ifIndex) {
        return getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfName();
    }

    public String getIfAlias(int ifIndex) {
        return getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfAlias();
    }

    public void updateSnmpInterfaceData(OnmsNode node) {
        for(IfXTableEntry entry : getEntries()) {
            updateSnmpInterfaceData(node, entry.getIfIndex());
        }
    }
    /**
     * @param node
     * @param ifIndex
     */
    public void updateSnmpInterfaceData(OnmsNode node, Integer ifIndex) {
        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf2 = node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf2 == null) {
            // if not then create one
            snmpIf2 = new OnmsSnmpInterface(null, ifIndex, node);
        }
        // ifXTable Attributes
        snmpIf2.setIfAlias(getIfAlias(ifIndex));
        snmpIf2.setIfName(getIfName(ifIndex));
    }

}
