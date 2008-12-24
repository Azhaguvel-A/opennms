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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.jmx.client;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;
import org.opennms.netmgt.provision.support.jmx.connectors.Jsr160ConnectionFactory;

/**
 * @author thedesloge
 *
 */
public class Jsr160Client extends JMXClient {
    
    private Map<String, Object> m_parameterMap;
    
    public Jsr160Client() {
        m_parameterMap = new HashMap<String, Object>();
    }
    
    @Override
    protected Map<String, Object> generateMap(int port, int timeout) {
        
        m_parameterMap.put("port",           port);
        m_parameterMap.put("timeout", timeout);
        return m_parameterMap;
    }

    @Override
    protected ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address) {
        return Jsr160ConnectionFactory.getMBeanServerConnection(parameterMap, address);
    }
    
    public void setFactory(String factory) {
        m_parameterMap.put("factory", factory);
    }
    
    public void setFriendlyName(String name) {
        m_parameterMap.put("friendlyname", name);
    }
    
    public void setUsername(String username) {
        m_parameterMap.put("username", username);
    }
    
    public void setPassword(String password) {
        m_parameterMap.put("password", password);
    }
    
    public void setUrlPath(String urlPath) {
        m_parameterMap.put("urlPath", urlPath);
    }
    
    public void setType(String type) {
        m_parameterMap.put("type", type);
    }

    public void setProtocol(String protocol) {
        m_parameterMap.put("protocol", protocol);
        
    }
    
}
