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
package org.opennms.netmgt.ackd.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.ackd.AckService;
import org.opennms.netmgt.config.common.JavamailProperty;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaMailer;


/**
 * Acknowledgment Reader implementation using Java Mail
 * 
 * DONE: Identify acknowledgments for sent notifications
 * DONE: Identify acknowledgments for alarm IDs (how the send knows the ID, good question)
 * DONE: Persist acknowledgments
 * DONE: Identify escalation reply
 * DONE: Identify clear reply
 * DOND: Identify unacknowledged reply
 * DONE: Formalize Acknowledgment parameters (ack-type, id)
 * DONE: JavaMail configuration factory
 * DONE: Ackd configuration factory
 * TODO: Associate email replies with openNMS user
 * TODO: Finish scheduling component of JavaAckReader
 * TODO: Configurable Schedule
 * DONE: Identify Java Mail configuration element to use for reading replies
 * TODO: Migrate JavaMailNotificationStrategy to new JavaMail Configuration
 * TODO: Migrate Availability Reports send via JavaMail to new JavaMail Configuration
 * TODO: Move reading email messages from MTM and this class to JavaMailer class
 * TODO: Do some proper logging
 * 
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * 
 */
public class JavaMailAckReaderImpl implements AckReader {

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int FINISHING = 2;
    private int m_status;
    
    private Timer m_timer;
    
    //I think this is a object reference leak... need a factory or something
    private AckService m_ackService;

    //Should look at using autowired annotation
    private AckdConfigurationDao m_daemonConfigDao;
    private JavaMailConfigurationDao m_jmConfigDao;

