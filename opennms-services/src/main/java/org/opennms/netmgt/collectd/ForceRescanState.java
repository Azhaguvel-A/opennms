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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>ForceRescanState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ForceRescanState {
    
    private CollectionAgent m_agent;
    private EventProxy m_eventProxy;
    
    private boolean m_forceRescanSent = false;

    /**
     * <p>Constructor for ForceRescanState.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public ForceRescanState(CollectionAgent agent, EventProxy eventProxy) {
        m_agent = agent;
        m_eventProxy = eventProxy;
    }
    
    /**
     * <p>getEventProxy</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public EventProxy getEventProxy() {
        return m_eventProxy;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>createForceResanEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createForceResanEvent() {
        // create the event to be sent
        EventBuilder bldr = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "SnmpCollector");
        
        bldr.setNodeid(m_agent.getNodeId());

        bldr.setInterface(m_agent.getInetAddress());
        
        bldr.setService(SnmpCollector.SERVICE_NAME);
        
        bldr.setHost(determineLocalHostName());

        return bldr.getEvent();
    }

    String determineLocalHostName() {
    	// Get local host name (used when generating threshold events)
    	try {
    		return InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		log().warn("initialize: Unable to resolve local host name.", e);
    		return "unresolved.host";
    	}
    }

    /**
     * <p>getAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public CollectionAgent getAgent() {
        return m_agent;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     * @param ifAddress
     *            interface address to which this event pertains
     * @param nodeId TODO
     */
    void sendForceRescanEvent() {
        // Log4j category
    	if (log().isDebugEnabled()) {
    		log().debug("generateForceRescanEvent: interface = " + getAgent().getHostAddress());
    	}
    
    	// Send event via EventProxy
    	try {
            getEventProxy().send(createForceResanEvent());
    	} catch (EventProxyException e) {
    		log().error("generateForceRescanEvent: Unable to send "
    				+ "forceRescan event.", e);
    	}
    }
    
    void rescanIndicated() {
        if (!m_forceRescanSent) {
            sendForceRescanEvent();
            m_forceRescanSent = true;
        }
    }

}
