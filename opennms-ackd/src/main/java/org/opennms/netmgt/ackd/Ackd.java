/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.ackd;

import java.text.ParseException;
import java.util.List;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;

/**
 * Acknowledgment management Daemon
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
//TODO: need a transactional service layer to process acknowledgments
@EventListener(name="Ackd")
public class Ackd implements SpringServiceDaemon, DisposableBean {
	
    
    //TODO: add a queue for processing acknowledgements from the reads, ReST, Events
    
	public static final String NAME = "Ackd";
	
	public AckdConfiguration m_config;

	private volatile EventSubscriptionService m_eventSubscriptionService;
	private volatile EventForwarder m_eventForwarder;
	
	private List<AckReader> m_ackReaders;
	private AckService m_ackService;
	
	public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventSubscriptionService getEventSubscriptionService() {
	    return m_eventSubscriptionService;
	}

	public void setEventSubscriptionService(EventSubscriptionService eventManager) {
		m_eventSubscriptionService = eventManager;
	}

	public void afterPropertiesSet() throws Exception {
	    m_ackService.setEventForwarder(m_eventForwarder);
	}

	public void destroy() throws Exception {
	}

	public String getName() {
		return NAME;
	}

    public void start() throws Exception {
        for (AckReader reader : m_ackReaders) {
            reader.start(m_config);
        }
    }
    
    public void setAckReaders(List<AckReader> ackReaders) {
        m_ackReaders = ackReaders;
    }

    public List<AckReader> getAckReaders() {
        return m_ackReaders;
    }
    
    public AckService getAckService() {
        return m_ackService;
    }

    public void setAckService(AckService ackService) {
        m_ackService = ackService;
    }

    //TODO: make event conf def for this uei
    @EventHandler(uei="uei.opennms.org/internal/ackd/Acknowledge")
    public void handleAckEvent(Event event) {
        OnmsAcknowledgment ack;
        
        try {
            ack = new OnmsAcknowledgment(event);
            m_ackService.processAck(ack);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
