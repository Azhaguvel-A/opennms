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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Thresholding Daemon from the threshd-configuration xml file.
 * 
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:jamesz@opennms.com>James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class ThreshdConfigFactory extends ThreshdConfigManager {
    /**
     * The singleton instance of this factory
     */
    private static ThreshdConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Public constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    // Changed scope to public to simplify tests
    public ThreshdConfigFactory(Reader rdr, String localServer, boolean verifyServer) throws IOException, MarshalException, ValidationException {
        super(rdr, localServer, verifyServer);

    }

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
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        
        OpennmsServerConfigFactory.init();
        boolean verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
        String localServer = OpennmsServerConfigFactory.getInstance().getServerName();


        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(ThreshdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        FileReader rdr = new FileReader(cfgFile);
        try {
            m_singleton = new ThreshdConfigFactory(rdr, localServer, verifyServer);
            m_loaded = true;
        } finally {
            rdr.close();
        }
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

         public void reloadXML() throws IOException, MarshalException, ValidationException {
             /* FIXME: THIS IS WAY WRONG! Should only reload the xml not recreate the instance
              otherwise references to the old instance will still linger */
            reload();
        }

        protected void saveXML(String xmlString) throws IOException {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        }
         


    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ThreshdConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    public static void setInstance(ThreshdConfigFactory configFactory) {
    	m_loaded = true;
    	m_singleton = configFactory;
    }
}
