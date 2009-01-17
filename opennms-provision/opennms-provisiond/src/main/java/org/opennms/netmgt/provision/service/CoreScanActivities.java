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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;

/**
 * CoreImportActivities
 *
 * @author brozow
 */
@ActivityProvider
public class CoreScanActivities {
    
    ProvisionService m_provisionService;
    
    public CoreScanActivities(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    /*
     *
     *  new LifeCycle("nodeScan")
            .addPhase("loadNode")
            .addPhase("")
            .addPhase("scan")
            .addPhase("delete")
            .addPhase("update")
            .addPhase("insert")
            .addPhase("relate");
            
            
 
     */
    
    /*
     * load the node from the database (or maybe the requistion)
     * 
     * walk the snmp interface table
     *   - add non-existent snmp interfaces to the database
     *   - update snmp interfaces that have changed
     *   - delete snmp interfaces that no longer exist
     * walk the ip interface table 
     *   - add non-existent ip interfaces to the database 
     *   - associate the ipinterface with the corresponding snmp interface
     *   - update ipInterfaces that have changed
     *   - delete ipInterfaces that no longer exist
     *   
     * for each ipinterface - detect services 
     *    - add serivces that have yet been detected/provisioned on the interface
     *    
     *    
     *  nodeScan.collectNodeInfo
     *  nodeScan.persistNodeInfo
     *  
     *  nodeScan.detectPhysicalInterfaces
     *  nodeScan.persistPhysicalInterfaces
     *  
     *  nodeScan.detectIpInterfaces
     *  nodeScan.persistIpInterfaces
     *  
     *  serviceDetect.detectIfService
     *  serviceDetect.persistIfService
     *  
     */


    @Activity( lifecycle = "nodeScan", phase = "collectNodeInfo" )
    public void collectNodeInfo() {
        System.err.println("collectNodeInfo");
    }

    @Activity( lifecycle = "nodeScan", phase = "persistNodeInfo", schedulingHint="write")
    public void persistNodeInfo() {
        System.err.println("persistNodeInfo");
    }

    @Activity( lifecycle = "nodeScan", phase = "detectPhysicalInterfaces" )
    public void detectPhysicalInterfaces() {
        System.err.println("detectPhysicalInterfaces");
    }

    @Activity( lifecycle = "nodeScan", phase = "persistPhysicalInterfaces", schedulingHint="write" )
    public void persistPhysicalInterfaces() {
        System.err.println("persistPhysicalInterfaces");
    }

    @Activity( lifecycle = "nodeScan", phase = "detectIpInterfaces" )
    public void detectIpInterfaces() {
        System.err.println("detectIpInterfaces");
    }

    @Activity( lifecycle = "nodeScan", phase = "persistIpInterfaces", schedulingHint="write" )
    public void persistIpInterfaces() {
        System.err.println("persistIpInterfaces");
    }

    
}
