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
 * 2007 Apr 10: Store the job start and stop date and the name. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
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
package org.opennms.netmgt.statsd;

import java.util.Date;
import java.util.SortedSet;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.support.AttributeMatchingResourceVisitor;
import org.opennms.netmgt.dao.support.ResourceAttributeFilteringResourceVisitor;
import org.opennms.netmgt.dao.support.ResourceTreeWalker;
import org.opennms.netmgt.dao.support.ResourceTypeFilteringResourceVisitor;
import org.opennms.netmgt.dao.support.RrdStatisticAttributeVisitor;
import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class UnfilteredReportInstance extends AbstractReportInstance implements ReportInstance, InitializingBean {
    private final AttributeStatisticVisitorWithResults m_attributeStatisticVisitor;
    private final RrdStatisticAttributeVisitor m_rrdVisitor = new RrdStatisticAttributeVisitor();
    private final AttributeMatchingResourceVisitor m_attributeVisitor = new AttributeMatchingResourceVisitor();
    private final ResourceTypeFilteringResourceVisitor m_resourceTypeVisitor = new ResourceTypeFilteringResourceVisitor();
    private final ResourceTreeWalker m_walker = new ResourceTreeWalker();
    private String m_resourceAttributeKey;
    private String m_resourceAttributeValueMatch;
    private ResourceAttributeFilteringResourceVisitor m_resourceAttributeVisitor;
    
    public UnfilteredReportInstance(AttributeStatisticVisitorWithResults visitor) {
        m_attributeStatisticVisitor = visitor;
    }
    
    public void setResourceDao(ResourceDao resourceDao) {
        m_walker.setResourceDao(resourceDao);
    }

    public void setRrdDao(RrdDao rrdDao) {
        m_rrdVisitor.setRrdDao(rrdDao);
    }

    public void walk() {
        setJobStartedDate(new Date());
        m_walker.walk();
        setJobCompletedDate(new Date());
    }

    public SortedSet<AttributeStatistic> getResults() {
        return m_attributeStatisticVisitor.getResults();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getResourceTypeMatch()
     */
    public String getResourceTypeMatch() {
        return m_resourceTypeVisitor.getResourceTypeMatch();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setResourceTypeMatch(java.lang.String)
     */
    public void setResourceTypeMatch(String resourceType) {
        m_resourceTypeVisitor.setResourceTypeMatch(resourceType);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getAttributeMatch()
     */
    public String getAttributeMatch() {
        return m_attributeVisitor.getAttributeMatch();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setAttributeMatch(java.lang.String)
     */
    public void setAttributeMatch(String attr) {
        m_attributeVisitor.setAttributeMatch(attr);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getStartTime()
     */
    public long getStartTime() {
        return m_rrdVisitor.getStartTime();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setStartTime(long)
     */
    public void setStartTime(long start) {
        m_rrdVisitor.setStartTime(start);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getEndTime()
     */
    public long getEndTime() {
        return m_rrdVisitor.getEndTime();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setEndTime(long)
     */
    public void setEndTime(long end) {
        m_rrdVisitor.setEndTime(end);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getConsolidationFunction()
     */
    public String getConsolidationFunction() {
        return m_rrdVisitor.getConsolidationFunction();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setConsolidationFunction(java.lang.String)
     */
    public void setConsolidationFunction(String cf) {
        m_rrdVisitor.setConsolidationFunction(cf);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getCount()
     */
    public int getCount() {
        return m_attributeStatisticVisitor.getCount();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setCount(int)
     */
    public void setCount(int count) {
        m_attributeStatisticVisitor.setCount(count);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        m_rrdVisitor.setStatisticVisitor(m_attributeStatisticVisitor);
        m_attributeVisitor.setAttributeVisitor(m_rrdVisitor);
        
        if (m_resourceAttributeKey != null && m_resourceAttributeValueMatch != null) {
            m_resourceAttributeVisitor = new ResourceAttributeFilteringResourceVisitor();
            m_resourceAttributeVisitor.setDelegatedVisitor(m_attributeVisitor);
            m_resourceAttributeVisitor.setResourceAttributeKey(m_resourceAttributeKey);
            m_resourceAttributeVisitor.setResourceAttributeValueMatch(m_resourceAttributeValueMatch);
            m_resourceAttributeVisitor.afterPropertiesSet();
            
            m_resourceTypeVisitor.setDelegatedVisitor(m_resourceAttributeVisitor);
        } else {
            m_resourceTypeVisitor.setDelegatedVisitor(m_attributeVisitor);
        }
        
        m_walker.setVisitor(m_resourceTypeVisitor);

        m_attributeStatisticVisitor.afterPropertiesSet();
        m_rrdVisitor.afterPropertiesSet();
        m_attributeVisitor.afterPropertiesSet();
        m_resourceTypeVisitor.afterPropertiesSet();
        m_walker.afterPropertiesSet();
    }

    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }

    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }

    public String getResourceAttributeKey() {
        return m_resourceAttributeKey;
    }

    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeValueMatch;
    }
}
