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
package org.opennms.netmgt.provision.detector.snmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.regex.Pattern;

import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.exchange.Exchange;
import org.opennms.netmgt.provision.support.AbstractDetector;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SnmpDetector extends AbstractDetector {
    
    public static class SnmpExchange implements Exchange{

        public boolean matchResponseByString(String input) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            // TODO Auto-generated method stub
            return false;
        }

        
        public boolean sendRequest(OutputStream out) throws IOException {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    private static final int DEFAULT_PORT = 21;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRIES = 3;
    
    private String m_oid;
    private String m_forceVersion;
    private String m_vbvalue;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    
    @SuppressWarnings("deprecation")
    public SnmpDetector() {
        setServiceName("SNMP");
        setPort(DEFAULT_PORT);
        setTimeout(DEFAULT_TIMEOUT);
        setRetries(DEFAULT_RETRIES);
        setOid(DEFAULT_OID);
    }

    @Override
    public void init() {}

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        try {

            String oid = getOid(); //ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address); //SnmpPeerFactory.getInstance().getAgentConfig(address);
            String expectedValue = null;
            
            agentConfig.setPort(getPort());
            agentConfig.setTimeout(getTimeout());
            agentConfig.setRetries(getRetries());
            
            if (getForceVersion() != null) {
                String version = getForceVersion();
                if (version.equalsIgnoreCase("snmpv1"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                else if (version.equalsIgnoreCase("snmpv2") || version.equalsIgnoreCase("snmpv2c"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
                
                //TODO: make sure JoeSnmpStrategy correctly handles this.
                else if (version.equalsIgnoreCase("snmpv3"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION3);
            }
            
            if (getVbvalue() != null) {
                expectedValue = getVbvalue();
            }
            
            String retrievedValue = getValue(agentConfig, oid);
            
            if (retrievedValue != null && expectedValue != null) {
                return (Pattern.compile(expectedValue).matcher(retrievedValue).find());
            } else {
                return (retrievedValue != null);
            }
            
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }
    
    private String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
        
    }

    public void setOid(String oid) {
        m_oid = oid;
    }

    public String getOid() {
        return m_oid;
    }

    public void setForceVersion(String forceVersion) {
        m_forceVersion = forceVersion;
    }

    public String getForceVersion() {
        return m_forceVersion;
    }

    public void setVbvalue(String vbvalue) {
        m_vbvalue = vbvalue;
    }

    public String getVbvalue() {
        return m_vbvalue;
    }
    
    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }
    
    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.AbstractDetector#onInit()
     */
    @Override
    protected void onInit() {
        // TODO Auto-generated method stub
        
    }
    

}
