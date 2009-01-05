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
/**
 * 
 */
package org.opennms.secret.model;

import java.util.Date;
import java.util.List;

/**
 * @author Ted Kazmarak
 * @author David Hustace
 * 
 * @hibernate.class table="node"
 *
 */
public class Node {
	
	Long nodeId;
	String dpName;
	Date nodeCreateTime;
	Long nodeParentId;
	String nodeType;
	String nodeSysOid;
	String nodeSysName;
	String nodeSysDescription;
	String nodeSysLocation;
	String nodeSysContact;
	String nodeLabel;
	String nodeLabelSource;
	String nodeNetBiosName;
	String nodeDomainName;
	String operatingSystem;
	Date lastCapsdPoll;
    List dataSources;
    NodeInterface[] interfaces;
	
	
	/**
	 * @return Returns the dbName.
	 * @hibernate.property
	 */
	public String getDpName() {
		return dpName;
	}
	
	/**
	 * @param dbName The dbName to set.
	 */
	public void setDpName(String dbName) {
		this.dpName = dbName;
	}
	
	/**
	 * @return Returns the lastCapsdPoll.
	 * @hibernate.property
	 */
	public Date getLastCapsdPoll() {
		return lastCapsdPoll;
	}
	
	/**
	 * @param lastCapsdPoll The lastCapsdPoll to set.
	 */
	public void setLastCapsdPoll(Date lastCapsdPoll) {
		this.lastCapsdPoll = lastCapsdPoll;
	}
	
	/**
	 * @return Returns the nodeCreateTime.
	 * @hibernate.property
	 */
	public Date getNodeCreateTime() {
		return nodeCreateTime;
	}
	
	/**
	 * @param nodeCreateTime The nodeCreateTime to set.
	 */
	public void setNodeCreateTime(Date nodeCreateTime) {
		this.nodeCreateTime = nodeCreateTime;
	}
	
	/**
	 * @return Returns the nodeDomainName.
	 * @hibernate.property
	 */
	public String getNodeDomainName() {
		return nodeDomainName;
	}
	
	/**
	 * @param nodeDomainName The nodeDomainName to set.
	 */
	public void setNodeDomainName(String nodeDomainName) {
		this.nodeDomainName = nodeDomainName;
	}
	
	/**
	 * @return Returns the nodeId.
	 * @hibernate.id generator-class="native"
	 */
	
	public Long getNodeId() {
		return nodeId;
	}
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

	/**
	 * @return Returns the nodeLabel.
     * @hibernate.property
	 */
    public String getNodeLabel() {
		return nodeLabel;
	}
    
	/**
	 * @param nodeLabel The nodeLabel to set.
	 */
	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}
    
	/**
	 * @return Returns the nodeLabelSource.
     * @hibernate.property
	 */
	public String getNodeLabelSource() {
		return nodeLabelSource;
	}
    
	/**
	 * @param nodeLabelSource The nodeLabelSource to set.
	 */
	public void setNodeLabelSource(String nodeLabelSource) {
		this.nodeLabelSource = nodeLabelSource;
	}
    
	/**
	 * @return Returns the nodeNetBiosName.
     * @hibernate.property
	 */
	public String getNodeNetBiosName() {
		return nodeNetBiosName;
	}
    
	/**
	 * @param nodeNetBiosName The nodeNetBiosName to set.
	 */
	public void setNodeNetBiosName(String nodeNetBiosName) {
		this.nodeNetBiosName = nodeNetBiosName;
	}
    
	/**
	 * @return Returns the nodeParentId.
     * @hibernate.property
	 */
	public Long getNodeParentId() {
		return nodeParentId;
	}
    
	/**
	 * @param nodeParentId The nodeParentId to set.
	 */
	public void setNodeParentId(Long nodeParentId) {
		this.nodeParentId = nodeParentId;
	}
    
	/**
	 * @return Returns the nodeSysContact.
     * @hibernate.property
	 */
	public String getNodeSysContact() {
		return nodeSysContact;
	}
    
	/**
	 * @param nodeSysContact The nodeSysContact to set.
	 */
	public void setNodeSysContact(String nodeSysContact) {
		this.nodeSysContact = nodeSysContact;
	}
    
	/**
	 * @return Returns the nodeSysDescription.
     * @hibernate.property
	 */
	public String getNodeSysDescription() {
		return nodeSysDescription;
	}
    
	/**
	 * @param nodeSysDescription The nodeSysDescription to set.
	 */
	public void setNodeSysDescription(String nodeSysDescription) {
		this.nodeSysDescription = nodeSysDescription;
	}
    
	/**
	 * @return Returns the nodeSysLocation.
     * @hibernate.property
	 */
	public String getNodeSysLocation() {
		return nodeSysLocation;
	}
    
	/**
	 * @param nodeSysLocation The nodeSysLocation to set.
	 */
	public void setNodeSysLocation(String nodeSysLocation) {
		this.nodeSysLocation = nodeSysLocation;
	}
    
	/**
	 * @return Returns the nodeSysName.
     * @hibernate.property
	 */
	public String getNodeSysName() {
		return nodeSysName;
	}
    
	/**
	 * @param nodeSysName The nodeSysName to set.
	 */
	public void setNodeSysName(String nodeSysName) {
		this.nodeSysName = nodeSysName;
	}
    
	/**
	 * @return Returns the nodeSysOid.
	 */
	public String getNodeSysOid() {
		return nodeSysOid;
	}
    
	/**
	 * @param nodeSysOid The nodeSysOid to set.
	 */
	public void setNodeSysOid(String nodeSysOid) {
		this.nodeSysOid = nodeSysOid;
	}
    
	/**
	 * @return Returns the nodeType.
     * @hibernate.property
	 */
	public String getNodeType() {
		return nodeType;
	}
    
	/**
	 * @param nodeType The nodeType to set.
	 */
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
    
	/**
	 * @return Returns the operatingSystem.
     * @hibernate.property
	 */
    
	public String getOperatingSystem() {
		return operatingSystem;
	}
    
	/**
	 * @param operatingSystem The operatingSystem to set.
	 */
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
    
    
    public boolean hasPerformanceDataSource() {
        return true;
        
    }
/*    
    public List getDataSources() {
        return dataSources;
    }
    
    public void setDataSources(List dataSources) {
        this.dataSources = dataSources;
    }
    
    public NodeInterface[] getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(NodeInterface[] interfaces) {
        this.interfaces = interfaces;
    }
    */
}
