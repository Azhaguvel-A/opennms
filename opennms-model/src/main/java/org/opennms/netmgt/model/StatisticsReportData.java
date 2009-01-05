/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file.
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
package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Model class for a piece of statistics report data.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReport
 */
@Entity
@Table(name="statisticsReportData")
public class StatisticsReportData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer m_id;
    private StatisticsReport m_report;
    private ResourceReference m_resource;
    private Double m_value;
    
    /**
     * Unique identifier for data.
     * 
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    public void setId(Integer id) {
        m_id = id;
    }
    
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="reportId") //, nullable=false)
    public StatisticsReport getReport() {
        return m_report;
    }
    public void setReport(StatisticsReport report) {
        m_report = report;
    }
    
    @ManyToOne(optional=false)
    @JoinColumn(name="resourceId")
    public ResourceReference getResource() {
        return m_resource;
    }
    public void setResource(ResourceReference resource) {
        m_resource = resource;
    }
    
    @Transient
    public String getResourceId() {
        return m_resource.getResourceId();
    }
    
    @Column(name="value", nullable=false)
    public Double getValue() {
        return m_value;
    }
    public void setValue(Double value) {
        m_value = value;
    }
}
