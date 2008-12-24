//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 2006 Aug 15: Formatting, use generics for collections. 
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpValue;


public abstract class SnmpCollectionResource implements CollectionResource {
    
    private ResourceType m_resourceType;

    private Map<AttributeGroupType, AttributeGroup> m_groups = new HashMap<AttributeGroupType, AttributeGroup>();

    public SnmpCollectionResource(final ResourceType def) {
        m_resourceType = def;
    }
    
    public ResourceType getResourceType() {
        return m_resourceType;
    }
    
    public final CollectionAgent getCollectionAgent() {
        return m_resourceType.getAgent();
    }

    public abstract boolean shouldPersist(ServiceParameters params);

    public String getOwnerName() {
        return getCollectionAgent().getHostAddress();
    }

    public abstract File getResourceDir(RrdRepository repository);
    
    public abstract int getType();
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public boolean rescanNeeded() {
    	return false;
    }
    
    public void setAttributeValue(final SnmpAttributeType type, final SnmpValue val) {
        SnmpAttribute attr = new SnmpAttribute(this, type, val);
        addAttribute(attr);
    }

    private void addAttribute(final SnmpAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        if (log().isDebugEnabled()) {
            log().debug("Adding attribute " + attr.getClass().getName() + ": " + attr + " to group " + group);
        }
        group.addAttribute(attr);
    }

    private AttributeGroup getGroup(final AttributeGroupType groupType) {
        AttributeGroup group = m_groups.get(groupType);
        if (group == null) {
            group = new AttributeGroup(this, groupType);
            m_groups.put(groupType, group);
        }
        return group;
    }

    public void visit(final CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        
        for (AttributeGroup group : getGroups()) {
            group.visit(visitor);
        }
        
        visitor.completeResource(this);
    }

    protected Collection<AttributeGroup> getGroups() {
        return m_groups.values();
    }

}
