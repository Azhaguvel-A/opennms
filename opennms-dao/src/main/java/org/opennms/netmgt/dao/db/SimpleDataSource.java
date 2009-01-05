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
// 2008 May 31: Allow properties to be set, as well, and use this for the
//              user name and password.  Also add toString() and a
//              constructor where we pass a JdbcDataSource. - dj@opennms.org
// 2007 Jun 10: Support login timeout (in hopefully a not-too-hackish way). - dj@opennms.org
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
package org.opennms.netmgt.dao.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

public class SimpleDataSource implements DataSource {
    private String m_driver;
    private String m_url;
    private Properties m_properties = new Properties();
    private Integer m_timeout = null;

    public SimpleDataSource(String driver, String url, String user, String password) throws ClassNotFoundException {
        m_driver = driver;
        m_url = url;
        
        m_properties.put("user", user);
        m_properties.put("password", password);
        
        Class.forName(m_driver);
    }
    
    public SimpleDataSource(JdbcDataSource ds) throws ClassNotFoundException {
        this(ds.getClassName(), ds.getUrl(), ds.getUserName(), ds.getPassword());
        
        for (Param param : ds.getParamCollection()) {
            m_properties.put(param.getName(), param.getValue());
        }
    }

    public Connection getConnection() throws SQLException {
        if (m_timeout == null) {
            return DriverManager.getConnection(m_url, m_properties);
        } else {
            int oldTimeout = DriverManager.getLoginTimeout();
            DriverManager.setLoginTimeout(m_timeout);
            Connection conn = DriverManager.getConnection(m_url, m_properties);
            DriverManager.setLoginTimeout(oldTimeout);
            return conn;
        }
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("getConnection(String, String) not implemented");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter() not implemented");
    }

    public int getLoginTimeout() throws SQLException {
        return m_timeout == null ? -1 : m_timeout;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        m_timeout = seconds;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }

    public String getDriver() {
        return m_driver;
    }

    public String getPassword() {
        return m_properties.getProperty("password");
    }

    public Integer getTimeout() {
        return m_timeout;
    }

    public String getUrl() {
        return m_url;
    }

    public String getUser() {
        return m_properties.getProperty("user");
    }
    
    public Properties getProperties() {
        return m_properties;
    }
    
    public String toString() {
        StringBuffer props = new StringBuffer();
        if (m_properties.isEmpty()) {
            props.append(" none");
        } else {
            boolean first = true;
            for (Entry<Object, Object> entry : m_properties.entrySet()) {
                if (!first) {
                    props.append(",");
                }
                props.append(" ");
                props.append(entry.getKey());
                props.append("='");
                props.append(entry.getValue());
                props.append("'");
                
                first = false;
            }
        }
        return "SimpleDataSource[URL='" + getUrl() + "', driver class='" + getDriver() + "', properties:" + props + "]";
    }
}
