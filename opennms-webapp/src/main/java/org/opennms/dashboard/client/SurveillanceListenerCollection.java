/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
 *
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
 */

package org.opennms.dashboard.client;

import java.util.Iterator;
import java.util.Vector;

/**
 * <p>SurveillanceListenerCollection class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceListenerCollection extends Vector<SurveillanceListener> {
    private static final long serialVersionUID = 5693264759623736384L;

    /**
     * <p>fireAllClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     */
    public void fireAllClicked(Dashlet viewer) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onAllClicked(viewer);
          }
    }
    
    /**
     * <p>fireSurveillanceGroupClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param group a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public void fireSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onSurveillanceGroupClicked(viewer, group);
          }
    }
    
    /**
     * <p>fireIntersectionClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param intersection a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public void fireIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onIntersectionClicked(viewer, intersection);
          }
    }


}
