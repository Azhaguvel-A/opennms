/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 21, 2005
 *
 * Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.passive;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.config.PassiveStatusKey;
import org.opennms.netmgt.config.PassiveStatusValue;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class PassiveStatusKeeper extends AbstractServiceDaemon implements EventListener {
    
    private static PassiveStatusKeeper s_instance = new PassiveStatusKeeper();
    
    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";

    private Map m_statusTable = null;
    private EventIpcManager m_eventMgr;
    private boolean m_initialized = false;

    private DataSource m_dataSource;

    
    public PassiveStatusKeeper() {
    	super("OpenNMS.PassiveStatusKeeper");
    }
    
    public PassiveStatusKeeper(EventIpcManager eventMgr) {
    	this();
        setEventManager(eventMgr);
    }
    
    public synchronized static void setInstance(PassiveStatusKeeper psk) {
        s_instance = psk;
    }
    
    public synchronized static PassiveStatusKeeper getInstance() {
        return s_instance;
    }

    
    protected void onInit() {
        if (m_initialized) return;
        
        checkPreRequisites();
        createMessageSelectorAndSubscribe();
        
        m_statusTable = new HashMap();
        
        String sql = "select node.nodeLabel AS nodeLabel, outages.ipAddr AS ipAddr, service.serviceName AS serviceName " +
                "FROM outages " +
                "JOIN node ON outages.nodeId = node.nodeId " +
                "JOIN service ON outages.serviceId = service.serviceId " +
                "WHERE outages.ifRegainedService is NULL";
        
        Querier querier = new Querier(m_dataSource, sql) {
        
            public void processRow(ResultSet rs) throws SQLException {
               
                PassiveStatusKey key = new PassiveStatusKey(rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"));
                m_statusTable.put(key, PollStatus.down());
                
                
                
            }
        
        };
        querier.execute();
        
        
        
        m_initialized = true;
    }

    private void checkPreRequisites() {
        if (m_eventMgr == null)
            throw new IllegalStateException("eventManager has not been set");
        if (m_dataSource == null)
            throw new IllegalStateException("dataSource has not been set");
    }

    protected void onStop() {
        m_initialized = false;
        m_eventMgr = null;
        m_statusTable = null;
    }

    public void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        checkInit();
        setStatus(new PassiveStatusKey(nodeLabel, ipAddr, svcName), pollStatus);
    }
    
    public void setStatus(PassiveStatusKey key, PollStatus pollStatus) {
        checkInit();
        m_statusTable.put(key, pollStatus);
    }

    private void checkInit() {
        if (!m_initialized)
            throw new IllegalStateException("the service has not been intialized");
    }

    public PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        //FIXME: Throw a log or exception here if this method is called and the this class hasn't been initialized
        PollStatus status = (PollStatus) (m_statusTable == null ? PollStatus.unknown() : m_statusTable.get(new PassiveStatusKey(nodeLabel, ipAddr, svcName)));
        return (status == null ? PollStatus.up() : status);
    }

    private void createMessageSelectorAndSubscribe() {
        // Subscribe to eventd
        getEventManager().addEventListener(this, PASSIVE_STATUS_UEI);
    }

    public void onEvent(Event e) {
        
        if (isPassiveStatusEvent(e)) {
            log().debug("onEvent: received valid registered passive status event: \n"+EventUtils.toString(e));
            PassiveStatusValue statusValue = getPassiveStatusValue(e);
            setStatus(statusValue.getKey(), statusValue.getStatus());
            log().debug("onEvent: passive status for: "+statusValue.getKey()+ "is: "+m_statusTable.get(statusValue.getKey()));
        } 
        
        if (!isPassiveStatusEvent(e))
        {
            log().debug("onEvent: received Invalid registered passive status event: \n"+EventUtils.toString(e));
        }
    }

    private PassiveStatusValue getPassiveStatusValue(Event e) {
    		return new PassiveStatusValue(
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_NODE_LABEL),
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_IPADDR),
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_NAME),
    				PollStatus.decode(EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_STATUS),EventUtils.getParm(e,EventConstants.PARM_PASSIVE_REASON_CODE))
    				);
    		
	}

	boolean isPassiveStatusEvent(Event e) {
		return PASSIVE_STATUS_UEI.equals(e.getUei()) &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_NODE_LABEL) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_IPADDR) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_NAME) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_STATUS) != null;
	}

	public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    public DataSource getDbConnectoinFactory() {
        return m_dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
}
