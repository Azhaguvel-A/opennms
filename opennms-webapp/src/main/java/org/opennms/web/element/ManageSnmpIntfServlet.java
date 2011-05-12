//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Organize imports, Java 5 generics. - dj@opennms.org
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
/*
 * Creato il 17-feb-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.web.WebSecurityUtils;

/**
 * <p>ManageSnmpIntfServlet class.</p>
 *
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 *
 * La servlet prende i seguenti parametri dal file web.xml
 * @version $Id: $
 * @since 1.8.1
 */
public final class ManageSnmpIntfServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 996461881276250543L;

    protected int snmpServiceId;

    protected SnmpPeerFactory snmpPeerFactory;

    protected String pageToRedirect;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            this.snmpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("SNMP");
            SnmpPeerFactory.init();
        } catch (Throwable e) {
            throw new ServletException(
                    "Could not determine the snmp service ID", e);
        }
    }

    /** {@inheritDoc} */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        doPost(request, response);
    }

    /** {@inheritDoc} */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {

		ThreadCategory log = ThreadCategory.getInstance();

		HttpSession userSession = request.getSession(false);
        if (userSession == null)
            throw new ServletException("Session exceeded");

        String nodeIdString = request.getParameter("node");
        if (nodeIdString == null) {
            throw new org.opennms.web.MissingParameterException("node");
        }
        int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        String intfIdString = request.getParameter("intf");
        if (intfIdString == null) {
            throw new org.opennms.web.MissingParameterException("intf");
        }
        int intfId = WebSecurityUtils.safeParseInt(intfIdString);

        String statusString = request.getParameter("status");
        if (statusString == null) {
            throw new org.opennms.web.MissingParameterException("status");
        }
        int status = WebSecurityUtils.safeParseInt(statusString);

        log.debug("ManageSnmpIntfServlet.doPost: parameters - node " + nodeId + " intf " + intfId + " status " + status);
        String snmpIp = null;
        Service[] snmpServices = null;
        try {
            snmpServices = NetworkElementFactory.getInstance(getServletContext()).getServicesOnNode(nodeId, this.snmpServiceId);
            if (snmpServices != null && snmpServices.length > 0) {
                List<InetAddress> ips = new ArrayList<InetAddress>();
                for (int i = 0; i < snmpServices.length; i++) {
                    ips.add(InetAddressUtils.addr(snmpServices[i]
                            .getIpAddress()));
                }

                InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

                if (lowest != null) {
                    snmpIp = InetAddressUtils.str(lowest);
                }
            }

            InetAddress[] inetAddress = InetAddress.getAllByName(snmpIp);
            SnmpAgentConfig agent = SnmpPeerFactory.getInstance().getAgentConfig(inetAddress[0]);
            
            log.debug("ManageSnmpIntfServlet.doPost: agent SNMP version/write community " + agent.getVersion()+"/"+agent.getWriteCommunity());
            SnmpIfAdmin snmpIfAdmin = new SnmpIfAdmin(nodeId, agent);
            if (snmpIfAdmin.setIfAdmin(intfId, status)) {
                log.debug("ManageSnmpIntfServlet.doPost: snmpIAdmin return OK ");
            } else {
            	log.debug("ManageSnmpIntfServlet.doPost: snmpIAdmin return error ");
            }
            redirect(request, response);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (UnknownHostException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private void redirect(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
    }

}
