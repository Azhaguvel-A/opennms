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
package org.opennms.netmgt.invd;

import org.opennms.netmgt.config.invd.Scanner;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.core.utils.ThreadCategory;
import org.apache.log4j.Category;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
public class ScannerCollection {
    /**
     * Instantiated service collectors specified in config file
     */
    private final Map<String,InventoryScanner> m_scanners = new HashMap<String,InventoryScanner>(4);

    private volatile InvdConfigDao m_inventoryConfigDao;

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void setInventoryScanner(String svcName, InventoryScanner scanner) {
        m_scanners.put(svcName, scanner);
    }

    public InventoryScanner getInventoryScanner(String svcName) {
        return m_scanners.get(svcName);
    }

    public Set<String> getScannerNames() {
        return m_scanners.keySet();
    }

    public void instantiateScanners() {
        log().debug("instantiateScanners: Loading scanners");

        /*
         * Load up an instance of each collector from the config
         * so that the event processor will have them for
         * new incomming events to create collectable service objects.
         */
        Collection<Scanner> scanners = getInvdConfigDao().getScanners();
        for (Scanner scanner : scanners) {
            String svcName = scanner.getService();
            try {
                if (log().isDebugEnabled()) {
                    log().debug("instantiateScanners: Loading scanner "
                                + svcName + ", classname "
                                + scanner.getClassName());
                }
                Class<?> cc = Class.forName(scanner.getClassName());
                InventoryScanner sc = (InventoryScanner) cc.newInstance();


                sc.initialize(Collections.<String, String>emptyMap());

                setInventoryScanner(svcName, sc);
            } catch (Throwable t) {
                log().warn("instantiateCollectors: Failed to load collector "
                           + scanner.getClassName() + " for service "
                           + svcName + ": " + t, t);
            }
        }
    }

    public void setInvdConfigDao(InvdConfigDao inventoryConfigDao) {
        m_inventoryConfigDao = inventoryConfigDao;
    }

    private InvdConfigDao getInvdConfigDao() {
        return m_inventoryConfigDao;
    }
}
