//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 31: Log details about why we can't close a data source. - dj@opennms.org
// 2007 Aug 02: Prepare for Castor 1.0.5. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

/**
 * <p>
 * This is the singleton class used to load the OpenNMS database configuration
 * from the opennms-database.xml. This provides convenience methods to create
 * database connections to the database configured in this default xml
 * </p>
 * 
 * <p>
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods
 * </p>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class DataSourceFactory implements DataSource {

    /**
     * The singleton instance of this factory
     */
    private static DataSource m_singleton = null;

    private static Map<String, DataSource> m_dataSources = new HashMap<String, DataSource>();
    
    private static List<Runnable> m_closers = new LinkedList<Runnable>();

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws SQLException 
     * @throws PropertyVetoException 
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException, ClassNotFoundException, PropertyVetoException, SQLException {
        if (!isLoaded("opennms")) {
            init("opennms");
        }
    }

    public static synchronized void init(final String dsName) throws IOException, MarshalException, ValidationException, ClassNotFoundException, PropertyVetoException, SQLException {
        if (isLoaded(dsName)) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
        final C3P0ConnectionFactory dataSource = new C3P0ConnectionFactory(cfgFile.getPath(), dsName);
        
        m_closers.add(new Runnable() {
            public void run() {
                try {
                    dataSource.close();
                } catch (Exception e) {
                    ThreadCategory.getInstance(DataSourceFactory.class).info("Unabled to close datasource " + dsName + ": " + e, e);
                }
            }
        });
        
        // Springframework provided proxies that make working with transactions much easier
        LazyConnectionDataSourceProxy lazyProxy = new LazyConnectionDataSourceProxy(dataSource);
        
        setInstance(dsName, lazyProxy);
    }

    private static boolean isLoaded(String dsName) {
        return m_dataSources.containsKey(dsName);			
    }

    /**
     * <p>
     * Return the singleton instance of this factory. This is the instance of
     * the factory that was last created when the <code>
     * init</code> or
     * <code>reload</code> method was invoked. The instance will not change
     * unless a <code>reload</code> method is invoked.
     * </p>
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DataSource getInstance() {
//      m_dataSources.put("opennms",m_singleton);
        return getInstance("opennms");
    }

    public static synchronized DataSource getInstance(String name) {
        DataSource dataSource = m_dataSources.get(name);
        if (dataSource == null) {
            throw new IllegalArgumentException("Unable to locate data source named " + name + ".  Does this need to be init'd?");
        }
        return m_dataSources.get(name);

    }

    /**
     * Return a new database connection to the database configured in the
     * <tt>opennms-database.xml</tt>. The database connection is not managed
     * by the factory and must be release by the caller by using the
     * <code>close</code> method.
     * 
     * @return a new database connection to the database configured in the
     *         <tt>opennms-database.xml</tt>
     * 
     * @throws java.sql.SQLException
     *             Thrown if there is an error opening the connection to the
     *             database.
     */
    public Connection getConnection() throws SQLException {
        return getConnection("opennms");
    }

    public Connection getConnection(String dsName) throws SQLException {
        return m_dataSources.get(dsName).getConnection();
    }

    public static void setInstance(DataSource singleton) {
        m_singleton=singleton;
        setInstance("opennms", singleton);
    }

    public static void setInstance(String dsName, DataSource singleton) {
        m_dataSources.put(dsName,singleton);
    }

    /**
     * Return the datasource configured for the database
     * 
     * @return the datasource configured for the database
     */
    public static DataSource getDataSource() {
        return getDataSource("opennms");
    }

    public static DataSource getDataSource(String dsName) {
        return m_dataSources.get(dsName);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return m_singleton.getLogWriter();
    }

    public PrintWriter getLogWriter(String dsName) throws SQLException {
        return m_dataSources.get(dsName).getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        setLogWriter("opennms", out);
    }

    public void setLogWriter(String dsName, PrintWriter out) throws SQLException {
        m_dataSources.get(dsName).setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        setLoginTimeout("opennms", seconds);
    }

    public void setLoginTimeout(String dsName, int seconds) throws SQLException {
        m_dataSources.get(dsName).setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return getLoginTimeout("opennms");
    }

    public int getLoginTimeout(String dsName) throws SQLException {
        return m_dataSources.get(dsName).getLoginTimeout();
    }

    public void initialize() throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }

    public static synchronized void close() throws SQLException {
        
        for(Runnable closer : m_closers) {
            closer.run();
        }
        
        m_closers.clear();
        m_dataSources.clear();

    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }
}
