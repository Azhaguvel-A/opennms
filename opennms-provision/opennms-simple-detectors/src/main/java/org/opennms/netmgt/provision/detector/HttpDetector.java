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
package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.StringTokenizer;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.exchange.Exchange;
import org.opennms.netmgt.provision.exchange.RequestHandler;
import org.opennms.netmgt.provision.exchange.ResponseHandler;

public class HttpDetector extends SimpleDetector implements ServiceDetector {
    
    public static class HttpExchange extends SimpleExchange implements Exchange {
        private boolean m_checkReturnCode = false;
        private int m_maxRetCode;
        private String m_url;
        
        public HttpExchange(ResponseHandler responseHandler, RequestHandler requestHandler){
            super(responseHandler, requestHandler);
        }
        
        public void setCheckReturnCode(boolean checkReturnCode) {
            m_checkReturnCode = checkReturnCode;
        }
        
        public boolean matchResponseByString(String input) {
            return false;
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            if(getResponseHandler() == null) {
                return true;
            }else {
                try {
                    
                    char[] cbuf = new char[1024];
                    int chars = 0;
                    StringBuffer response = new StringBuffer();
                    try {
                        while ((chars = in.read(cbuf, 0, 1024)) != -1) {
                            String line = new String(cbuf, 0, chars);
                            response.append(line);
                        }

                    } catch (java.net.SocketTimeoutException timeoutEx) {
                        if (timeoutEx.bytesTransferred > 0) {
                            String line = new String(cbuf, 0, timeoutEx.bytesTransferred);
                            response.append(line);
                        }
                    }
                    if (response.toString() != null && getResponseHandler().matches(response.toString())) {
                        System.out.println("Return from server was: " + response.toString());
                        if (m_checkReturnCode) {
                                                        
                            int maxRetCode = getMaxRetCode();
                                                        
                            if ((DEFAULT_URL.equals(getUrl())) || (m_checkReturnCode == false)) {
                                maxRetCode = 600;
                            }
                            
                            StringTokenizer t = new StringTokenizer(response.toString());
                            t.nextToken();
                            int rVal = Integer.parseInt(t.nextToken());
                                                       
                            if (rVal >= 99 && rVal <= maxRetCode) {
                                System.out.println("RetCode Passed");
                                return true;
                            }
                        } else {
                            System.out.println("isAServer");
                            return true;
                        }
                    }
                } catch (SocketException e) {
                    //log.debug(getPluginName() + ": a protocol error occurred talking to host " + config.getInetAddress().getHostAddress(), e);
                    return false;
                } catch (NumberFormatException e) {
//                    log.debug(getPluginName()
//                            + ": failed to parse response code from host "
//                            + config.getInetAddress().getHostAddress(), e);
                    return false;
                }
                
                return false;  
            }
        }

        public void setMaxRetCode(int maxRetCode) {
            m_maxRetCode = maxRetCode;
        }

        public int getMaxRetCode() {
            return m_maxRetCode;
        }

        public void setUrl(String url) {
            m_url = url;
        }

        public String getUrl() {
            return m_url;
        }        
    }
    
    private static String DEFAULT_URL="/";
    private static int DEFAULT_MAX_RET_CODE = 399;
    private String m_url;
    private int m_maxRetCode;
    private boolean m_checkRetCode = false;
    
    protected HttpDetector() {
        super(80, 3000, 0);
        setServiceName("HTTP");
        setUrl(DEFAULT_URL);
        setMaxRetCode(DEFAULT_MAX_RET_CODE);
    }
    
    /**
     * 
     */
    public void onInit() {
        sendHttpQuery(queryURLRequest(getUrl()));
        addHttpResponseHandler(contains("HTTP/"), null, getUrl(), isCheckRetCode(), getMaxRetCode());
    }
    
    /**
     * 
     * @param contains
     * @param checkRetCode
     * @param maxRetCode
     * @param requestHandler
     */
    protected void addHttpResponseHandler(ResponseHandler contains, RequestHandler requestHandler, String url, boolean checkRetCode, int maxRetCode) {
        HttpExchange exchange = new HttpExchange(contains, requestHandler);
        exchange.setUrl(url);
        exchange.setCheckReturnCode(checkRetCode);
        exchange.setMaxRetCode(maxRetCode);
        getConversation().addExchange(exchange);
    }
    
    /**
     * 
     * @param requestHandler
     */
    protected void sendHttpQuery(RequestHandler requestHandler) {
        getConversation().addExchange(new HttpExchange(null, requestHandler));
    }
    
    /**
     * 
     */
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        getConversation().addExchange(new HttpExchange(responseHandler, requestHandler));
    }
    
    /**
     * 
     * @param url
     * @return
     */
    protected RequestHandler queryURLRequest(final String url) {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                out.write(String.format("GET %s  HTTP/1.0\r\n\r\n", url).getBytes());                
            }
            
        };
    }
    
    //Public setters and getters
    
    public void setUrl(String url) {
        m_url = url;
    }

    public String getUrl() {
        return m_url;
    }

    public void setMaxRetCode(int maxRetCode) {
        m_maxRetCode = maxRetCode;
    }

    public int getMaxRetCode() {
        return m_maxRetCode;
    }

    public void isCheckRetCode(boolean checkRetCode) {
        m_checkRetCode = checkRetCode;
    }

    public boolean isCheckRetCode() {
        return m_checkRetCode;
    }  

}
