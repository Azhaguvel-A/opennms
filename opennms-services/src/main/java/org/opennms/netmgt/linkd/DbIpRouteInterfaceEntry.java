/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2007
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.linkd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
final class DbIpRouteInterfaceEntry {
	/**
	 * The character returned if the entry is active
	 */

	static final char STATUS_ACTIVE = 'A';

	/**
	 * The character returned if the entry is not active
	 * means last polled
	 */

	static final char STATUS_NOT_POLLED = 'N';

	/**
	 * It stats that node is deleted
	 * The character returned if the node is deleted
	 */
	static final char STATUS_DELETE = 'D';

	/**
	 * The character returned if the entry type is unset/unknown.
	 */

	static final char STATUS_UNKNOWN = 'K';

	/** 
	 * Integer representing route type
	 */
	static final int ROUTE_TYPE_OTHER = 1;

	static final int ROUTE_TYPE_INVALID = 2;

	static final int ROUTE_TYPE_DIRECT = 3;

	static final int ROUTE_TYPE_INDIRECT = 4;

	/**
	 * The node identifier
	 */

	int m_nodeId;

	/**
	 * The port index on which this route is routed
	 */

	int m_routeifindex;

	/**
	 * The route metrics
	 */

	int m_routemetric1;

	int m_routemetric2;

	int m_routemetric3;

	int m_routemetric4;

	int m_routemetric5;

	/**
	 * The route type
	 */

	int m_routetype;

	/**
	 * The routing mechanism via which this route was learned
	 */
	int m_routeproto;

	/**
	 *  The destination IP address of this route. 
	 * An entry with a value of 0.0.0.0 is considered a default route.
	 */

	String m_routedest;

	/**
	 * Indicate the mask to be logical-ANDed with the
	 * destination address before being compared to the
	 * value in the ipRouteDest field.
	 --#  routeifIndex      : The index value which uniquely identifies the
	 --#                      local interface through which the next hop of this
	 --#                      route should be reached. 
	 */
	String m_routemask;

	/**
	 * The IP address of the next hop of this route.
	 * (In the case of a route bound to an interface
	 * which is realized via a broadcast media, the value
	 * of this field is the agent's IP address on that
	 * interface.)
	 */
	String m_routenexthop;

	/**
	 * The Status of
	 * this information
	 */

	char m_status = STATUS_UNKNOWN;

	/**
	 * The Time when
	 * this information was learned
	 */

	Timestamp m_lastPollTime;

	/**
	 * the sql statement to load data from database
	 */
	private static final String SQL_LOAD_IPROUTEINTERFACE = "SELECT routeMask,routeNextHop,routeifindex,routemetric1,routemetric2,routemetric3,routemetric4,routemetric5,routetype,routeproto,status,lastpolltime FROM iprouteinterface WHERE nodeid = ? AND routeDest = ? ";

	/**
	 * True if this recored was loaded from the database.
	 * False if it's new.
	 */
	private boolean m_fromDb;

	/**
	 * The bit map used to determine which elements have
	 * changed since the record was created.
	 */
	private int m_changed;

	// Mask fields
	//
	private static final int CHANGED_MASK = 1 << 0;

	private static final int CHANGED_NXT_HOP = 1 << 1;

	private static final int CHANGED_IFINDEX = 1 << 2;

	private static final int CHANGED_METRIC1 = 1 << 3;

	private static final int CHANGED_METRIC2 = 1 << 4;

	private static final int CHANGED_METRIC3 = 1 << 5;

	private static final int CHANGED_METRIC4 = 1 << 6;

	private static final int CHANGED_METRIC5 = 1 << 7;

	private static final int CHANGED_TYPE = 1 << 8;

	private static final int CHANGED_PROTO = 1 << 9;

	private static final int CHANGED_STATUS = 1 << 10;

	private static final int CHANGED_POLLTIME = 1 << 11;

