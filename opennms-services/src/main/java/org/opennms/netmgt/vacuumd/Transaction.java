/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: April 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vacuumd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;

public class Transaction {
	
	private static ThreadLocal<Transaction> s_threadTX = new ThreadLocal<Transaction>();

	private static Transaction getTX() {
		Transaction tx = s_threadTX.get();
		if (tx == null) {
			throw new IllegalStateException("No transaction has been started for this thread!");
		}
		return tx;
	}
    
    private static void clearTX() {
        s_threadTX.set(null);
    }
	
	public static void begin() {
        
        log().debug("About to being Transaction for "+Thread.currentThread());
		Transaction tx = s_threadTX.get();
		if (tx != null) {
			throw new IllegalStateException("Cannot begin a transaction.. one has already been begun");
		}
        log().debug("Began Transaction for "+Thread.currentThread());
		s_threadTX.set(new Transaction());
		
	}
    
    private static Category log() {
        return ThreadCategory.getInstance(Transaction.class);
    }

    public static Connection getConnection(String dsName) throws SQLException {
        return getTX().doGetConnection(dsName);
    }

    public static void register(Statement stmt) {
        getTX().doRegister(stmt);
    }

    public static void register(ResultSet rs) {
        getTX().doRegister(rs);
    }

    public static void rollbackOnly() throws SQLException {
        getTX().doRollbackOnly();
    }

    public static void end() throws SQLException {
        log().debug("Ending transaction for "+Thread.currentThread());
        try {
            Transaction tx = getTX();
            tx.doEnd();
            log().debug((tx.m_rollbackOnly ? "Rolled Back" : "Committed") + " transcation for "+Thread.currentThread());
        } finally {
            clearTX();
        }
	}
    
    private Map<String, Connection> m_connections = new HashMap<String, Connection>();
    private List<Statement> m_statements = new LinkedList<Statement>();
    private List<ResultSet> m_resultSets = new LinkedList<ResultSet>();
    private boolean m_rollbackOnly = false;
	

    private void doRegister(Statement stmt) {
        m_statements.add(stmt);
    }

    private void doRegister(ResultSet rs) {
        m_resultSets.add(rs);
    }

    private void doClose() throws SQLException {
        for(ResultSet rs : m_resultSets) {
            rs.close();
        }
        for(Statement stmt : m_statements) {
            stmt.close();
        }
        for(Connection conn : m_connections.values()) {
            conn.close();
        }        
    }

    private void doEnd() throws SQLException {
        try {
            for(Connection conn : m_connections.values()) {
                if (m_rollbackOnly) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            }
        } finally {
            doClose();
        }
    }

    private void doRollbackOnly() throws SQLException {
        m_rollbackOnly = true;
    }

    private Connection doGetConnection(String dsName) throws SQLException {
        if (!m_connections.containsKey(dsName)) {
            DataSource ds = DataSourceFactory.getDataSource(dsName);
            Connection conn = ds.getConnection();
            m_connections.put(dsName, conn);
            conn.setAutoCommit(false);
        } 
        
        return m_connections.get(dsName);
    }




}
