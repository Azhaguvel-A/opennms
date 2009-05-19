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
// 2006 Aug 15: Formatting. - dj@opennms.org
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

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * <P>
 * The SNMPCollectorEntry class is designed to hold all SNMP collected data
 * pertaining to a particular interface.
 * </P>
 * 
 * <P>
 * An instance of this class is created by calling the constructor and passing a
 * list of SnmpVarBind objects from an SNMP PDU response. This class extends
 * java.util.TreeMap which is used to store each of the collected data points
 * indexed by object identifier.
 * </P>
 * 
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 */
public final class SNMPCollectorEntry extends AbstractSnmpStore {
    /**
     * The list of MIBObjects that will used for associating the the data within
     * the map.
     */
    private Collection<SnmpAttributeType> m_attrList;
    private SnmpCollectionSet m_collectionSet;

    public SNMPCollectorEntry(Collection<SnmpAttributeType> attrList, SnmpCollectionSet collectionSet) {
        if (attrList == null) {
            throw new NullPointerException("attrList is null!");
        }
        m_attrList = attrList;
        m_collectionSet = collectionSet;
    }


    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    private List<SnmpAttributeType> findAttributeTypeForOid(SnmpObjId base, SnmpInstId inst) {
        List<SnmpAttributeType> matching = new LinkedList<SnmpAttributeType>();
        for (SnmpAttributeType attrType : m_attrList) {
            if (attrType.matches(base, inst)) {
                matching.add(attrType);
            }
        }
        return matching;
    }


    public void storeResult(SnmpResult res) {
        String key = res.getAbsoluteInstance().toString();
        putValue(key, res.getValue());
        List<SnmpAttributeType> attrTypes = findAttributeTypeForOid(res.getBase(), res.getInstance());
        if (attrTypes.isEmpty()) {
        	throw new IllegalArgumentException("Received result for unexpected oid ["+res.getBase()+"].["+res.getInstance()+"]");
        }
        
        for (SnmpAttributeType attrType : attrTypes) {
            if (attrType.getInstance().equals(MibObject.INSTANCE_IFINDEX)) {
                putIfIndex(res.getInstance().toInt());
            }
            attrType.storeResult(m_collectionSet, this, res);
            log().debug("storeResult: added value for "+attrType.getAlias()+": " + res.toString());
        }
    }


    String getValueForBase(String baseOid) {
    
        String instance = String.valueOf(getIfIndex()); 
        if (instance == null || instance.equals("")) {
            return null;
        }
        
    
        String fullOid = baseOid + "." + instance;
    
        String snmpVar = getDisplayString(fullOid);
        if (snmpVar == null) {
            return null;
        }
    
        snmpVar.trim();
    
        if (snmpVar.equals("")) {
            return null;
        }
    
        return snmpVar;
    
    }
}
