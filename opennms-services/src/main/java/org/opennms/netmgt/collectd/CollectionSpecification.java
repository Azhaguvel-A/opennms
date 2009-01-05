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
// Modfications:
//
// 2008 Aug 29: collect() can now throw a CollectionException which will
//              signal a connection failure and the resulting
//              details are logged and included in data collection
//              failure events. - dj@opennms.org
// 2008 Feb 09: Indent, format code and comments, remove Outage collection. - dj@opennms.org
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class CollectionSpecification {

    private CollectdPackage m_package;
    private String m_svcName;
    private ServiceCollector m_collector;
    private Map<String, String> m_parameters;

    public CollectionSpecification(CollectdPackage wpkg, String svcName, ServiceCollector collector) {
        m_package = wpkg;
        m_svcName = svcName;
        m_collector = collector;
        
        initializeParameters();
    }

    public String getPackageName() {
        return m_package.getName();
    }

    private String storeByIfAlias() {
        return m_package.storeByIfAlias();
    }

    private String ifAliasComment() {
        return m_package.ifAliasComment();
    }

    private String storeFlagOverride() {
        return m_package.getStorFlagOverride();
    }

    private String ifAliasDomain() {
        return m_package.ifAliasDomain();
    }

    private String storeByNodeId() {
        return m_package.storeByNodeId();
    }

    private Service getService() {
        return m_package.getService(m_svcName);
    }

    public String getServiceName() {
        return m_svcName;
    }

    private void setPackage(CollectdPackage pkg) {
        m_package = pkg;
    }

    public long getInterval() {
        return getService().getInterval();

    }

    public String toString() {
        return m_svcName + '/' + m_package.getName();
    }

    private ServiceCollector getCollector() {
        return m_collector;
    }

    private Map<String, String> getPropertyMap() {
        return m_parameters;
    }

    /**
     * Return a read only instance of the parameters, which consists of the overall service parameters,
     * plus various other Collection specific parameters (e.g. storeByNodeID etc)
     * @return A read only Map instance
     */
    public Map<String, String> getReadOnlyPropertyMap() {
        return Collections.unmodifiableMap(m_parameters);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private boolean isTrue(String stg) {
        return stg.equalsIgnoreCase("yes") || stg.equalsIgnoreCase("on") || stg.equalsIgnoreCase("true");
    }

    private boolean isFalse(String stg) {
        return stg.equalsIgnoreCase("no") || stg.equalsIgnoreCase("off") || stg.equalsIgnoreCase("false");
    }

    private void initializeParameters() {
        Map<String, String> m = new TreeMap<String, String>();
        m.put("SERVICE", m_svcName);
        StringBuffer sb;
        Collection<Parameter> params = getService().getParameterCollection();
        for (Parameter p : params) {
            if (log().isDebugEnabled()) {
                sb = new StringBuffer();
                sb.append("initializeParameters: adding service: ");
                sb.append(getServiceName());
                sb.append(" parameter: ");
                sb.append(p.getKey());
                sb.append(" of value ");
                sb.append(p.getValue());
                log().debug(sb.toString());
            }
            m.put(p.getKey(), p.getValue());
        }

        if (storeByIfAlias() != null && isTrue(storeByIfAlias())) {
            m.put("storeByIfAlias", "true");
            if (storeByNodeId() != null) {
                if (isTrue(storeByNodeId())) {
                    m.put("storeByNodeID", "true");
                } else if(isFalse(storeByNodeId())) {
                    m.put("storeByNodeID", "false");
                } else {
                    m.put("storeByNodeID", "normal");
                }
            }
            if (ifAliasDomain() != null) {
                m.put("domain", ifAliasDomain());
            } else {
                m.put("domain", getPackageName());
            }
            if (storeFlagOverride() != null && isTrue(storeFlagOverride())) {
                m.put("storFlagOverride", "true");
            }
            m.put("ifAliasComment", ifAliasComment());
            if (log().isDebugEnabled()) {
                sb = new StringBuffer();
                sb.append("ifAliasDomain = ");
                sb.append(ifAliasDomain());
                sb.append(", storeByIfAlias = ");
                sb.append(storeByIfAlias());
                sb.append(", storeByNodeID = ");
                sb.append(storeByNodeId());
                sb.append(", storFlagOverride = ");
                sb.append(storeFlagOverride());
                sb.append(", ifAliasComment = ");
                sb.append(ifAliasComment());
                log().debug(sb.toString());
            }
        }
        m_parameters = m;
    }

    public void initialize(CollectionAgent agent) {
        Collectd.instrumentation().beginCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_collector.initialize(agent, getPropertyMap());
        } finally {
            Collectd.instrumentation().endCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    public void release(CollectionAgent agent) {
        Collectd.instrumentation().beginCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_collector.release(agent);
        } finally {
            Collectd.instrumentation().endCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    public CollectionSet collect(CollectionAgent agent) throws CollectionException {
        Collectd.instrumentation().beginCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            return getCollector().collect(agent, eventProxy(), getPropertyMap());
        } finally {
            Collectd.instrumentation().endCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    private EventProxy eventProxy() {
        return new EventProxy() {
            public void send(Event e) {
                EventIpcManagerFactory.getIpcManager().sendNow(e);
            }

            public void send(Log log) {
                EventIpcManagerFactory.getIpcManager().sendNow(log);
            }
        };
    }

    public boolean scheduledOutage(CollectionAgent agent) {
        boolean outageFound = false;

        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();

        /*
         * Iterate over the outage names defined in the interface's package.
         * For each outage...if the outage contains a calendar entry which
         * applies to the current time and the outage applies to this
         * interface then break and return true. Otherwise process the
         * next outage.
         */ 
        for (String outageName : m_package.getPackage().getOutageCalendarCollection()) {
            // Does the outage apply to the current time?
            if (outageFactory.isCurTimeInOutage(outageName)) {
                // Does the outage apply to this interface?
                if ((outageFactory.isNodeIdInOutage(agent.getNodeId(), outageName)) ||
                        (outageFactory.isInterfaceInOutage(agent.getHostAddress(), outageName)))
                {
                    if (log().isDebugEnabled()) {
                        log().debug("scheduledOutage: configured outage '" + outageName + "' applies, interface " + agent.getHostAddress() + " will not be collected for " + this);
                    }
                    outageFound = true;
                    break;
                }
            }
        }

        return outageFound;
    }

    public void refresh(CollectorConfigDao collectorConfigDao) {
        CollectdPackage refreshedPackage = collectorConfigDao.getPackage(getPackageName());
        if (refreshedPackage != null) {
            setPackage(refreshedPackage);
        }
    }

    public RrdRepository getRrdRepository(String collectionName) {
        return m_collector.getRrdRepository(collectionName);
    }
}
