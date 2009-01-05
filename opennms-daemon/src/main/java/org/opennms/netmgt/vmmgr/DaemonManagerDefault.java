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


package org.opennms.netmgt.vmmgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.ServiceDaemon;

public class DaemonManagerDefault implements DaemonManager {
	
	private List<ServiceDaemon> m_serviceDaemons;
	
	public void setServiceDaemons(List<ServiceDaemon> serviceDaemons) {
		m_serviceDaemons = serviceDaemons;
	}

	public void pause() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.pause();
		}
	}

	public void resume() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.resume();
		}
	}

	public void start() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.start();
		}
	}

	public Map<String, String> status() {
        Map<String, String> stati = new HashMap<String, String>();
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			stati.put(serviceDaemon.getName(), serviceDaemon.status());
		}
		return stati;
	}

	public void stop() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			stopService(serviceDaemon);
		}
		System.exit(0);
	}

	
	private void stopService(ServiceDaemon serviceDaemon) {
		try {
			serviceDaemon.stop();
		} catch (Exception e) {
			System.err.println("An exception occurred stoppoing service "+serviceDaemon.getName());
			e.printStackTrace();
		}
	}

}
