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
 * Created October 10, 2006
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
package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.PollStatus;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class ServicePollState {
    
    private PolledService m_polledService;
    private int m_index;
    private PollStatus m_lastPoll;
    private Date m_initialPollTime;

    public ServicePollState(PolledService polledService, int index) {
        m_polledService = polledService;
        m_index = index;
    }

    public PollStatus getLastPoll() {
        return m_lastPoll;
    }

    public void setLastPoll(PollStatus lastPoll) {
        m_lastPoll = lastPoll;
    }
    
    public Date getLastPollTime() {
        return (m_lastPoll == null ? null : m_lastPoll.getTimestamp());
    }
    
    public Date getNextPollTime() {
        if (m_lastPoll == null) {
            return m_initialPollTime;
        }
        else {
            return m_polledService.getPollModel().getNextPollTime(getLastPollTime());
        }
    }

    public int getIndex() {
        return m_index;
    }

    public PolledService getPolledService() {
        return m_polledService;
    }

    public void setInitialPollTime(Date initialPollTime) {
        m_initialPollTime = initialPollTime;
    }

}
