/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 1, 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ticketd;

import java.util.Arrays;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * Manages Events trouble ticket related events and passes them to the service layer
 * implementation. 
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class TroubleTicketer implements SpringServiceDaemon, EventListener {
	
    private volatile boolean m_initialized = false;
    
    /**
     * Typically wired in by Spring (applicationContext-troubleTicketer.xml)
     * @param eventIpcManager
     */
	private volatile EventIpcManager m_eventIpcManager;
    private volatile TicketerServiceLayer m_ticketerServiceLayer;

	/**
	 * @param eventIpcManager
	 */
	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
	
    /**
     * @param ticketerServiceLayer
     */
    public void setTicketerServiceLayer(TicketerServiceLayer ticketerServiceLayer) {
        m_ticketerServiceLayer = ticketerServiceLayer;
    }

    /**
     * SpringFramework method from implementation of the Spring Interface
     * <code>org.springframework.beans.factory.InitializingBean</code>
     * @throws Exception An exception is thrown when detecting an invalid state such 
     *         as data not properly initialized or this method called more then once.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(!m_initialized, "shouldn't be calling afterProperties set more than once");
        Assert.state(m_eventIpcManager != null, "property eventIpcManager must be set to a non-null value");
        Assert.state(m_ticketerServiceLayer != null, "property ticketerServiceLayer must be set to a non-null value");

        String[] ueis = {
    			EventConstants.TROUBLETICKET_CANCEL_UEI,
    			EventConstants.TROUBLETICKET_CLOSE_UEI,
    			EventConstants.TROUBLETICKET_CREATE_UEI,
    			EventConstants.TROUBLETICKET_UPDATE_UEI
    	};
    	m_eventIpcManager.addEventListener(this, Arrays.asList(ueis));
        
        m_initialized = true;
    }

    //FIXME
    public void start() throws Exception {
        // DO NOTHING?
    }

    /**
     * EventListener Interface required implementation
     * @return <code>java.lang.String</code> representing the name of this service daemon
     */
	public String getName() {
		return "OpenNMS.TroubleTicketer";
	}

	/**
	 * Event listener Interface required implementation
	 * @param e Event received from Eventd
	 */
	public void onEvent(Event e) {
        try {
		if (EventConstants.TROUBLETICKET_CANCEL_UEI.equals(e.getUei())) {
			handleCancelTicket(e);
		} else if (EventConstants.TROUBLETICKET_CLOSE_UEI.equals(e.getUei())) {
			handleCloseTicket(e);
		} else if (EventConstants.TROUBLETICKET_CREATE_UEI.equals(e.getUei())) {
			handleCreateTicket(e);
		} else if (EventConstants.TROUBLETICKET_UPDATE_UEI.equals(e.getUei())) {
			handleUpdateTicket(e);
		}
        } catch (InsufficientInformationException ex) {
            log().warn("Unable to create trouble ticket due to lack of information: "+ex.getMessage());
        } catch (Throwable t) {
            log().error("Error occurred during trouble ticket processing!", t);
        }
	}

	private Category log() {
	    return ThreadCategory.getInstance(getClass());
    }


	/**
	 * Makes call to API to close a trouble ticket associated with an OnmsAlarm.
	 * @param e An OpenNMS event.
	 * @throws InsufficientInformationException
	 */
    private void handleCloseTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.closeTicketForAlarm(alarmId, ticketId);
	}

    /**
     * Make call to API to Update a trouble ticket with new data from an OnmsAlarm.
     * @param e An OpenNMS Event
     * @throws InsufficientInformationException
     */
	private void handleUpdateTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.updateTicketForAlarm(alarmId, ticketId);
    }

	/**
	 * Make call to API to Create a new Trouble Ticket to be associated with an OnmsAlarm.
	 * @param e An OpenNMS Event
	 * @throws InsufficientInformationException
	 */
	private void handleCreateTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        
        m_ticketerServiceLayer.createTicketForAlarm(alarmId);
	}

	/**
	 * Makes call to API to Cancel a Trouble Ticket associated with an OnmsAlarm.
	 * @param e
	 * @throws InsufficientInformationException
	 */
	private void handleCancelTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.cancelTicketForAlarm(alarmId, ticketId);
	}

}
