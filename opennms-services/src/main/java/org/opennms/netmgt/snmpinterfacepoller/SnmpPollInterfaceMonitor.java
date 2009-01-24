/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.snmpinterfacepoller;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface.SnmpMinimalPollInterface;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */

//this does snmp and there relies on the snmp configuration so it is not distributable
@Distributable(DistributionContext.DAEMON)
public class SnmpPollInterfaceMonitor {

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String IF_ADMIN_STATUS_OID = ".1.3.6.1.2.1.2.2.1.7."; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    private static final String IF_OPER_STATUS_OID = ".1.3.6.1.2.1.2.2.1.8."; // MIB-II
    // System
    // Object
    // Id

//    private List<>
    /**
     * <P>
     * Called by the poller framework when an interface is being added to the
     * scheduler. Here we perform any necessary initialization to prepare the
     * NetworkInterface object for polling.
     * </P>
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */

    /**
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availability of the interface and if a transition event
     *         should be supressed.
     * 
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public List<SnmpMinimalPollInterface> poll(SnmpAgentConfig agentConfig, List<SnmpMinimalPollInterface> mifaces) {

        if (mifaces == null ) {
            log().error("Null Interfaces passed to Monitor, exiting");
            return null;
        }
        
        log().debug("Got " + mifaces.size() + " interfaces to poll");
        
        // Retrieve this interface's SNMP peer object
        //
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available");

        SnmpObjId[] oids = new SnmpObjId[2 * mifaces.size()];
        //int maxVarsPerPdu = agentConfig.getMaxVarsPerPdu();
       
        for (int i=0;i < mifaces.size(); i++) {
            SnmpMinimalPollInterface miface = mifaces.get(i);
            miface.setStatus(PollStatus.unavailable());
            mifaces.set(i, miface);
            oids[i] = SnmpObjId.get(IF_ADMIN_STATUS_OID + miface.getIfindex());
            log().debug("Adding oid: " + oids[i] + " at position " + i);
            oids[i+mifaces.size()] = SnmpObjId.get(IF_OPER_STATUS_OID + miface.getIfindex());
            log().debug("Adding oid: " + oids[i+mifaces.size()] + " at position " + (i+mifaces.size()));
        }

        try {
        	SnmpValue[] results = SnmpUtils.get(agentConfig, oids);
    		log().debug("got " + results.length +" SnmpValues");
            int i=0;
            for(SnmpValue result : results) {
                if (result != null) {
                    log().debug("Snmp Value is "+ result.toInt() + " for oid: " + oids[i]);
                    if (i< mifaces.size()) {
                        SnmpMinimalPollInterface miface = mifaces.get(i);
                        miface.setStatus(PollStatus.up());
                        miface.setAdminstatus(result.toInt());
                    } else {
                        SnmpMinimalPollInterface miface = mifaces.get(i-mifaces.size());
                        miface.setStatus(PollStatus.up());
                        miface.setOperstatus(result.toInt());
                    }
                } else {
                    log().error("Snmp Value is null for oid: " + oids[i]);
                }
                i++;
            }
        } catch (NumberFormatException e) {
            log().error("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().error("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().error("Unexpected exception during SNMP poll of interface " + agentConfig, t);
        }
        

        // Establish SNMP session with interface
        //
        /*
    	SnmpValue[] totalresults = new SnmpValue[2 * mifaces.size()];

    	try {

        	if (maxVarsPerPdu > oids.length) {
        		
        		totalresults = SnmpUtils.get(agentConfig, oids);
        		log().debug("got " + totalresults.length +" SnmpValues");
            } else {
            	int remaining = 2 * mifaces.size();
            	while (remaining > maxVarsPerPdu) {
            		SnmpObjId[] curoids = new SnmpObjId[maxVarsPerPdu];
            		log().debug("max-var=per-pdu: " +  maxVarsPerPdu);
            		for (int j=0; j< maxVarsPerPdu; j++) {
            			curoids[j]= oids[2*mifaces.size() - remaining + j];
            		}
        			SnmpValue[] results = SnmpUtils.get(agentConfig, curoids);
            		log().debug("got " + results.length +" SnmpValues");
            		for (int j=0; j< maxVarsPerPdu; j++) {
            			totalresults[2* mifaces.size() - remaining + j]= results[j];
            		}
        			remaining = remaining - maxVarsPerPdu;
            	}
            	if (remaining > 0 ) {
            		SnmpObjId[] curoids = new SnmpObjId[remaining];
            		for (int j=0; j< maxVarsPerPdu; j++) {
            			curoids[j]= oids[2*mifaces.size() - remaining + j];
            		}
        			SnmpValue[] results = SnmpUtils.get(agentConfig, curoids);
            		log().debug("got " + results.length +" SnmpValues");
            		for (int j=0; j< remaining; j++) {
            			totalresults[2*mifaces.size() - remaining + j]= results[j];
            		}
            	}
            }

        } catch (NumberFormatException e) {
            log().error("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().error("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().error("Unexpected exception during SNMP poll of interface " + agentConfig, t);
        }
        
        int i=0;
        for(SnmpValue result : totalresults) {
            if (result != null) {
                log().debug("Snmp Value is "+ result.toInt() + " for oid: " + oids[i]);
                if (i< mifaces.size()) {
                    SnmpMinimalPollInterface miface = mifaces.get(i);
                    miface.setStatus(PollStatus.up());
                    miface.setAdminstatus(result.toInt());
                } else {
                    SnmpMinimalPollInterface miface = mifaces.get(i-mifaces.size());
                    miface.setStatus(PollStatus.up());
                    miface.setOperstatus(result.toInt());
                }
            } else {
                log().error("Snmp Value is null for oid: " + oids[i]);
            }
            i++;
        } */
        return mifaces;
    }
    
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
