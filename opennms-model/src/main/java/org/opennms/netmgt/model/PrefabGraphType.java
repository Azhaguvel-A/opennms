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
// Modifications:
//
// 2007 Apr 05: Remove unused/deprecated getQueries and supporting
//              code. - dj@opennms.org
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
package org.opennms.netmgt.model;

/**
 * <p>PrefabGraphType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PrefabGraphType {

    private String m_defaultReport;

    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_graphWidth;

    private String m_graphHeight;

    private String m_includeDirectory;

    private int m_includeRescanInterval;

    /**
     * <p>Constructor for PrefabGraphType.</p>
     */
    public PrefabGraphType() {
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setDefaultReport</p>
     *
     * @param defaultReport a {@link java.lang.String} object.
     */
    public void setDefaultReport(String defaultReport) {
        m_defaultReport = defaultReport;
    }

    /**
     * <p>getDefaultReport</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultReport() {
        return m_defaultReport;
    }
    
    
    /**
     * <p>setGraphWidth</p>
     *
     * @param graphWidth a {@link java.lang.String} object.
     */
    public void setGraphWidth(String graphWidth) {
        m_graphWidth = graphWidth;
    }

    /**
     * <p>getGraphWidth</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphWidth() {
        return m_graphWidth;
    }
    
    /**
     * <p>setGraphHeight</p>
     *
     * @param graphHeight a {@link java.lang.String} object.
     */
    public void setGraphHeight(String graphHeight) {
        m_graphHeight = graphHeight;
    }

    /**
     * <p>getGraphHeight</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphHeight() {
        return m_graphHeight;
    }

    /**
     * <p>setCommandPrefix</p>
     *
     * @param commandPrefix a {@link java.lang.String} object.
     */
    public void setCommandPrefix(String commandPrefix) {
        m_commandPrefix = commandPrefix;
    }
    
    /**
     * <p>getCommandPrefix</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommandPrefix() {
        return m_commandPrefix;
    }

    /**
     * <p>setOutputMimeType</p>
     *
     * @param outputMimeType a {@link java.lang.String} object.
     */
    public void setOutputMimeType(String outputMimeType) {
        m_outputMimeType = outputMimeType;
    }
    
    /**
     * <p>getOutputMimeType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputMimeType() {
        return m_outputMimeType;
    }

    /**
     * Set the directory from which individual graph files should be included
     * @param includeDirectory - the new path for the include directory 
     * If not absolute, is a path relative to the directory containing the main configuration file
     */
    public void setIncludeDirectory(String includeDirectory) {
        m_includeDirectory = includeDirectory;
    }
    
    /** 
     * @return the include directory in which to look for any individual graph files
     */
    public String getIncludeDirectory() {
        return m_includeDirectory;
    }

    /**
     * Set the interval between rescans of the include directory, in milliseconds.  
     * The includeDirectory will only be rescanned for new files if it's been at least 
     * this long since the last scan, or the initial load.
     * @param timeout - the new timeout, in milliseconds
     */
    public void setIncludeDirectoryRescanInterval(int interval) {
        m_includeRescanInterval = interval;
    }

    /**
     * @return the timeout used
     */
    public int getIncludeDirectoryRescanTimeout() {
        return m_includeRescanInterval;
    }
}
