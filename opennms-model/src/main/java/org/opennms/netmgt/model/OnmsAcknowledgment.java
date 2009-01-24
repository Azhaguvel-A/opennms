/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

@XmlRootElement(name="ack")  //hmmm
@Entity
@Table(name = "acks")
public class OnmsAcknowledgment {

    private Integer m_id; 
    private Date m_ackTime;
    private String m_ackUser;
    private AckType m_ackType;
    private Integer m_refId;
    
    //main constructor
    public OnmsAcknowledgment(Date time, String user) {
        m_ackTime = (time == null) ? new Date() : time;
        m_ackUser = (user == null) ? "admin" : user;
        m_ackType = AckType.Unspecified;
    }
    
    public OnmsAcknowledgment() {
        this(new Date(), "admin");
    }
    
    public OnmsAcknowledgment(String user) {
        this(new Date(), user);
    }
    
    public OnmsAcknowledgment(final Date time) {
        this(time, "admin");
    }

    public OnmsAcknowledgment(final Event e) throws ParseException {
        this(DateFormat.getDateInstance(DateFormat.FULL).parse(e.getTime()), "admin");
        Collection<Parm> parms = e.getParms().getParmCollection();
        
        if (parms.size() <= 2) {
            throw new IllegalArgumentException("Event:"+e.getUei()+" has invalid paramenter list, requires ackType and refId.");
        }
        
        for (Parm parm : parms) {
            final String parmValue = parm.getValue().getContent();
            
            if (!"ackType".equals(parm.getParmName()) && !"refId".equals(parm.getParmName()) && !"user".equals(parm.getParmName()) ) {
                throw new IllegalArgumentException("Event parm: "+parm.getParmName()+", is an invalid paramter");
            } else {
            
                if ("ackType".equals(parm.getParmName())) {

                    if ("Alarm".equals(parmValue) || "Notification".equals(parmValue)) {
                        m_ackType = ("Alarm".equals(parmValue) ? AckType.Alarm : AckType.Notification);
                    } else {
                        throw new IllegalArgumentException("Event parm: "+parm.getParmName()+", has invalid value, requires: \"Alarm\" or \"Notification\"." );
                    }
                    
                } else if ("refId".equals(parm.getParmName())){
                    m_refId = Integer.valueOf(parm.getValue().getContent());
                } else {
                    m_ackUser = parm.getValue().getContent();
                }
            }                
        }
    }

    public OnmsAcknowledgment(final Acknowledgeable a) {
        this(a, "admin", new Date());
        
        //not sure this is a valid use case but doing it for now
        if (a.getType() == AckType.Alarm) {
            if (a.getAckUser() != null) {
                m_ackUser = a.getAckUser();
                m_ackTime = a.getAckTime();
            }
        }
        
        
    }
    
    public OnmsAcknowledgment(final Acknowledgeable a, String user) {
        this(a, user, new Date());
    }
    
    public OnmsAcknowledgment(final Acknowledgeable a, String user, Date ackTime) {
        this();
        if (a == null) {
            throw new IllegalArgumentException("OnmsAlarm is null.");
        }
        
        m_ackType = a.getType();
        m_refId = a.getAckId();
    }
    
    
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ackTime", nullable=false)
    public Date getAckTime() {
        return m_ackTime;
    }
    
    public void setAckTime(Date time) {
        m_ackTime = time;
    }

    //TODO: make this right when Users are persisted to the DB
    @Column(name="ackUser", length=64, nullable=false)
    public String getAckUser() {
        return m_ackUser;
    }
    
    public void setAckUser(String user) {
        m_ackUser = user;
    }

    @Column(name="ackType", nullable=false)
    public AckType getAckType() {
        return m_ackType;
    }

    public void setAckType(AckType ackType) {
        m_ackType = ackType;
    }

    @Column(name="refId")
    public Integer getRefId() {
        return m_refId;
    }

    public void setRefId(Integer refId) {
        m_refId = refId;
    }

}
