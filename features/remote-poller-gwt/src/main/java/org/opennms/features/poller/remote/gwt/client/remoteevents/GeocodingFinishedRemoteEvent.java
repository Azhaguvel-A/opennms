/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client.remoteevents;


/**
 * <p>GeocodingFinishedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GeocodingFinishedRemoteEvent implements MapRemoteEvent {

	private static final long serialVersionUID = 5945314534020957562L;

	private int m_size = 0;

	/**
	 * <p>Constructor for GeocodingFinishedRemoteEvent.</p>
	 */
	public GeocodingFinishedRemoteEvent() {}

	/**
	 * <p>Constructor for GeocodingFinishedRemoteEvent.</p>
	 *
	 * @param size a int.
	 */
	public GeocodingFinishedRemoteEvent(final int size) {
		m_size = size;
	}

	/** {@inheritDoc} */
        @Override
	public void dispatch(final MapRemoteEventHandler presenter) {
//		Window.alert("Updated geocoding on " + m_size + " location definitions.");
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
	    return "GeocodingFinishedRemoteEvent[size=" + m_size + "]";
	}
}