    protected void findAndProcessAcks() {
        
        Collection<OnmsAcknowledgment> acks;

        try {
            List<Message> msgs = readMessages();  //TODO: need a read *new* messages feature
            acks = detectAcks(msgs);
            m_ackService.processAcks(acks);
        } catch (JavaMailerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    protected Collection<OnmsAcknowledgment> detectAcks(List<Message> msgs) {
        Collection<OnmsAcknowledgment> acks = null;
        
        if (msgs != null) {
            acks = new ArrayList<OnmsAcknowledgment>();
            for (Message msg : msgs) {
                try {
                    Integer id = detectId(msg.getSubject(), m_daemonConfigDao.getConfig().getNotifyidMatchExpression());
                    
                    if (id.intValue() > 0) {
                        final OnmsAcknowledgment ack = createAcknowledgment(msg, id);
                        ack.setAckType(AckType.NOTIFICATION);
                        acks.add(ack);
                        continue;
                    }
                    
                    id = detectId(msg.getSubject(), m_daemonConfigDao.getConfig().getAlarmidMatchExpression());
                    
                    if (id.intValue() > 0) {
                        final OnmsAcknowledgment ack = createAcknowledgment(msg, id);
                        ack.setAckType(AckType.ALARM);
                        acks.add(ack);
                        continue;
                    }
                    
                } catch (MessagingException e) {
                    //FIXME: do something audit like here
                    continue;
                } catch (IOException e) {
                    // FIXME: ditto
                    continue;
                }
            }
        }
        return acks;
    }


    protected Integer detectId(final String subject, final String expression) {
        Integer id = Integer.valueOf(0);
        
        //TODO: force opennms config '~' style regex attribute identity because this is the only way for this to work
        if (expression.startsWith("~")) {
            String ackExpression = expression.substring(1);
            Pattern pattern = Pattern.compile(ackExpression);
            Matcher matcher = pattern.matcher(subject);
            
            if (matcher.matches() && matcher.groupCount() > 0) {
                id = Integer.valueOf(matcher.group(1));
            }
            
        }
        return id;
    }

    protected OnmsAcknowledgment createAcknowledgment(Message msg, Integer refId) throws MessagingException, IOException {
        String ackUser = ((InternetAddress)msg.getFrom()[0]).getAddress();
        Date ackTime = msg.getReceivedDate();
        OnmsAcknowledgment ack = new OnmsAcknowledgment(ackTime, ackUser);
        ack.setAckType(AckType.NOTIFICATION);
        ack.setAckAction(determineAckAction(msg));
        ack.setRefId(refId);
        return ack;
    }

    protected AckAction determineAckAction(Message msg) throws IOException, MessagingException {
        if (msg.getContent().toString().matches(m_daemonConfigDao.getConfig().getAckExpression())) {
            return AckAction.ACKNOWLEDGE;
        } else if (msg.getContent().toString().matches(m_daemonConfigDao.getConfig().getClearExpression())) {
            return AckAction.CLEAR;
        } else if (msg.getContent().toString().matches(m_daemonConfigDao.getConfig().getEscalateExpression())) {
            return AckAction.ESCALATE;
        } else if (msg.getContent().toString().matches(m_daemonConfigDao.getConfig().getUnackExpression())) {
            return AckAction.UNACKNOWLEDGE;
        } else {
            return AckAction.UNSPECIFIED;
        }
    }

    private List<Message> readMessages() throws JavaMailerException {
        List<Message> messages = null;
        
        ReadmailConfig config = m_jmConfigDao.getReadMailConfig(m_daemonConfigDao.getConfig().getReadmailConfig());
        String protocol = config.getReadmailHost().getReadmailProtocol().getTransport();
        Properties jmProps = createProperties(config.getJavamailPropertyCollection());
        jmProps.put("mail." + protocol + ".host", config.getReadmailHost().getHost());
        jmProps.put("mail." + protocol + ".user", config.getUserAuth().getUserName());
        jmProps.put("mail." + protocol + ".port", config.getReadmailHost().getPort());
        jmProps.put("mail." + protocol + ".starttls.enable", config.getReadmailHost().getReadmailProtocol().isStartTls());
        jmProps.put("mail.smtp.auth", "true");

        if (config.getReadmailHost().getReadmailProtocol().isSslEnable()) {
            jmProps.put("mail." + protocol + ".socketFactory.port", config.getReadmailHost().getPort());
            jmProps.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            jmProps.put("mail." + protocol + ".socketFactory.fallback", "false");
        }

        //FIXME: need config for these
        jmProps.put("mail." + protocol + ".connectiontimeout", 3000);
        jmProps.put("mail." + protocol + ".timeout", 3000);
        jmProps.put("mail.store.protocol", protocol);

        Store mailStore = null;
        Folder mailFolder = null;
        

        try {
            JavaMailer readMailer = new JavaMailer(jmProps);
            TimeoutTrackerMap map = new TimeoutTrackerMap(Integer.valueOf(3), Integer.valueOf(3000), Boolean.TRUE);
            TimeoutTracker tracker = new TimeoutTracker(map.getParameterMap(), 1, 3000);

            for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
                tracker.startAttempt();
                
                try {
                    mailStore = readMailer.getSession().getStore();
                    mailFolder = retrieveMailFolder(config, mailStore);
                    mailFolder.open(Folder.READ_WRITE);  //TODO: Make sure configuration supports flag for deleting acknowledgments
                    
                    if (mailFolder.isOpen()) {
                        Message[] msgs = mailFolder.getMessages();
                        messages = Arrays.asList(msgs);
                    }
                } catch (MessagingException e) {
                    //TODO: something clever here and continue?
                    continue;
                }
            }

            
        } finally {
            
        }

        return messages;
    }


    /**
     * Establish connection with mail store and return the configured mail folder.
     * 
     * @param mailParms
     * @param mailStore
     * @return the folder specified in configuration
     * @throws MessagingException
     */
    private Folder retrieveMailFolder(final ReadmailConfig config, final Store mailStore) throws MessagingException {
        mailStore.connect(config.getReadmailHost().getHost(), (int)config.getReadmailHost().getPort(), config.getUserAuth().getUserName(), config.getUserAuth().getPassword());
        
        //TODO: figure out the difference between getting a named folder from the store and getting a named folder from a folder (perhaps a heiarchy thing?)
        Folder mailFolder = mailStore.getDefaultFolder();
        mailFolder = mailFolder.getFolder(config.getMailFolder());
        if (!mailFolder.exists()) {
            throw new IllegalArgumentException("The specified mail folder doesn't exist in the store: "+config.getMailFolder());
        }
        return mailFolder;
    }

    
    private class TimeoutTrackerMap {
        Map<String, String> m_map;
        
        TimeoutTrackerMap(Integer retry, Integer timeout, Boolean strict) {
            
            m_map = new HashMap<String, String>();
            m_map.put("timeout", timeout.toString());
            m_map.put("retry", retry.toString());
            m_map.put("strict-timeout", strict.toString());
        }
        
        public Map<String, String> getParameterMap() {
            return m_map;
        }
        
    }

    private Properties createProperties(final List<JavamailProperty> javamailPropertyCollection) {
        Properties props = new Properties();
        
        for (JavamailProperty javamailProperty : javamailPropertyCollection) {
            props.setProperty(javamailProperty.getName(), javamailProperty.getValue());
        }
        
        return props;
    }

    public void start(final AckdConfigurationDao config) {
        m_daemonConfigDao = config;
        scheduleReads();
    }
    
    public void pause() {
        unScheduleReads();
    }

    public void resume() {
        scheduleReads();
    }

    public void stop() {
        unScheduleReads();
    }

    private void unScheduleReads() {
        if (m_timer != null) {
            m_status = FINISHING;
            m_timer.cancel();
            m_timer = null;
        } else {
            //TODO: log something
        }
    }
    
    private void scheduleReads() {
        if (m_timer != null) {
            m_status = FINISHING;
            m_timer.cancel();
        }
        m_timer = new Timer("Ackd.JavaMailReader", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                findAndProcessAcks();  //should be something else, place holder for now
            }

        };
        m_timer.scheduleAtFixedRate(task, 3000, 3000);

    }
    
    public void setAckService(AckService ackService) {
        m_ackService = ackService;
    }

    public AckService getAckService() {
        return m_ackService;
    }

    public void setAckdConfig(AckdConfigurationDao configDao) {
        m_daemonConfigDao = configDao;
    }

    public void setJmConfigDao(JavaMailConfigurationDao jmConfigDao) {
        m_jmConfigDao = jmConfigDao;
    }

    public JavaMailConfigurationDao getJmConfigDao() {
        return m_jmConfigDao;
    }

}
