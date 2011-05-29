//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Feb 16: Make fatal error codes configurable (bug 3564) - jeffg@opennms.org
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 12: Display DNS response time data in webUI.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the DNS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable
final public class DnsMonitor extends AbstractServiceMonitor {
    /**
     * Default DNS port.
     */
    private static final int DEFAULT_PORT = 53;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 5000;
    
    /**
     * Default list of fatal response codes. Original behavior was hard-coded
     * so that only a ServFail(2) was fatal, so make that the configurable
     * default even though it makes little sense.
     */
    private static final int[] DEFAULT_FATAL_RESP_CODES = { 2 };

    /**
     * {@inheritDoc}
     *
     * <P>
     * Poll the specified address for DNS service availability.
     * </P>
     *
     * <P>
     * During the poll an DNS address request query packet is generated for
     * hostname 'localhost'. The query is sent via UDP socket to the interface
     * at the specified port (by default UDP port 53). If a response is
     * received, it is parsed and validated. If the DNS lookup was successful
     * the service status is set to SERVICE_AVAILABLE and the method returns.
     * </P>
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        // get the parameters
        //
        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // Host to lookup?
        //
        String lookup = ParameterMap.getKeyedString(parameters, "lookup", null);
        if (lookup == null || lookup.length() == 0) {
            // Get hostname of local machine for future DNS lookups
            try {
            	lookup = InetAddressUtils.str(InetAddress.getLocalHost());
            } catch (final UnknownHostException ukE) {
                ukE.fillInStackTrace();
                throw new UndeclaredThrowableException(ukE);
            }
        }
        
        // get the address and DNS address request
        //
        InetAddress addr = iface.getAddress();
        
        PollStatus serviceStatus = null;
        serviceStatus = pollDNS(timeoutTracker, port, addr, lookup);

        if (serviceStatus == null) {
            serviceStatus = logDown(Level.DEBUG, "Never received valid DNS response for address: " + addr);
        }
        
        // 
        //
        // return the status of the service
        //
        return serviceStatus;
    }

    private PollStatus pollDNS(TimeoutTracker timeoutTracker, int port, InetAddress addr, String lookup) {
            for (timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
                try {
                    timeoutTracker.startAttempt();
                    
                    Lookup l = new Lookup(lookup);
                    SimpleResolver resolver = new SimpleResolver(addr.getHostAddress());
                    resolver.setPort(port);
                    double timeout = timeoutTracker.getSoTimeout()/1000;
                    resolver.setTimeout((timeout < 1 ? 1 : (int) timeout));
                    l.setResolver(resolver);
                    l.run();

                    double responseTime = timeoutTracker.elapsedTimeInMillis();
                    if(l.getResult() == Lookup.SUCCESSFUL) {
                        return logUp(Level.DEBUG, responseTime, "valid DNS request received, responseTime= " + responseTime + "ms");
                    }else if(l.getResult() == Lookup.HOST_NOT_FOUND) {
                        return logDown(Level.DEBUG, "host not found on DNS (" + l.getErrorString() + "), responseTime= " + responseTime + "ms");
                    }else if(l.getResult() == Lookup.TRY_AGAIN) {
                        if(!timeoutTracker.shouldRetry()) {
                            return logDown(Level.DEBUG, "Never received valid DNS response for address: " + addr);
                        }
                    }else if(l.getResult() == Lookup.TYPE_NOT_FOUND) {

                    }else if(l.getResult() == Lookup.UNRECOVERABLE) {
                        return logDown(Level.DEBUG, "Never received valid DNS response for address (" + l.getErrorString() + "): " + addr);
                    }

                } catch (IOException ex) {
                    return logDown(Level.WARN, "IOException while polling address: " + addr + " " + ex.getMessage(), ex);
                }
            }
       
        return logDown(Level.DEBUG, "Never received valid DNS response for address: " + addr);
    }

    
}
