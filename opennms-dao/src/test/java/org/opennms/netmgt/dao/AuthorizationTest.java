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
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AuthorizationTest implements InitializingBean {
    
    @Autowired
    AlarmDao m_alarmDao;

    @Autowired
    CategoryDao m_categoryDao;

    @Autowired
    DatabasePopulator m_populator;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_alarmDao);
        assertNotNull(m_categoryDao);
        assertNotNull(m_populator);
    }

    @Before
    @Transactional
    public void setUp() throws Exception {
        m_populator.populateDatabase();
    }

    @Test
    @Transactional
    public void testAuthorizedAlarms() {
       
       Collection<OnmsAlarm> matching = m_alarmDao.findAll();
       
       assertNotNull(matching);
       assertEquals(1, matching.size());
       
       System.err.println(matching);
       
       enableAuthorizationFilter("NonExistentGroup");

       Collection<OnmsAlarm> matching2 = m_alarmDao.findAll();

       assertNotNull(matching2);
       assertEquals(0, matching2.size());
       
       System.err.println(matching2);

       disableAuthorizationFilter();

       Collection<OnmsAlarm> matching3 = m_alarmDao.findAll();

       assertNotNull(matching3);
       assertEquals(1, matching3.size());
               
       System.err.println(matching3);
    }
   
   @Test
   @Transactional
   public void testGetCategoriesWithAuthorizedGroups() {
       
       List<OnmsCategory> categories = m_categoryDao.getCategoriesWithAuthorizedGroup("RoutersGroup");
       
       assertNotNull(categories);
       assertEquals(1, categories.size());
       assertEquals("Routers", categories.get(0).getName());
       
   }
   
   public void enableAuthorizationFilter(final String... groupNames) {
       
       HibernateCallback<Object> cb = new HibernateCallback<Object>() {

           public Object doInHibernate(Session session) throws HibernateException, SQLException {
               session.enableFilter("authorizedOnly").setParameterList("userGroups", groupNames);
               return null;
           }
           
       };
       
       ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
   }

   public void disableAuthorizationFilter() {
       
       HibernateCallback<Object> cb = new HibernateCallback<Object>() {

           public Object doInHibernate(Session session) throws HibernateException, SQLException {
               session.disableFilter("authorizedOnly");
               return null;
           }
           
       };
       
       ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
   }
}
