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
// Modifications:
//
// 2007 Apr 05: Store a reference to the OnmsResource and add a
//              getRrdRelativePath method. - dj@opennms.org
//
// Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.model;

import java.io.File;


public class RrdGraphAttribute implements OnmsAttribute {

	private String m_name;
    private String m_relativePath;
    private String m_rrdFile;
    private OnmsResource m_resource;
    
    public RrdGraphAttribute(String name, String relativePath, String rrdFile) {
        m_name = name;
        m_relativePath = relativePath;
        m_rrdFile = rrdFile;
    }

    public String getName() {
        return m_name;
    }

    /**
     * Retrieve the resource for this attribute.
     */
    public OnmsResource getResource() {
        return m_resource;
    }

    /**
     * Set the resource for this attribute.  This is called
     * when the attribute is added to a resource.
     */
    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }

    public String getRrdRelativePath() {
        return m_relativePath + File.separator + m_rrdFile;
    }
    
    @Override
	public String toString() {
    	return ""+m_resource + '.' + m_name;
	}


}
