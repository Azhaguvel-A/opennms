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
// Modifications:
//
// 2007 Apr 10: Organized imports. - dj@opennms.org
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

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.springframework.core.style.ToStringCreator;


/** 
 * Contains information on nodes discovered and potentially managed by OpenNMS.  
 * sys* properties map to SNMP MIB 2 system table information.
 * 
 * @hibernate.class table="node"
 *     
*/
@XmlRootElement(name = "node")
@Entity
@Table(name="node")
@SecondaryTable(name="pathOutage")
public class OnmsNode extends OnmsEntity implements Serializable,
        Comparable<OnmsNode> {

    private static final long serialVersionUID = -5736397583719151493L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private Date m_createTime = new Date();

    /** nullable persistent field */
    private OnmsNode m_parent;

    /** nullable persistent field */
    private String m_type;

    /** nullable persistent field */
    private String m_sysObjectId;

    /** nullable persistent field */
    private String m_sysName;

    /** nullable persistent field */
    private String m_sysDescription;

    /** nullable persistent field */
    private String m_sysLocation;

    /** nullable persistent field */
    private String m_sysContact;

    /** nullable persistent field */
    private String m_label;

    /** nullable persistent field */
    private String m_labelSource;

    /** nullable persistent field */
    private String m_netBiosName;

    /** nullable persistent field */
    private String m_netBiosDomain;

    /** nullable persistent field */
    private String m_operatingSystem;

    /** nullable persistent field */
    private Date m_lastCapsdPoll;
    
    private String m_foreignSource;
    
    private String m_foreignId;

    /** persistent field */
    private OnmsDistPoller m_distPoller;

    /** persistent field */
    private OnmsAssetRecord m_assetRecord;

    /** persistent field */
    private Set<OnmsIpInterface> m_ipInterfaces = new LinkedHashSet<OnmsIpInterface>();

    /** persistent field */
    private Set<OnmsSnmpInterface> m_snmpInterfaces = new LinkedHashSet<OnmsSnmpInterface>();

    /** persistent field */
    private Set<OnmsArpInterface> m_arpInterfaces = new LinkedHashSet<OnmsArpInterface>();

    private Set<OnmsCategory> m_categories = new LinkedHashSet<OnmsCategory>();

	private PathElement m_pathElement;

    public OnmsNode() {
        this(null);
    }

    public OnmsNode(OnmsDistPoller distPoller) {
        m_distPoller = distPoller;
        m_assetRecord = new OnmsAssetRecord();
        m_assetRecord.setNode(this);
    }

    /**
     * Unique identifier for node.
     * 
     */
    @Id
    @Column(name="nodeId")
    @SequenceGenerator(name="nodeSequence", sequenceName="nodeNxtId")
    @GeneratedValue(generator="nodeSequence")
    @XmlTransient
    public Integer getId() {
        return m_id;
    }
    
    @XmlID
    @Transient
    public String getNodeId() {
        return getId().toString();
    }

    public void setId(Integer nodeid) {
        m_id = nodeid;
    }

    /** 
     * Time node was added to the database.
     * 
     * @hibernate.property column="nodecreatetime" length="8" not-null="true"
     *         
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="nodeCreateTime", nullable=false)
    public Date getCreateTime() {
        return m_createTime;
    }

    public void setCreateTime(Date nodecreatetime) {
        m_createTime = nodecreatetime;
    }

    /** 
     * In the case that the node is virtual or an independent device in a chassis
     * that should be reflected as a subcomponent or "child", this field reflects 
     * the nodeID of the chassis/physical node/"parent" device.
     * 
     */
    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeParentID")
    public OnmsNode getParent() {
        return m_parent;
    }

    /**
     * 
     */
    public void setParent(OnmsNode parent) {
        m_parent = parent;
    }

    /** 
     * Flag indicating status of node
     * - 'A' - active
     * - 'D' - deleted
     * 
     * TODO: Eventually this will be deprecated and deleted nodes will actually be deleted.
     * 
     */
    @Column(name="nodeType", length=1)
    public String getType() {
        return m_type;
    }

    public void setType(String nodetype) {
        m_type = nodetype;
    }

    /** 
     * SNMP MIB-2 system.sysObjectID.0
     */
    @Column(name="nodeSysOID", length=256)
    public String getSysObjectId() {
        return m_sysObjectId;
    }

    public void setSysObjectId(String nodesysoid) {
        m_sysObjectId = nodesysoid;
    }

    /** 
     * SNMP MIB-2 system.sysName.0
     * 
     */
    @Column(name="nodeSysName", length=256)
    public String getSysName() {
        return m_sysName;
    }

    public void setSysName(String nodesysname) {
        m_sysName = nodesysname;
    }

    /** 
     * SNMP MIB-2 system.sysDescr.0
     */
    @Column(name="nodeSysDescription", length=256)
    public String getSysDescription() {
        return m_sysDescription;
    }

    public void setSysDescription(String nodesysdescription) {
        m_sysDescription = nodesysdescription;
    }

    /** 
     * SNMP MIB-2 system.sysLocation.0
     */
    @Column(name="nodeSysLocation", length=256)
    public String getSysLocation() {
        return m_sysLocation;
    }

    public void setSysLocation(String nodesyslocation) {
        m_sysLocation = nodesyslocation;
    }

    /** 
     * SNMP MIB-2 system.sysContact.0
     */
    @Column(name="nodeSysContact", length=256)
    public String getSysContact() {
        return m_sysContact;
    }

    public void setSysContact(String nodesyscontact) {
        m_sysContact = nodesyscontact;
    }

    /** 
     * User-friendly name associated with the node.
     */
    @Column(name="nodeLabel", length=256)
    public String getLabel() {
        return m_label;
    }

    public void setLabel(String nodelabel) {
        m_label = nodelabel;
    }

    /** 
     * Flag indicating source of nodeLabel
     * - 'U' = user defined
     * - 'H' = IP hostname
     * - 'S' = sysName
     * - 'A' = IP address
     * 
     * TODO: change this to an enum
     */
    @Column(name="nodeLabelSource", length=1)
    public String getLabelSource() {
        return m_labelSource;
    }

    public void setLabelSource(String nodelabelsource) {
        m_labelSource = nodelabelsource;
    }

    /** 
     * NetBIOS workstation name associated with the node.
     */
    @Column(name="nodeNetBIOSName", length=16)
    public String getNetBiosName() {
        return m_netBiosName;
    }

    public void setNetBiosName(String nodenetbiosname) {
        m_netBiosName = nodenetbiosname;
    }

    /**
     * NetBIOS damain name associated with the node.
     */
    @Column(name="nodeDomainName", length=16)
    public String getNetBiosDomain() {
        return m_netBiosDomain;
    }

    public void setNetBiosDomain(String nodedomainname) {
        m_netBiosDomain = nodedomainname;
    }

    /** 
     * Operating system running on the node.
     */
    @Column(name="operatingSystem", length=64)
    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    public void setOperatingSystem(String operatingsystem) {
        m_operatingSystem = operatingsystem;
    }

    /** 
     * Date and time of last Capsd scan.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastCapsdPoll")
    public Date getLastCapsdPoll() {
        return m_lastCapsdPoll;
    }

    public void setLastCapsdPoll(Date lastcapsdpoll) {
        m_lastCapsdPoll = lastcapsdpoll;
    }
    
    
    @Column(name="foreignId")
    public String getForeignId() {
        return m_foreignId;
    }

    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
    }

    @Column(name="foreignSource")
    public String getForeignSource() {
        return m_foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }
    
    /**
     * Distributed Poller responsible for this node
     * 
     */
    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="dpName")
    public OnmsDistPoller getDistPoller() {
        return m_distPoller;
    }

    public void setDistPoller(org.opennms.netmgt.model.OnmsDistPoller distpoller) {
        m_distPoller = distpoller;
    }
    
    /** 
     * The assert record associated with this node
     */
    @OneToOne(mappedBy="node", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    public OnmsAssetRecord getAssetRecord() {
        return m_assetRecord;
    }

    public void setAssetRecord(OnmsAssetRecord asset) {
        m_assetRecord = asset;
    }
    
    @XmlTransient
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name="ipAddress", column=@Column(name="criticalPathIp", table="pathOutage")),
    	@AttributeOverride(name="serviceName", column=@Column(name="criticalPathServiceName", table="pathOutage"))
    })
    public PathElement getPathElement() {
    	return m_pathElement;
    }
    
    public void setPathElement(PathElement pathElement) {
    	m_pathElement = pathElement;
    }


    /** 
     * The interfaces on this node
     * 
     */
    @XmlTransient
    @OneToMany(mappedBy="node")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OnmsIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

    public void setIpInterfaces(Set<OnmsIpInterface> ipinterfaces) {
        m_ipInterfaces = ipinterfaces;
    }
    
    public void addIpInterface(OnmsIpInterface iface) {
    	iface.setNode(this);
    	getIpInterfaces().add(iface);
    }

    /**
     * The information from the SNMP interfaces/ipAddrTables for the node
     *  
     */
    @XmlTransient
    @OneToMany(mappedBy="node")
    @org.hibernate.annotations.Cascade( {
         org.hibernate.annotations.CascadeType.ALL,
         org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OnmsSnmpInterface> getSnmpInterfaces() {
        return m_snmpInterfaces;
    }

    public void setSnmpInterfaces(Set<OnmsSnmpInterface> snmpinterfaces) {
        m_snmpInterfaces = snmpinterfaces;
    }
    
    /** 
     * The arp interfaces on this node
     * 
     */
    @XmlTransient
    @OneToMany(mappedBy="node")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OnmsArpInterface> getArpInterfaces() {
        return m_arpInterfaces;
    }

    public void setArpInterfaces(Set<OnmsArpInterface> arpInterfaces) {
        m_arpInterfaces = arpInterfaces;
    }
    
    public void addArpInterface(OnmsArpInterface iface) {
        iface.setNode(this);
        getArpInterfaces().add(iface);
    }

    @XmlTransient
    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
    		name="category_node",
    		joinColumns={@JoinColumn(name="nodeId")},
    		inverseJoinColumns={@JoinColumn(name="categoryId")}
    )
    public Set<OnmsCategory> getCategories() {
        return m_categories;
    }
    
    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }
    
    public boolean addCategory(OnmsCategory category) {
        return getCategories().add(category);
    }
    
    public boolean removeCategory(OnmsCategory category) {
        return getCategories().remove(category);
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("label", getLabel())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		visitor.visitNode(this);
		
		for (Iterator<OnmsIpInterface> it = getIpInterfaces().iterator(); it.hasNext();) {
			OnmsIpInterface iface = it.next();
			iface.visit(visitor);
		}
		
		for (Iterator<OnmsSnmpInterface> it = getSnmpInterfaces().iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIface = it.next();
			snmpIface.visit(visitor);
		}
		
		visitor.visitNodeComplete(this);
	}

	public void addSnmpInterface(OnmsSnmpInterface snmpIface) {
    	snmpIface.setNode(this);
    	getSnmpInterfaces().add(snmpIface);
	}

	@Transient
    public boolean isDown() {
        boolean down = true;
        for (Iterator<OnmsIpInterface> it = m_ipInterfaces.iterator(); it.hasNext();) {
            OnmsIpInterface ipIf = it.next();
            if (!ipIf.isDown()) {
                return !down;
            }
        }
        return down;
    }

    @Transient
    public OnmsSnmpInterface getSnmpInterfaceWithIfIndex(int ifIndex) {
        for (OnmsSnmpInterface dbSnmpIface : getSnmpInterfaces()) {
            if (dbSnmpIface.getIfIndex().equals(ifIndex)) {
                return dbSnmpIface;
            }
        }
        return null;
    }

    public OnmsIpInterface getIpInterfaceByIpAddress(String ipAddress) {
        for (OnmsIpInterface iface : getIpInterfaces()) {
            if (ipAddress.equals(iface.getIpAddress())) {
                return iface;
            }
        }
        return null;
    }

    public int compareTo(OnmsNode o) {
        return getLabel().compareToIgnoreCase(o.getLabel());
    }

    @Transient
	public OnmsIpInterface getPrimaryInterface() {
		for(OnmsIpInterface iface : getIpInterfaces()) {
			if (iface.getIsSnmpPrimary() == CollectionType.PRIMARY) {
				return iface;
			}
		}
		return null;
	}
    
    @Transient
	public OnmsIpInterface getInterfaceWithService(String svcName) {
		for(OnmsIpInterface iface : getIpInterfaces()) {
			if (iface.getMonitoredServiceByServiceType(svcName) != null) {
				return iface;
			}	
		}
		return null;
	}

    @Transient
    public OnmsIpInterface getCriticalInterface() {
    	
    	OnmsIpInterface critIface = getPrimaryInterface();
    	if (critIface != null) {
    		return critIface;
    	}
    	
    	return getInterfaceWithService("ICMP");
    	
    }

}
