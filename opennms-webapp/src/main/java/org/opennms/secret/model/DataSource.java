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

package org.opennms.secret.model;
/**
 * @author Ted Kaczmarek
 * @author DJ Gregor
 *
 */
public class DataSource {

    String m_id;  // Unique ID that identifes this particular piece(Is this a concat
	              // of the nodeId:ifAlias:ifIndex:ifDescr:physAddr:DS
	              // example  -
	              // 1:Ted's port::::ifInOctets
	              // 2::1:::ifInOctets
	              // 3:::::Cpu5Util
    String m_name; // Human readable name of the data source
    String m_source; // file url
    String m_dataSource; //DS Name within the file
    
    public DataSource() {
    }
    
    public DataSource(String id, String name, String source, String dataSource) {
        setId(id);
        setName(name);
        setSource(source);
        setDataSource(dataSource);
    }
    
	public String getDataSource() {
		return m_dataSource;
	}
	public void setDataSource(String dataSource) {
		m_dataSource = dataSource;
	}
	public String getId() {
		return m_id;
	}
	public void setId(String id) {
		m_id = id;
	}
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}
	public String getSource() {
		return m_source;
	}
	public void setSource(String source) {
		m_source = source;
	}
}

