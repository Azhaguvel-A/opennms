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
// 2006 Aug 15: Use generics for collections, add a log message. - dj@opennms.org
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

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.netmgt.snmp.SnmpInstId;

public class IfResourceType extends ResourceType {

    private TreeMap<Integer, IfInfo> m_ifMap;

    public IfResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_ifMap = new TreeMap<Integer, IfInfo>();
        addKnownIfResources();
    }
    
    private Map<Integer, IfInfo> getIfMap() {
        return m_ifMap;
    }

    void addIfInfo(IfInfo ifInfo) {
        getIfMap().put(new Integer(ifInfo.getIndex()), ifInfo);
    }

    void addKnownIfResources() {
    	Set<IfInfo> ifInfos = getAgent().getSnmpInterfaceInfo(this);
        
        for(IfInfo ifInfo : ifInfos) {
            addIfInfo(ifInfo);
        }
    }

    IfInfo getIfInfo(int ifIndex) {
        return getIfMap().get(new Integer(ifIndex));
    }

    public Collection<IfInfo> getIfInfos() {
        return getIfMap().values();
    }

    public SnmpCollectionResource findResource(SnmpInstId inst) {
        return getIfInfo(inst.toInt());
    }

    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
        log().debug("findAliasedResource: Should not get called from IfResourceType");
        return null;
    }

    public Collection<IfInfo> getResources() {
        return m_ifMap.values();
    }

    @Override
    protected Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getIndexedAttributeTypesForResourceType(getAgent(), this);
    }
    
    
}
