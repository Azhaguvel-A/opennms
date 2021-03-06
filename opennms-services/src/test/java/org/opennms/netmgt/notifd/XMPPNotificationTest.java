/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class XMPPNotificationTest {
    private XMPPNotificationManager m_xmppManager;

    @Before
    public void setUp() {
        System.setProperty("useSystemXMPPConfig", "true");
        System.setProperty("xmpp.server", "jabber.example.com");
        System.setProperty("xmpp.port", "5222");
        System.setProperty("xmpp.TLSEnabled", "true");
        System.setProperty("xmpp.selfSignedCertificateEnabled", "true");
        System.setProperty("xmpp.user", "test");
        System.setProperty("xmpp.pass", "testpass");
        m_xmppManager = XMPPNotificationManager.getInstance();
    }

    @Test
    @Ignore("requires a working test jabber server")
    public void testNotification() {
        m_xmppManager.sendMessage("test@jabber.example.com", "This is a single-user test.");
    }
    
    @Test
    @Ignore("requires a working test jabber server")
    public void testGroupNotification() {
        m_xmppManager.sendGroupChat("test@conference.jabber.example.com", "This is a conference test.");
    }
}
