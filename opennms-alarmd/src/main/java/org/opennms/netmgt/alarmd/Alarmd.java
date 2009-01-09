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
package org.opennms.netmgt.alarmd;

import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;

/**
 * Alarm management Daemon
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class Alarmd implements EventListener, SpringServiceDaemon, DisposableBean {
	
	public static final String NAME = "Alarmd";

	private volatile EventSubscriptionService m_eventSubscriptionService;
	private volatile EventForwarder m_eventForwarder;
	
	private AlarmPersister m_persister;
	            
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

	//Get all events
	public void afterPropertiesSet() throws Exception {
	    m_eventSubscriptionService.addEventListener(this);
	}

	public void destroy() throws Exception {
	}

	public String getName() {
		return NAME;
	}

    public void start() throws Exception {
    }

    public void onEvent(Event e) {
        m_persister.persist(e);
    }

    public void setPersister(AlarmPersister persister) {
        this.m_persister = persister;
    }

    public AlarmPersister getPersister() {
        return m_persister;
    }

}
