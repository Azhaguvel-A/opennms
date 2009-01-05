//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.poller.Interface;
import org.opennms.netmgt.config.poller.Node;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;

/**
 * Represents a PollOutagesConfigManager
 * 
 * @author brozow
 */
abstract public class PollOutagesConfigManager implements PollOutagesConfig {

    /**
     * The config class loaded from the config file
     */
    private Outages m_config;

    /**
     * @param config
     *            The config to set.
     */
    protected void setConfig(Outages config) {
        m_config = config;
    }

    /**
     * @return Returns the config.
     */
    protected Outages getConfig() {
        return m_config;
    }

    /**
     * Return the outages configured.
     * 
     * @return the outages configured
     */
    public synchronized Outage[] getOutages() {
        return getConfig().getOutage();
    }

    /**
     * Return the specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the specified outage, null if not found
     */
    public synchronized Outage getOutage(String name) {
        for (Outage out : getConfig().getOutageCollection()) {
            if (out.getName().equals(name)) {
                return out;
            }
        }

        return null;
    }

    /**
     * Return the type for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the type for the specified outage, null if not found
     */
    public synchronized String getOutageType(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getType();
    }

    /**
     * Return the outage times for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the outage times for the specified outage, null if not found
     */
    public synchronized Time[] getOutageTimes(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getTime();
    }

    /**
     * Return the interfaces for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the interfaces for the specified outage, null if not found
     */
    public synchronized Interface[] getInterfaces(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getInterface();
    }

    /**
     * Return if interfaces is part of specified outage.
     * 
     * @param linterface
     *            the interface to be looked up
     * @param outName
     *            the outage name
     * 
     * @return the interface is part of the specified outage
     */
    public synchronized boolean isInterfaceInOutage(String linterface, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;

        return isInterfaceInOutage(linterface, out);
    }

    /**
     * Return if interfaces is part of specified outage.
     * 
     * @param linterface
     *            the interface to be looked up
     * @param out
     *            the outage
     * 
     * @return the interface is part of the specified outage
     */
    public synchronized boolean isInterfaceInOutage(String linterface, Outage out) {
        if (out == null)
            return false;

        for (Interface ointerface : out.getInterfaceCollection()) {
            if (ointerface.getAddress().equals(linterface)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param cal
     *            the calendar to lookup
     * @param outName
     *            the outage name
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(Calendar cal, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;

        return isTimeInOutage(cal, out);
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param time
     *            the time in millis to look up
     * @param outName
     *            the outage name
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(long time, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return isTimeInOutage(cal, out);
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param cal
     *            the calendar to lookup
     * @param outage
     *            the outage
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(Calendar cal, Outage outage) {
        return BasicScheduleUtils.isTimeInSchedule(cal, outage);

    }

    /**
     * Return if current time is part of specified outage.
     * 
     * @param outName
     *            the outage name
     * 
     * @return true if current time is in outage
     */
    public synchronized boolean isCurTimeInOutage(String outName) {
        // get current time
        Calendar cal = new GregorianCalendar();

        return isTimeInOutage(cal, outName);
    }

    /**
     * Return if current time is part of specified outage.
     * 
     * @param out
     *            the outage
     * 
     * @return true if current time is in outage
     */
    public synchronized boolean isCurTimeInOutage(Outage out) {
        // get current time
        Calendar cal = new GregorianCalendar();

        return isTimeInOutage(cal, out);
    }

    public synchronized void addOutage(Outage newOutage) {
        m_config.addOutage(newOutage);
    }

    public synchronized void removeOutage(String outageName) {
        m_config.removeOutage(getOutage(outageName));
    }

    public synchronized void removeOutage(Outage outageToRemove) {
        m_config.removeOutage(outageToRemove);
    }

    public synchronized void replaceOutage(Outage oldOutage, Outage newOutage) {
        int count = m_config.getOutageCount();
        for (int i = 0; i < count; i++) {
            if (m_config.getOutage(i).equals(oldOutage)) {
                m_config.setOutage(i, newOutage);
                return;
            }
        }
    }

    /*
     * <p>Return the nodes for specified outage</p>
     * 
     * @param name the outage that is to be looked up
     * 
     * @return the nodes for the specified outage, null if not found
     */
    public synchronized Node[] getNodeIds(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getNode();
    }

    /**
     * <p>
     * Return if nodeid is part of specified outage
     * </p>
     * 
     * @param lnodeid
     *            the nodeid to be looked up
     * @param outName
     *            the outage name
     * 
     * @return the node is part of the specified outage
     */
    public synchronized boolean isNodeIdInOutage(long lnodeid, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;

        return isNodeIdInOutage(lnodeid, out);
    }

    public synchronized Calendar getEndOfOutage(String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return null;

        return getEndOfOutage(out);
    }

    /**
     * Return a calendar representing the end time of this outage, assuming it's
     * currently active (i.e. right now is within one of the time periods)
     * 
     * FIXME: This code is almost identical to isTimeInOutage... We need to fix
     * it
     */
    public static synchronized Calendar getEndOfOutage(Outage out) {
        // FIXME: We need one that takes the time as a parm.  This makes it more testable
        return BasicScheduleUtils.getEndOfSchedule(out);
    }

    /**
     * <p>
     * Return if nodeid is part of specified outage
     * </p>
     * 
     * @param lnodeid
     *            the nodeid to be looked up
     * @param outName
     *            the outage
     * 
     * @return the node iis part of the specified outage
     */
    public synchronized boolean isNodeIdInOutage(long lnodeid, Outage out) {
        if (out == null)
            return false;

        for (Node onode : out.getNodeCollection()) {
            if ((long) onode.getId() == lnodeid) {
                return true;
            }
        }

        return false;
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     */
    public synchronized void saveCurrent() throws MarshalException, IOException, ValidationException {
        // Marshal to a string first, then write the string to the file. This
        // way the original configuration isn't lost if the XML from the
        // marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);

        String xmlString = stringWriter.toString();
        if (xmlString != null) {
            saveXML(xmlString);
        }
        
        update();

    }

    abstract protected void saveXML(String xmlString) throws IOException, MarshalException, ValidationException;
    
    abstract public void update() throws IOException, MarshalException, ValidationException;


}
