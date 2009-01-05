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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.core.style.ToStringCreator;


/** 
 * @hibernate.class table="service"
 *     
*/
@XmlRootElement(name = "serviceType")
@Entity
@Table(name="service")
public class OnmsServiceType implements Serializable {

    private static final long serialVersionUID = -459218937667452586L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private String m_name;

    /** full constructor */
    public OnmsServiceType(String servicename) {
        m_name = servicename;
    }

    /** default constructor */
    public OnmsServiceType() {
    }

    @Id
    @Column(name="serviceId")
    @SequenceGenerator(name="serviceTypeSequence", sequenceName="serviceNxtId")
    @GeneratedValue(generator="serviceTypeSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer serviceid) {
        m_id = serviceid;
    }

    @Column(name="serviceName", nullable=false, unique=true, length=32)
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("name", getName())
            .toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof OnmsServiceType) {
            OnmsServiceType t = (OnmsServiceType)obj;
            return m_id.equals(t.m_id);
        }
        return false;
    }

    public int hashCode() {
        return m_id.intValue();
    }

}
