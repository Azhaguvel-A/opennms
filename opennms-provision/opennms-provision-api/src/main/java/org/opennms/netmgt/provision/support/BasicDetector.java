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
 * Modifications;
 * Created 10/16/2008
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
package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;

/**
 * <p>Abstract BasicDetector class.</p>
 *
 * @author <a href=mailto:desloge@opennms.com>Donald Desloge</a>
 * @version $Id: $
 */
public abstract class BasicDetector<Request, Response> extends AbstractDetector implements SyncServiceDetector {
    
    private ClientConversation<Request, Response> m_conversation = new ClientConversation<Request, Response>();
    
    /**
     * <p>Constructor for BasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    protected BasicDetector(String serviceName, int port, int timeout, int retries) {
        super(serviceName, port, timeout, retries);
    }
    
    /**
     * <p>Constructor for BasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected BasicDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    /**
     * <p>onInit</p>
     */
    abstract protected void onInit();
    
    /** {@inheritDoc} */
    public boolean isServiceDetected(final InetAddress address, final DetectorMonitor detectorMonitor) {
    	final String ipAddr = InetAddressUtils.str(address);
    	final int port = getPort();
    	final int retries = getRetries();
        final int timeout = getTimeout();
        LogUtils.infof(this, "Address: %s || port: %s || \n", address, getPort());
        detectorMonitor.start(this, "Checking address: %s for %s capability", address, getServiceName());

        final Client<Request, Response> client = getClient();
        for (int attempts = 0; attempts <= retries; attempts++) {

            try {
                client.connect(address, port, timeout);
                detectorMonitor.attempt(this, attempts, "Attempting to connect to address: %s port %d attempt #%s",ipAddr,port,attempts);
                
                if (attemptConversation(client)) {
                    return true;
                }
                
            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
                detectorMonitor.info(this, cE, "%s: Excpetion attempting to connect to address: %s port %d, attempt #%s",getServiceName(), ipAddr,port, attempts);
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                detectorMonitor.info(this, e, "%s: No route to address %s was available", getServiceName(), ipAddr);
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // Expected exception
                detectorMonitor.info(this, e, "%s: Did not connect to to address %s port %d within timeout: %d attempt: %d", getServiceName(), ipAddr, port, timeout, attempts);
            } catch (IOException e) {
                detectorMonitor.info(this, e, "%s: An unexpected I/O exception occured contacting address %s port %d",getServiceName(), ipAddr, port);
            } catch (Throwable t) {
                detectorMonitor.failure(this, "%s: Failed to detect %s on address %s port %d", getServiceName(), getServiceName(), ipAddr, port);
                detectorMonitor.error(this, t, "%s: An undeclared throwable exception was caught contating address %s port %d", getServiceName(), ipAddr, port);
            } finally {
                client.close();
            }
        }
        return false;
    }
    
    /**
     * <p>dispose</p>
     */
    public void dispose(){
        
    }

    /**
     * <p>getClient</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.Client} object.
     */
    abstract protected Client<Request, Response> getClient();

    private boolean attemptConversation(Client<Request, Response> client) throws IOException, Exception {
        return getConversation().attemptConversation(client);
    }
    
    /**
     * <p>expectBanner</p>
     *
     * @param bannerValidator a {@link org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator} object.
     */
    protected void expectBanner(ResponseValidator<Response> bannerValidator) {
        getConversation().expectBanner(bannerValidator);
    }
    
    /**
     * <p>send</p>
     *
     * @param requestBuilder a {@link org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder} object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator} object.
     */
    protected void send(RequestBuilder<Request> requestBuilder, ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(requestBuilder, responseValidator);
    }
    /**
     * <p>send</p>
     *
     * @param request a Request object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator} object.
     */
    protected void send(Request request, ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(request, responseValidator);
    }
    
    /**
     * <p>getConversation</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.ClientConversation} object.
     */
    protected ClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
}
