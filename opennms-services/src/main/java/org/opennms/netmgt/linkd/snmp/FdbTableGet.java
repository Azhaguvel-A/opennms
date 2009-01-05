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

package org.opennms.netmgt.linkd.snmp;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and colletion occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * 
 */

public final class FdbTableGet {

	private final static String FDB_PORT_OID = ".1.3.6.1.2.1.17.4.3.1.2";

	private final static String FDB_STATUS_OID = ".1.3.6.1.2.1.17.4.3.1.3";

	private final static String QFDB_PORT_OID = ".1.3.6.1.2.1.17.7.1.2.2.1.2";

	private final static String QFDB_STATUS_OID = ".1.3.6.1.2.1.17.7.1.2.2.1.3";
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	private String m_mac;

	public FdbTableGet(SnmpAgentConfig config, String mac) {
		m_agentConfig = config;
		m_mac = getInstanceString(mac);
	}


	public int getBridgePort() {
		
		Category log = ThreadCategory.getInstance(getClass());
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(FDB_PORT_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		if (log.isDebugEnabled())
			log.debug("getBridgePort: mac/bridgeport: " + m_mac +"/" + val.toDisplayString());

		return -1;
	}

	public int getQBridgePort() {
		
		Category log = ThreadCategory.getInstance(getClass());
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(QFDB_PORT_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		if (log.isDebugEnabled())
			log.debug("getQBridgePort: mac/bridgeport: " + m_mac +"/" + val.toDisplayString());

		return -1;
	}

	public int getBridgePortStatus() {
		Category log = ThreadCategory.getInstance(getClass());
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(FDB_STATUS_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		if (log.isDebugEnabled())
			log.debug("getBridgePortStatus: mac/bridgeport: " + m_mac +"/" + val.toDisplayString());
		return -1;
		
	}

	public int getQBridgePortStatus() {
		Category log = ThreadCategory.getInstance(getClass());
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(QFDB_STATUS_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		if (log.isDebugEnabled())
			log.debug("getQBridgePortStatus: mac/bridgeport: " + m_mac +"/" + val.toDisplayString());
		return -1;
		
	}

	private SnmpObjId getOid(String oid) {
		return SnmpObjId.get(oid+"."+m_mac);
	}
	
	private String getInstanceString(String mac) {
		
		return 		Integer.parseInt(mac.substring(0, 2),16) + "." +
					Integer.parseInt(mac.substring(2, 4),16) + "." +
					Integer.parseInt(mac.substring(4, 6),16) + "." +
					Integer.parseInt(mac.substring(6, 8),16) + "." +
					Integer.parseInt(mac.substring(8, 10),16)+ "." +
					Integer.parseInt(mac.substring(10, 12),16);

	}

}
