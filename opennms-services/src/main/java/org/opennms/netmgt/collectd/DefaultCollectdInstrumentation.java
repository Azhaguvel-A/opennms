/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DefaultCollectdInstrumentation class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultCollectdInstrumentation implements CollectdInstrumentation {
    
    public static final Logger LOG = LoggerFactory.getLogger("Instrumentation.Collectd");

    /**
     * <p>beginScheduleExistingInterfaces</p>
     */
    @Override
    public void beginScheduleExistingInterfaces() {
        LOG.debug("scheduleExistingInterfaces: begin");
    }

    /**
     * <p>endScheduleExistingInterfaces</p>
     */
    @Override
    public void endScheduleExistingInterfaces() {
        LOG.debug("scheduleExistingInterfaces: end");
    }

    /** {@inheritDoc} */
    @Override
    public void beginScheduleInterfacesWithService(String svcName) {
        LOG.debug("scheduleInterfacesWithService: begin: {}", svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void endScheduleInterfacesWithService(String svcName) {
        LOG.debug("scheduleInterfacesWithService: end: {}", svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void beginFindInterfacesWithService(String svcName) {
        LOG.debug("scheduleFindInterfacesWithService: begin: {}", svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void endFindInterfacesWithService(String svcName, int count) {
        LOG.debug("scheduleFindInterfacesWithService: end: {}. found {} interfaces.", svcName, count);
    }

    /** {@inheritDoc} */
    @Override
    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName) {
        LOG.debug("collector.collect: collectData: begin: {}/{}/{}", nodeId, ipAddress, svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void endCollectingServiceData(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.collect: collectData: end: {}/{}/{}", nodeId, ipAddress, svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void beginCollectorCollect(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.collect: begin:{}/{}/{}", nodeId, ipAddress, svcName);
    }

    /** {@inheritDoc} */
    @Override
    public void endCollectorCollect(int nodeId, String ipAddress, String svcName) {
        LOG.debug("collector.collect: end:{}/{}/{}", nodeId, ipAddress, svcName);
        
    }

    /** {@inheritDoc} */
    @Override
    public void beginCollectorRelease(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.release: begin: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void endCollectorRelease(int nodeId, String ipAddress, String svcName) {
        LOG.debug("collector.release: end: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void beginPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.collect: persistDataQueueing: begin: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void endPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.collect: persistDataQueueing: end: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void beginCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.initialize: begin: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void endCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("collector.initialize: end: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void beginScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("scheduleInterfaceWithService: begin: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void endScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        LOG.debug("scheduleInterfaceWithService: end: {}/{}/{}", nodeId, ipAddress, svcName);

    }

    /** {@inheritDoc} */
    @Override
    public void reportCollectionException(int nodeId, String ipAddress,
            String svcName, CollectionException e) {
        LOG.debug("collector.collect: error: {}/{}/{}", nodeId, ipAddress, svcName, e);
    }

}
