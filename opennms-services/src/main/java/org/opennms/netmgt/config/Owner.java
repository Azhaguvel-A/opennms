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
// Modifications:
//
// 2007 Jul 14: Use Java 5 generics. - dj@opennms.org
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

public class Owner implements Comparable<Owner> {
    
    private String m_roleid;
    private String m_user;
    private int m_schedIndex;
    private int m_timeIndex;

    public Owner(String roleid, String user, int schedIndex) {
        this(roleid, user, schedIndex, -1);
    }        
    
    public Owner(String roleid, String supervisor) {
        this(roleid, supervisor, -1, -1);
    }

    public Owner(Owner base, int timeIndex) {
        this(base.getRoleid(), base.getUser(), base.getSchedIndex(), timeIndex);
    }
    
    public Owner(String roleid, String user, int schedIndex, int timeIndex) {
        m_roleid = roleid;
        m_user = user;
        m_schedIndex = schedIndex;
        m_timeIndex = timeIndex;
    }
    
    public boolean isSupervisor() {
        return m_schedIndex == -1 && m_timeIndex == -1;
    }

    public String getRoleid() {
        return m_roleid;
    }

    public int getSchedIndex() {
        return m_schedIndex;
    }

    public int getTimeIndex() {
        return m_timeIndex;
    }

    public String getUser() {
        return m_user;
    }
    
    Owner addTimeIndex(int timeIndex) {
        return new Owner(this, timeIndex);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Owner) {
            Owner o = (Owner) obj;
            return m_user.equals(o.m_user);
        }
        return false;
    }
    
    public int hashCode() {
        return m_user.hashCode();
    }

    public int compareTo(Owner o) {
        return m_user.compareTo(o.m_user);
    }
    
    public String toString() {
        return m_user;
    }
}
