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
package org.opennms.netmgt.provision.server;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SimpleUDPServer {
    
    public static interface RequestMatcher{
        public boolean matches(DatagramPacket input);
    }
    
    public static interface Exchange {
        public boolean sendReply(DatagramSocket socket) throws IOException;
        public boolean processRequest(DatagramSocket socket) throws IOException;
    }
    
    public static class SimpleServerExchange implements Exchange{
        private byte[] m_response;
        private RequestMatcher m_requestMatcher;
        private int m_responsePort;
        private InetAddress m_responseAddress;
        
        public SimpleServerExchange(RequestMatcher requestMatcher, byte[] response){
            m_response = response;
            m_requestMatcher = requestMatcher;
        }
        
        public boolean processRequest(DatagramSocket socket) throws IOException {
            byte[] data = new byte[512];
            DatagramPacket packet = new DatagramPacket(data, data.length, socket.getLocalAddress(), socket.getLocalPort());
            
            socket.receive(packet);
            
            setResponsePort(packet.getPort());
            setResponseAddress(packet.getAddress());
            return m_requestMatcher.matches(packet);
        }

        public boolean sendReply(DatagramSocket socket) throws IOException {
            DatagramPacket packet = new DatagramPacket(m_response, m_response.length, getResponseAddress(), getResponsePort());
            socket.send(packet);
            
            return true;
        }

        public void setResponsePort(int responsePort) {
            m_responsePort = responsePort;
        }

        public int getResponsePort() {
            return m_responsePort;
        }

        public void setResponseAddress(InetAddress responseAddress) {
            m_responseAddress = responseAddress;
        }

        public InetAddress getResponseAddress() {
            return m_responseAddress;
        }
        
    }
    
    private static int DEFAULT_TEST_PORT = 8888;
    
    private Thread m_serverThread = null;
    private int m_timeout;
    private DatagramSocket m_socket;
    private List<Exchange> m_conversation = new ArrayList<Exchange>();
    private int m_port = DEFAULT_TEST_PORT;
    private InetAddress m_testInetAddress;
    
    public void onInit() {} 
    
    public void startServer() throws Exception{
        m_serverThread = new Thread(getRunnable());
        m_serverThread.start();
    }
    
    public void stopServer() throws IOException {
        if(getServerSocket() != null ){
            getServerSocket().close();
        }
        if(m_serverThread != null && m_serverThread.isAlive()) { 
            
            if(m_socket != null && !m_socket.isClosed()) {
                m_socket.close();  
            }
            
        }
    }
    
    private DatagramSocket getServerSocket() {
        return m_socket;
    }

    public Runnable getRunnable() throws Exception{
        return new Runnable(){
            
            public void run(){
                try{
                    m_socket = new DatagramSocket(getPort(), getInetAddress());
                    m_socket.setSoTimeout(getTimeout());
                    
                    attemptConversation(m_socket);
                    
                    m_socket.close();
                }catch(Exception e){
                    throw new UndeclaredThrowableException(e);
                }
            }
            
        };
    }
    
    public void setPort(int port) {
        m_port = port;
    }  
    
    public int getPort(){
        return m_port;
    }
    
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    public int getTimeout() {
        return m_timeout;
    }
    
    protected boolean attemptConversation(DatagramSocket socket) throws IOException{
        for(Exchange ex : m_conversation){
            if(!ex.processRequest(socket)){
                return false;
            }
            
            if(!ex.sendReply(socket)){
                return false;
            }
        }
        
        return true;
    }
    
    protected void addRequestResponse(DatagramPacket request, byte[] response){
        m_conversation.add(new SimpleServerExchange(recievedPacket(request), response));
    }
    
    protected RequestMatcher recievedPacket(final DatagramPacket request) {
        return new RequestMatcher() {

            public boolean matches(DatagramPacket packet) {
                return packet != null ? true : false;
            }
            
        };
    }

    public void setInetAddress(InetAddress testInetAddress) {
        m_testInetAddress = testInetAddress;
    }

    public InetAddress getInetAddress() {
        if(m_testInetAddress == null){
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return m_testInetAddress;
        }
    }

    
    
}
