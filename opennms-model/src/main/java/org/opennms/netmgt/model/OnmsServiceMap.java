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
package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="servicemap"
 *     
*/
public class OnmsServiceMap extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6550492519192174055L;
    
    private Integer m_id;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_serviceMapName;

    /** full constructor */
    public OnmsServiceMap(String ipAddr, String serviceMapName) {
        this.m_ipAddr = ipAddr;
        this.m_serviceMapName = serviceMapName;
    }

    /** default constructor */
    public OnmsServiceMap() {
    }
    

    /**
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="svcMapNxtId"
     *         
     */
public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        this.m_id = id;
    }

    /** 
     *                @hibernate.property
     *                 column="ipAddr"
     *                 length="16"
     *             
     */
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.m_ipAddr = ipAddr;
    }

    /** 
     *                @hibernate.property
     *                 column="serviceMapName"
     *                 length="32"
     *             
     */
    public String getServiceMapName() {
        return this.m_serviceMapName;
    }

    public void setServiceMapName(String serviceMapName) {
        this.m_serviceMapName = serviceMapName;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddr())
            .append("serviceMapName", getServiceMapName())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
		
	}

}
