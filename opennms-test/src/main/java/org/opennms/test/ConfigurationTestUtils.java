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
// 2008 Oct 24: Add getSpringResourceForResource. - dj@opennms.org
// 2008 Feb 05: Allow setting the object to null and we'll use ourselves. - dj@opennms.org
// 2007 Dec 25: Add getUrlForResource and getFileForResource. - dj@opennms.org
// 2007 Aug 02: Add getFileForConfigFile. - dj@opennms.org
// 2007 Jul 03: Check for something passing in a Class as an object (as I tend to do,
//              which breaks tests under Maven 2, but not under Eclipse) and suggest
//              that 'this' be used instead. - dj@opennms.org
// 2007 Apr 05: Add methods to get the opennms-daemon src/main/filtered/etc directory
//              and to set an absolute home directory. - dj@opennms.org
// 2007 Apr 05: Add methods to get the current directory, top-level project directory,
//              and daemon directory.  Add methods to set some common system properties
//              used in tests. - dj@opennms.org
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
package org.opennms.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import junit.framework.Assert;

public class ConfigurationTestUtils extends Assert {
    private static final String POM_FILE = "pom.xml";
    private static final String DAEMON_DIRECTORY = "opennms-daemon";

    public static URL getUrlForResource(Object obj, String resource) {
        URL url = getClass(obj).getResource(resource);
        assertNotNull("could not get resource '" + resource + "' as a URL", url);
        return url;
    }

    private static Class<? extends Object> getClass(Object obj) {
        return (obj != null) ? obj.getClass() : ConfigurationTestUtils.class.getClass();
    }
    
    public static Resource getSpringResourceForResource(Object obj, String resource) {
        try {
            return new FileSystemResource(getFileForResource(obj, resource));
        } catch (Throwable t) {
            return new InputStreamResource(getInputStreamForResource(obj, resource));
        }
    }
    
    public static File getFileForResource(Object obj, String resource) {
        URL url = getUrlForResource(obj, resource);
        
        String path = url.getFile();
        assertNotNull("could not get resource '" + resource + "' as a file", path);
        
        File file = new  File(path);
        assertTrue("could not get resource '" + resource + "' as a file--the file at path '" + path + "' does not exist", file.exists());
        
        return file;
    }

    public static Reader getReaderForResource(Object obj, String resource) {
        return new InputStreamReader(getInputStreamForResource(obj, resource));
    }

    public static InputStream getInputStreamForResource(Object obj,
            String resource) {
        assertFalse("obj should not be an instance of java.lang.Class; you usually want to use 'this'", obj instanceof Class);
        InputStream is = getClass(obj).getResourceAsStream(resource);
        assertNotNull("could not get resource '" + resource + "' as an input stream", is);
        return is;
    }
    
    public static Reader getReaderForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new StringReader(newConfig);
    }
    
    
    public static InputStream getInputStreamForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new ByteArrayInputStream(newConfig.getBytes());
    }
    
    
    public static String getConfigForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {

        Reader inputReader = getReaderForResource(obj, resource);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        
        StringBuffer buffer = new StringBuffer();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
    
        String newConfig = buffer.toString();
        for (String[] replacement : replacements) {
            newConfig = newConfig.replaceAll(replacement[0], replacement[1]);
        }
    
        return newConfig;
    }

    public static Reader getReaderForConfigFile(String configFile) throws FileNotFoundException {
        return new InputStreamReader(getInputStreamForConfigFile(configFile));
    }

    public static InputStream getInputStreamForConfigFile(String configFile) throws FileNotFoundException {
        return new FileInputStream(getFileForConfigFile(configFile));
    }

    public static File getFileForConfigFile(String configFile) {
        File file = new File(getDaemonEtcDirectory(), configFile);
        assertTrue("configuration file '" + configFile + "' does not exist at " + file.getAbsolutePath(), file.exists());
        return file;
    }

    public static File getDaemonEtcDirectory() {
        String etcPath = 
            "src"+File.separator+
            "main"+File.separator+
            "filtered"+File.separator+
            "etc";
        return new File(getDaemonProjectDirectory(), etcPath);
    }
    
    public static void setRelativeHomeDirectory(String relativeHomeDirectory) {
        setAbsoluteHomeDirectory(new File(getCurrentDirectory().getAbsolutePath(), relativeHomeDirectory).getAbsolutePath());
    }

    public static void setAbsoluteHomeDirectory(final String absoluteHomeDirectory) {
        System.setProperty("opennms.home", absoluteHomeDirectory);
    }

    public static File getTopProjectDirectory() {
        File currentDirectory = getCurrentDirectory();

        File pomFile = new File(currentDirectory, POM_FILE);
        assertTrue("pom.xml in current directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
        
        return findTopProjectDirectory(currentDirectory);
    }

    private static File getCurrentDirectory() {
        File currentDirectory = new File(System.getProperty("user.dir"));
        assertTrue("current directory should exist: " + currentDirectory.getAbsolutePath(), currentDirectory.exists());
        assertTrue("current directory should be a directory: " + currentDirectory.getAbsolutePath(), currentDirectory.isDirectory());
        return currentDirectory;
    }

    public static File getDaemonProjectDirectory() {
        File topLevelDirectory = getTopProjectDirectory();
        File daemonDirectory = new File(topLevelDirectory, DAEMON_DIRECTORY);
        if (!daemonDirectory.exists()) {
            throw new IllegalStateException("Could not find a " + DAEMON_DIRECTORY + " in the location top-level directory: " + topLevelDirectory);
        }
        
        File pomFile = new File(daemonDirectory, POM_FILE);
        assertTrue("pom.xml in " + DAEMON_DIRECTORY + " directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
        
        return daemonDirectory;
    }

    private static File findTopProjectDirectory(File currentDirectory) {
        File buildFile = new File(currentDirectory, "build.sh");
        if (buildFile.exists()) {
            File pomFile = new File(currentDirectory, POM_FILE);
            assertTrue("pom.xml in " + DAEMON_DIRECTORY + " directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
            
            return currentDirectory;
        } else {
            File parentDirectory = currentDirectory.getParentFile();
            
            if (parentDirectory == null || parentDirectory == currentDirectory) {
                return null;
            } else {
                return findTopProjectDirectory(parentDirectory);
            }
        }
    }

    public static void setRrdBinary(String path) {
        System.setProperty("rrd.binary", path);
    }

    public static void setRelativeRrdBaseDirectory(String relativePath) {
        File rrdDir = new File(getCurrentDirectory(), relativePath);
        if (!rrdDir.exists()) {
            rrdDir.mkdirs();
        }
        System.setProperty("rrd.base.dir", rrdDir.getAbsolutePath());
    }

}
