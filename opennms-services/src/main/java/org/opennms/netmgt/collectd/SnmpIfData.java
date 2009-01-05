/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SnmpIfData {

    private int m_nodeId;
    private CollectionType m_collectionType;
    private int m_ifIndex;
    private int m_ifType;
    private String m_rrdLabel;
    private String m_ifAlias;

    public SnmpIfData(OnmsSnmpInterface snmpIface) {
        m_nodeId = nullSafeUnbox(snmpIface.getNode().getId(), -1);
        m_collectionType = snmpIface.getCollectionType();
        m_ifIndex = nullSafeUnbox(snmpIface.getIfIndex(), -1);
        m_ifType = nullSafeUnbox(snmpIface.getIfType(), -1);
        m_rrdLabel = snmpIface.computeLabelForRRD();
        m_ifAlias = snmpIface.getIfAlias();
    }
    
    int nullSafeUnbox(Integer num, int dflt) {
        return (num == null ? dflt : num.intValue());
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public CollectionType getCollectionType() {
        return m_collectionType;
    }

    public int getIfIndex() {
        return m_ifIndex;
    }

    public int getIfType() {
        return m_ifType;
    }

    public String getLabelForRRD() {
        return m_rrdLabel;
    }

    public String getIfAlias() {
        return m_ifAlias;
    }

}
