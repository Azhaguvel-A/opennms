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
package org.opennms.netmgt.provision.detector.jdbc;

import org.opennms.netmgt.provision.detector.jdbc.client.JDBCClient;
import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.jdbc.DBTools;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JdbcDetector extends BasicDetector<JDBCRequest, JDBCResponse>{
    
    private static int DEFAULT_PORT = 3306;
    private static int DEFAULT_TIMEOUT = 1000;
    private static int DEFAULT_RETRIES = 0;
    
    
    private String m_dbDriver = DBTools.DEFAULT_JDBC_DRIVER;
    private String m_user = DBTools.DEFAULT_DATABASE_USER;
    private String m_password = DBTools.DEFAULT_DATABASE_PASSWORD;
    private String m_url = DBTools.DEFAULT_URL;
    
    protected JdbcDetector() {
        super(DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        setServiceName("JDBC");
    }

    @Override
    protected void onInit() {
        expectBanner(resultSetNotNull());
    }
    
    @Override
    protected Client<JDBCRequest, JDBCResponse> getClient() {
        JDBCClient client = new JDBCClient();
        client.setDbDriver(getDbDriver());
        client.setUser(getUser());
        client.setPassword(getPassword());
        client.setUrl(getUrl());
        return client;
    }
    
    private ResponseValidator<JDBCResponse> resultSetNotNull(){
        return new ResponseValidator<JDBCResponse>() {

            public boolean validate(JDBCResponse response) throws Exception {
                return response.resultSetNotNull();
            }
        };
    }

    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    public String getDbDriver() {
        return m_dbDriver;
    }

    public void setUser(String username) {
        m_user = username;
    }

    public String getUser() {
        return m_user;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public String getUrl() {
        return m_url;
    }
	
    
    
}