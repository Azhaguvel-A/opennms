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
// 2008 Aug 31: Created AlarmFilterController from AlarmFilterServlet. - dj@opennms.org
// 2007 Jul 24: Add serialVersionUID and Java 5 generics. - dj@opennms.org
// 2005 Apr 18: This file created from EventFilterServlet.java
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.controller.alarm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.Alarm;
import org.opennms.web.alarm.AlarmFactory;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.AlarmFactory.AcknowledgeType;
import org.opennms.web.alarm.AlarmFactory.SortStyle;
import org.opennms.web.alarm.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the event table by using filters to create an
 * event list and and then forwards that event list to a JSP for display.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AlarmFilterController extends AbstractController implements InitializingBean {
    public static final int DEFAULT_MULTIPLE = 0;

    private String m_successView;

    private Integer m_defaultShortLimit;

    private Integer m_defaultLongLimit;
    
    private AcknowledgeType m_defaultAcknowledgeType = AcknowledgeType.UNACKNOWLEDGED;

    private SortStyle m_defaultSortStyle = SortStyle.ID;


    /**
     * Parses the query string to determine what types of event filters to use
     * (for example, what to filter on or sort by), then does the database query
     * (through the AlarmFactory) and then forwards the results to a JSP for
     * display.
     * 
     * <p>
     * Sets the <em>alarms</em> and <em>parms</em> request attributes for
     * the forwardee JSP (or whatever gets called).
     * </p>
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String display = request.getParameter("display");

        // handle the style sort parameter
        String sortStyleString = request.getParameter("sortby");
        SortStyle sortStyle = m_defaultSortStyle;
        if (sortStyleString != null) {
            SortStyle temp = AlarmUtil.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = temp;
            }
        }

        // handle the acknowledgement type parameter
        String ackTypeString = request.getParameter("acktype");
        AcknowledgeType ackType = m_defaultAcknowledgeType;
        if (ackTypeString != null) {
            AcknowledgeType temp = AlarmUtil.getAcknowledgeType(ackTypeString);
            if (temp != null) {
                ackType = temp;
            }
        }

        // handle the filter parameters
        String[] filterStrings = request.getParameterValues("filter");
        List<Filter> filterArray = new ArrayList<Filter>();
        if (filterStrings != null) {
            for (int i = 0; i < filterStrings.length; i++) {
                Filter filter = AlarmUtil.getFilter(filterStrings[i]);
                if (filter != null) {
                    filterArray.add(filter);
                }
            }
        }

        // handle the optional limit parameter
        String limitString = request.getParameter("limit");
        int limit = "long".equals(display) ? getDefaultLongLimit() : getDefaultShortLimit();

        if (limitString != null) {
            try {
                limit = WebSecurityUtils.safeParseInt(limitString);
            } catch (NumberFormatException e) {
                // do nothing, the default is aready set
            }
        }

        // handle the optional multiple parameter
        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = WebSecurityUtils.safeParseInt(multipleString);
            } catch (NumberFormatException e) {
            }
        }

        try {
            // put the parameters in a convenient struct
            AlarmQueryParms parms = new AlarmQueryParms();
            parms.sortStyle = sortStyle;
            parms.ackType = ackType;
            parms.filters = filterArray;
            parms.limit = limit;
            parms.multiple = multiple;
            parms.display = display;

            // query the alarms with the new filters array
            Alarm[] alarms = AlarmFactory.getAlarms(sortStyle, ackType, parms.getFilters(), limit, multiple * limit);
            
            ModelAndView modelAndView = new ModelAndView(getSuccessView());
            modelAndView.addObject("alarms", alarms);
            modelAndView.addObject("parms", parms);
            return modelAndView;
        } catch (SQLException e) {
            throw new ServletException("", e);
        }
    }

    private Integer getDefaultShortLimit() {
        return m_defaultShortLimit;
    }

    public void setDefaultShortLimit(Integer limit) {
        m_defaultShortLimit = limit;
    }

    private Integer getDefaultLongLimit() {
        return m_defaultLongLimit;
    }

    public void setDefaultLongLimit(Integer limit) {
        m_defaultLongLimit = limit;
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_defaultShortLimit, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultShortLimit > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(m_defaultLongLimit, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultLongLimit > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_successView, "property successView must be set");
    }

    public AcknowledgeType getDefaultAcknowledgeType() {
        return m_defaultAcknowledgeType;
    }

    public void setDefaultAcknowledgeType(AcknowledgeType defaultAcknowledgeType) {
        m_defaultAcknowledgeType = defaultAcknowledgeType;
    }

    public SortStyle getDefaultSortStyle() {
        return m_defaultSortStyle;
    }

    public void setDefaultSortStyle(SortStyle defaultSortStyle) {
        m_defaultSortStyle = defaultSortStyle;
    }

}
