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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;

/**
 * <p>IPAddress class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IPAddress implements Comparable<IPAddress> {

    final byte[] m_ipAddr;
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress(IPAddress addr) {
        m_ipAddr = addr.m_ipAddr;
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     */
    public IPAddress(String dottedNotation) {
        m_ipAddr = InetAddressUtils.toIpAddrBytes(dottedNotation);
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param inetAddress a {@link java.net.InetAddress} object.
     */
    public IPAddress(InetAddress inetAddress) {
        m_ipAddr = inetAddress.getAddress();
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param ipAddrOctets an array of byte.
     */
    public IPAddress(byte[] ipAddrOctets) {
        m_ipAddr = ipAddrOctets;
    }
    
    /**
     * <p>toInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress toInetAddress() {
        return InetAddressUtils.getInetAddress(m_ipAddr);
    }
    
    /**
     * <p>toOctets</p>
     *
     * @return an array of byte.
     */
    public byte[] toOctets() {
        return m_ipAddr;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IPAddress) {
            return new ByteArrayComparator().compare(m_ipAddr, ((IPAddress) obj).toOctets()) == 0;
        }
        return false;
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a int.
     */
    public int compareTo(IPAddress o) {
        return new ByteArrayComparator().compare(m_ipAddr, o.toOctets());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return InetAddressUtils.toIpAddrString(m_ipAddr);
    }
    
    /** {@inheritDoc} */
    public BigInteger toBigInteger() {
        return new BigInteger(1, m_ipAddr);
    }
    
    /**
     * <p>incr</p>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress incr() {
        byte[] b = new byte[m_ipAddr.length];

        int carry = 1;
        for(int i = m_ipAddr.length-1; i >= 0; i--) {
            b[i] = (byte)(m_ipAddr[i] + carry);
            // if overflow we need to carry to the next byte
            carry = b[i] == 0 ? carry : 0;
        }
        
        return new IPAddress(b);
    }
    
    /**
     * <p>decr</p>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress decr() {
        byte[] b = new byte[m_ipAddr.length];
        
        int borrow = 1;
        for(int i = m_ipAddr.length-1; i >= 0; i--) {
            b[i] = (byte)(m_ipAddr[i] - borrow);
            // if underflow then we need to borrow from the next byte
            borrow = b[i] == (byte)0xff ? borrow : 0;
        }
        
        return new IPAddress(b);
        
    }
    
    /**
     * <p>isPredecessorOf</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isPredecessorOf(IPAddress other) {
        return other.decr().equals(this);
    }
    
    /**
     * <p>isSuccessorOf</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isSuccessorOf(IPAddress other) {
        return other.incr().equals(this);
    }
    
    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThan(IPAddress other) {
        return compareTo(other) < 0;
    }
    
    /**
     * <p>isLessThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqualTo(IPAddress other) {
        return compareTo(other) <= 0;
    }
    
    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(IPAddress other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqualTo(IPAddress other) {
        return compareTo(other) >= 0;
    }

    /**
     * <p>min</p>
     *
     * @param a a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @param b a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public static IPAddress min(IPAddress a, IPAddress b) {
        return (a.isLessThan(b) ? a : b);
    }

    /**
     * <p>max</p>
     *
     * @param a a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @param b a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public static IPAddress max(IPAddress a, IPAddress b) {
        return (a.isGreaterThan(b) ? a : b);
    }

}
