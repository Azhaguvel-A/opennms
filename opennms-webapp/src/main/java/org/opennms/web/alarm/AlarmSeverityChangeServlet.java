//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
// 2005 Apr 18: This file created from AcknowledgeEventServlet.java
//
// Original Code Base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.alarm;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.alarm.AlarmFactory;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;

/**
 * This servlet receives an HTTP POST with a list of alarms to escalate or
 * clear, and then it redirects the client to a URL for display. The
 * target URL is configurable in the servlet config (web.xml file).
 * 
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AlarmSeverityChangeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public final static String ESCALATE_ACTION = "1";
    public final static String CLEAR_ACTION = "2";

    /** The URL to redirect the client to in case of success. */
    protected String redirectSuccess;

    /**
     * Looks up the <code>redirect.success</code> parameter in the servlet's
     * config. If not present, this servlet will throw an exception so it will
     * be marked unavailable.
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter("redirect.success");

        if (this.redirectSuccess == null) {
            throw new UnavailableException("Require a redirect.success init parameter.");
        }
    }

    /**
     * Adjust the severity of the alarms specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // required parameter
        String[] alarmIdStrings = request.getParameterValues("alarm");
        String action = request.getParameter("actionCode");

        if (alarmIdStrings == null) {
            throw new MissingParameterException("alarm", new String[] { "alarm", "actionCode" });
        }

        if (action == null) {
            throw new MissingParameterException("actionCode", new String[] { "alarm", "actionCode" });
        }

        // convert the alarm id strings to ints
        int[] alarmIds = new int[alarmIdStrings.length];
        for (int i = 0; i < alarmIds.length; i++) {
            alarmIds[i] = WebSecurityUtils.safeParseInt(alarmIdStrings[i]);
        }

        try {
            if (action.equals(ESCALATE_ACTION)) {
                AlarmFactory.escalateAlarms(alarmIds, request.getRemoteUser());
            } else if (action.equals(CLEAR_ACTION)) {
                AlarmFactory.clearAlarms(alarmIds, request.getRemoteUser());
            } else {
                throw new ServletException("Unknown alarm severity action: " + action);
            }

            response.sendRedirect(this.getRedirectString(request));
        } catch (SQLException e) {
            throw new ServletException("Database exception", e);
        }
    }

    /**
     * Convenience method for dynamically creating the redirect URL if
     * necessary.
     */
    protected String getRedirectString(HttpServletRequest request) {
        String redirectValue = request.getParameter("redirect");

        if (redirectValue != null) {
            return (redirectValue);
        }

        redirectValue = this.redirectSuccess;
        String redirectParms = request.getParameter("redirectParms");

        if (redirectParms != null) {
            StringBuffer buffer = new StringBuffer(this.redirectSuccess);
            buffer.append("?");
            buffer.append(redirectParms);
            redirectValue = buffer.toString();
        }

        return (redirectValue);
    }

}
