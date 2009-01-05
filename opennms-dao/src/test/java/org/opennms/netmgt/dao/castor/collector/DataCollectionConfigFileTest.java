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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.InvocationAnticipator;
import org.springframework.core.io.ClassPathResource;

public class DataCollectionConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private DataCollectionVisitor m_visitor;
    

    protected void setUp() throws Exception {
        super.setUp();
        
        InvocationHandler noNullsAllowed = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                assertNotNull(args);
                assertEquals(1, args.length);
                assertNotNull(args[0]);
                return null;
            }
            
        };
        m_invocationAnticipator = new InvocationAnticipator(DataCollectionVisitor.class);
        m_invocationAnticipator.setInvocationHandler(noNullsAllowed);
        
        m_visitor = (DataCollectionVisitor)m_invocationAnticipator.getProxy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisit() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/datacollectionconfigfile-testdata.xml");
        DataCollectionConfigFile configFile = new DataCollectionConfigFile(resource.getFile());
        
        anticipateVisits(1, "DataCollectionConfig");
        anticipateVisits(1, "SnmpCollection");
        anticipateVisits(1, "Rrd");
        anticipateVisits(4, "Rra");
        anticipateVisits(26, "SystemDef");
        anticipateVisits(4, "SysOid");
        anticipateVisits(22, "SysOidMask");
        anticipateVisits(0, "IpList");
        anticipateVisits(26, "Collect");
        anticipateVisits(69, "IncludeGroup");
        anticipateVisits(57, "Group");
        anticipateVisits(0, "SubGroup");
        anticipateVisits(809, "MibObj");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

    private void anticipateVisits(int count, String visited) {
        m_invocationAnticipator.anticipateCalls(count, "visit"+visited);
        m_invocationAnticipator.anticipateCalls(count, "complete"+visited);
    }

}
