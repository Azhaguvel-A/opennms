/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created February 1, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.correlation.drools;

import java.util.Date;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class Flap {
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Date m_startTime;
    Date m_endTime;
    Integer m_locationMonitor;
    boolean m_counted;
    Integer m_timerId;
    
    public Flap(Long nodeid, String ipAddr, String svcName, Integer locationMonitor, Integer timerId) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_timerId = timerId;
        m_startTime = new Date();
        m_counted = false;
    }
    
    public Date getEndTime() {
        return m_endTime;
    }
    public void setEndTime(Date end) {
        m_endTime = end;
    }
    public String getIpAddr() {
        return m_ipAddr;
    }
    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }
    public Integer getLocationMonitor() {
        return m_locationMonitor;
    }
    public void setLocationMonitor(Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    public Long getNodeid() {
        return m_nodeid;
    }
    public void setNodeid(Long nodeid) {
        m_nodeid = nodeid;
    }
    public Date getStartTime() {
        return m_startTime;
    }
    public void setStartTime(Date start) {
        m_startTime = start;
    }
    public String getSvcName() {
        return m_svcName;
    }
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public boolean isCounted() {
        return m_counted;
    }

    public void setCounted(boolean counted) {
        m_counted = counted;
    }

    public Integer getTimerId() {
        return m_timerId;
    }

    public void setTimerId(Integer timerId) {
        m_timerId = timerId;
    }
    
    
}
