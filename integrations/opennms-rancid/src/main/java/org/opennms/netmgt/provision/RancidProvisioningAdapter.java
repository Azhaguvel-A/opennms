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
 * Created: December 16, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RWSResourceList;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.StoppableEventListener;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;

import org.apache.log4j.Logger;
import org.apache.log4j.Category;

/**
 * A Rancid provisioning adapter for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:guglielmoincisa@gmail.com">Guglielmo Incisa</a>
 *
 */
@EventListener(name="Provisiond:RancidProvisioningAdaptor")
public class RancidProvisioningAdapter implements ProvisioningAdapter {
    
    /*
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    private NodeDao m_nodeDao;
    private StoppableEventListener m_eventListener;
    private EventForwarder m_eventForwarder;
    private static final String MESSAGE_PREFIX = "Rancid provisioning failed: ";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED addNode");
        OnmsNode node = null;
        try {
            node = m_nodeDao.get(nodeId);
//            RancidNode rn = new RancidNode("demo", "gugli_DIC2_1759");
//            rn.setDeviceType(RancidNode.DEVICE_TYPE_BAYNET);
//            rn.setComment("Dic2 1759");
//            RWSClientApi.createRWSRancidNode("http://www.rionero.com/rws-current",rn);

            RancidNode r_node = new RancidNode("demo", node.getLabel());
            r_node.setDeviceType(node.getType());
            RWSClientApi.createRWSRancidNode("http://www.rionero.com/rws-current",r_node);
        
//            DnsRecord record = new DnsRecord(node);
//            DynamicDnsAdapter.add(record);
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
//            DnsRecord record = new DnsRecord(node);
//            DynamicDnsAdapter.update(record);
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
//            DnsRecord record = new DnsRecord(node);
//            DynamicDnsAdapter.delete(record);
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleInterfaceAddedEvent(Event e) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED handleInterfaceAddedEvent");
        throw new UnsupportedOperationException("method not yet implemented.");
    }
    
    private void sendAndThrow(int nodeId, Exception e) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED sendAndThrow");
        m_eventForwarder.sendNow(buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent());
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED EventBuilder");
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
    }
    
    public void setEventListener(StoppableEventListener eventListener) {
        m_eventListener = eventListener;
    }

    public StoppableEventListener getEventListener() {
        return m_eventListener;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    
    private Category log() {
        return Logger.getLogger("Rancid");
    }

//    class DnsRecord {
//        private InetAddress m_ip;
//        private String m_hostname;
//        
//        DnsRecord(OnmsNode node) {
//            m_ip = node.getCriticalInterface().getInetAddress();
//            m_hostname = node.getLabel();
//        }
//
//        public InetAddress getIp() {
//            return m_ip;
//        }
//
//        public String getHostname() {
//            return m_hostname;
//        }
//    }
//    
//    static class DynamicDnsAdapter {
//        
//        static boolean add(DnsRecord record) {
//            throw new UnsupportedOperationException("method not yet implemented.");
//        }
//        
//        static boolean update(DnsRecord record) {
//            throw new UnsupportedOperationException("method not yet implemented.");
//        }
//        
//        static boolean delete(DnsRecord record) {
//            throw new UnsupportedOperationException("method not yet implemented.");
//        }
//
//        static public DnsRecord getRecord(DnsRecord record) {
//            throw new UnsupportedOperationException("method not yet implemented.");
//        }
//    }

}
