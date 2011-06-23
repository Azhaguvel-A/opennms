/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import java.text.ParseException;
import java.util.Hashtable;

/**
 * <p>SyslogDefs class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @version $Id: $
 */
public class SyslogDefs {
    /** Constant <code>RCS_ID="$Id: SyslogDefs-OLD.java,v 1.1.1.1 1998"{trunked}</code> */
    public static final String RCS_ID = "$Id: SyslogDefs-OLD.java,v 1.1.1.1 1998/02/22 05:47:54 time Exp $";

    /** Constant <code>RCS_REV="$Revision: 1.1.1.1 $"</code> */
    public static final String RCS_REV = "$Revision: 1.1.1.1 $";

    /** Constant <code>RCS_NAME="$Name:  $"</code> */
    public static final String RCS_NAME = "$Name:  $";

    //
    // SyslogLvl
    //
    /** Constant <code>LOG_EMERG=0</code> */
    public static final int LOG_EMERG = 0; /* system is unusable */

    /** Constant <code>LOG_ALERT=1</code> */
    public static final int LOG_ALERT = 1; /*
                                             * action must be taken
                                             * immediately
                                             */

    /** Constant <code>LOG_CRIT=2</code> */
    public static final int LOG_CRIT = 2; /* critical conditions */

    /** Constant <code>LOG_ERR=3</code> */
    public static final int LOG_ERR = 3; /* error conditions */

    /** Constant <code>LOG_WARNING=4</code> */
    public static final int LOG_WARNING = 4; /* warning conditions */

    /** Constant <code>LOG_NOTICE=5</code> */
    public static final int LOG_NOTICE = 5; /*
                                             * normal but significant
                                             * condition
                                             */

    /** Constant <code>LOG_INFO=6</code> */
    public static final int LOG_INFO = 6; /* informational */

    /** Constant <code>LOG_DEBUG=7</code> */
    public static final int LOG_DEBUG = 7; /* debug-level messages */

    /** Constant <code>LOG_ALL=8</code> */
    public static final int LOG_ALL = 8; /* '*' in config, all levels */

    //
    // SyslogFac
    //
    /** Constant <code>LOG_KERN=0</code> */
    public static final int LOG_KERN = 0; /* kernel messages */

    /** Constant <code>LOG_USER=1</code> */
    public static final int LOG_USER = 1; /* random user-level messages */

    /** Constant <code>LOG_MAIL=2</code> */
    public static final int LOG_MAIL = 2; /* mail system */

    /** Constant <code>LOG_DAEMON=3</code> */
    public static final int LOG_DAEMON = 3; /* system daemons */

    /** Constant <code>LOG_AUTH=4</code> */
    public static final int LOG_AUTH = 4; /* security/authorization messages */

    /** Constant <code>LOG_SYSLOG=5</code> */
    public static final int LOG_SYSLOG = 5; /*
                                             * messages generated internally
                                             * by syslogd
                                             */

    /** Constant <code>LOG_LPR=6</code> */
    public static final int LOG_LPR = 6; /* line printer subsystem */

    /** Constant <code>LOG_NEWS=7</code> */
    public static final int LOG_NEWS = 7; /* network news subsystem */

    /** Constant <code>LOG_UUCP=8</code> */
    public static final int LOG_UUCP = 8; /* UUCP subsystem */

    /** Constant <code>LOG_CRON=9</code> */
    public static final int LOG_CRON = 9; /* clock daemon */
    
    /**********************************************
     * RFC3164 codifies facilities 10-15 as follows
     **********************************************/
    
    /** Constant <code>LOG_AUTHPRIV</code> */
    public static final int FACILITY_AUTHPRIV = 10; /* security / authorization (private) */
    
    /** Constant <code>LOG_FTP</code> */
    public static final int FACILITY_FTP = 11; /* FTP daemon */
    
    /** Constant <code>LOG_NTP</code> */
    public static final int FACILITY_NTP = 12; /* NTP subsystem */
    
    /** Constant <code>LOG_AUDIT</code> */
    public static final int FACILITY_AUDIT = 13;
    
