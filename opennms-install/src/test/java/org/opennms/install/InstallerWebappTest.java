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
// 2007 Jun 23: Organize imports.
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
//
// $Id$
//

package org.opennms.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.opennms.test.FileAnticipator;

public class InstallerWebappTest extends TestCase {
    private Installer m_installer;

    private FileAnticipator m_anticipator;

    private File m_tomcat;
    
    private File m_tomcat_webapps;

    private File m_tomcat_conf_dir;

    public void setUp() throws IOException, SQLException {
        m_anticipator = new FileAnticipator();
        File dist = m_anticipator.tempDir("dist");
        File dist_webapps = m_anticipator.tempDir(dist, "webapps");
        File opennms_webapp = m_anticipator.tempDir(dist_webapps, "opennms");
        File meta_inf = m_anticipator.tempDir(opennms_webapp, "META-INF");
        m_anticipator.tempFile(meta_inf, "context.xml");

        File web_inf = m_anticipator.tempDir(opennms_webapp, "WEB-INF");
        File lib = m_anticipator.tempDir(web_inf, "lib");
        m_anticipator.tempFile(lib, "log4j.jar");
        m_anticipator.tempFile(lib, "castor-0.9.3.9.jar");
        m_anticipator.tempFile(lib, "castor-0.9.3.9-xml.jar");
        m_anticipator.tempFile(lib, "opennms_core.jar");
        m_anticipator.tempFile(lib, "opennms_services.jar");
        m_anticipator.tempFile(lib, "opennms_web.jar");

        m_tomcat = m_anticipator.tempDir("tomcat");
        m_tomcat_webapps = m_anticipator.tempDir(m_tomcat, "webapps");
        m_tomcat_conf_dir = m_anticipator.tempDir(m_tomcat, "conf");
        File tomcat_server = m_anticipator.tempDir(m_tomcat, "server");
        File tomcat_lib = m_anticipator.tempDir(tomcat_server, "lib");

        m_anticipator.expecting(m_tomcat_webapps, "opennms.xml");
        m_anticipator.expecting(tomcat_lib, "log4j.jar");
        m_anticipator.expecting(tomcat_lib, "castor-0.9.3.9.jar");
        m_anticipator.expecting(tomcat_lib, "castor-0.9.3.9-xml.jar");
        m_anticipator.expecting(tomcat_lib, "opennms_core.jar");
        m_anticipator.expecting(tomcat_lib, "opennms_services.jar");
        m_anticipator.expecting(tomcat_lib, "opennms_web.jar");

        m_installer = new Installer();
        m_installer.m_install_servletdir = opennms_webapp.getAbsolutePath();
        m_installer.m_webappdir = m_tomcat_webapps.getAbsolutePath();
    }

    public void tearDown() throws Exception {
        m_anticipator.tearDown();
    }

    // Grrr... Java 5 and symlinks....
    public void XXXtestWebappInstall() throws Exception {
        m_installer.installWebApp();
        m_anticipator.deleteExpected();
    }

    public void testWebappExistingOpennms() throws Exception {
        m_anticipator.tempDir(m_tomcat_webapps, "opennms");

        final String expecting = "Old OpenNMS web application exists";

        try {
            m_installer.checkWebappOldOpennmsDir();
        } catch (Throwable e) {
            if (!e.getMessage().startsWith(expecting)) {
                fail("Unexpected exception received while waiting for \""
                        + expecting + "\": " + e);
            }
            return;
        }
        fail("Did not receive expected exception: \"" + expecting + "\"");
    }

    public void testWebappNoOpennms() throws Exception {
        m_installer.checkWebappOldOpennmsDir();
    }

    public void testServerXmlContext() throws Exception {
        final String expecting = "Old OpenNMS context found";
        final String context = "		<Context \n"
                + "            path=\"/opennms\" docBase=\"opennms\" debug=\"0\" reloadable=\"true\">\n"
                + "            <Logger className=\"org.opennms.web.log.Log4JLogger\"\n"
                + "                    homeDir=\"${OPENNMS_HOME}\"/>\n"
                + "            <Realm className=\"org.opennms.web.authenticate.OpenNMSTomcatRealm\"\n"
                + "                    homeDir=\"${OPENNMS_HOME}\"/>\n"
                + "        </Context>\n";

        File f = m_anticipator.tempFile(m_tomcat_conf_dir, "server.xml");

        PrintWriter w = new PrintWriter(new FileOutputStream(f));

        w.print(context);
        w.close();

        try {
            m_installer.checkServerXmlOldOpennmsContext();
        } catch (Throwable e) {
            if (!e.getMessage().startsWith(expecting)) {
                fail("Unexpected exception received while waiting for \""
                        + expecting + "\": " + e);
            }
            return;
        }
        fail("Did not receive expected exception: \"" + expecting + "\"");
    }

    public void testServerXmlNoContext() throws Exception {
        m_installer.checkServerXmlOldOpennmsContext();
    }

    public void testServerXmlNoFile() throws Exception {
        m_installer.checkServerXmlOldOpennmsContext();
    }
    
    public void testServerVersion41() throws IOException {
        String readme = 
            "$Id$\n"
            + "\n"
            + "                   The Tomcat 4.1 Servlet/JSP Container\n"
            + "                   ====================================\n";
        String running =
            "$Id$\n"
            + "\n"
            + "\n"
            + "               Running The Tomcat 4.0 Servlet/JSP Container\n"
            + "               ============================================\n";

        testServerVersion(readme, running, "4.1");
    }
    
    public void testServerVersion5() throws IOException {
        String running =
            "$Id$\n"
            + "\n"
            + "\n"
            + "                 Running The Tomcat 5 Servlet/JSP Container\n"
            + "                 ==========================================\n";

        testServerVersion(null, running, "5");
    }
    
    public void testServerVersion55() throws IOException {
        String running = 
            "$Id$\n"
            + "\n"
            + "                 ============================================\n"
            + "                 Running The Tomcat 5.5 Servlet/JSP Container\n"
            + "                 ============================================\n";

        testServerVersion(null, running, "5.5");
    }
    
    private void testServerVersion(String readme, String running, String version)
        throws IOException {
        if (readme != null) {
            m_anticipator.tempFile(m_tomcat, "README.txt", readme);
        }
        if (running != null) {
            m_anticipator.tempFile(m_tomcat, "RUNNING.txt", running);
        }
                               
        assertEquals("Server version", version, m_installer.checkServerVersion());
    }
}
