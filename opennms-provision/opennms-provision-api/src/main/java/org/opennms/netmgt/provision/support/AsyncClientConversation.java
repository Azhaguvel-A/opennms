/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support;

import java.util.ArrayList;
import java.util.List;



/**
 * <p>AsyncClientConversation class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class AsyncClientConversation<Request, Response> {
    
    public static interface ResponseValidator<Response>{
        boolean validate(Response message);
    }
    
    public static interface AsyncExchange<Request, Response>{

        /**
         * @param message
         * @return
         */
        boolean validateResponse(final Response response);

        /**
         * @return
         */
        Request getRequest();
        
       
        
    }
    
    public static class AsyncExchangeImpl<Request, Response> implements AsyncExchange<Request, Response>{
        
        private final Request m_request;
        private final ResponseValidator<Response> m_responseValidator;

        public AsyncExchangeImpl(final Request request, final ResponseValidator<Response> responseValidator) {
            m_request = request;
            m_responseValidator = responseValidator;
        }
        
        public Request getRequest() {
            return m_request;
        }

        public boolean validateResponse(final Response message) {
            return m_responseValidator.validate(message);
        }
        
    }
    
    private final List<AsyncExchange<Request, Response>> m_conversation = new ArrayList<AsyncExchange<Request, Response>>();
    private boolean m_isComplete = false;
    private boolean m_hasBanner = false;
    
    /**
     * <p>addExchange</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.AsyncExchange} object.
     */
    public void addExchange(final AsyncExchange<Request, Response> request) {
        m_conversation.add(request);
    }
    
    /**
     * <p>setHasBanner</p>
     *
     * @param hasBanner a boolean.
     */
    public void setHasBanner(final boolean hasBanner){
        m_hasBanner = hasBanner;
    }
    
    /**
     * <p>hasBanner</p>
     *
     * @return a boolean.
     */
    public boolean hasBanner(){
        return m_hasBanner;
    }


    /**
     * <p>hasExchanges</p>
     *
     * @return a boolean.
     */
    public boolean hasExchanges() {
        return !m_conversation.isEmpty();
    }

    /**
     * <p>isComplete</p>
     *
     * @return a boolean.
     */
    public boolean isComplete() {
        return m_isComplete;
    }

    /**
     * <p>validate</p>
     *
     * @param message a Response object.
     * @return a boolean.
     */
    public boolean validate(final Response message) {
        final AsyncExchange<Request, Response> ex = m_conversation.remove(0);

        if(m_conversation.isEmpty()) {
            m_isComplete = true;
        }
        
        return ex.validateResponse(message);
    }

    /**
     * <p>getRequest</p>
     *
     * @return a Request object.
     */
    public Request getRequest() {
        return extracted();
    }

    private Request extracted() {
        return m_conversation.isEmpty() ? null : m_conversation.get(0).getRequest();
    }
    
   
}
