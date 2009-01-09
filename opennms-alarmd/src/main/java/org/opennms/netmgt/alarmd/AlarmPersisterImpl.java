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
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.alarmd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Singleton to persist OnmsAlarms.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AlarmPersisterImpl implements AlarmPersister {

    private AlarmDao m_alarmDao;
    private EventDao m_eventDao;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.AlarmPersister#persist(org.opennms.netmgt.xml.event.Event)
     */
    public void persist(Event event) {
        if (!checkEventSanityAndDoWeProcess(event)) {
            return;
        }
        log().debug("process: " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());

        addOrReduceEventAsAlarm(event);
    }

    @Transactional
    private void addOrReduceEventAsAlarm(Event event) {
        OnmsEvent e = m_eventDao.get(event.getDbid());
        Assert.notNull(e, "Event was deleted before we could retrieve it and create an alarm."); //TODO: Understand why we use Assert

        OnmsAlarm alarm = m_alarmDao.findByReductionKey(event.getAlarmData().getReductionKey());

        if (alarm == null) {
            alarm = createNewAlarm(e, event);
            
            //FIXME: this should be a cascaded save
            m_alarmDao.save(alarm);
            m_eventDao.saveOrUpdate(e);
        } else {
            reduceEvent(e, alarm);
            m_alarmDao.update(alarm);
            m_eventDao.update(e);

            if (event.getAlarmData().isAutoClean()) {
                m_eventDao.deletePreviousEventsForAlarm(alarm.getId(), e);
            }
        }
    }

    private void reduceEvent(OnmsEvent e, OnmsAlarm alarm) {
        alarm.setLastEvent(e);
        alarm.setLastEventTime(e.getEventTime());
        alarm.setLogMsg(e.getEventLogMsg());
        alarm.setCounter(alarm.getCounter() + 1);
        e.setAlarm(alarm);
    }

    private OnmsAlarm createNewAlarm(OnmsEvent e, Event event) {
        OnmsAlarm alarm;
        alarm = new OnmsAlarm();
        alarm.setAlarmType(event.getAlarmData().getAlarmType());
        alarm.setClearKey(event.getAlarmData().getClearKey());
        alarm.setClearUei(event.getAlarmData().getClearUei());
        alarm.setCounter(1);
        alarm.setDescription(e.getEventDescr());
        alarm.setDistPoller(e.getDistPoller());
        alarm.setEventParms(e.getEventParms());
        alarm.setFirstEventTime(e.getEventTime());
        alarm.setIfIndex(e.getIfIndex());
        alarm.setIpAddr(e.getIpAddr());
        alarm.setLastEventTime(e.getEventTime());
        alarm.setLastEvent(e);
        alarm.setLogMsg(e.getEventLogMsg());
        alarm.setMouseOverText(e.getEventMouseOverText());
        alarm.setNode(e.getNode());
        alarm.setOperInstruct(e.getEventOperInstruct());
        alarm.setReductionKey(event.getAlarmData().getReductionKey());
        alarm.setServiceType(e.getServiceType());
        alarm.setSeverity(OnmsSeverity.get(e.getEventSeverity())); //TODO: what to do?
        alarm.setSeverityId(e.getEventSeverity());  //TODO: what to do?
        alarm.setSuppressedUntil(e.getEventTime()); //TODO: fix UI to not require this be set
        alarm.setSuppressedTime(e.getEventTime()); //TODO: Fix UI to not require this be set
        //alarm.setTTicketId(e.getEventTTicket());
        //alarm.setTTicketState(TroubleTicketState.CANCEL_FAILED);  //FIXME
        alarm.setUei(e.getEventUei());
        e.setAlarm(alarm);
        return alarm;
    }
    
    private boolean checkEventSanityAndDoWeProcess(final Event event) {
        Assert.notNull(event, "event argument must not be null");
        Assert.notNull(event.getLogmsg(), "event does not have a logmsg");
        Assert.notNull(event.getLogmsg().getDest(), "event logmsg does not have a destination");
        Assert.isTrue(event.getDbid() > 0, "event does not have a dbid");//TODO: figure out what happens when this exception is thrown
        
        if ("donotpersist".equals(event.getLogmsg().getDest())) {
            log().debug("checkEventSanity" + ": uei '" + event.getUei() + "' marked as 'donotpersist'; not processing event.");
            return false;
        }
        
        if (event.getAlarmData() == null) {
            log().debug("checkEventSanity" + ": uei '" + event.getUei() + "' has no alarm data'; not processing event.");
            return false;
        }
        
        
        return true;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

    public EventDao getEventDao() {
        return m_eventDao;
    }

}
