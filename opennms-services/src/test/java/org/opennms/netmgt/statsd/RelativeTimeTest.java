/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.mail.internet.MailDateFormat;

import org.junit.Test;
import org.opennms.core.utils.TimeKeeper;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RelativeTimeTest {
//    private DateFormat m_dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
    private DateFormat m_dateFormat = new MailDateFormat();
    private TimeZone m_timeZone = TimeZone.getTimeZone("GMT-05:00");
    
    @Test
    public void testYesterdayBeginningDST() {
        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
                cal.set(2006, Calendar.APRIL, 3, 10, 0, 0);
                return cal.getTime();
            }

            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();

        Calendar c = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
        c.setTime(start);

        assertEquals(-18000000, c.get(Calendar.ZONE_OFFSET));
        assertEquals(2006, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(2, c.get(Calendar.DAY_OF_MONTH));

        c.setTime(end);

        assertEquals(-18000000, c.get(Calendar.ZONE_OFFSET));
        assertEquals(2006, c.get(Calendar.YEAR));
        assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(2, c.get(Calendar.DAY_OF_MONTH));

        assertEquals("start date", "Sun, 2 Apr 2006 00:00:00 -0500 (EST)", m_dateFormat.format(start));
        assertEquals("end date", "Mon, 3 Apr 2006 00:00:00 -0400 (EDT)", m_dateFormat.format(end));
        assertEquals("end date - start date", 82800000, end.getTime() - start.getTime());
    }
    
    @Test
    public void testYesterdayEndingDST() {
        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
                cal.set(2006, Calendar.OCTOBER, 30, 10, 0, 0);
                return cal.getTime();
            }

            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();

        Calendar c = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
        c.setTime(start);

        assertEquals(-18000000, c.get(Calendar.ZONE_OFFSET));
        assertEquals(2006, c.get(Calendar.YEAR));
        assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.SATURDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        c.setTime(end);

        assertEquals(-18000000, c.get(Calendar.ZONE_OFFSET));
        assertEquals(2006, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.MONDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(30, c.get(Calendar.DAY_OF_MONTH));

        assertEquals("start date", "Sun, 29 Oct 2006 00:00:00 -0400 (EDT)", m_dateFormat.format(start));
        assertEquals("end date", "Mon, 30 Oct 2006 00:00:00 -0500 (EST)", m_dateFormat.format(end));
        assertEquals("end date - start date", 90000000, end.getTime() - start.getTime());
    }
    
}
