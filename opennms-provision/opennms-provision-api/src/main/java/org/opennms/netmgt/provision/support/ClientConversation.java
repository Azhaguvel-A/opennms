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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.support.Client;

/**
 * @author Donald Desloge
 *
 */
public class ClientConversation<Request, Response> {
    
    public static interface RequestBuilder<T> {
        T getRequest() throws Exception;
    }
    
    public static interface ResponseValidator<T> {
        boolean validate(T response)throws Exception; 
    }
    
    public static interface ClientExchange<Request, Response> extends RequestBuilder<Request>, ResponseValidator<Response> {
    }
    
    public static class SimpleClientExchange<Request, RespType> implements ClientExchange<Request, RespType> {
        private RequestBuilder<Request> m_requestBuilder;
        private ResponseValidator<RespType> m_responseValidator;
        
        public SimpleClientExchange(RequestBuilder<Request> reqBuilder, ResponseValidator<RespType> respValidator) {
            m_requestBuilder = reqBuilder;
            m_responseValidator = respValidator;
        }
        
        public Request getRequest() throws Exception {
            return m_requestBuilder.getRequest();
        }
        
        public boolean validate(RespType response) throws Exception {
            return m_responseValidator.validate(response);
        }
        
    }

    private ResponseValidator<Response> m_bannerValidator;
    private List<ClientExchange<Request, Response>> m_conversation = new ArrayList<ClientExchange<Request, Response>>();
    
    public void expectBanner(ResponseValidator<Response> bannerValidator) {
        m_bannerValidator = bannerValidator;
    }
    
    public void addExchange(final Request request, ResponseValidator<Response> validator) {
        RequestBuilder<Request> builder = new RequestBuilder<Request>() {
            public Request getRequest() {
                return request;
            }
        };
        addExchange(builder, validator);
    }
    
    public void addExchange(RequestBuilder<Request> requestBuilder, ResponseValidator<Response> validator) {
        addExchange(new SimpleClientExchange<Request, Response>(requestBuilder, validator));
    }
    
    public void addExchange(ClientExchange<Request, Response> exchange) {
        m_conversation.add(exchange); 
    }
    
    public boolean attemptConversation(Client<Request, Response> client) throws IOException, Exception { 
        
        if (m_bannerValidator != null) {
            Response banner = client.receiveBanner();
            if (!m_bannerValidator.validate(banner)) {
                System.out.println("False on Banner");
                return false;
            }
        }
        
        for(ClientExchange<Request, Response> ex : m_conversation) {
            
            Request request = ex.getRequest();
            
            System.out.printf("Sending Request %s\n", request);
            Response response = client.sendRequest(request);
            
            System.out.printf("Received Response %s\n", response);
            if (!ex.validate(response)) {
                return false;
            }
        }
        
        
        
        return true;
        
    }
    
}