    /** Constant <code>LOG_ALERT</code> */
    public static final int FACILITY_ALERT = 14;
    
    /** Constant <code>FACILITY_CLOCK</code> */
    public static final int FACILITY_CLOCK = 15;
    
    /** Constant <code>LOG_LOCAL0=16</code> */
    public static final int LOG_LOCAL0 = 16; /* reserved for local use */

    /** Constant <code>LOG_LOCAL1=17</code> */
    public static final int LOG_LOCAL1 = 17; /* reserved for local use */

    /** Constant <code>LOG_LOCAL2=18</code> */
    public static final int LOG_LOCAL2 = 18; /* reserved for local use */

    /** Constant <code>LOG_LOCAL3=19</code> */
    public static final int LOG_LOCAL3 = 19; /* reserved for local use */

    /** Constant <code>LOG_LOCAL4=20</code> */
    public static final int LOG_LOCAL4 = 20; /* reserved for local use */

    /** Constant <code>LOG_LOCAL5=21</code> */
    public static final int LOG_LOCAL5 = 21; /* reserved for local use */

    /** Constant <code>LOG_LOCAL6=22</code> */
    public static final int LOG_LOCAL6 = 22; /* reserved for local use */

    /** Constant <code>LOG_LOCAL7=23</code> */
    public static final int LOG_LOCAL7 = 23; /* reserved for local use */

    /** Constant <code>LOG_NFACILITIES=24</code> */
    public static final int LOG_NFACILITIES = 24; /*
                                                     * current number of
                                                     * facilities
                                                     */

    /** Constant <code>LOG_PRIMASK=0x07</code> */
    public static final int LOG_PRIMASK = 0x07; /*
                                                 * mask to extract priority
                                                 * part (internal)
                                                 */

    /** Constant <code>LOG_FACMASK=0x03F8</code> */
    public static final int LOG_FACMASK = 0x03F8; /*
                                                     * mask to extract
                                                     * facility part
                                                     */

    /** Constant <code>INTERNAL_NOPRI=0x10</code> */
    public static final int INTERNAL_NOPRI = 0x10; /*
                                                     * the "no priority"
                                                     * priority
                                                     */

    /** Constant <code>LOG_PID=0x01</code> */
    public static final int LOG_PID = 0x01; /* log the pid with each message */

    /** Constant <code>LOG_CONS=0x02</code> */
    public static final int LOG_CONS = 0x02; /*
                                                 * log on the console if
                                                 * errors in sending
                                                 */

    /** Constant <code>LOG_ODELAY=0x04</code> */
    public static final int LOG_ODELAY = 0x04; /*
                                                 * delay open until first
                                                 * syslog() (default)
                                                 */

    /** Constant <code>LOG_NDELAY=0x08</code> */
    public static final int LOG_NDELAY = 0x08; /* don't delay open */

    /** Constant <code>LOG_NOWAIT=0x10</code> */
    public static final int LOG_NOWAIT = 0x10; /*
                                                 * don't wait for console
                                                 * forks: DEPRECATED
                                                 */

    /** Constant <code>LOG_PERROR=0x20</code> */
    public static final int LOG_PERROR = 0x20; /* log to stderr as well */

    /** Constant <code>DEFAULT_PORT=514</code> */
    public static final int DEFAULT_PORT = 514;

    static private Hashtable<String, Integer> facHash;

    static private Hashtable<String, Integer> priHash;

