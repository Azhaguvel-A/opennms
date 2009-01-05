/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2006
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
package org.opennms.netmgt.linkd.jmx;
/*
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.apache.log4j.Category;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.LinkdConfigFactory;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

import org.opennms.netmgt.linkd.DbEventWriter;

public class Linkd implements LinkdMBean {
   public final static String LOG4J_CATEGORY = "OpenNMS.Linkd";

   public void init() {
	   ThreadCategory.setPrefix(LOG4J_CATEGORY);
		// Initialize the Capsd configuration factory.
		//
		try {
			LinkdConfigFactory.init();
            DataSourceFactory.init();
		} catch (ClassNotFoundException ex) {
			log().error("init: Failed to load configuration file ", ex);
		} catch (MarshalException ex) {
			log().error("Failed to load configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (ValidationException ex) {
			log().error("Failed to load configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (IOException ex) {
			log().error("Failed to load configuration", ex);
			throw new UndeclaredThrowableException(ex);
	    } catch (PropertyVetoException pve) {
	        log().fatal("Property veto failure loading database config", pve);
	        throw new UndeclaredThrowableException(pve);
	    } catch (SQLException sqle) {
	    	log().fatal("SQL exception loading database config", sqle);
	        throw new UndeclaredThrowableException(sqle);
	    }

	    
	    EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();
        
        org.opennms.netmgt.linkd.Linkd linkd = getLinkd();

        linkd.setEventMgr(mgr);
        linkd.setDbConnectionFactory(DataSourceFactory.getInstance());
        linkd.setLinkdConfig(LinkdConfigFactory.getInstance());
        linkd.setQueryManager(new DbEventWriter(DataSourceFactory.getDataSource()));
        
	    linkd.init();
	}
	public void start() {
		getLinkd().start();
	}
	public void stop() {
		getLinkd().stop();
	}
	public int getStatus() {
		return getLinkd().getStatus();
	}
	public String status() {
		return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
	}
	public String getStatusText() {
		return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
	}

	private Category log() {
	        return ThreadCategory.getInstance();
	}
	
	private org.opennms.netmgt.linkd.Linkd getLinkd() {
	        // Set the category prefix
	        ThreadCategory.setPrefix(LOG4J_CATEGORY);

	        return org.opennms.netmgt.linkd.Linkd.getInstance();
	}
}

*/
import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

public class Linkd extends AbstractSpringContextJmxServiceDaemon implements LinkdMBean {

    @Override
    protected String getLoggingPrefix() {
        return "OpenNMS.Linkd";
    }

    @Override
    protected String getSpringContext() {
        return "linkdContext";
    }

}
