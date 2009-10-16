//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config;

import java.util.Date;

public class TimeInterval implements Comparable<TimeInterval> {
    
    private Date m_start;
    private Date m_end;
    

    public TimeInterval(Date start, Date end) {
        if (start == null) throw new NullPointerException("start is null");
        if (end == null) throw new NullPointerException("end is null");
        if (start.compareTo(end) >= 0)
            throw new IllegalArgumentException("start ("+start+") must come strictly before end ("+end+")");
        
        m_start = start;
        m_end = end;
            
    }
    
    public Date getStart() {
        return m_start;
    }
    
    public Date getEnd() {
        return m_end;
    }
    
    /**
     * Returns -1, 0, 1 based on how date compares to this interval
     * 
     * @param date
     * @return -1 if the interval is entirely before date, 
     *          0 if the interval contains date,
     *           1 if the interface entirely follows date, 
     *           for these the starting date is included the ending date excluded
     */
    public int comparesTo(Date date) {
        if (date.before(m_start))
            return 1;
        if (date.after(m_end) || date.equals(m_end) )
            return -1;
        else return 0;
    }
    
    public String toString() {
        return "["+m_start+" - "+m_end+']';
    }
    
    public boolean equals(Object o) {
        if (o instanceof TimeInterval) {
            TimeInterval t = (TimeInterval)o;
            return (m_start.equals(t.m_start) && m_end.equals(t.m_end));
        }
        return false;
    }

    public int hashCode() {
        return m_start.hashCode() ^ m_end.hashCode();
    }

    // I don't implement Comparable because this relation is not consistent with equals
    public int compareTo(TimeInterval t) {
        if (t.m_end.before(m_start) || t.m_end.equals(m_start))
            return 1;
        if (t.m_start.after(m_end) || t.m_start.equals(m_end))
            return -1;
        else return 0;
    }

    boolean preceeds(TimeInterval interval) {
        return compareTo(interval) < 0;
    }

    boolean follows(TimeInterval interval) {
        return compareTo(interval) > 0;
    }

    boolean overlaps(TimeInterval interval) {
        return compareTo(interval) == 0;
    }
    
    

}