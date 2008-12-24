//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Created this class based on initialization code in Eventd. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.daemon;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;

public class DaemonUtils {
    /**
     * No public constructor.  This has static methods only.
     */
    private DaemonUtils() {
    }

    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ThreadCategory.getInstance(DaemonUtils.class).warn("getLocalHostAddress: Could not lookup the host address for the local host machine, address set to 127.0.0.1: " + e, e);
            return "127.0.0.1";
        }
    }
    
    public static String getLocalHostName() {
        String localhost;
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            ThreadCategory.getInstance(DaemonUtils.class).warn("getLocalHostName: Could not lookup the host name for the local host machine, name set to 'localhost': " + e, e);
            localhost = "localhost";
        }
        return localhost;
    }


}
