/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dhcpd.Dhcpd;

/**
 * @author thedesloge
 *
 */
public class DhcpTestDetector {
    /**
     * The port where the DHCP server is detected. This is a well known port and
     * this integer is always returned in the qualifier map.
     */
    private final static Integer PORT_NUMBER = new Integer(67);

    /**
     * <P>
     * The protocol name of the plugin.
     * </P>
     */
    private final static String PROTOCOL_NAME = "DHCP";

    /**
     * Default number of retries for DHCP requests
     */
    private final static int DEFAULT_RETRY = 3;

    /**
     * Default timeout (in milliseconds) for DHCP requests
     */
    private final static int DEFAULT_TIMEOUT = 3000; // in milliseconds

    /**
     * This method is used to test a passed address for DHCP server support. If
     * the target system is running a DHCP server and responds to the request
     * then a value of true is returned.
     * 
     * @param host
     *            The host address to check
     * @param retries
     *            The maximum number of attempts to try.
     * @param timeout
     *            The time to wait for a response to each request.
     * 
     * @return True if the remote host supports DHCP.
     */
    private boolean isServer(InetAddress host, int retries, int timeout) {
        // Load the category for logging
        //
        Category log = ThreadCategory.getInstance(getClass());

        boolean isAServer = false;
        long responseTime = -1;

        try {
            // Dhcpd.isServer() returns the response time in milliseconds
            // if the remote host is a DHCP server or -1 if the remote
            // host is not a DHCP server.
            responseTime = Dhcpd.isServer(host, timeout, retries);
        } catch (InterruptedIOException ioE) {
            if (log.isDebugEnabled()) {
                ioE.fillInStackTrace();
                log.debug("isServer: The DHCP discovery operation was interrupted", ioE);
            }
            ioE.printStackTrace();
        } catch (IOException ioE) {
            log.warn("isServer: An I/O exception occured during DHCP discovery", ioE);
            isAServer = false;
            ioE.printStackTrace();
        } catch (Throwable t) {
            log.error("isServer: An undeclared throwable exception was caught during test", t);
            isAServer = false;
            t.printStackTrace();
        }

        // If response time is equal to or greater than zero
        // the remote host IS a DHCP server.
        if (responseTime >= 0)
            isAServer = true;

        // return the success/failure of this
        // attempt to contact a DHCP server.
        //
        return isAServer;
    }

    /**
     * This method returns the name of the protocol supported by this plugin.
     * 
     * @return The name of the protocol for the plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * This method is used to test the passed host for DHCP server support. The
     * remote host is queried using the DHCP protocol by sending a formatted
     * datagram to the DHCP server port. If a response is received by the DHCP
     * listenter that matches our original request then a value of true is
     * returned to the caller.
     * 
     * @param host
     *            The remote host to test.
     * 
     * @return True if the remote interface responds to the DHCP request, false
     *         otherwise
     */
    public boolean isProtocolSupported(InetAddress host) {
        return isServer(host, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    /**
     * This method is used to test the passed host for DHCP server support. The
     * remote host is queried using the DHCP protocol by sending a formatted
     * datagram to the DHCP server port. If a response is received by the DHCP
     * listenter that matches our original request then a value of true is
     * returned to the caller.
     * 
     * @param host
     *            The remote host to test.
     * @param qualifiers
     *            The location where qualifier parameters are read and written.
     * 
     * @return True if the remote interface responds to the DHCP request, false
     *         otherwise
     */
    public boolean isProtocolSupported(InetAddress host, Map<String, Object> qualifiers) {
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;

        if (qualifiers != null) {
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        }

        boolean isAServer = isServer(host, retries, timeout);
        if (isAServer && qualifiers != null)
            qualifiers.put("port", PORT_NUMBER);

        return isAServer;
    }
}
