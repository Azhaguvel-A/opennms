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

package org.opennms.netmgt.alarmd;

import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;

/**
 * Alarm management Daemon
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name=Alarmd.NAME)
public class Alarmd implements SpringServiceDaemon, DisposableBean {

    /** Constant <code>NAME="Alarmd"</code> */
    public static final String NAME = "Alarmd";

    private volatile EventForwarder m_eventForwarder;

    private AlarmPersister m_persister;

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void destroy() throws Exception {
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return NAME;
    }

    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void start() throws Exception {
    }

    //Get all events
    /**
     * <p>onEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(Event e) {
        m_persister.persist(e);
    }

    /**
     * <p>setPersister</p>
     *
     * @param persister a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public void setPersister(AlarmPersister persister) {
        this.m_persister = persister;
    }

    /**
     * <p>getPersister</p>
     *
     * @return a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public AlarmPersister getPersister() {
        return m_persister;
    }
}
