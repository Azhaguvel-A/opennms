//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
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

package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.Argument;
import org.opennms.test.mock.MockLogAppender;
public class SnmpTrapNotificationStrategyTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging(true);
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithEmptyArgumentList() {
        List<Argument> arguments = new ArrayList<Argument>();
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithNamedHost() {
        List<Argument> arguments = new ArrayList<Argument>();
        Argument arg = new Argument("trapHost", null, "localhost", false);
        arguments.add(arg);
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }
    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV1Trap()'
     */
    public void testSendV1Trap() {

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV2Trap()'
     */
    public void testSendV2Trap() {

    }

}
