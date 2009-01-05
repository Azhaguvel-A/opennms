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
package org.opennms.netmgt.provision.support;

import java.util.ArrayList;
import java.util.List;



/**
 * @author Donald Desloge
 *
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
        boolean validateResponse(Response response);

        /**
         * @return
         */
        Request getRequest();
        
       
        
    }
    
    public static class AsyncExchangeImpl<Request, Response> implements AsyncExchange<Request, Response>{
        
        private Request m_request;
        private ResponseValidator<Response> m_responseValidator;

        public AsyncExchangeImpl(Request request, ResponseValidator<Response> responseValidator) {
            m_request = request;
            m_responseValidator = responseValidator;
        }
        
        public Request getRequest() {
            return m_request;
        }

        public boolean validateResponse(Response message) {
            return m_responseValidator.validate(message);
        }
        
    }
    
    private List<AsyncExchange<Request, Response>> m_conversation = new ArrayList<AsyncExchange<Request, Response>>();
    private boolean m_isComplete = false;
    private boolean m_hasBanner = false;
    
    /**
     * 
     */
    public void addExchange(AsyncExchange<Request, Response> request) {
        m_conversation.add(request);
    }
    
    public void setHasBanner(boolean hasBanner){
        m_hasBanner = hasBanner;
    }
    
    public boolean hasBanner(){
        return m_hasBanner;
    }


    /**
     * @return
     */
    public boolean hasExchanges() {
        return !m_conversation.isEmpty();
    }

    /**
     * @return
     */
    public boolean isComplete() {
        return m_isComplete;
    }

    /**
     * @param message
     * @return
     */
    public boolean validate(Response message) {
        AsyncExchange<Request, Response> ex = m_conversation.remove(0);
        
        if(m_conversation.isEmpty()) {
            m_isComplete = true;
        }
        
        return ex.validateResponse(message);
    }

    /**
     * @return
     */
    public Request getRequest() {
        return extracted();
    }

    private Request extracted() {
        return m_conversation.isEmpty() ? null : m_conversation.get(0).getRequest();
    }
    
   
}
