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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.castor.collector;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.common.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

public interface DataCollectionVisitor {

        public abstract void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void visitSnmpCollection(SnmpCollection snmpCollection);

        public abstract void completeSnmpCollection(SnmpCollection snmpCollection);

        public abstract void visitRrd(Rrd rrd);

        public abstract void completeRrd(Rrd rrd);

        public abstract void visitRra(String rra);

        public abstract void completeRra(String rra);

        public abstract void visitSystemDef(SystemDef systemDef);

        public abstract void completeSystemDef(SystemDef systemDef);

        public abstract void visitSysOid(String sysoid);

        public abstract void completeSysOid(String sysoid);

        public abstract void visitSysOidMask(String sysoidMask);

        public abstract void completeSysOidMask(String sysoidMask);

        public abstract void visitIpList(IpList ipList);

        public abstract void completeIpList(IpList ipList);

        public abstract void visitCollect(Collect collect);

        public abstract void completeCollect(Collect collect);

        public abstract void visitIncludeGroup(String includeGroup);

        public abstract void completeIncludeGroup(String includeGroup);

        public abstract void visitGroup(Group group);

        public abstract void completeGroup(Group group);

        public abstract void visitSubGroup(String subGroup);

        public abstract void completeSubGroup(String subGroup);

        public abstract void visitMibObj(MibObj mibObj);

        public abstract void completeMibObj(MibObj mibObj);

        
}