    static {
        facHash = new Hashtable<String, Integer>(20);

        facHash.put("KERN", SyslogDefs.LOG_KERN);
        facHash.put("KERNEL", SyslogDefs.LOG_KERN);
        facHash.put("USER", SyslogDefs.LOG_USER);
        facHash.put("MAIL", SyslogDefs.LOG_MAIL);
        facHash.put("DAEMON", SyslogDefs.LOG_DAEMON);
        facHash.put("AUTH", SyslogDefs.LOG_AUTH);
        facHash.put("SYSLOG", SyslogDefs.LOG_SYSLOG);
        facHash.put("LPR", SyslogDefs.LOG_LPR);
        facHash.put("NEWS", SyslogDefs.LOG_NEWS);
        facHash.put("UUCP", SyslogDefs.LOG_UUCP);
        facHash.put("CRON", SyslogDefs.LOG_CRON);
        facHash.put("LOCAL0", SyslogDefs.LOG_LOCAL0);
        facHash.put("LOCAL1", SyslogDefs.LOG_LOCAL1);
        facHash.put("LOCAL2", SyslogDefs.LOG_LOCAL2);
        facHash.put("LOCAL3", SyslogDefs.LOG_LOCAL3);
        facHash.put("LOCAL4", SyslogDefs.LOG_LOCAL4);
        facHash.put("LOCAL5", SyslogDefs.LOG_LOCAL5);
        facHash.put("LOCAL6", SyslogDefs.LOG_LOCAL6);
        facHash.put("LOCAL7", SyslogDefs.LOG_LOCAL7);

        priHash = new Hashtable<String, Integer>(20);

        priHash.put("EMERG", SyslogDefs.LOG_EMERG);
        priHash.put("EMERGENCY", SyslogDefs.LOG_EMERG);
        priHash.put("LOG_EMERG", SyslogDefs.LOG_EMERG);
        priHash.put("ALERT", SyslogDefs.LOG_ALERT);
        priHash.put("LOG_ALERT", SyslogDefs.LOG_ALERT);
        priHash.put("CRIT", SyslogDefs.LOG_CRIT);
        priHash.put("CRITICAL", SyslogDefs.LOG_CRIT);
        priHash.put("LOG_CRIT", SyslogDefs.LOG_CRIT);
        priHash.put("ERR", SyslogDefs.LOG_ERR);
        priHash.put("ERROR", SyslogDefs.LOG_ERR);
        priHash.put("LOG_ERR", SyslogDefs.LOG_ERR);
        priHash.put("WARNING", SyslogDefs.LOG_WARNING);
        priHash.put("LOG_WARNING", SyslogDefs.LOG_WARNING);
        priHash.put("NOTICE", SyslogDefs.LOG_NOTICE);
        priHash.put("LOG_NOTICE", SyslogDefs.LOG_NOTICE);
        priHash.put("INFO", SyslogDefs.LOG_INFO);
        priHash.put("LOG_INFO", SyslogDefs.LOG_INFO);
        priHash.put("DEBUG", SyslogDefs.LOG_DEBUG);
        priHash.put("LOG_DEBUG", SyslogDefs.LOG_DEBUG);
    }

    /**
     * <p>extractFacility</p>
     *
     * @param code a int.
     * @return a int.
     */
    static public int extractFacility(final int code) {
        return ((code & SyslogDefs.LOG_FACMASK) >> 3);
    }

    /**
     * <p>extractPriority</p>
     *
     * @param code a int.
     * @return a int.
     */
    static public int extractPriority(final int code) {
        return (code & SyslogDefs.LOG_PRIMASK);
    }

    /**
     * <p>computeCode</p>
     *
     * @param facility a int.
     * @param priority a int.
     * @return a int.
     */
    static public int computeCode(final int facility, final int priority) {
        return ((facility << 3) | priority);
    }

    /*
     * Critical (red) This event means numerous devices on the network are
     * affected by the event. Everyone who can should stop what they are doing
     * and focus on fixing the problem. Major (orange) A device is completely
     * down or in danger of going down. Attention needs to be paid to this
     * problem immediately. Minor (yellow) A part of a device (a service, and
     * interface, a power supply, etc.) has stopped functioning. The device
     * needs attention. Warning (cyan) An event has occurred that may require
     * action. This severity can also be used to indicate a condition that
     * should be noted (logged) but does not require direct action. Normal
     * (green) Informational message. No action required. Cleared (white) This
     * event indicates that a prior error condition has been corrected and
     * service is restored. Indeterminate (light blue) The severity of the
     * event cannot be determined.
     */

