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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.springframework.core.io.Resource;

/**
 * CoreImportActivities
 *
 * @author brozow
 */
@ActivityProvider
public class CoreImportActivities {
    
    ProvisionService m_provisionService;
    
    public CoreImportActivities(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    /*
     *
     *                   LifeCycle importLifeCycle = new LifeCycle("import")
        .addPhase("validate")
            .addPhase("audit")
            .addPhase("scan")
            .addPhase("delete")
            .addPhase("update")
            .addPhase("insert")
            .addPhase("relate");
            
 
     */

    /*
     *         LifeCycle nodeScanLifeCycle = new LifeCycle("nodeScan")
            .addPhase("scan")
            .addPhase("persist");

     */

    @Activity( lifecycle = "import", phase = "validate", schedulingHint="import")
    public Requisition loadSpecFile(Resource resource) throws ModelImportException, IOException {
        info("Loading requisition from resource " + resource);
        Requisition specFile = m_provisionService.loadRequisition(resource);
        debug("Finished loading requisition.");

        return specFile;
    }
    
    @Activity( lifecycle = "import", phase = "audit", schedulingHint="import" )
    public ImportOperationsManager auditNodes(Requisition specFile) {
        info("Auditing nodes for requisition " + specFile);

        m_provisionService.createDistPollerIfNecessary("localhost", "127.0.0.1");
        
        String foreignSource = specFile.getForeignSource();
        Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);
        
        ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService);
        
        opsMgr.setForeignSource(foreignSource);
        opsMgr.auditNodes(specFile);

        debug("Finished auditing nodes.");
        
        return opsMgr;
    }
    
    @Activity( lifecycle = "import", phase = "scan", schedulingHint="import" )
    public void scanNodes(Phase currentPhase, ImportOperationsManager opsMgr) {

        info("Scheduling nodes for phase " + currentPhase);
        
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        
        for(final ImportOperation op : operations) {
            LifeCycleInstance nodeScan = currentPhase.createNestedLifeCycle("nodeImport");

            debug("Created lifecycle %s for operation %s", nodeScan, op);
            
            nodeScan.setAttribute("operation", op);
            nodeScan.trigger();
        }


    }
    
    
    @Activity( lifecycle = "nodeImport", phase = "scan", schedulingHint="import" )
    public void scanNode(ImportOperation operation) {
        info("Running scan phase of " + operation);
        operation.scan();

        System.out.println("Finished Running scan phase of "+operation);
    }
    
    @Activity( lifecycle = "nodeImport", phase = "persist" , schedulingHint = "import" )
    public void persistNode(ImportOperation operation) {

        System.out.println("Running persist phase of "+operation);
        operation.persist();
        System.out.println("Finished Running persist phase of "+operation);

    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "import" )
    public void relateNodes(final Phase currentPhase, final Requisition requisition) {
        
        System.out.println("Running relate phase");
        
        RequisitionVisitor visitor = new AbstractRequisitionVisitor() {
            @Override
            public void visitNode(OnmsNodeRequisition nodeReq) {
                System.out.println("Scheduling relate of node "+nodeReq);
                currentPhase.add(parentSetter(nodeReq, requisition.getForeignSource()));
            }
        };
        
        requisition.visit(visitor);
        
        System.out.println("Finished Running relate phase");

    }
    
    private Runnable parentSetter(final OnmsNodeRequisition nodeReq, final String foreignSource) {
        return new Runnable() {
           public void run() {
               m_provisionService.setNodeParentAndDependencies(foreignSource, nodeReq.getForeignId(), nodeReq.getParentForeignId(),
                                                               nodeReq.getParentNodeLabel());

               m_provisionService.clearCache();
           }
           public String toString() {
               return "set parent for node "+nodeReq.getNodeLabel();
           }
        }; 
    }

    protected void info(String format, Object... args) {
        log().info(String.format(format, args));
    }

    protected void debug(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
