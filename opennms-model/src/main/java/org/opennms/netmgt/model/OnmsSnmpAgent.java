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
package org.opennms.netmgt.model;

import java.util.Set;

import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class OnmsSnmpAgent extends OnmsAgent {
	
	private String m_sysObjectId;
	private String m_sysName;
	private String m_sysDescription;
	private String m_sysLocation;
	private String m_sysContact;
	
	private SnmpAgentConfig m_config;
	@SuppressWarnings("unchecked")
    private Set m_attributes;
	
	@SuppressWarnings("unchecked")
    public Set getAttributes() {
		return m_attributes;
	}
	@SuppressWarnings("unchecked")
    public void setAttributes(Set attributes) {
		m_attributes = attributes;
	}
	public SnmpAgentConfig getConfig() {
		return m_config;
	}
	public void setConfig(SnmpAgentConfig config) {
		m_config = config;
	}
	public String getSysObjectId() {
		return m_sysObjectId;
	}
	public void setSysObjectId(String sysObjectId) {
		m_sysObjectId = sysObjectId;
	}
	public String getSysContact() {
		return m_sysContact;
	}
	public void setSysContact(String sysContact) {
		m_sysContact = sysContact;
	}
	public String getSysDescription() {
		return m_sysDescription;
	}
	public void setSysDescription(String sysDescription) {
		m_sysDescription = sysDescription;
	}
	public String getSysLocation() {
		return m_sysLocation;
	}
	public void setSysLocation(String sysLocation) {
		m_sysLocation = sysLocation;
	}
	public String getSysName() {
		return m_sysName;
	}
	public void setSysName(String sysName) {
		m_sysName = sysName;
	}
}