    /**
     * <p>getPriorityName</p>
     *
     * @param level a int.
     * @return a {@link java.lang.String} object.
     */
    static public String getPriorityName(final int level) {
        switch (level) {
            case SyslogDefs.LOG_EMERG:
                return "Emergency";
            case SyslogDefs.LOG_ALERT:
                return "Alert";
            case SyslogDefs.LOG_CRIT:
                return "Critical";
            case SyslogDefs.LOG_ERR:
                return "Error";
            case SyslogDefs.LOG_WARNING:
                return "Warning";
            case SyslogDefs.LOG_NOTICE:
                return "Notice";
            case SyslogDefs.LOG_INFO:
                return "Info";
            case SyslogDefs.LOG_DEBUG:
                return "Debug";

                // This is a really lazy way of mapping syslog
                // to OpenNMS, but it should conform to thinking...
                /*
                * case SyslogDefs.LOG_EMERG: return "1"; case
                * SyslogDefs.LOG_ALERT: return "1"; case SyslogDefs.LOG_CRIT:
                * return "2"; case SyslogDefs.LOG_ERR: return "2"; case
                * SyslogDefs.LOG_WARNING: return "3"; case SyslogDefs.LOG_NOTICE:
                * return "4"; case SyslogDefs.LOG_INFO: return "5"; case
                * SyslogDefs.LOG_DEBUG: return "5";
                */
        }

        return "unknown level='" + level + "'";
    }

    /**
     * <p>getFacilityName</p>
     *
     * @param facility a int.
     * @return a {@link java.lang.String} object.
     */
    static public String getFacilityName(final int facility) {
        switch (facility) {
            case SyslogDefs.LOG_KERN:
                return "kernel";
            case SyslogDefs.LOG_USER:
                return "user";
            case SyslogDefs.LOG_MAIL:
                return "mail";
            case SyslogDefs.LOG_DAEMON:
                return "daemon";
            case SyslogDefs.LOG_AUTH:
                return "auth";
            case SyslogDefs.LOG_SYSLOG:
                return "syslog";
            case SyslogDefs.LOG_LPR:
                return "lpr";
            case SyslogDefs.LOG_NEWS:
                return "news";
            case SyslogDefs.LOG_UUCP:
                return "uucp";
            case SyslogDefs.LOG_CRON:
                return "cron";
            case SyslogDefs.FACILITY_AUTHPRIV:
            	return "authpriv";
            case SyslogDefs.FACILITY_FTP:
            	return "ftp";
            case SyslogDefs.FACILITY_NTP:
            	return "ntp";
            case SyslogDefs.FACILITY_AUDIT:
            	return "audit";
            case SyslogDefs.FACILITY_ALERT:
            	return "alert";
            case SyslogDefs.FACILITY_CLOCK:
            	return "clock";
            case SyslogDefs.LOG_LOCAL0:
                return "local0";
            case SyslogDefs.LOG_LOCAL1:
                return "local1";
            case SyslogDefs.LOG_LOCAL2:
                return "local2";
            case SyslogDefs.LOG_LOCAL3:
                return "local3";
            case SyslogDefs.LOG_LOCAL4:
                return "local4";
            case SyslogDefs.LOG_LOCAL5:
                return "local5";
            case SyslogDefs.LOG_LOCAL6:
                return "local6";
            case SyslogDefs.LOG_LOCAL7:
                return "local7";
        }

        return "unknown facility='" + facility + "'";
    }

    /**
     * <p>getPriority</p>
     *
     * @param priority a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.text.ParseException if any.
     */
    static public String getPriority(final String priority) throws ParseException {
        final String priKey = priority.toUpperCase();
        final String result = SyslogDefs.priHash.get(priKey).toString();

        if (result == null) {
            throw new ParseException("unknown priority '" + priority + "'", 0);
        }

        return result;
    }

    /**
     * <p>getFacility</p>
     *
     * @param facility a {@link java.lang.String} object.
     * @return a int.
     * @throws java.text.ParseException if any.
     */
    static public int getFacility(final String facility) throws ParseException {
        final String facKey = facility.toUpperCase();
        final Integer result = SyslogDefs.facHash.get(facKey);

        if (result == null) {
            throw new ParseException("unknown facility '" + facility + "'", 0);
        }

        return result;
    }

}
