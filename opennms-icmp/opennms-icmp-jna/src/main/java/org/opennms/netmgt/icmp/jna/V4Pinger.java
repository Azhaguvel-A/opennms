/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.icmp.jna;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.ip.ICMPPacket;
import org.opennms.jicmp.ip.IPPacket;
import org.opennms.jicmp.ip.ICMPPacket.Type;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

import com.sun.jna.Platform;

/**
 * PingListener
 *
 * @author brozow
 */
public class V4Pinger extends AbstractPinger<Inet4Address> {
    

    public V4Pinger(int pingerId) throws Exception {
        super(pingerId, NativeDatagramSocket.create(NativeDatagramSocket.PF_INET, Platform.isMac() ? NativeDatagramSocket.SOCK_DGRAM : NativeDatagramSocket.SOCK_RAW, NativeDatagramSocket.IPPROTO_ICMP));
        
        // Windows requires at least one packet sent before a receive call can be made without error
        // so we send a packet here to make sure...  This one should not match the normal ping requests
        // since it does not contain the cookie so it won't interface.
        if (Platform.isWindows()) {
            ICMPEchoPacket packet = new ICMPEchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            getPingSocket().send(packet.toDatagramPacket(InetAddress.getLocalHost()));
        }
    }
    
//    @Override
//    public void start() {
//        throw new UnsupportedOperationException("Put socket initialization here rather than the constructor");
//    }

    public void run() {
        try {
            final int pingerId = getPingerId();
            NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
            while (!isFinished()) {
                getPingSocket().receive(datagram);
                long received = System.nanoTime();
    
                ICMPPacket icmpPacket = new ICMPPacket(getIPPayload(datagram));
                V4PingReply echoReply = icmpPacket.getType() == Type.EchoReply ? new V4PingReply(icmpPacket, received) : null;
            
                if (echoReply != null && echoReply.getIdentifier() ==  pingerId && echoReply.isValid()) {
                    notifyPingListeners(datagram.getAddress(), echoReply);
                }
            }
        } catch(Throwable e) {
            setThrowable(e);
            e.printStackTrace();
        }
    }

    private ByteBuffer getIPPayload(NativeDatagramPacket datagram) {
        return new IPPacket(datagram.getContent()).getPayload();
    }
    
    public void ping(Inet4Address addr, int identifier, int sequenceNumber, long threadId, long count, long interval) throws InterruptedException {
        NativeDatagramSocket socket = getPingSocket();
        for(int i = sequenceNumber; i < sequenceNumber + count; i++) {
            V4PingRequest request = new V4PingRequest(identifier, i, threadId);
            request.send(socket, addr);
            Thread.sleep(interval);
        }
    }
}
