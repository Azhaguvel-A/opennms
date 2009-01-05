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
 * September 10, 2008
 * Walt Bowers
 * - Added a computation for connection attempts
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.tl1d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Category;
import org.opennms.netmgt.config.tl1d.Tl1Element;

/**
 * Default Implementation of the Tl1Client API.
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 */
public class Tl1ClientImpl implements Tl1Client {

    String m_host;
    int m_port;
    boolean m_started = false;

    private Socket m_tl1Socket;
    private Thread m_socketReader;
    private BlockingQueue<Tl1AutonomousMessage> m_tl1Queue;
    private BufferedReader m_reader;
    private TimeoutSleeper m_sleeper;
    private Category m_log;
    private Tl1AutonomousMessageProcessor m_messageProcessor;
    //private long m_reconnectionDelay = 30000;
    private long m_reconnectionDelay;  //see configuration xsd for default and set by Tl1d after instantiation
    private int m_reconnectAttempts = 0;
    
    public Tl1ClientImpl() {
    }
    
    public Tl1ClientImpl(BlockingQueue<Tl1AutonomousMessage> queue, Tl1Element element, Category log) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        
        m_host = element.getHost();
        m_port = element.getPort();
        
        m_tl1Queue = queue;
        m_messageProcessor = (Tl1AutonomousMessageProcessor) Class.forName(element.getTl1MessageParser()).newInstance();
        m_reconnectionDelay = element.getReconnectDelay();
        m_log = log;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#start()
     */
    public void start() {
        m_log.info("start: TL1 client: "+m_host+":"+String.valueOf(m_port));
        m_log.info("start:Connection delay = " + m_reconnectionDelay );
        m_started = true;

        m_socketReader = new Thread("TL1-Socket-Reader") {

            public void run() {
                readMessages();
            }

        };

        m_socketReader.start();
        m_log.info("Started TL1 client: "+m_host+":"+String.valueOf(m_port));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#stop()
     */
    public void stop() {
        m_log.info("Stopping TL1 client: "+m_host+":"+String.valueOf(m_port));
        m_started = false;
    }

    private BufferedReader getReader() {
        if (m_reader == null) {
            m_reader = createReader();
        }
        return m_reader;
    }

    private BufferedReader createReader() {
        BufferedReader reader;
        while (m_started) {
            try {
                m_tl1Socket = new Socket(m_host, m_port);
                reader = new BufferedReader(new InputStreamReader(m_tl1Socket.getInputStream()));
                resetTimeout();
                return reader;
            } catch (IOException e) {
                m_log.error("TL1 Connection Failed to " + m_host + ":" + m_port);
                m_log.debug(e);
                waitUntilNextConnectTime();
            } 
        }
        return null;
    }

    private void resetTimeout() {
        m_log.debug("resetTimeout: Resetting timeout Thread");
        m_reconnectAttempts = 0;
        m_sleeper = null;
    }

    private void waitUntilNextConnectTime() {
        m_log.debug("waitUntilNextConnectTime: current connection attempts: "+m_reconnectAttempts);
        if (m_started) {
            if (m_sleeper == null) {
                m_sleeper = new TimeoutSleeper();
            }
            
            m_reconnectAttempts++;
            /* If the system is not responding, we want to wait longer and longer for the retry */
            long waitTime = computeWait();
            m_log.info("waitUntilNextConnectTime: Waiting " + waitTime + " ms......");
            try { m_sleeper.sleep(waitTime); } catch (InterruptedException e) {}
        }
    }

    private long computeWait() {
        long waitTime = m_reconnectionDelay;
        
        if (m_reconnectAttempts > 5) {
            waitTime = m_reconnectionDelay * 5;
        } else if (m_reconnectAttempts > 10) {
            waitTime = m_reconnectionDelay * 10;
        }
        return waitTime;
    }

    private void readMessages() {
        StringBuilder rawMessageBuilder = new StringBuilder();
        
        m_log.info("readMessages: Begin reading off socket...");
        while(m_started) {
            try {
                m_log.info("readMessages: reading line from TL1 socket...");
                BufferedReader reader = getReader();
                
                if (reader != null) {
                    int ch;
                    while((ch = reader.read()) != -1) {
                        rawMessageBuilder.append((char)ch);
                        
                        if((char)ch == ';') {
                            createAndQueueTl1Message(rawMessageBuilder);
                            rawMessageBuilder.setLength(0);
                        }
                    }
                    m_log.warn("readMessages: resetting socket reader to client: "+m_host+":"+m_port);
                    resetReader(null);
                }
            } catch (IOException e) {
                resetReader(e);
            }
        }
        m_log.info("Stopping TL1 client: "+m_host+":"+String.valueOf(m_port));
    }

    private void createAndQueueTl1Message(StringBuilder rawMessageBuilder) {
        m_log.debug("readMessages: offering message to queue: "+rawMessageBuilder.toString());
        Tl1AutonomousMessage message = detectMessageType(rawMessageBuilder);
        if (message != null) {
            m_tl1Queue.offer(message);
            m_log.debug("readMessages: successfully offered to queue.");
        } else {
            m_log.debug("readMessages: message was null, not offered to queue.");
        }
    }

    //TODO: Lots of work to do here
    private Tl1AutonomousMessage detectMessageType(StringBuilder rawMessage) {
        
        //check token 5 to see if this is a reply message.  This implies that the Tl1Client must
        //track message TAGs (Correlation TAGs (CTAG) vs. Autonomous TAGs (ATAG))
        
        if(isAutonomousMessage(rawMessage)) {
            return m_messageProcessor.process(rawMessage.toString(), Tl1Message.AUTONOMOUS);
        }
        
        return null;
    }

    //TODO: Lots of work to do here
    private boolean isAutonomousMessage(StringBuilder rawMessage) {
        return true;
    }

    private void resetReader(IOException ex) {
        if (ex != null) {
            m_log.error("resetReader: connection failure.", ex);
        }
        try {
            m_reader.close();
        } catch (IOException e) { 
        } finally {
            m_reader = null;
        }
        try {
            m_tl1Socket.close();
        } catch (IOException e) {
            m_tl1Socket = null;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getHost()
     */
    public String getHost() {
        return m_host;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setHost(java.lang.String)
     */
    public void setHost(String host) {
        m_host = host;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getPort()
     */
    public int getPort() {
        return m_port;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setPort(int)
     */
    public void setPort(int port) {
        m_port = port;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getReconnectionDelay()
     */
     public long getReconnectionDelay() {
        return m_reconnectionDelay;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setReconnectionDelay(long)
     */
    public void setReconnectionDelay(long reconnectionDelay) {
        m_reconnectionDelay = reconnectionDelay;
    }

    public BlockingQueue<Tl1AutonomousMessage> getTl1Queue() {
        return m_tl1Queue;
    }

    public void setTl1Queue(BlockingQueue<Tl1AutonomousMessage> tl1Queue) {
        m_tl1Queue = tl1Queue;
    }

    public Tl1AutonomousMessageProcessor getMessageProcessor() {
        return m_messageProcessor;
    }

    public void setMessageProcessor(Tl1AutonomousMessageProcessor messageProcessor) {
        m_messageProcessor = messageProcessor;
    }

    public void setLog(Category log) {
        m_log = log;
    }

    private class TimeoutSleeper {

        public void sleep() throws InterruptedException {
            Thread.sleep(m_reconnectionDelay);
        }

        public void sleep(long sleepTime) throws InterruptedException {
            Thread.sleep(sleepTime);
        }
    }
}