	/**
	 * Inserts the new row into the IpRouteInterface table
	 * of the OpenNMS databasee.
	 *
	 * @param c	The connection to the database.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private void insert(Connection c) throws SQLException {
		if (m_fromDb)
			throw new IllegalStateException(
					"The record already exists in the database");

		Category log = ThreadCategory.getInstance(getClass());

		// first extract the next node identifier
		//
		StringBuffer names = new StringBuffer(
				"INSERT INTO IpRouteInterface (nodeid,routeDest");
		StringBuffer values = new StringBuffer("?,?");

		if ((m_changed & CHANGED_MASK) == CHANGED_MASK) {
			values.append(",?");
			names.append(",routeMask");
		}

		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP) {
			values.append(",?");
			names.append(",routeNextHop");
		}

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			values.append(",?");
			names.append(",routeifindex");
		}

		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1) {
			values.append(",?");
			names.append(",routemetric1");
		}

		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2) {
			values.append(",?");
			names.append(",routemetric2");
		}

		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3) {
			values.append(",?");
			names.append(",routemetric3");
		}

		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4) {
			values.append(",?");
			names.append(",routemetric4");
		}

		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5) {
			values.append(",?");
			names.append(",routemetric5");
		}

		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE) {
			values.append(",?");
			names.append(",routetype");
		}

		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO) {
			values.append(",?");
			names.append(",routeproto");
		}

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
			values.append(",?");
			names.append(",status");
		}

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			values.append(",?");
			names.append(",lastpolltime");
		}

		names.append(") VALUES (").append(values).append(')');

		if (log.isDebugEnabled())
			log.debug("IpRouteInterfaceEntry.insert: SQL insert statment = " + names.toString());

		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = c.prepareStatement(names.toString());

		int ndx = 1;
		stmt.setInt(ndx++, m_nodeId);
		stmt.setString(ndx++, m_routedest);

		
		if ((m_changed & CHANGED_MASK) == CHANGED_MASK)
			stmt.setString(ndx++, m_routemask);

		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP)
			stmt.setString(ndx++, m_routenexthop);

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
			stmt.setInt(ndx++, m_routeifindex);

		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1)
			stmt.setInt(ndx++, m_routemetric1);

		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2)
			stmt.setInt(ndx++, m_routemetric2);

		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3)
			stmt.setInt(ndx++, m_routemetric3);

		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4)
			stmt.setInt(ndx++, m_routemetric4);

		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5)
			stmt.setInt(ndx++, m_routemetric5);

		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
			stmt.setInt(ndx++, m_routetype);

		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO)
			stmt.setInt(ndx++, m_routeproto);

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
			stmt.setString(ndx++, new String(new char[] { m_status }));

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			stmt.setTimestamp(ndx++, m_lastPollTime);
		}
		
		// Run the insert
		//
		int rc = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("IpRouteInterfaceEntry.insert: row " + rc);
		stmt.close();

		// clear the mask and mark as backed
		// by the database
		//
		m_fromDb = true;
		m_changed = 0;
	}

	/** 
	 * Updates an existing record in the OpenNMS AtInterface table.
	 * 
	 * @param c	The connection used for the update.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private void update(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException(
					"The record does not exists in the database");

		Category log = ThreadCategory.getInstance(getClass());

		// first extract the next node identifier
		//
		StringBuffer sqlText = new StringBuffer("UPDATE IpRouteInterface SET ");

		char comma = ' ';

		if ((m_changed & CHANGED_MASK) == CHANGED_MASK) {
			sqlText.append(comma).append("routeMask = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP) {
			sqlText.append(comma).append("routeNextHop = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			sqlText.append(comma).append("routeifindex = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1) {
			sqlText.append(comma).append("routemetric1 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2) {
			sqlText.append(comma).append("routemetric2 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3) {
			sqlText.append(comma).append("routemetric3 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4) {
			sqlText.append(comma).append("routemetric4 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5) {
			sqlText.append(comma).append("routemetric5 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE) {
			sqlText.append(comma).append("routetype = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO) {
			sqlText.append(comma).append("routeproto = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
			sqlText.append(comma).append("status = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			sqlText.append(comma).append("lastpolltime = ?");
			comma = ',';
		}

		sqlText.append(" WHERE nodeid = ? AND routeDest = ? ");

		if (log.isDebugEnabled())
			log.debug("IpRouteInterfaceEntry.update: SQL insert statment = " + sqlText.toString());
		
		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = c.prepareStatement(sqlText.toString());

		int ndx = 1;

		if ((m_changed & CHANGED_MASK) == CHANGED_MASK)
			stmt.setString(ndx++, m_routemask);

		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP)
			stmt.setString(ndx++, m_routenexthop);

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
			stmt.setInt(ndx++, m_routeifindex);

		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1)
			stmt.setInt(ndx++, m_routemetric1);

		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2)
			stmt.setInt(ndx++, m_routemetric2);

		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3)
			stmt.setInt(ndx++, m_routemetric3);

		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4)
			stmt.setInt(ndx++, m_routemetric4);

		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5)
			stmt.setInt(ndx++, m_routemetric5);

		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
			stmt.setInt(ndx++, m_routetype);

		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO)
			stmt.setInt(ndx++, m_routeproto);

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
			stmt.setString(ndx++, new String(new char[] { m_status }));

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			stmt.setTimestamp(ndx++, m_lastPollTime);
		}

		stmt.setInt(ndx++, m_nodeId);
		stmt.setString(ndx++, m_routedest);

		// Run the insert
		//
		int rc = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("IpRouteInterfaceEntry.update: row " + rc);
		stmt.close();

		// clear the mask and mark as backed
		// by the database
		//
		m_changed = 0;
	}

	/**
	 * Load the current interface from the database. If the interface
	 * was modified, the modifications are lost. The nodeid
	 * and ip address must be set prior to this call.
	 *
	 * @param c	The connection used to load the data.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private boolean load(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException(
					"The record does not exists in the database");

		Category log = ThreadCategory.getInstance(getClass());

		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = null;
		stmt = c.prepareStatement(SQL_LOAD_IPROUTEINTERFACE);
		stmt.setInt(1, m_nodeId);
		stmt.setString(2, m_routedest);

		// Run the select
		//
		ResultSet rset = stmt.executeQuery();
		if (!rset.next()) {
			rset.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("IpRouteInterfaceEntry.load: no result found");
			return false;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the route netmask
		//
		m_routemask = rset.getString(ndx++);
		if (rset.wasNull())
			m_routemask = null;

		// get the next hop ip address
		//
		m_routenexthop = rset.getString(ndx++);
		if (rset.wasNull())
			m_routenexthop = null;

		// get the interface ifindex for routing info
		//
		m_routeifindex = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routeifindex = -1;
		// get the metrics
		m_routemetric1 = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routemetric1 = -1;

		m_routemetric2 = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routemetric2 = -1;

		m_routemetric3 = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routemetric3 = -1;
		
		m_routemetric4 = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routemetric4 = -1;

		m_routemetric5 = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routemetric5 = -1;
		
		m_routetype = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routetype = -1;
		
		m_routeproto = rset.getInt(ndx++);
		if (rset.wasNull())
			m_routeproto = -1;
		
		// the entry status
		//
		String str = rset.getString(ndx++);
		if (str != null && !rset.wasNull())
			m_status = str.charAt(0);
		else
			m_status = STATUS_UNKNOWN;

		m_lastPollTime = rset.getTimestamp(ndx++);

		rset.close();
		stmt.close();

		// clear the mask and mark as backed
		// by the database
		//
		if (log.isDebugEnabled())
			log.debug("IpRouteInterfaceEntry.load: result found");
		m_changed = 0;
		return true;
	}

	DbIpRouteInterfaceEntry() {
		throw new UnsupportedOperationException(
				"Default constructor not supported!");
	}

	DbIpRouteInterfaceEntry(int nodeId, String routedest, boolean exists) {
		m_nodeId = nodeId;
		m_fromDb = exists;
		m_routeifindex = -1;
		m_routemetric1 = -1;
		m_routemetric2 = -1;
		m_routemetric3 = -1;
		m_routemetric4 = -1;
		m_routemetric5 = -1;
		m_routetype = -1;
		m_routeproto = -1;
		m_routenexthop = null;
		m_routedest = routedest;
		m_routemask = null;
	}

	static DbIpRouteInterfaceEntry create(int nodeid, String routedest) {
		return new DbIpRouteInterfaceEntry(nodeid, routedest, false);
	}

	/**
	 * @return
	 */
	int get_nodeId() {
		return m_nodeId;
	}

