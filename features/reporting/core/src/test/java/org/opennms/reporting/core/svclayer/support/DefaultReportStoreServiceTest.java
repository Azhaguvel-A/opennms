/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 12th 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.reporting.core.svclayer.support;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.netmgt.dao.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:org/opennms/reporting/core/svclayer/support/DefaultReportStoreServiceTest.xml"
})
public class DefaultReportStoreServiceTest implements InitializingBean {

    @Autowired
    ReportStoreService m_reportStoreService;
    
    @Autowired
    ReportCatalogDao m_reportCatalogDao;
    
    @Autowired
    ReportServiceLocator m_reportServiceLocator;
    
    @Autowired
    DatabaseReportConfigDao m_databaseReportConfigDao;
    
    public void afterPropertiesSet() {
        Assert.assertNotNull(m_reportStoreService);
        Assert.assertNotNull(m_reportCatalogDao);
        Assert.assertNotNull(m_reportServiceLocator);
        Assert.assertNotNull(m_databaseReportConfigDao);
    }
    
    @Test
    public void testSave(){
        
        ReportCatalogEntry reportCatalogEntry = new ReportCatalogEntry();
        m_reportCatalogDao.save(reportCatalogEntry);
        m_reportCatalogDao.flush();
        replay(m_reportCatalogDao);
        
        m_reportStoreService.save(reportCatalogEntry);
        verify(m_reportCatalogDao);
        
    }
    
    @Test
    public void testReder(){
        // TODO something useful here
    }
    
}
