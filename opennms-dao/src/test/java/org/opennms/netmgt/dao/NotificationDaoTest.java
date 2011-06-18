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
// 2007 Jul 03: Organize imports. - dj@opennms.org
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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NotificationDaoTest {
	@Autowired
	private DatabasePopulator m_databasePopulator;

	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private NotificationDao m_notificationDao;

	@Autowired
	private EventDao m_eventDao;
	
	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}
	
	@Test
	@Transactional
    public void testNotificationSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(7);
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/NotificationDaoTest");
//        OnmsAlarm alarm = new OnmsAlarm();
//        event.setAlarm(alarm);

        OnmsNode node = m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getIpAddress());
        m_eventDao.save(event);
        OnmsEvent newEvent = m_eventDao.load(event.getId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newEvent.getEventUei());
        
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(newEvent);
        notification.setTextMsg("Tests are fun!");
        m_notificationDao.save(notification);
       
        OnmsNotification newNotification = m_notificationDao.load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newNotification.getEvent().getEventUei());
    }
}
