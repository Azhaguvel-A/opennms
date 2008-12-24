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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *
 */

package org.opennms.netmgt.provision.persist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
public class MockForeignSourceRepository implements ForeignSourceRepository {
    private final Map<String,OnmsRequisition> m_requisitions = new HashMap<String,OnmsRequisition>();
    private final Map<String,OnmsForeignSource> m_foreignSources = new HashMap<String,OnmsForeignSource>();
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#createRequisition(org.springframework.core.io.Resource)
     */
    public OnmsRequisition createRequisition(Resource resource) {
        Assert.notNull(resource);
        OnmsRequisition r = new OnmsRequisition();
        r.loadResource(resource);
        return r;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#get(java.lang.String)
     */
    public OnmsForeignSource get(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_foreignSources.get(foreignSourceName);
    }

    public Collection<OnmsForeignSource> getAll() {
        return m_foreignSources.values();
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#getRequisition(java.lang.String)
     */
    public OnmsRequisition getRequisition(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#getRequisition(org.opennms.netmgt.provision.persist.OnmsForeignSource)
     */
    public OnmsRequisition getRequisition(OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        return getRequisition(foreignSource.getName());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#save(org.opennms.netmgt.provision.persist.OnmsForeignSource)
     */
    public void save(OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepository#save(org.opennms.netmgt.provision.persist.OnmsRequisition)
     */
    public void save(OnmsRequisition requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());
        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

}
