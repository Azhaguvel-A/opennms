/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Aug 29: reportCollectionError now takes a CollectionException. - dj@opennms.org
 *
 * Created: July 20, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultCollectdInstrumentation implements CollectdInstrumentation {

    private Category log() {
        return Logger.getLogger("Instrumentation.Collectd");
    }

    public void beginScheduleExistingInterfaces() {
        log().debug("scheduleExistingInterfaces: begin");
    }

    public void endScheduleExistingInterfaces() {
        log().debug("scheduleExistingInterfaces: end");
    }

    public void beginScheduleInterfacesWithService(String svcName) {
        log().debug("scheduleInterfacesWithService: begin: "+svcName);
    }

    public void endScheduleInterfacesWithService(String svcName) {
        log().debug("scheduleInterfacesWithService: end: "+svcName);
    }

    public void beginFindInterfacesWithService(String svcName) {
        log().debug("scheduleFindInterfacesWithService: begin: "+svcName);
    }

    public void endFindInterfacesWithService(String svcName, int count) {
        log().debug("scheduleFindInterfacesWithService: end: "+svcName+". found "+count+" interfaces.");
    }

    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.collect: collectData: begin: "+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void endCollectingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: collectData: end: "+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void beginCollectorCollect(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: begin:"+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void endCollectorCollect(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.collect: end:"+nodeId+"/"+ipAddress+"/"+svcName);
        
    }

    public void beginCollectorRelease(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.release: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endCollectorRelease(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.release: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: persistDataQueueing: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: persistDataQueueing: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.initialize: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.initialize: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("scheduleInterfaceWithService: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("scheduleInterfaceWithService: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void reportCollectionException(int nodeId, String ipAddress,
            String svcName, CollectionException e) {
        log().debug("collector.collect: error: "+nodeId+"/"+ipAddress+"/"+svcName+": "+e);
    }

}
