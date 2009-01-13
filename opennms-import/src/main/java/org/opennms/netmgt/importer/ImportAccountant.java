//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.importer;

import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.operations.SaveOrUpdateOperation;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;

public class ImportAccountant extends AbstractImportVisitor {
	private final ImportOperationsManager m_opsMgr;
    private SaveOrUpdateOperation m_currentOp;
        
    public ImportAccountant(ImportOperationsManager opsMgr) {
        m_opsMgr = opsMgr;
    }
    
    public void visitNode(Node node) {
        m_currentOp = m_opsMgr.foundNode(node.getForeignId(), node.getNodeLabel(), node.getBuilding(), node.getCity());        
    }
    public void completeNode(Node node) {
        m_currentOp = null;
    }

    public void visitInterface(Interface iface) {
        m_currentOp.foundInterface(iface.getIpAddr(), iface.getDescr(), iface.getSnmpPrimary(), iface.getManaged(), iface.getStatus());
        
    }
    
    public void visitMonitoredService(MonitoredService svc) {
        m_currentOp.foundMonitoredService(svc.getServiceName());
    }

    public void visitCategory(Category category) {
        m_currentOp.foundCategory(category.getName());
    }

    public void visitAsset(Asset asset) {
        m_currentOp.foundAsset(asset.getName(), asset.getValue());
    }
}
