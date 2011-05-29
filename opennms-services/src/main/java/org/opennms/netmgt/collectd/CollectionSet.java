/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.collectd;

import java.util.Date;

/**
 * <p>CollectionSet interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionSet {
    
    /**
     * <p>getStatus</p>
     *
     * @return an int (one of the ServiceCollector.COLLECTION_<FOO> values)
     */
    public int getStatus();
    
    /**
     * Provide a way to visit all the values in the CollectionSet, for any appropriate purposes (persisting, thresholding, or others)
     * The expectation is that calling this method will ultimately call visitResource, visitGroup and visitAttribute (as appropriate)
     *
     * @param visitor a {@link org.opennms.netmgt.collectd.CollectionSetVisitor} object.
     */
    public void visit(CollectionSetVisitor visitor);
    
    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    public boolean ignorePersist();
    
    /**
     * Returns the timestamp of when this data collection was taken.
     * Used by thresholding
     * @return
    */
	public Date getCollectionTimestamp();
}
