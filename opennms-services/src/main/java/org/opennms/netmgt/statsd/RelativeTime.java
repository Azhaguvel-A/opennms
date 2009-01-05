/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Added LASTHOUR; useful for testing. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.statsd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.opennms.core.utils.DefaultTimeKeeper;
import org.opennms.core.utils.TimeKeeper;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public enum RelativeTime {
    YESTERDAY {
        public Date getStart() {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(getCurrentTime());
            
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            
            return calendar.getTime();
        }
        
        public Date getEnd() {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(getCurrentTime());

            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            
            return calendar.getTime();
        }
    },
    
    LASTHOUR {
        public Date getStart() {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(getCurrentTime());
            
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.HOUR, -1);
            
            return calendar.getTime();
        }
        
        public Date getEnd() {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(getCurrentTime());

            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            
            return calendar.getTime();
        }
    };

    public abstract Date getStart();
    public abstract Date getEnd();
    
    private static TimeKeeper DEFAULT_TIME_KEEPER = new DefaultTimeKeeper();
    
    private TimeKeeper m_timeKeeper = null;
    
    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }
    
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }
    
    protected long getCurrentTime() {
        if (getTimeKeeper() == null) {
            return DEFAULT_TIME_KEEPER.getCurrentTime();
        } else {
            return getTimeKeeper().getCurrentTime();
        }
    }


}
