//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Dec 03: Organized imports, formatted code - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.notifd;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DestinationPathManager;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.notifd.Queue;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;

/**
 * This class is used to represent the notification execution service. When an
 * event is received by this service that has one of either a notification,
 * trouble ticket, or auto action then a process is launched to execute the
 * appropriate commands.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * 
 */
public final class Notifd extends AbstractServiceDaemon {

    /**
     * The singleton instance.
     */
    private static final Notifd m_singleton = new Notifd();

    /**
     * The map for holding different notice queues
     */
    private final Map<String, NoticeQueue> m_noticeQueues = new HashMap<String, NoticeQueue>();

    /**
     * 
     */
    private final Map<String, NotifdQueueHandler> m_queueHandlers = new HashMap<String, NotifdQueueHandler>();

    /**
     * The broadcast event receiver.
     */
    private volatile BroadcastEventProcessor m_eventReader;

    // Would be better if these were final but the are initialized in setters 
    private volatile EventIpcManager m_eventManager;

    private volatile NotifdConfigManager m_configManager;

    private volatile NotificationManager m_notificationManager;
    
    private volatile GroupManager m_groupManager;

    private volatile UserManager m_userManager;

    private volatile DestinationPathManager m_destinationPathManager;

    private volatile NotificationCommandManager m_notificationCommandManager;

    private volatile PollOutagesConfigManager m_pollOutagesConfigManager;

    /**
     * Constructs a new Notifd service daemon.
     */
    protected Notifd() {
    	super("OpenNMS.Notifd");
    }

    protected void onInit() {
        
        m_eventReader = new BroadcastEventProcessor();

        try {
            log().info("Notification status = " + getConfigManager().getNotificationStatus());

            Queue queues[] = getConfigManager().getConfiguration().getQueue();
            for (Queue queue : queues) {
                NoticeQueue curQueue = new NoticeQueue();

                Class<?> handlerClass = Class.forName(queue.getHandlerClass().getName());
                NotifdQueueHandler handlerQueue = (NotifdQueueHandler) handlerClass.newInstance();

                handlerQueue.setQueueID(queue.getQueueId());
                handlerQueue.setNoticeQueue(curQueue);
                handlerQueue.setInterval(queue.getInterval());

                m_noticeQueues.put(queue.getQueueId(), curQueue);
                m_queueHandlers.put(queue.getQueueId(), handlerQueue);
            }
        } catch (Throwable t) {
            log().error("start: Failed to load notifd queue handlers.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        m_eventReader.setDestinationPathManager(getDestinationPathManager());
        m_eventReader.setEventManager(getEventManager());
        m_eventReader.setGroupManager(getGroupManager());
        m_eventReader.setNoticeQueues(m_noticeQueues);
        m_eventReader.setNotifdConfigManager(getConfigManager());
        m_eventReader.setNotificationCommandManager(getNotificationCommandManager());
        m_eventReader.setNotificationManager(getNotificationManager());
        m_eventReader.setPollOutagesConfigManager(getPollOutagesConfigManager());
        m_eventReader.setUserManager(getUserManager());

        // start the event reader
        try {
            m_eventReader.init();
        } catch (Exception e) {
            log().error("Failed to setup event receiver", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * @return
     */
    public NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
    
    public void setConfigManager(NotifdConfigManager manager ) {
        m_configManager = manager;
    }
    
    public GroupManager getGroupManager() {
        return m_groupManager;
    }
    
    public void setGroupManager(GroupManager manager) {
        m_groupManager = manager;
    }
    
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    public void setUserManager(UserManager manager) {
        m_userManager = manager;
    }
    
    public DestinationPathManager getDestinationPathManager() {
        return m_destinationPathManager;
    }
    
    public void setDestinationPathManager(DestinationPathManager manager) {
        m_destinationPathManager = manager;
    }
    
    public NotificationCommandManager getNotificationCommandManager() {
        return m_notificationCommandManager;
    }

    public void setNotificationCommandManager(NotificationCommandManager manager) {
        m_notificationCommandManager = manager;
    }
    
    public NotificationManager getNotificationManager() {
        return m_notificationManager;
    }
    
    public void setNotificationManager(NotificationManager notificationManager) {
        m_notificationManager = notificationManager;
    }
    
    public BroadcastEventProcessor getBroadcastEventProcessor() {
        return m_eventReader;
    }

    protected void onStart() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.start();
        }
    }

    protected void onStop() {
        try {
            for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
                curHandler.stop();
            }
        } catch (Exception e) {
        }

        if (m_eventReader != null) {
            m_eventReader.close();
        }

        m_eventReader = null;
    }

    protected void onPause() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.pause();
        }
    }

    protected void onResume() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.resume();
        }
    }

    /**
     * Returns the singular instance of the Notifd daemon. There can be only
     * one instance of this service per virtual machine.
     */
    public static Notifd getInstance() {
        return m_singleton;
    }

    /**
     * @return
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    
    /**
     * @param eventManager The eventManager to set.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    public void setPollOutagesConfigManager(PollOutagesConfigManager configManager) {
        m_pollOutagesConfigManager = configManager;
    }
    
    public PollOutagesConfigManager getPollOutagesConfigManager() {
        return m_pollOutagesConfigManager;
    }
    
    

}
