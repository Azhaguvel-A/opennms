/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jul 04: Test for the case where no date is set on the event. - dj@opennms.org
 * 
 * Created: January 15, 2008
 *
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
package org.opennms.netmgt.eventd;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventIpcManagerDefaultImplTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private EventIpcManagerDefaultImpl m_manager;
    private EventHandler m_eventHandler = m_mocks.createMock(EventHandler.class);
    private MockEventListener m_listener = new MockEventListener();
    private Throwable m_caughtThrowable = null;
    private Thread m_caughtThrowableThread = null;

    @Override
    public void setUp() throws Exception {
        m_manager = new EventIpcManagerDefaultImpl();
        m_manager.setEventHandler(m_eventHandler);
        m_manager.setHandlerPoolSize(5);
        m_manager.afterPropertiesSet();

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable throwable) {
                m_caughtThrowable = throwable;
                m_caughtThrowableThread = thread;
            }
        });
    }
    
    @Override
    public void runTest() throws Throwable {
        super.runTest();
        
        assertEquals("unprocessed received events", 0, m_listener.getEvents().size());
        
        if (m_caughtThrowable != null) {
            throw new Exception("Thread " + m_caughtThrowableThread + " threw an uncaught exception: " + m_caughtThrowable, m_caughtThrowable);
        }
    }
    
    public void testInitWithNoHandlerPoolSize() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("handlerPoolSize not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventHandler(m_eventHandler);
        
        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInitWithNoEventHandler() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("eventHandler not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setHandlerPoolSize(5);

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInit() throws Exception {
        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventHandler(m_eventHandler);
        manager.setHandlerPoolSize(5);
        manager.afterPropertiesSet();
    }
    
    public void testBroadcastWithNoListeners() throws Exception {
        Event e = new Event();

        m_mocks.replayAll();

        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public void testSendNowNullEvent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event argument cannot be null"));

        try {
            m_manager.sendNow((Event) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testSendNowNullEventLog() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("eventLog argument cannot be null"));

        try {
            m_manager.sendNow((Log) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerAndBroadcast() throws Exception {
        Event e = new Event();

        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }

    public void testAddEventListenerTwoArgumentListNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null, new ArrayList<String>(0));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerTwoArgumentListNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("ueilist argument cannot be null"));

        try {
            m_manager.addEventListener(m_listener, (List<String>) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerTwoArgumentStringAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartMultipleTrimAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo/bar");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooLittleAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooMuchAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/*");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }

    public void testAddEventListenerWithUeiAndSubUeiMatchAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/foo");
        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null, "");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerTwoArgumentStringNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("uei argument cannot be null"));

        try {
            m_manager.addEventListener(m_listener, (String) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentListNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null, new ArrayList<String>(0));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentListNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("ueilist argument cannot be null"));

        try {
            m_manager.removeEventListener(m_listener, (List<String>) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testRemoveEventListenerTwoArgumentStringNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null, "");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentStringNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("uei argument cannot be null"));

        try {
            m_manager.removeEventListener(m_listener, (String) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerThenAddEventListenerWithUeiAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerWithUeiAndBroadcastThenAddEventListener() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    

    /**
     * This is the type of exception we want to catch.
     * 
     * 2006-05-28 18:30:12,532 WARN  [EventHandlerPool-fiber0] OpenNMS.Xmlrpcd.org.opennms.netmgt.eventd.EventHandler: Unknown exception processing event
     * java.lang.NullPointerException
     *    at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1076)
     *    at java.text.DateFormat.parse(DateFormat.java:333)
     *    at org.opennms.netmgt.EventConstants.parseToDate(EventConstants.java:744)
     *    at org.opennms.netmgt.eventd.Persist.getEventTime(Persist.java:801)
     *    at org.opennms.netmgt.eventd.Persist.insertEvent(Persist.java:581)
     *    at org.opennms.netmgt.eventd.EventWriter.persistEvent(EventWriter.java:131)
     *    at org.opennms.netmgt.eventd.EventHandler.run(EventHandler.java:154)
     *    at org.opennms.core.concurrent.RunnableConsumerThreadPool$FiberThreadImpl.run(RunnableConsumerThreadPool.java:412)
     *    at java.lang.Thread.run(Thread.java:613)
     */
    public void testNoDateDate() throws InterruptedException {
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeLostService");
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        e.setService("ICMP");

        m_mocks.replayAll();

        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public class MockEventListener implements EventListener {
        private List<Event> m_events = new ArrayList<Event>();
        
        public String getName() {
            return "party on, Wayne";
        }

        public void onEvent(Event e) {
            m_events.add(e);
        }
        
        public List<Event> getEvents() {
            return m_events;
        }
    }
}
