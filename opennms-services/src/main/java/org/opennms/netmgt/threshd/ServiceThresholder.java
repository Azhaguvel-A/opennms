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
// 2007 Jan 29: Use new ThresholdNetworkInterface instead of NetworkInterface so we can pass the node ID to the implementing service thresholder. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.netmgt.model.events.EventProxy;

/**
 * <P>
 * The Thresholder class...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public interface ServiceThresholder {
    /**
     * Status of the thresholder object.
     */
    public static final int THRESHOLDING_UNKNOWN = 0;

    public static final int THRESHOLDING_SUCCEEDED = 1;

    public static final int THRESHOLDING_FAILED = 2;

    public static final String[] statusType = { "Unknown", "THRESHOLDING_SUCCEEDED", "THRESHOLDING_FAILED" };

    public void initialize(Map parameters);
    
    /**
     * Called when configurations have changed and need to be refreshed at the ServiceThresolder level.  
     * Should not do a "full" initialization, but just reload any config objects that might have 
     * incorrect cached data.  It is up to the caller to call "release/initialize" for any interfaces
     * that need reinitialization, and it is recommended to do so *after* calling reinitialize(), so that
     * any objects that might be used in initializing the interfaces have been reloaded.   
     */
    public void reinitialize();

    public void release();

    public void initialize(ThresholdNetworkInterface iface, Map parameters);

    public void release(ThresholdNetworkInterface iface);

    /**
     * <P>
     * Invokes threshold checking on the object.
     * </P>
     */
    public int check(ThresholdNetworkInterface iface, EventProxy eproxy, Map parameters);
}
