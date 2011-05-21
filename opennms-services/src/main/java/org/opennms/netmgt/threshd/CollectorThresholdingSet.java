//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>CollectorThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class CollectorThresholdingSet extends ThresholdingSet {
    
    // CollectionSpecification parameters
    boolean storeByIfAlias = false;
    
    /**
     * <p>Constructor for CollectorThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param interval a long.
     */
    public CollectorThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository, long interval, Map<String, Object> roProps) {
        super(nodeId, hostAddress, serviceName, repository, interval);
        String storeByIfAliasString = ParameterMap.getKeyedString(roProps, "storeByIfAlias", null);
        storeByIfAlias = storeByIfAliasString != null && storeByIfAliasString.toLowerCase().equals("true");
    }
    
    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     * @return a boolean.
     */
    public boolean hasThresholds(CollectionAttribute attribute) {
        CollectionResource resource = attribute.getResource();
        if (!isCollectionEnabled(attribute.getResource()))
            return false;
        if (resource instanceof AliasedResource && !storeByIfAlias)
            return false;
        return hasThresholds(resource.getResourceTypeName(), attribute.getName());
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    /** {@inheritDoc} */
    public List<Event> applyThresholds(CollectionResource resource, Map<String, CollectionAttribute> attributesMap) {
        if (!isCollectionEnabled(resource)) {
            log().debug("applyThresholds: Ignoring resource " + resource + " because data collection is disabled for this resource.");
            return new LinkedList<Event>();
        }
        CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(m_interval, m_nodeId, m_hostAddress, m_serviceName, m_repository, resource, attributesMap);
        return applyThresholds(resourceWrapper, attributesMap);
    }

    /*
     * Check Valid Interface Resource based on suggestions from Bug 2711
     */
    /** {@inheritDoc} */
    @Override
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        if (resource.isAnInterfaceResource() && !resource.isValidInterfaceResource()) {
            log().info("passedThresholdFilters: Could not get data interface information for '" + resource.getIfLabel() + "' or this interface has an invalid ifIndex.  Not evaluating threshold.");
            return false;
        }
        return super.passedThresholdFilters(resource, thresholdEntity);
    }
    
    protected boolean isCollectionEnabled(CollectionResource resource) {
        if (resource instanceof IfInfo) {
            return ((IfInfo) resource).isScheduledForCollection();
        }
        return true;
    }

}
