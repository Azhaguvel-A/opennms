// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
package org.opennms.report.availability.svclayer;

/**
 * AvailabilityReportCriteria is the backing object for the availability
 * report page. It contains the criteria for running the report, the address
 * of mail recipients for the report and whether to put and entry in the
 * report locator table in the database (so that the report can be retrieved
 * in the future).
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */

import java.util.Date;

public class AvailabilityReportCriteria {

    private String m_monthFormat;

    private String m_categoryName;

    private String m_format;

    private Date m_periodEndDate;

    private String m_email;

    private String m_logo;

    private Boolean m_persist;
    
    private Boolean m_sendEmail;

    public String getLogo() {
        return m_logo;
    }

    public void setLogo(String logo) {
        m_logo = logo;
    }

    public String getEmail() {
        return m_email;
    }

    public void setEmail(String email) {
        m_email = email;
    }

    public String getCategoryName() {
        return m_categoryName;
    }

    public void setCategoryName(String categoryName) {
        m_categoryName = categoryName;
    }

    public String getFormat() {
        return m_format;
    }

    public void setFormat(String format) {
        m_format = format;
    }

    public String getMonthFormat() {
        return m_monthFormat;
    }

    public void setMonthFormat(String monthFormat) {
        m_monthFormat = monthFormat;
    }

    public Date getPeriodEndDate() {
        return m_periodEndDate;
    }

    public void setPeriodEndDate(Date periodEndDate) {
        m_periodEndDate = periodEndDate;
    }

    public void setPersist(Boolean persist) {
        m_persist = persist;
    }

    public Boolean getPersist() {
        return m_persist;
    }
    
    public void setSendEmail(Boolean sendEmail) {
        m_sendEmail = sendEmail;
    }

    public Boolean getSendEmail() {
        return m_sendEmail;
    }

}
