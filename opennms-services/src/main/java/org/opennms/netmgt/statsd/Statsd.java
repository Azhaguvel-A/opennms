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
 * 2007 Apr 10: Externalize the building of report definitions. - dj@opennms.org
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

import java.text.ParseException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class Statsd implements SpringServiceDaemon {
    private ResourceDao m_resourceDao;
    private RrdDao m_rrdDao;
    private FilterDao m_filterDao;
    private TransactionTemplate m_transactionTemplate;
    private ReportPersister m_reportPersister;
    private Scheduler m_scheduler;
    private ReportDefinitionBuilder m_reportDefinitionBuilder;

    public void start() throws InterruptedException, ParseException, SchedulerException, ClassNotFoundException, NoSuchMethodException {
        for (ReportDefinition reportDef : m_reportDefinitionBuilder.buildReportDefinitions()) {
            scheduleReport(reportDef);
        }
    }

    private void scheduleReport(ReportDefinition reportDef) throws ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException {
        MethodInvokingJobDetailFactoryBean jobFactory = new MethodInvokingJobDetailFactoryBean();
        jobFactory.setTargetObject(this);
        jobFactory.setTargetMethod("runReport");
        jobFactory.setArguments(new Object[] { reportDef });
        jobFactory.setConcurrent(false);
        jobFactory.setBeanName(reportDef.getDescription());
        jobFactory.afterPropertiesSet();
        JobDetail jobDetail = (JobDetail) jobFactory.getObject();
        
        CronTriggerBean cronReportTrigger = new CronTriggerBean();
        cronReportTrigger.setBeanName(reportDef.getDescription());
        cronReportTrigger.setJobDetail(jobDetail);
        cronReportTrigger.setCronExpression(reportDef.getCronExpression());
        cronReportTrigger.afterPropertiesSet();
        
        m_scheduler.scheduleJob(cronReportTrigger.getJobDetail(), cronReportTrigger);
        log().debug("Schedule report " + cronReportTrigger);
    }

    public void runReport(ReportDefinition reportDef) throws Throwable {
        final ReportInstance report;
        try {
            report = reportDef.createReport(m_resourceDao, m_rrdDao, m_filterDao);
        } catch (Throwable t) {
            log().error("Could not create a report instance for report definition " + reportDef + ": " + t, t);
            throw t;
        }
        
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                log().debug("Starting report " + report);
                report.walk();
                log().debug("Completed report " + report);
                
                m_reportPersister.persist(report);
                log().debug("Report " + report + " persisted");
            }
        });
    }

    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceDao != null, "property resourceDao must be set to a non-null value");
        Assert.state(m_rrdDao != null, "property rrdDao must be set to a non-null value");
        Assert.state(m_filterDao != null, "property filterDao must be set to a non-null value");
        Assert.state(m_transactionTemplate != null, "property transactionTemplate must be set to a non-null value");
        Assert.state(m_reportPersister != null, "property reportPersister must be set to a non-null value");
        Assert.state(m_scheduler != null, "property scheduler must be set to a non-null value");
        Assert.state(m_reportDefinitionBuilder != null, "property reportDefinitionBuilder must be set to a non-null value");
    }
    
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

    public TransactionTemplate getTransactionTemplate() {
        return m_transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
    
    public ReportPersister getReportPersister() {
        return m_reportPersister;
    }

    public void setReportPersister(ReportPersister reportPersister) {
        m_reportPersister = reportPersister;
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public ReportDefinitionBuilder getReportDefinitionBuilder() {
        return m_reportDefinitionBuilder;
    }

    public void setReportDefinitionBuilder(ReportDefinitionBuilder reportDefinitionBuilder) {
        m_reportDefinitionBuilder = reportDefinitionBuilder;
    }

    public FilterDao getFilterDao() {
        return m_filterDao;
    }

    public void setFilterDao(FilterDao filterDao) {
        m_filterDao = filterDao;
    }
}
