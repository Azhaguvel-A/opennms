/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller;


import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext;
import org.opennms.netmgt.utils.Updater;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a DefaultPollContext
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class DefaultPollContext implements PollContext {
    
    private volatile EventIpcManager m_eventManager;
    private volatile String m_name;
    private volatile String m_localHostName;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private DataSource m_dataSource;

    private String m_serviceName="SNMP";

    /**
     * <p>getSnmpInterfaceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.SnmpInterfaceDao} object.
     */
    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    /**
     * <p>setSnmpInterfaceDao</p>
     *
     * @param snmpInterfaceDao a {@link org.opennms.netmgt.dao.SnmpInterfaceDao} object.
     */
    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    
    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }
    
    /**
     * <p>setLocalHostName</p>
     *
     * @param localHostName a {@link java.lang.String} object.
     */
    public void setLocalHostName(String localHostName) {
        m_localHostName = localHostName;
    }
    
    /**
     * <p>getLocalHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocalHostName() {
        return m_localHostName;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#getCriticalServiceName()
     */
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }
    
    /** {@inheritDoc} */
    public void setServiceName(String serviceName) {
        m_serviceName=serviceName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#sendEvent(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    public void sendEvent(Event event) {
        getEventManager().sendNow(event);
    }

    ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    /** {@inheritDoc} */
    public Event createEvent(String uei, int nodeId, String address, Date date, OnmsSnmpInterface snmpinterface) {
        
            log().debug("createEvent: uei = " + uei + " nodeid = " + nodeId + " date = " + date);
        
        EventBuilder bldr = new EventBuilder(uei, this.getName(), date);
        bldr.setNodeid(nodeId);
        if (address != null) {
            bldr.setInterface(addr(address));
        }
        bldr.setService(getServiceName());

        bldr.setHost(this.getLocalHostName());
        bldr.setField("ifIndex", snmpinterface.getIfIndex().toString());

        bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_IFINDEX, snmpinterface.getIfIndex().toString());
        // TODO: This doesn't handle cases where there are multiple addresses on the same ifindex
        final Set<OnmsIpInterface> ipInterfaces = snmpinterface.getIpInterfaces();
		bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_IP, ipInterfaces.size() > 0 ? InetAddressUtils.str(ipInterfaces.iterator().next().getIpAddress()) : null
        );
        if (snmpinterface.getIfName() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_NAME, snmpinterface.getIfName());
        if (snmpinterface.getIfDescr() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_DESC, snmpinterface.getIfDescr());
        if (snmpinterface.getIfAlias() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_ALIAS, snmpinterface.getIfAlias());
        if (snmpinterface.getNetMask() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_MASK, snmpinterface.getNetMask());        
        
        return bldr.getEvent();
    }

    /** {@inheritDoc} */
    public List<OnmsSnmpInterface> get(int nodeId, String criteria) {
        final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
        onmsCriteria.add(Restrictions.sqlRestriction(criteria + " and nodeid = " + nodeId));
        return getSnmpInterfaceDao().findMatching(onmsCriteria);

    }
        
    /** {@inheritDoc} */
    public void update(OnmsSnmpInterface snmpinterface) {
        getSnmpInterfaceDao().update(snmpinterface);
    }

    /** {@inheritDoc} */
    public void updatePollStatus(int nodeId, String criteria, String status) {
        String sql = "update snmpinterface set snmppoll = ? where nodeid = ? and " + criteria;
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status,new Integer(nodeId));  
    }
    
    /** {@inheritDoc} */
    public void updatePollStatus(int nodeId, String status) {
        String sql = "update snmpinterface set snmppoll = ? where nodeid = ? ";
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status,new Integer(nodeId));  
        
    }

    /** {@inheritDoc} */
    public void updatePollStatus(String status) {
        final String sql = "update snmpinterface set snmppoll = ? ";
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status);  
    }

}
