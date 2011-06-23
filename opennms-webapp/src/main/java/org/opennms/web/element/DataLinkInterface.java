/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.element;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.linkd.DbDataLinkInterfaceEntry;

/**
 * <p>DataLinkInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class DataLinkInterface
{
        private int     m_nodeId;
        private int     m_nodeparentid;
        private int     m_ifindex;
        private int     m_parentifindex;
        private String  m_ipaddress;
        private String  m_parentipaddress;
        private final String  m_lastPollTime;
        private final char    m_status;

        private static final Map<Character, String> statusMap = new HashMap<Character, String>();

        static {
            statusMap.put( DbDataLinkInterfaceEntry.STATUS_ACTIVE, "Active" );
            statusMap.put( DbDataLinkInterfaceEntry.STATUS_UNKNOWN, "Unknown" );
            statusMap.put( DbDataLinkInterfaceEntry.STATUS_DELETED, "Deleted" );
            statusMap.put( DbDataLinkInterfaceEntry.STATUS_NOT_POLLED, "Not Active" );
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        DataLinkInterface(   int nodeId,
                int nodeparentid,
				int ifindex,
				int parentifindex,
				String ipaddress,
				String parentipaddress,
                String lastPollTime,
                char status)
        {
                m_nodeId = nodeId;
                m_nodeparentid = nodeparentid;
				m_ifindex = ifindex;
				m_parentifindex = parentifindex;
			    m_ipaddress = ipaddress; 
				m_parentipaddress = parentipaddress;
			    m_lastPollTime = lastPollTime; 
                m_status = status;
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
				str.append("IfIndex = " + m_ifindex + "\n" );
                str.append("Node Parent = " + m_nodeparentid + "\n" );
				str.append("Parent IfIndex = " + m_parentifindex + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }
		/**
		 * <p>get_ifindex</p>
		 *
		 * @return a int.
		 */
		public int get_ifindex() {
			return m_ifindex;
		}

	/**
	 * <p>get_parentifindex</p>
	 *
	 * @return a int.
	 */
	public int get_parentifindex() {
		return m_parentifindex;
	}

	/**
	 * <p>get_ipaddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String get_ipaddr() {
		return m_ipaddress;
	}

	/**
	 * <p>get_parentipaddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String get_parentipaddr() {
		return m_parentipaddress;
	}


		/**
		 * <p>get_lastPollTime</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_lastPollTime() {
			return m_lastPollTime;
		}

		/**
		 * <p>get_nodeId</p>
		 *
		 * @return a int.
		 */
		public int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * <p>get_nodeparentid</p>
		 *
		 * @return a int.
		 */
		public int get_nodeparentid() {
			return m_nodeparentid;
		}

		/**
		 * <p>get_status</p>
		 *
		 * @return a char.
		 */
		public char get_status() {
			return m_status;
		}
		
        /**
         */
        public String getStatusString() {
            return statusMap.get( new Character(m_status) );
        }
        
		/**
		 * <p>invertNodewithParent</p>
		 */
		public void invertNodewithParent() {
			int nodeid = m_nodeId;
			String ipaddr = m_ipaddress;
			int ifindex = m_ifindex;
			
			int nodeparentid = m_nodeparentid;
			String parentipaddr = m_parentipaddress;
			int parentifindex = m_parentifindex;
			
			m_nodeId = nodeparentid;
			m_ipaddress = parentipaddr;
			m_ifindex = parentifindex;
			
			m_nodeparentid = nodeid;
			m_parentipaddress = ipaddr;
			m_parentifindex = ifindex;
			
		}

}
