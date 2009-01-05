
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:

//2006 Aug 15: Formatting. - dj@opennms.org

//Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact: 
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

//Tab Size = 8


package org.opennms.netmgt.collectd;


import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;


/**
 * This class encapsulates all the information required by the SNMP collector in
 * order to perform data collection for an individual interface and store that
 * data in an appropriately named RRD file.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class IfInfo extends SnmpCollectionResource {

    private CollectionType m_collType;
    private SNMPCollectorEntry m_entry;
    private int m_nodeId;
    private int m_ifIndex;
    private int m_ifType;
    private String m_label;
    private String m_ifAlias;

    public IfInfo(ResourceType def, CollectionAgent agent, SnmpIfData snmpIfData) {
        super(def);
        m_nodeId = snmpIfData.getNodeId();
        m_collType = snmpIfData.getCollectionType();
        m_ifIndex = snmpIfData.getIfIndex();
        m_ifType = snmpIfData.getIfType();
        m_label = snmpIfData.getLabelForRRD();
        m_ifAlias = snmpIfData.getIfAlias();
    }
    
    private int getNodeId() {
        return m_nodeId;
    }

    public int getIndex() {
        return m_ifIndex;
    }

    public int getType() {
        return m_ifType;
    }

    public String getLabel() {
        return m_label;
    }

    public void setIfAlias(String ifAlias) {
        m_ifAlias = ifAlias;
    }

    String getCurrentIfAlias() {
        return m_ifAlias;
    }

    public CollectionType getCollType() {
        return m_collType;
    }

    public void setEntry(SNMPCollectorEntry ifEntry) {
        m_entry = ifEntry;
    }

    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }


    /**
     ** @deprecated
     **/

    String getNewIfAlias() {
        // FIXME: This should not be null
        if (getEntry() == null) {
            return getCurrentIfAlias();
        }
        return getEntry().getValueForBase(SnmpCollector.IFALIAS_OID);
    }

    boolean currentAliasIsOutOfDate(String ifAlias) {
        log().debug("currentAliasIsOutOfDate: ifAlias from collection = " + ifAlias + ", current ifAlias = " + getCurrentIfAlias());
        return ifAlias != null && !ifAlias.equals(getCurrentIfAlias());
    }

    void logAlias(String ifAlias) {
        Category log = log();
        if (log.isDebugEnabled()) {
            log.debug("Alias for RRD directory name = " + ifAlias);
        }
    }

    String getAliasDir(String ifAlias, String ifAliasComment) {
        if (ifAlias != null) {
            if (ifAliasComment != null) {
                int si = ifAlias.indexOf(ifAliasComment);
                if (si > -1) {
                    ifAlias = ifAlias.substring(0, si).trim();
                }
            }
            if (ifAlias != null) {
                ifAlias = AlphaNumeric.parseAndReplaceExcept(ifAlias,
                                                             SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
            }
        }

        logAlias(ifAlias);

        return ifAlias;
    }

    void logForceRescan(String ifAlias) {

        if (log().isDebugEnabled()) {
            log().debug("Forcing rescan.  IfAlias " + ifAlias
                        + " for index " + getIndex()
                        + " does not match DB value: "
                        + getCurrentIfAlias());
        }
    }

    boolean isScheduledForCollection() {
        log().debug(this+".isSnmpPrimary = "+getCollType());
        log().debug("minCollType = "+getCollection().getMinimumCollectionType());

        boolean isScheduled = !getCollType().isLessThan(getCollection().getMinimumCollectionType());
        log().debug(getCollType()+" >= "+getCollection().getMinimumCollectionType()+" = "+isScheduled);
        return isScheduled;

    }

    private OnmsSnmpCollection getCollection() {
        return getResourceType().getCollection();
    }

    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(getNodeId()));
        File ifDir = new File(nodeDir, getLabel());
        return ifDir;
    }

    public String toString() {
        return "node["+ getNodeId() + "].interfaceSnmp[" + getLabel() + ']';
    }

    boolean shouldStore(ServiceParameters serviceParameters) {
        if (serviceParameters.getStoreByNodeID().equals("normal")) {
            return isScheduledForCollection();
        } else {
            return serviceParameters.getStoreByNodeID().equals("true");
        }
    }

    public boolean shouldPersist(ServiceParameters serviceParameters) {

        boolean shdprsist = shouldStore(serviceParameters) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getCurrentIfAlias()));
        log().debug("shouldPersist = " + shdprsist);
        return shdprsist;
    }
    
    public String getResourceTypeName() {
        return "if"; //This is IfInfo, must be an interface
    }
    
    public String getInstance() {
        return Integer.toString(getIndex()); //For interfaces, use ifIndex as it's unique within a node (by definition)
    }
} // end class
