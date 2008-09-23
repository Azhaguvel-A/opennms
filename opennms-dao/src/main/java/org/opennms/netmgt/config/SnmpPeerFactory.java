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
// 2005 Mar 08: Added saveCurrent, optimize, and define methods.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.protocols.ip.IPv4Address;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This class is the main respository for SNMP configuration information used by
 * the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.snmp.SnmpAgentConfig SnmpAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public final class SnmpPeerFactory extends PeerFactory {
    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static SnmpConfig m_config;
    
    private static File m_configFile;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    private static final int VERSION_UNSPECIFIED = -1;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private SnmpPeerFactory(File configFile) throws IOException, MarshalException, ValidationException {
        this(new FileSystemResource(configFile));
    }
    
    public SnmpPeerFactory(Resource resource) {
        m_config = CastorUtils.unmarshalWithTranslatedExceptions(SnmpConfig.class, resource);
    }
    
    public SnmpPeerFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshalWithTranslatedExceptions(SnmpConfig.class, rdr);
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

        File cfgFile = getFile();

        log().debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new SnmpPeerFactory(cfgFile);

        m_loaded = true;
    }

    private static Category log() {
        return ThreadCategory.getInstance(SnmpPeerFactory.class);
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

    /**
     * Saves the current settings to disk
     */
    public static synchronized void saveCurrent() throws IOException, MarshalException, ValidationException {

        // Marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshall is hosed.
        String marshalledConfig = marshallConfig();
        if (marshalledConfig != null) {
            FileWriter fileWriter = new FileWriter(getFile());
            fileWriter.write(marshalledConfig);
            fileWriter.flush();
            fileWriter.close();
        }

        reload();
    }


    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized SnmpPeerFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    public static synchronized void setFile(File configFile) {
        File oldFile = m_configFile;
        m_configFile = configFile;
        
        // if the file changed then we need to reload the config
        if (oldFile == null || m_configFile == null || !oldFile.equals(m_configFile)) {
            m_singleton = null;
            m_loaded = false;
        }
    }
    
    public static synchronized File getFile() throws IOException {
        if (m_configFile == null) {
            m_configFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME);
        }
        return m_configFile;
        
    }
    
    public static synchronized void setInstance(SnmpPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    /**
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     */
    @SuppressWarnings("unused")
    private void define(InetAddress ip, String community) throws UnknownHostException {
        Category log = log();

        // Convert IP to long so that it easily compared in range elements
        int address = new IPv4Address(ip).getAddress();

        // Copy the current definitions so that elements can be added and
        // removed
        ArrayList<Definition> definitions =
            new ArrayList<Definition>(m_config.getDefinitionCollection());

        // First step: Find the first definition matching the read-community or
        // create a new definition, then add the specific IP
        Definition definition = null;
        for (Iterator<Definition> definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition = definitionsIterator.next();

            if ((currentDefinition.getReadCommunity() != null
                 && currentDefinition.getReadCommunity().equals(community))
                || (currentDefinition.getReadCommunity() == null
                    && m_config.getReadCommunity() != null
                    && m_config.getReadCommunity().equals(community))) {
                if (log.isDebugEnabled())
                    log.debug("define: Found existing definition "
                              + "with read-community " + community);
                definition = currentDefinition;
                break;
            }
        }
        if (definition == null) {
            if (log.isDebugEnabled())
                log.debug("define: Creating new definition");

            definition = new Definition();
            definition.setReadCommunity(community);
            definitions.add(definition);
        }
        definition.addSpecific(ip.getHostAddress());

        // Second step: Find and remove any existing specific and range
        // elements with matching IP among all definitions except for the
        // definition identified in the first step
        for (Iterator<Definition> definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition = definitionsIterator.next();

            // Ignore this definition if it was the one identified by the first
            // step
            if (currentDefinition == definition)
                continue;

            // Remove any specific elements that match IP
            while (currentDefinition.removeSpecific(ip.getHostAddress())) {
                if (log.isDebugEnabled())
                    log.debug("define: Removed an existing specific "
                              + "element with IP " + ip);
            }

            // Split and replace any range elements that contain IP
            ArrayList<Range> ranges =
                new ArrayList<Range>(currentDefinition.getRangeCollection());
            Range[] rangesArray = currentDefinition.getRange();
            for (int rangesArrayIndex = 0;
                 rangesArrayIndex < rangesArray.length;
                 rangesArrayIndex++) {
                Range range = rangesArray[rangesArrayIndex];
                int begin = new IPv4Address(range.getBegin()).getAddress();
                int end = new IPv4Address(range.getEnd()).getAddress();
                if (address >= begin && address <= end) {
                    if (log.isDebugEnabled())
                        log.debug("define: Splitting range element "
                                  + "with begin " + range.getBegin() + " and "
                                  + "end " + range.getEnd());

                    if (begin == end) {
                        ranges.remove(range);
                        continue;
                    }

                    if (address == begin) {
                        range.setBegin(IPv4Address.addressToString(address + 1));
                        continue;
                    }

                    if (address == end) {
                        range.setEnd(IPv4Address.addressToString(address - 1));
                        continue;
                    }

                    Range head = new Range();
                    head.setBegin(range.getBegin());
                    head.setEnd(IPv4Address.addressToString(address - 1));

                    Range tail = new Range();
                    tail.setBegin(IPv4Address.addressToString(address + 1));
                    tail.setEnd(range.getEnd());

                    ranges.remove(range);
                    ranges.add(head);
                    ranges.add(tail);
                }
            }
            currentDefinition.setRange(ranges);
        }

        // Store the altered list of definitions
        m_config.setDefinition(definitions);
    }
    
    public synchronized SnmpAgentConfig getAgentConfig(InetAddress agentAddress) {
        return getAgentConfig(agentAddress, VERSION_UNSPECIFIED);
    }
    
    private synchronized SnmpAgentConfig getAgentConfig(InetAddress agentInetAddress, int requestedSnmpVersion) {

        if (m_config == null) {
            SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
            if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
                agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
            } else {
                agentConfig.setVersion(requestedSnmpVersion);
            }
            
            return agentConfig;
        }
        
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
        
        //Now set the defaults from the m_config
        setSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);

        // Attempt to locate the node
        //
        DEFLOOP: for (Definition def: m_config.getDefinitionCollection()) {
            // check the specifics first
            //
            for (String saddr : def.getSpecificCollection()) {
                try {
                    InetAddress addr = InetAddress.getByName(saddr);
                    if (addr.equals(agentConfig.getAddress())) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host " + saddr + " to InetAddress", e);
                }
            }

            // check the ranges
            //
            long lhost = toLong(agentConfig.getAddress());
            for (Range rng : def.getRangeCollection()) {
                try {
                    InetAddress begin = InetAddress.getByName(rng.getBegin());
                    InetAddress end = InetAddress.getByName(rng.getEnd());

                    long start = toLong(begin);
                    long stop = toLong(end);

                    if (start <= lhost && lhost <= stop) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host(s) " + rng.getBegin() + " - " + rng.getEnd() + " to InetAddress", e);
                }
            }
            
            // check the matching ip expressions
            for (String ipMatch : def.getIpMatchCollection()) {
                if (verifyIpMatch(agentInetAddress.getHostAddress(), ipMatch)) {
                    setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                    break DEFLOOP;
                }
            }
            
        } // end DEFLOOP

        if (agentConfig == null) {
            Definition def = new Definition();
            setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
        }

        return agentConfig;

    }

    private void setSnmpAgentConfig(SnmpAgentConfig agentConfig, Definition def, int requestedSnmpVersion) {
        
        int version = determineVersion(def, requestedSnmpVersion);
        
        setCommonAttributes(agentConfig, def, version);
        agentConfig.setSecurityLevel(determineSecurityLevel(def));
        agentConfig.setSecurityName(determineSecurityName(def));
        agentConfig.setAuthProtocol(determineAuthProtocol(def));
        agentConfig.setAuthPassPhrase(determineAuthPassPhrase(def));
        agentConfig.setPrivPassPhrase(determinePrivPassPhrase(def));
        agentConfig.setPrivProtocol(determinePrivProtocol(def));
        agentConfig.setReadCommunity(determineReadCommunity(def));
        agentConfig.setWriteCommunity(determineWriteCommunity(def));
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(SnmpAgentConfig agentConfig, Definition def, int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
        agentConfig.setMaxRequestSize(determineMaxRequestSize(def));
        agentConfig.setMaxVarsPerPdu(determineMaxVarsPerPdu(def));
        agentConfig.setMaxRepetitions(determineMaxRepetitions(def));
        InetAddress proxyHost = determineProxyHost(def);
        
        if (proxyHost != null) {
            agentConfig.setProxyFor(agentConfig.getAddress());
            agentConfig.setAddress(determineProxyHost(def));
        }
    }

    private int determineMaxRepetitions(Definition def) {
        return (!def.hasMaxRepetitions() ? 
                (!m_config.hasMaxRepetitions() ?
                  SnmpAgentConfig.DEFAULT_MAX_REPETITIONS : m_config.getMaxRepetitions()) : def.getMaxRepetitions());
    }

	private InetAddress determineProxyHost(Definition def) {
        InetAddress inetAddr = null;
        String address = def.getProxyHost() == null ? 
                (m_config.getProxyHost() == null ? null : m_config.getProxyHost()) : def.getProxyHost();
        if (address != null) {
            try {
                inetAddr =  InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                log().error("determineProxyHost: Problem converting proxy host string to InetAddress", e);
            }
        }
        return inetAddr;
    }

    private int determineMaxVarsPerPdu(Definition def) {
        return (!def.hasMaxVarsPerPdu() ? 
                (!m_config.hasMaxVarsPerPdu() ?
                  SnmpAgentConfig.DEFAULT_MAX_VARS_PER_PDU : m_config.getMaxVarsPerPdu()) : def.getMaxVarsPerPdu());
    }
    /**
     * Helper method to search the snmp-config for the appropriate read
     * community string.
     * @param def
     * @return
     */
    private String determineReadCommunity(Definition def) {
        return (def.getReadCommunity() == null ? (m_config.getReadCommunity() == null ? SnmpAgentConfig.DEFAULT_READ_COMMUNITY :m_config.getReadCommunity()) : def.getReadCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate write
     * community string.
     * @param def
     * @return
     */
    private String determineWriteCommunity(Definition def) {
        return (def.getWriteCommunity() == null ? (m_config.getWriteCommunity() == null ? SnmpAgentConfig.DEFAULT_WRITE_COMMUNITY :m_config.getWriteCommunity()) : def.getWriteCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate maximum
     * request size.  The default is the minimum necessary for a request.
     * @param def
     * @return
     */
    private int determineMaxRequestSize(Definition def) {
        return (!def.hasMaxRequestSize() ? (!m_config.hasMaxRequestSize() ? SnmpAgentConfig.DEFAULT_MAX_REQUEST_SIZE : m_config.getMaxRequestSize()) : def.getMaxRequestSize());
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineSecurityName(Definition def) {
        String securityName = (def.getSecurityName() == null ? m_config.getSecurityName() : def.getSecurityName() );
        if (securityName == null) {
            securityName = SnmpAgentConfig.DEFAULT_SECURITY_NAME;
        }
        return securityName;
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthProtocol(Definition def) {
        String authProtocol = (def.getAuthProtocol() == null ? m_config.getAuthProtocol() : def.getAuthProtocol());
        if (authProtocol == null) {
            authProtocol = SnmpAgentConfig.DEFAULT_AUTH_PROTOCOL;
        }
        return authProtocol;
    }
    
    /**
     * Helper method to find a authentication passphrase to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthPassPhrase(Definition def) {
        String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        if (authPassPhrase == null) {
            authPassPhrase = SnmpAgentConfig.DEFAULT_AUTH_PASS_PHRASE;
        }
        return authPassPhrase;
    }

    /**
     * Helper method to find a privacy passphrase to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determinePrivPassPhrase(Definition def) {
        String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        if (privPassPhrase == null) {
            privPassPhrase = SnmpAgentConfig.DEFAULT_PRIV_PASS_PHRASE;
        }
        return privPassPhrase;
    }

    /**
     * Helper method to find a privacy protocol to use from the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determinePrivProtocol(Definition def) {
        String authPrivProtocol = (def.getPrivacyProtocol() == null ? m_config.getPrivacyProtocol() : def.getPrivacyProtocol());
        if (authPrivProtocol == null) {
            authPrivProtocol = SnmpAgentConfig.DEFAULT_PRIV_PROTOCOL;
        }
        return authPrivProtocol;
    }

    /**
     * Helper method to set the security level in v3 operations.  The default is
     * noAuthNoPriv if there is no authentication passphrase.  From there, if
     * there is a privacy passphrase supplied, then the security level is set to
     * authPriv else it falls out to authNoPriv.  There are only these 3 possible
     * security levels.
     * default 
     * @param def
     * @return
     */
    private int determineSecurityLevel(Definition def) {
        
        // use the def security level first
        if (def.hasSecurityLevel()) {
            return def.getSecurityLevel();
        }
        
        // use a configured default security level next
        if (m_config.hasSecurityLevel()) {
            return m_config.getSecurityLevel();
        }

        // if no security level configuration exists use
        int securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;

        String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        
        if (authPassPhrase == null) {
            securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;
        } else {
            if (privPassPhrase == null) {
                securityLevel = SnmpAgentConfig.AUTH_NOPRIV;
            } else {
                securityLevel = SnmpAgentConfig.AUTH_PRIV;
            }
        }
        
        return securityLevel;
    }

    /**
     * Helper method to search the snmp-config for a port
     * @param def
     * @return
     */
    private int determinePort(Definition def) {
        int port = 161;
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? port : m_config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the snmp-config 
     * @param def
     * @return
     */
    private long determineTimeout(Definition def) {
        long timeout = SnmpAgentConfig.DEFAULT_TIMEOUT;
        return (long)(def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? timeout : m_config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(Definition def) {        
        int retries = SnmpAgentConfig.DEFAULT_RETRIES;
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? retries : m_config.getRetry()) : def.getRetry());
    }

    /**
     * This method determines the configured SNMP version.
     * the order of operations is:
     * 1st: return a valid requested version
     * 2nd: return a valid version defined in a definition within the snmp-config
     * 3rd: return a valid version in the snmp-config
     * 4th: return the default version
     * 
     * @param def
     * @param requestedSnmpVersion
     * @return
     */
    private int determineVersion(Definition def, int requestedSnmpVersion) {
        
        int version = SnmpAgentConfig.VERSION1;
        
        String cfgVersion = "v1";
        if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
            if (def.getVersion() == null) {
                if (m_config.getVersion() == null) {
                    return version;
                } else {
                    cfgVersion = m_config.getVersion();
                }
            } else {
                cfgVersion = def.getVersion();
            }
        } else {
            return requestedSnmpVersion;
        }
        
        if (cfgVersion.equals("v1")) {
            version = SnmpAgentConfig.VERSION1;
        } else if (cfgVersion.equals("v2c")) {
            version = SnmpAgentConfig.VERSION2C;
        } else if (cfgVersion.equals("v3")) {
            version = SnmpAgentConfig.VERSION3;
        }
        
        return version;
    }

    public static synchronized SnmpConfig getSnmpConfig() {
        return m_config;
    }

    @SuppressWarnings("unused")
    private static synchronized void setSnmpConfig(SnmpConfig m_config) {
        SnmpPeerFactory.m_config = m_config;
    }

    /**
     * display an IP as a dotted quad xxx.xxx.xxx.xxx
     */
    public static String toIpAddr (long ip) {
        StringBuffer sb = new StringBuffer( 15 );
        for ( int shift=24; shift >0; shift-=8 ) {
            //process 3 bytes, from high order byte down.
            sb.append( Long.toString( (ip >>> shift) & 0xff ));
            sb.append('.');
        }
        sb.append(Long.toString( ip & 0xff ));
        return sb.toString();
    }
    
    /**
     * Enhancement: Allows specific or ranges to be merged into snmp configuration
     * with many other attributes.  Uses new classes the wrap Castor generated code to
     * help with merging, comparing, and optimizing definitions.  Thanks for your
     * initial work on this Gerald.
     * 
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     */
    public synchronized void define(SnmpEventInfo info) {
        SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
        mgr.mergeIntoConfig(info.createDef());
    }


    /**
     * Creates a string containing the XML of the current SnmpConfig
     * 
     * @return Marshalled SnmpConfig 
     */
    public static synchronized String marshallConfig() {
        String marshalledConfig = null;
        
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            Marshaller.marshal(m_config, writer);
            marshalledConfig = writer.toString();
        } catch (MarshalException e) {
            log().error("marshallConfig: Error marshalling configuration", e);
        } catch (ValidationException e) {
            log().error("marshallConfig: Error validating configuration", e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                log().error("marshallConfig: I/O Error closing string writer!", e);
            }
        }
        return marshalledConfig;
    }

}
