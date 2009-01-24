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

import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.OnmsRequisition;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.tasks.Task;
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

    @Activity( lifecycle = "import", phase = "validate" )
    public OnmsRequisition loadSpecFile(@Attribute("foreignSource") String foreignSource, Resource resource) throws ModelImportException, IOException {

        System.out.println("Loading Spec File!");
        
        OnmsRequisition specFile = new OnmsRequisition();
        specFile.loadResource(resource);
        
        if (foreignSource != null) {
            specFile.setForeignSource(foreignSource);
        }
        
        System.out.println("Finished Loading Spec File!");

        return specFile;
    }
    
    
    
    @Activity( lifecycle = "import", phase = "audit" )
    public ImportOperationsManager auditNodes(OnmsRequisition specFile) {
        
        System.out.println("Auditing Nodes");
        

        m_provisionService.createDistPollerIfNecessary("localhost", "127.0.0.1");
        
        String foreignSource = specFile.getForeignSource();
        Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);
        
        ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService);
        
        opsMgr.setForeignSource(foreignSource);
        
        opsMgr.auditNodes(specFile);
        
        System.out.println("Finished Auditing Nodes");
        
        return opsMgr;
    }
    
    @Activity( lifecycle = "import", phase = "scan" )
    public void scanNodes(LifeCycleInstance lifeCycle, Phase currentPhase, ImportOperationsManager opsMgr) {

        
        
        System.out.println("Scheduling Nodes");
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        

        for(final ImportOperation op : operations) {
            LifeCycleInstance nodeScan = lifeCycle.createNestedLifeCycle("nodeImport");
            
            System.out.printf("Created  LifeCycle %s for op %s\n", nodeScan, op);
            nodeScan.setAttribute("operation", op);
            currentPhase.add((Task)nodeScan);
        }


    }
    
    
    @Activity( lifecycle = "nodeImport", phase = "scan" )
    public void scanNode(ImportOperation operation) {
        
        System.out.println("Running scan phase of "+operation);
        operation.scan();
        System.out.println("Finished Running scan phase of "+operation);
    }
    
    @Activity( lifecycle = "nodeImport", phase = "persist" , schedulingHint = "write" )
    public void persistNode(ImportOperation operation) {

        System.out.println("Running persist phase of "+operation);
        operation.persist();
        System.out.println("Finished Running persist phase of "+operation);

    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "write" )
    public void relateNodes(final Phase currentPhase, final OnmsRequisition requisition) {
        
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
}
