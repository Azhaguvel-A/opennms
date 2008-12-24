/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc. All rights
 * reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 23: Java 5 generics, eliminate partially used s_loggingSetup. - dj@opennms.org
 * 2006 Aug 15: fix logger for org.snmp4j - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.test.mock;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author brozow
 */
public class MockLogAppender extends AppenderSkeleton {
    private static List<LoggingEvent> s_events = null;

    private static Level s_logLevel = Level.ALL;

    public MockLogAppender() {
        super();
        resetEvents();
        resetLogLevel();
    }

    public synchronized void doAppend(LoggingEvent event) {
        super.doAppend(event);
        receivedLogLevel(event.getLevel());
    }

    protected void append(LoggingEvent event) {
        s_events.add(event);
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

    public static void resetEvents() {
        s_events = Collections.synchronizedList(new LinkedList<LoggingEvent>());
    }

    public static LoggingEvent[] getEvents() {
        return (LoggingEvent[]) s_events.toArray(new LoggingEvent[0]);
    }

    public static LoggingEvent[] getEventsGreaterOrEqual(Level level) {
        LinkedList<LoggingEvent> matching = new LinkedList<LoggingEvent>();

        synchronized (s_events) {
            for (LoggingEvent event : s_events) {
                if (event.getLevel().isGreaterOrEqual(level)) {
                    matching.add(event);
                }
            }
        }

        return matching.toArray(new LoggingEvent[0]);
    }

    public static void setupLogging() {
        setupLogging(new Properties());
    }

    public static void setupLogging(Properties config) {
        setupLogging(true, config);
    }

    public static void setupLogging(boolean toConsole) {
        setupLogging(toConsole, new Properties());
    }

    public static void setupLogging(boolean toConsole, Properties props) {
        String level = System.getProperty("mock.logLevel", "DEBUG");
        setupLogging(toConsole, level, props);
    }
    
    public static void setupLogging(boolean toConsole, String level) {
        setupLogging(toConsole, level, new Properties());
    }
    
    public static void setupLogging(boolean toConsole, String level, Properties config) {
        resetLogLevel();
        
        Properties logConfig = new Properties(config);

        String consoleAppender = (toConsole ? ", CONSOLE" : "");
        
        setProperty(logConfig, "log4j.appender.CONSOLE",
                      "org.apache.log4j.ConsoleAppender");
        setProperty(logConfig, "log4j.appender.CONSOLE.layout",
                      "org.apache.log4j.PatternLayout");
        setProperty(logConfig, "log4j.appender.CONSOLE.layout.ConversionPattern",
                      "%d %-5p [%t] %c: %m%n");
        setProperty(logConfig, "log4j.appender.MOCK", MockLogAppender.class.getName());
        setProperty(logConfig, "log4j.appender.MOCK.layout",
                      "org.apache.log4j.PatternLayout");
        setProperty(logConfig, "log4j.appender.MOCK.layout.ConversionPattern",
                      "%-5p [%t] %c: %m%n");

        setProperty(logConfig, "log4j.rootCategory", level + consoleAppender
                + ", MOCK");
        setProperty(logConfig, "log4j.logger.org.apache.commons.httpclient.HttpMethodBase", "ERROR");
        setProperty(logConfig, "log4j.logger.org.exolab.castor", "INFO");
        setProperty(logConfig, "log4j.logger.org.snmp4j", "ERROR");
        setProperty(logConfig, "log4j.logger.org.snmp4j.agent", "ERROR");
        setProperty(logConfig, "log4j.logger.org.hibernate.cfg.AnnotationBinder",
                      "ERROR" + consoleAppender + ", MOCK");
        
        PropertyConfigurator.configure(logConfig);
    }
    
    private static void setProperty(Properties logConfig, String key, String value) {
        if (!logConfig.containsKey(key)) {
            logConfig.put(key, value);
        }
    }

    public static boolean isLoggingSetup() {
        return s_events != null;
    }

    public static void receivedLogLevel(Level level) {
        if (level.isGreaterOrEqual(s_logLevel)) {
            s_logLevel = level;
        }
    }

    public static void resetLogLevel() {
        s_logLevel = Level.ALL;
    }

    public static boolean noWarningsOrHigherLogged() {
        return Level.INFO.isGreaterOrEqual(s_logLevel);
    }

    public static void assertNotGreaterOrEqual(Level level) throws AssertionFailedError {
        if (!isLoggingSetup()) {
            throw new AssertionFailedError("MockLogAppender has not been initialized");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // do nothing
        }
        LoggingEvent[] events = getEventsGreaterOrEqual(level);
        if (events.length == 0) {
            return;
        }

        StringBuffer message = new StringBuffer("Log messages at or greater than the log level "
                + level.toString() + " received:");

        for (LoggingEvent event : events) {
            message.append("\n\t[" + event.getLevel().toString() + "] "
                    + event.getLoggerName() + ": "
                    + event.getMessage());
        }

        throw new AssertionFailedError(message.toString());
    }

    public static void assertNoWarningsOrGreater() throws AssertionFailedError {
        assertNotGreaterOrEqual(Level.WARN);
    }
}