	/**
	 * @return
	 */
	String get_routedest() {
		return m_routedest;
	}

	/**
	 * @return
	 */
	String get_routemask() {
		return m_routemask;
	}

	void set_routemask(String routemask) {
		m_routemask = routemask;
		m_changed |= CHANGED_MASK;
	}

	boolean hasRouteMaskChanged() {
		if ((m_changed & CHANGED_MASK) == CHANGED_MASK)
			return true;
		else
			return false;
	}

	boolean updateRouteMask(String routemask) {
		if (routemask != m_routemask) {
			set_routemask(routemask);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public String get_routenexthop() {
		return m_routenexthop;
	}

	void set_routenexthop(String routenexthop) {
		m_routenexthop = routenexthop;
		m_changed |= CHANGED_NXT_HOP;
	}

	boolean hasRouteNextHopChanged() {
		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP)
			return true;
		else
			return false;
	}

	boolean updateRouteNextHop(String routenexthop) {
		if (routenexthop != m_routenexthop) {
			set_routenexthop(routenexthop);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_ifindex() {
		return m_routeifindex;
	}

	void set_ifindex(int ifindex) {
		m_routeifindex = ifindex;
		m_changed |= CHANGED_IFINDEX;
	}

	boolean hasIfIndexChanged() {
		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
			return true;
		else
			return false;
	}

	boolean updateIfIndex(int ifindex) {
		if (ifindex != m_routeifindex) {
			set_ifindex(ifindex);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routemetric1() {
		return m_routemetric1;
	}

	void set_routemetric1(int routemetric) {
		m_routemetric1 = routemetric;
		m_changed |= CHANGED_METRIC1;
	}

	boolean hasRouteMetric1Changed() {
		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric1(int routemetric) {
		if (routemetric != m_routemetric1) {
			set_routemetric1(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routemetric2() {
		return m_routemetric2;
	}

	void set_routemetric2(int routemetric) {
		m_routemetric2 = routemetric;
		m_changed |= CHANGED_METRIC2;
	}

	boolean hasRouteMetric2Changed() {
		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric2(int routemetric) {
		if (routemetric != m_routemetric2) {
			set_routemetric2(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routemetric3() {
		return m_routemetric3;
	}

	void set_routemetric3(int routemetric) {
		m_routemetric3 = routemetric;
		m_changed |= CHANGED_METRIC3;
	}

	boolean hasRouteMetric3Changed() {
		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric3(int routemetric) {
		if (routemetric != m_routemetric3) {
			set_routemetric3(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routemetric4() {
		return m_routemetric4;
	}

	void set_routemetric4(int routemetric) {
		m_routemetric4 = routemetric;
		m_changed |= CHANGED_METRIC4;
	}

	boolean hasRouteMetric4Changed() {
		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric4(int routemetric) {
		if (routemetric != m_routemetric4) {
			set_routemetric4(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routemetric5() {
		return m_routemetric5;
	}

	void set_routemetric5(int routemetric) {
		m_routemetric5 = routemetric;
		m_changed |= CHANGED_METRIC5;
	}

	boolean hasRouteMetric5Changed() {
		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric5(int routemetric) {
		if (routemetric != m_routemetric5) {
			set_routemetric5(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routetype() {
		return m_routetype;
	}

	void set_routetype(int routetype) {
		if (routetype == ROUTE_TYPE_OTHER || routetype == ROUTE_TYPE_INVALID
				|| routetype == ROUTE_TYPE_DIRECT
				|| routetype == ROUTE_TYPE_INDIRECT)
			m_routetype = routetype;
		m_changed |= CHANGED_TYPE;
	}

	boolean hasRouteTypeChanged() {
		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
			return true;
		else
			return false;
	}

	boolean updateRouteType(int routetype) {
		if (routetype != m_routetype) {
			set_routetype(routetype);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_routeproto() {
		return m_routeproto;
	}

	void set_routeproto(int routeproto) {
		m_routeproto = routeproto;
		m_changed |= CHANGED_PROTO;
	}

	boolean hasRouteProtoChanged() {
		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO)
			return true;
		else
			return false;
	}

	boolean updateRouteProto(int routeproto) {
		if (routeproto != m_routeproto) {
			set_routeproto(routeproto);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	char get_status() {
		return m_status;
	}

	void set_status(char status) {
		if (status == STATUS_ACTIVE || status == STATUS_NOT_POLLED
				|| status == STATUS_DELETE)
			m_status = status;
		m_changed |= CHANGED_STATUS;
	}

	boolean hasStatusChanged() {
		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
			return true;
		else
			return false;
	}

	boolean updateStatus(char status) {
		if (status != m_status) {
			set_status(status);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	Timestamp get_lastpolltime() {
		return m_lastPollTime;
	}

	/**
	 * Gets the last poll time of the record
	 */
	String getLastPollTimeString() {
		String result = null;
		if (m_lastPollTime != null) {
			result = m_lastPollTime.toString();
		}
		return result;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	void set_lastpolltime(String time) throws ParseException {
		if (time == null) {
			m_lastPollTime = null;
		} else {
			Date tmpDate = EventConstants.parseToDate(time);
			m_lastPollTime = new Timestamp(tmpDate.getTime());
		}
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	void set_lastpolltime(Date time) {
		m_lastPollTime = new Timestamp(time.getTime());
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	void set_lastpolltime(Timestamp time) {
		m_lastPollTime = time;
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Updates the interface information in the configured database. If the 
	 * interface does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as 
	 * needed based upon the current changes to the node.
	 */
	void store() throws SQLException {
		if (m_changed != 0 || m_fromDb == false) {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				store(db);
				if (db.getAutoCommit() == false)
					db.commit();
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
					ThreadCategory.getInstance(getClass()).warn(
							"Exception closing JDBC connection", e);
				}
			}
		}
		return;
	}

	/**
	 * Updates the interface information in the configured database. If the 
	 * atinterface does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as 
	 * needed based upon the current changes to the node.
	 *
	 * @param db	The database connection used to write the record.
	 */
	void store(Connection db) throws SQLException {
		if (m_changed != 0 || m_fromDb == false) {
			if (m_fromDb)
				update(db);
			else
				insert(db);
		}
	}

	/**
	 * Retreives a current record from the database based upon the
	 * key fields of <em>nodeID</em> and <em>ipaddr</em>. If the
	 * record cannot be found then a null reference is returnd.
	 *
	 * @param nid	The node id key
	 * @param ipaddr The ip address
	 *
	 * @return The loaded entry or null if one could not be found.
	 *
	 */
	static DbIpRouteInterfaceEntry get(int nid, String routedest)
			throws SQLException {
		Connection db = null;
		try {
			db = DataSourceFactory.getInstance().getConnection();
			return get(db, nid, routedest);
		} finally {
			try {
				if (db != null)
					db.close();
			} catch (SQLException e) {
				ThreadCategory.getInstance(DbIpRouteInterfaceEntry.class).warn(
						"Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * Retreives a current record from the database based upon the
	 * key fields of <em>nodeID</em> and <em>routedest</em>. If the
	 * record cannot be found then a null reference is returnd.
	 *
	 * @param db	The database connection used to load the entry.
	 * @param nid	The node id key
	 * @param routedest The ip route destination address
	 *
	 * @return The loaded entry or null if one could not be found.
	 *
	 */
	static DbIpRouteInterfaceEntry get(Connection db, int nid, String routedest)
			throws SQLException {
		DbIpRouteInterfaceEntry entry = new DbIpRouteInterfaceEntry(nid,
				routedest,true);
		if (!entry.load(db))
			entry = null;
		return entry;
	}

	public String toString() {
		String sep = System.getProperty("line.separator");
		StringBuffer buf = new StringBuffer();

		buf.append("from db = ").append(m_fromDb).append(sep);
		buf.append("node id = ").append(m_nodeId).append(sep);
		buf.append("route destination = ").append(m_routedest).append(sep);
		buf.append("route mask = ").append(m_routemask).append(sep);
		buf.append("route next hop = ").append(m_routenexthop).append(sep);
		buf.append("ifindex = ").append(m_routeifindex).append(sep);
		buf.append("route metric1= ").append(m_routemetric1).append(sep);
		buf.append("route metric2 = ").append(m_routemetric2).append(sep);
		buf.append("route metric3 = ").append(m_routemetric3).append(sep);
		buf.append("route metric4 = ").append(m_routemetric4).append(sep);
		buf.append("route metric5 = ").append(m_routemetric5).append(sep);
		buf.append("route type = ").append(m_routetype).append(sep);
		buf.append("route proto = ").append(m_routeproto).append(sep);
		buf.append("status = ").append(m_status).append(sep);
		buf.append("last poll time = ").append(m_lastPollTime).append(sep);
		return buf.toString();

	}

}