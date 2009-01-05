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
// 2008 Feb 15: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.eventd.datablock.EventConfData;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.Resource;

class EventConfiguration {
    /**
     * Map of configured event files and their events
     */
    private Map<Resource, Events> m_eventFiles = new HashMap<Resource, Events>();
    
    /**
     * The mapping of all the event configuration objects for searching
     */
    private EventConfData m_eventConfData = new EventConfData();
    
    /**
     * The list of secure tags.
     */
    private Set<String> m_secureTags = new HashSet<String>();
    
    /**
     * Total count of events in these files.
     */
    private int m_eventCount = 0;

    public EventConfData getEventConfData() {
        return m_eventConfData;
    }

    public void setEventConfData(EventConfData eventConfData) {
        m_eventConfData = eventConfData;
    }

    public Map<Resource, Events> getEventFiles() {
        return m_eventFiles;
    }

    public void setEventFiles(Map<Resource, Events> eventFiles) {
        m_eventFiles = eventFiles;
    }

    public Set<String> getSecureTags() {
        return m_secureTags;
    }

    public void setSecureTags(Set<String> secureTags) {
        m_secureTags = secureTags;
    }

    public int getEventCount() {
        return m_eventCount;
    }

    public void incrementEventCount(int incrementCount) {
        m_eventCount += incrementCount;
    }
}