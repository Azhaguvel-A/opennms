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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.model;



public class PollResult {
	
	private Integer m_id;
	private DemandPoll m_demandPoll;
	private OnmsMonitoredService m_monitoredService;
	private PollStatus m_status;
	
	public PollResult() {
		
	}
	
	public PollResult(int id) {
		m_id = id;
	}
	
	public Integer getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}

	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}

	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}

	public PollStatus getStatus() {
		return m_status;
	}

	public void setStatus(PollStatus status) {
		m_status = status;
	}

	public DemandPoll getDemandPoll() {
		return m_demandPoll;
	}

	public void setDemandPoll(DemandPoll poll) {
		this.m_demandPoll = poll;
	}	

}
