//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-20011 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.icmp;

/**
 * <p>PingerFactory class.</p>
 *
 * @author <A HREF="mailto:seth@opennms.org">Seth Leger</A>
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public abstract class PingerFactory {
	/**
     * The {@link Pinger} instance.
     */
    private static Pinger m_pinger;

    /**
     * Returns an implementation of the default {@link Pinger} class
     *
     * @return a {@link Pinger} object.
     */
    public static Pinger getInstance() {
        if (m_pinger == null) {
            String pingerClassName = System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jna.JnaPinger");
            Class<? extends Pinger> clazz = null;
            try {
                clazz = Class.forName(pingerClassName).asSubclass(Pinger.class);
                m_pinger = clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unable to find class named " + pingerClassName, e);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Error trying to create pinger of type " + clazz, e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to create pinger of type " + clazz + ".  It does not appear to have a public constructor", e);
            }
        }
        return m_pinger;
    }

    /**
     * <p>setIpcManager</p>
     *
     * @param pinger a {@link Pinger} object.
     */
    public static void setInstance(Pinger pinger) {
        m_pinger = pinger;
    }
    
    /**
     * This is here for unit testing so we can reset this class before
     * every test.
     */
    protected static void reset() {
        m_pinger = null;
    }
    
}
