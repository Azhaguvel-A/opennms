
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Where;
import org.springframework.core.style.ToStringCreator;

@XmlRootElement(name = "service")
@Entity
@Table(name="ifServices")
public class OnmsMonitoredService extends OnmsEntity implements Serializable,
Comparable<OnmsMonitoredService> {

    /**
     * 
     */
    private static final long serialVersionUID = -7106081378757872886L;

    private Integer m_id;

    private Date m_lastGood;

    private Date m_lastFail;

    private String m_qualifier;

    private String m_status;

    private String m_source;

    private String m_notify;

    private OnmsServiceType m_serviceType;

    private OnmsIpInterface m_ipInterface;

    /*
     * This is a set only because we want it to be lazy
     * and we need a better query language (i.e. HQL)
     * to make this work.  In this case, the Set size
     * will always be 1 or empty because there can only
     * be one outage at a time on a service.
     * 
     * With distributed monitoring, there will probably
     * be a model change were one service can be represented
     * by more than one outage.
     */
    private Set<OnmsOutage> m_currentOutages = new LinkedHashSet<OnmsOutage>();

    private Set<OnmsApplication> m_applications;

    public OnmsMonitoredService() {
    }

    public OnmsMonitoredService(OnmsIpInterface ipIf, OnmsServiceType serviceType) {
        m_ipInterface = ipIf;
        m_ipInterface.getMonitoredServices().add(this);
        m_serviceType = serviceType;

    }

    /**
     * Unique identifier for ifServivce.
     */
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    @XmlTransient
    @Transient
    public String getIpAddress() {
        return m_ipInterface.getIpAddress();
    }

    @XmlTransient
    @Transient
    public Integer getIfIndex() {
        return m_ipInterface.getIfIndex();
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastGood")
    public Date getLastGood() {
        return m_lastGood;
    }

    public void setLastGood(Date lastgood) {
        m_lastGood = lastgood;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastFail")
    public Date getLastFail() {
        return m_lastFail;
    }

    public void setLastFail(Date lastfail) {
        m_lastFail = lastfail;
    }

    @Column(name="qualifier", length=16)
    public String getQualifier() {
        return m_qualifier;
    }

    public void setQualifier(String qualifier) {
        m_qualifier = qualifier;
    }

    @Column(name="status", length=1)
    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    @Column(name="source", length=1)
    public String getSource() {
        return m_source;
    }

    public void setSource(String source) {
        m_source = source;
    }

    @Column(name="notify", length=1)
    public String getNotify() {
        return m_notify;
    }

    public void setNotify(String notify) {
        m_notify = notify;
    }

    @XmlIDREF
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="ipInterfaceId")
    public OnmsIpInterface getIpInterface() {
        return m_ipInterface;
    }

    public void setIpInterface(OnmsIpInterface ipInterface) {
        m_ipInterface = ipInterface;
    }

    @XmlTransient
    @Transient
    public Integer getNodeId() {
        return m_ipInterface.getNode().getId();
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="serviceId")
    public OnmsServiceType getServiceType() {
        return m_serviceType;
    }

    public void setServiceType(OnmsServiceType service) {
        m_serviceType = service;
    }

    public String toString() {
        return new ToStringCreator(this)
        .append("ipAddr", getIpAddress())
        .append("ifindex", getIfIndex())
        .append("lastgood", getLastGood())
        .append("lastfail", getLastFail())
        .append("qualifier", getQualifier())
        .append("status", getStatus())
        .append("source", getSource())
        .append("notify", getNotify())
        .toString();
    }

    @Transient
    public Integer getServiceId() {
        return getServiceType().getId();
    }



    public void visit(EntityVisitor visitor) {
        visitor.visitMonitoredService(this);
        visitor.visitMonitoredServiceComplete(this);
    }

    @Transient
    public String getServiceName() {
        return getServiceType().getName();
    }

    @Transient
    public boolean isDown() {
        boolean down = true;
        if (!"A".equals(getStatus()) || m_currentOutages.isEmpty()) {
            return !down;
        }

        return down;
    }

    @XmlTransient
    @OneToMany(mappedBy="monitoredService", fetch=FetchType.LAZY)
    @Where(clause="ifRegainedService is null")
    public Set<OnmsOutage> getCurrentOutages() {
        return m_currentOutages;
    }

    public void setCurrentOutages(Set<OnmsOutage> currentOutages) {
        m_currentOutages = currentOutages;
    }

//  @ManyToMany(mappedBy="memberServices")
    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
               name="application_service_map",
               joinColumns={@JoinColumn(name="ifserviceid")},
               inverseJoinColumns={@JoinColumn(name="appid")}
    )
    public Set<OnmsApplication> getApplications() {
        return m_applications;
    }

    public void setApplications(Set<OnmsApplication> applications) {
        m_applications = applications;
    }

    public boolean addApplication(OnmsApplication application) {
        return getApplications().add(application);
    }

    public boolean removeApplication(OnmsApplication application) {
        return getApplications().remove(application);
    }

    public int compareTo(OnmsMonitoredService o) {
        int diff;

        diff = getIpInterface().getNode().getLabel().compareToIgnoreCase(o.getIpInterface().getNode().getLabel());
        if (diff != 0) {
            return diff;
        }

        diff = getIpAddress().compareToIgnoreCase(o.getIpAddress());
        if (diff != 0) {
            return diff;
        }

        return getServiceName().compareToIgnoreCase(o.getServiceName());
    }
}
