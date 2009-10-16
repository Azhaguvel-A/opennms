package org.opennms.report.availability.svclayer;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.report.availability.AvailabilityConstants;
import org.opennms.report.datablock.Node;
import org.opennms.report.datablock.Outage;
import org.opennms.report.datablock.OutageSvcTimesList;

public class LegacyAvailabilityDataService implements
        AvailabilityDataService {
    
    CatFactory m_catFactory;
    
    /**
     * Common Rule for the category group.
     */
    
    private String m_commonRule;

    private Connection m_availConn;
    
    private Category log;
    
    private List<Node> m_nodes;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";
    
    public LegacyAvailabilityDataService() {
        
    ThreadCategory.setPrefix(LOG4J_CATEGORY);
    log = ThreadCategory.getInstance(DefaultAvailabilityReportService.class);
    
    
    
    log.debug("initialised DefaultAvailablityReportService");
    }

    public List<Node> getNodes(org.opennms.netmgt.config.categories.Category category, long startTime, long endTime) throws AvailabilityDataServiceException {
        
        m_nodes = new ArrayList<Node>();
        
        PreparedStatement ipInfoGetStmt = null;
        PreparedStatement servicesGetStmt = null;
        PreparedStatement outagesGetStmt = null;
        
        String categoryName = category.getLabel();
        try {
            CategoryFactory.init();
            m_catFactory = CategoryFactory.getInstance();
        } catch (IOException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw new AvailabilityDataServiceException("faild to init catFactory");
        } catch (MarshalException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw new AvailabilityDataServiceException("faild to init catFactory");
        } catch (ValidationException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw new AvailabilityDataServiceException("faild to init catFactory");
        }
        m_commonRule = m_catFactory.getEffectiveRule(categoryName);
        
        List<String> nodeIPs = FilterDaoFactory.getInstance().getIPList(m_commonRule);

        if (log.isDebugEnabled()) {
            log.debug("Number of IPs satisfying rule: " + nodeIPs.size());
        }
        

        List<String> monitoredServices = new ArrayList<String>(category.getServiceCollection());
        
        log.debug("categories in monitoredServices = " + monitoredServices.toString());
        
        initialiseConnection();
        // Prepare the statement to get service entries for each IP
        try {
            // Prepared statement to get node info for an IP
            ipInfoGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_INFO_FOR_IP);
            // Prepared statedment to get services info for an IP address
            servicesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_SVC_ENTRIES);
            // Prepared statement to get outages entries
            outagesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_OUTAGE_ENTRIES);
        } catch (SQLException e) {
            log.fatal("failed to setup prepared statement", e);
            throw new AvailabilityDataServiceException("failed to setup prepared statement");
        }
        
        /*
         * For each of these IP addresses, get the details from the
         * ifServices and services tables.
         */
        Iterator<String> ipIter = nodeIPs.iterator();
        String ip = null;
        ResultSet ipRS = null;
        try {
            // Prepared statement to get node info for an IP
            ipInfoGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_INFO_FOR_IP);
            while (ipIter.hasNext()) {
                ip = (String) ipIter.next();
                log.debug("ecexuting " + AvailabilityConstants.DB_GET_INFO_FOR_IP + " for " + ip);
    
                // get node info for this ip
                ipInfoGetStmt.setString(1, ip);
    
                ipRS = ipInfoGetStmt.executeQuery();
                
                // now handle all the results from this
                while (ipRS.next()) {

                    int nodeid = ipRS.getInt(1);
                    String nodeName = ipRS.getString(2);

                    // get the services for this IP address
                    ResultSet svcRS = null;
                    servicesGetStmt.setLong(1, nodeid);
                    servicesGetStmt.setString(2, ip);
                    servicesGetStmt.setString(3, ip);
                    servicesGetStmt.setLong(4, nodeid);
                    svcRS = servicesGetStmt.executeQuery();
                    // create node objects for this nodeID/IP/service
                    while (svcRS.next()) {
                        // read data from the resultSet
                        int svcid = svcRS.getInt(1);
                        String svcname = svcRS.getString(2);

                        /*
                         * If the list is empty, we assume all services are
                         * monitored. If it has any, we use it as a filter
                         */
                        if (monitoredServices.isEmpty() || monitoredServices.contains(svcname)) {

                            OutageSvcTimesList outageSvcTimesList = new OutageSvcTimesList();
                            getOutagesNodeIpSvc(nodeid, nodeName, ip, svcid,
                                                svcname, outageSvcTimesList,
                                                outagesGetStmt,
                                                startTime, endTime);

                           
                        }
                    }

                }
               
            }
        } catch (SQLException e) {
            log.fatal("failed to execute prepared statement", e);
            throw new AvailabilityDataServiceException("failed to execute prepared statement");
        } finally {
            try {
                if (ipRS != null) {
                    ipRS.close();
                }
                if (servicesGetStmt != null) {
                    servicesGetStmt.close();
                }

                if (ipInfoGetStmt != null) {
                    ipInfoGetStmt.close();
                }

                if (outagesGetStmt != null) {
                    outagesGetStmt.close();
                }

                if (m_availConn != null) {
                    closeConnection();
                }
            } catch (SQLException e) {
                log.fatal("failed to close ipInfo prepared statement", e);
                throw new AvailabilityDataServiceException("failed to close ipInfo prepared statement");
            } 
        }

        
        return m_nodes;
    }
    
    /**
     * Get all outages for this nodeid/ipaddr/service combination and add it
     * to m_nodes.
     */
    private void getOutagesNodeIpSvc(int nodeid, String nodeName,
            String ipaddr, int serviceid, String serviceName,
            OutageSvcTimesList outageSvcTimesList,
            PreparedStatement outagesGetStmt, 
            long startTime,long endTime) throws SQLException {
        

        // Get outages for this node/ip/svc pair
        try {

            outagesGetStmt.setInt(1, nodeid);
            outagesGetStmt.setString(2, ipaddr);
            outagesGetStmt.setInt(3, serviceid);

            ResultSet rs = outagesGetStmt.executeQuery();
            

            if (m_nodes != null && m_nodes.size() > 0) {
                ListIterator<Node> lstIter = m_nodes.listIterator();
                boolean foundFlag = false;
                Node oldNode = null;
                while (lstIter.hasNext()) {
                    oldNode = (Node) lstIter.next();
                    if (oldNode != null && oldNode.getNodeID() == nodeid) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    Node newNode = new Node(nodeName, nodeid);
                    newNode.addInterface(ipaddr, serviceName);
                    m_nodes.add(newNode);
                } else {
                    oldNode.addInterface(ipaddr, serviceName);
                }
            } else {
                Node newNode = new Node(nodeName, nodeid);
                newNode.addInterface(ipaddr, serviceName);
                if (m_nodes == null) {
                    log.debug("NODES IS NULL");
                }
                m_nodes.add(newNode);
            }

            while (rs.next()) {
                Timestamp lost = rs.getTimestamp(1);
                Timestamp regained = rs.getTimestamp(2);
                long losttime = lost.getTime();
                long regainedtime = 0;

                if (regained != null) {
                    regainedtime = regained.getTime();
                }

                if (regainedtime > 0) {
                    if (regainedtime <= startTime
                            || losttime >= endTime) {
                        continue;
                    }
                } else {
                    if (losttime >= endTime) {
                        continue;
                    }
                }
                Outage outage = new Outage(losttime, regainedtime);
                outageSvcTimesList.add(outage);
                addNode(nodeName, nodeid, ipaddr, serviceName, losttime,
                        regainedtime);
            }
            if (rs != null) {
                rs.close();
            }

        } catch (SQLException e) {
            log.fatal("SQL Error occured while getting the outages ", e);
            throw e;
        }
    }

    /**
     * This method adds a unique tuple to the list of nodes m_nodes.
     */
    public void addNode(String nodeName, int nodeid, String ipaddr,
            String serviceid, long losttime, long regainedtime) {
        if (m_nodes == null) {
            log.debug("adding new arraylis");
            m_nodes = new ArrayList<Node>();
        } else {
            if (m_nodes.size() <= 0) {
                Node newNode = new Node(nodeName, nodeid);
                // if(log.isDebugEnabled())
                // log.debug("Created the new node.");
                if (losttime > 0) {
                    if (regainedtime > 0) {
                        newNode.addInterface(ipaddr, serviceid, losttime,
                                             regainedtime);
                    } else {
                        newNode.addInterface(ipaddr, serviceid, losttime);
                    }
                } else {
                    newNode.addInterface(ipaddr, serviceid);
                }
                m_nodes.add(newNode);
                return;
            } else // look for the node with the nodeName
            {
                Node newNode = null;
                boolean foundFlag = false;
                ListIterator<Node> lstIter = m_nodes.listIterator();
                while (lstIter.hasNext()) {
                    newNode = lstIter.next();
                    if (newNode.getNodeID() == nodeid) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    newNode = new Node(nodeName, nodeid);
                    if (losttime > 0) {
                        if (regainedtime > 0) {
                            newNode.addInterface(ipaddr, serviceid, losttime,
                                                 regainedtime);
                        } else {
                            newNode.addInterface(ipaddr, serviceid, losttime);
                        }
                    } else {
                        newNode.addInterface(ipaddr, serviceid);
                    }
                    m_nodes.add(newNode);
                    return;
                } else {
                    if (losttime > 0) {
                        if (regainedtime > 0) {
                            newNode.addInterface(ipaddr, serviceid, losttime,
                                                 regainedtime);
                        } else {
                            newNode.addInterface(ipaddr, serviceid, losttime);
                        }
                    } else {
                        newNode.addInterface(ipaddr, serviceid);
                    }
                    return;
                }
            }
        }
    }
    /**
     * Initializes the database connection.
     */
    private void initialiseConnection() throws AvailabilityDataServiceException {
        
        //
        // Initialize the DataCollectionConfigFactory
        //
        try {
            DataSourceFactory.init();
            m_availConn = DataSourceFactory.getInstance().getConnection();
        } catch (MarshalException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new AvailabilityDataServiceException("failed to load data collection configuration");
        } catch (ValidationException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new AvailabilityDataServiceException("failed to load data collection configuration");
        } catch (IOException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log.fatal("initialize: Failed loading database driver.", e);
            throw new AvailabilityDataServiceException("failed to load data collection configuration");
        } catch (SQLException e) {
            log.fatal(
                      "initialize: Failed getting connection to the database.",
                      e);
            throw new AvailabilityDataServiceException("failed to load data collection configuration");
        } catch (PropertyVetoException e) {
            log.fatal(
                      "initialize: Failed getting connection to the database.",
                      e);
            throw new AvailabilityDataServiceException("initialize: Failed getting connection to the database");
        }
    }

    /**
     * Closes the database connection.
     */
    private void closeConnection() {
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (m_availConn != null) {
            try {
                m_availConn.close();
                m_availConn = null;
            } catch (Throwable t) {
                log.warn(
                         "initialize: an exception occured while closing the "
                                 + "JDBC connection", t);
            }
        }
    }
    
    

}
