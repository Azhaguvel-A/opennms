//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Eliminate a warning. - dj@opennms.org
// 2006 Apr 27: Added support for pathOutageEnabled
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.rws.StandbyUrl;

import org.opennms.netmgt.config.rws.BaseUrl;

/**
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class RWSConfigManager implements RWSConfig {
    
    public synchronized BaseUrl getBaseUrl() {
        
        BaseUrl url = m_config.getBaseUrl();
        return url;
    }
 
    public synchronized StandbyUrl[] getStanbyUrls() {
        
        StandbyUrl[] urls = m_config.getStandbyUrl();
        return urls;
    }
 

    public RWSConfigManager(Reader reader) throws MarshalException, ValidationException, IOException {
        reloadXML(reader);
    }

    public RWSConfigManager() {
    }
    
//    public abstract void update() throws IOException, MarshalException, ValidationException;
//
//    protected abstract void saveXml(String xml) throws IOException;
//
//    /**
//     * The config class loaded from the config file
//     */
    private RwsConfiguration m_config;

    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = (RwsConfiguration) Unmarshaller.unmarshal(RwsConfiguration.class, reader);
        // call the init methids that populate local object
    }

//    /**
//     * Saves the current in-memory configuration to disk and reloads
//     */
//    public synchronized void save() throws MarshalException, IOException, ValidationException {
//    
//        // marshall to a string first, then write the string to the file. This
//        // way the original config
//        // isn't lost if the xml from the marshall is hosed.
//        StringWriter stringWriter = new StringWriter();
//        Marshaller.marshal(m_config, stringWriter);
//        saveXml(stringWriter.toString());
//    
//        update();
//    }

    /**
     * Return the poller configuration object.
     */
    public synchronized RwsConfiguration getConfiguration() {
        return m_config;
    }

    
 
    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

     
}
