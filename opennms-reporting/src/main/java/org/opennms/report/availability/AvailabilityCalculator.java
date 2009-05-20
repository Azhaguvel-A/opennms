//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.report.availability;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Category;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.AvailabilityReportLocator;

/**
 * AvailabilityCalculator is a partial refectoring of AvailabilityReport that
 * removes the responsibility for rendering the report. This can now be found
 * in the ReportRenderer implementations HTMLReportRenderer and
 * PDFReportRenderer (for batch-mode report rendering) and in the availability
 * report viewer service and its associated views. Depending on the type of
 * reportStore configured, Availability Calulator will marshall the
 * availability report to either a predefined file on disk, or a file on disk
 * with attendant report locator table entry. This table entry can be used
 * later to retrive the ready run report. The castor generated object created needs a string
 * representation for the month in the year. This is unnecessarily complex for
 * the information that it conveys and should be changed.
 * 
 * TODO: This is still not locale independent. 
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */

public class AvailabilityCalculator {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String XML_FORMAT = "xml";

    // String of Months

    public static String[] months = new String[] { "January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };

    // calendar

    private Calendar m_calendar;

    // start date

    private Date m_periodEndDate;

    // format for report (calendar or classic)

    private String m_monthFormat;

    // eventual output format

    private String m_reportFormat;

    // URL for logo

    private String m_logoURL;
    
    // output base dir
    
    private String m_baseDir;

    // output file name

    private String m_outputFileName;

    // author

    private String m_author;

    // category name

    private String m_categoryName;

    /**
     * Castor object that holds all the information required for the
     * generating xml to be translated to the pdf.
     */

    private Report m_report = null;

    private Category log;
    
    private AvailabilityReportLocatorService m_locatorService;

    private AvailabilityData m_availabilityData;

    public AvailabilityCalculator() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
        
        m_report = new Report();
        m_report.setAuthor(m_author);

        Calendar today = new GregorianCalendar();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int year = today.get(Calendar.YEAR);
        String month = months[today.get(Calendar.MONTH)];
        int hour = today.get(Calendar.HOUR);
        int minute = today.get(Calendar.MINUTE);
        int second = today.get(Calendar.SECOND);
        Created created = new Created();
        created.setDay(day);
        created.setHour(hour);
        created.setMin(minute);
        created.setMonth(month);
        created.setSec(second);
        created.setYear(year);
        created.setContent(new BigDecimal(today.getTime().getTime()));
        m_report.setCreated(created);

    }

    public void calculate() throws AvailabilityCalculationException {

        log.debug("Calculation Started");
        log.debug("periodEndDate: " + m_periodEndDate);

        m_report.setLogo(m_logoURL);
        ViewInfo viewInfo = new ViewInfo();
        m_report.setViewInfo(viewInfo);
        org.opennms.report.availability.Categories categories = new org.opennms.report.availability.Categories();
        m_report.setCategories(categories);
        try {
            log.debug("Populating datastructures and calculating availability");
            log.debug("category:     " + m_categoryName);
            log.debug("monthFormat:  " + m_monthFormat);
            log.debug("reportFormat: " + m_reportFormat);
            /* We just initialize this to make sure there are no exceptions, I guess?
             * AvailabilityData availData =
             */
            
            m_availabilityData.fillReport(m_categoryName,
                                                              m_report,
                                                              m_reportFormat,
                                                              m_monthFormat,
                                                              m_periodEndDate);
        } catch (MarshalException me) {
            log.fatal("MarshalException ", me);
            throw new AvailabilityCalculationException(me);
        } catch (ValidationException ve) {
            log.fatal("Validation Exception ", ve);
            throw new AvailabilityCalculationException(ve);
        } catch (IOException ioe) {
            log.fatal("Validation Exception ", ioe);
            throw new AvailabilityCalculationException(ioe);
        } catch (Exception e) {
            log.fatal("Exception ", e);
            throw new AvailabilityCalculationException(e);
        }

    }

   public void writeXML() throws AvailabilityCalculationException {
        try {
            log.debug("Writing the XML");
            // Create a file name of type Category-monthFormat-startDate.xml
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            String catFileName = m_categoryName.replace(' ', '-');
            m_outputFileName = catFileName + "-" + m_monthFormat
                    + fmt.format(m_periodEndDate) + ".xml";
            log.debug("Report Store XML file: " + m_outputFileName);
            File reportFile = new File(m_baseDir, m_outputFileName);
            // marshal the XML into the file.
            marshal(reportFile);
            
        } catch (AvailabilityCalculationException e) {
            log.fatal("Unable to marshal report as XML");
            throw new AvailabilityCalculationException(e);
        }
    }
   
   public void writeXML(String outputFileName) throws AvailabilityCalculationException {
       try {
           log.debug("Writing the XML");
           m_outputFileName = outputFileName;
           // Create a file name of type Category-monthFormat-startDate.xml
           log.debug("Report Store XML file: " + m_outputFileName);
           File reportFile = new File(m_baseDir, m_outputFileName);
           // marshal the XML into the file.
           marshal(reportFile);
       } catch (AvailabilityCalculationException e) {
           log.fatal("Unable to marshal report as XML");
           throw new AvailabilityCalculationException(e);
       }
   }
    
    public void writeLocateableXML() throws AvailabilityCalculationException {
        try {
            log.debug("Writing the XML");
            // Create a file name of type Category-monthFormat-startDate.xml
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            String catFileName = m_categoryName.replace(' ', '-');
            m_outputFileName = catFileName + "-" + m_monthFormat
                    + fmt.format(m_periodEndDate) + ".xml";
            log.debug("Report Store XML file: " + m_outputFileName);
            File reportFile = new File(m_baseDir, m_outputFileName);
            // marshal the XML into the file.
            marshal(reportFile);
            // save a locator
            AvailabilityReportLocator locator = new AvailabilityReportLocator();
            locator.setCategory(m_categoryName);
            locator.setDate(m_periodEndDate);
            locator.setType(m_monthFormat);
            locator.setFormat(XML_FORMAT);
            locator.setLocation(reportFile.getAbsolutePath());
            locator.setAvailable(true);
            m_locatorService.addReport(locator);

        } catch (AvailabilityCalculationException e) {
            log.fatal("Unable to marshal report as XML");
            throw new AvailabilityCalculationException(e);
        }
    }

    public void marshal(File outputFile)
            throws AvailabilityCalculationException {
        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            Marshaller marshaller = new Marshaller(fileWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(m_report);
            log.debug("The xml marshalled from the castor classes is saved in "
                    + outputFile.getAbsoluteFile());
            fileWriter.close();
        } catch (MarshalException me) {
            log.fatal("MarshalException ", me);
            throw new AvailabilityCalculationException(me);
        } catch (ValidationException ve) {
            log.fatal("Validation Exception ", ve);
            throw new AvailabilityCalculationException(ve);
        } catch (IOException ioe) {
            log.fatal("IO Exception ", ioe);
            throw new AvailabilityCalculationException(ioe);
        }
    }

    public String getLogoURL() {
        return m_logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.m_logoURL = logoURL;
    }

    public String getOutputFileName() {
        return m_outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.m_outputFileName = outputFileName;
    }

    public String getAuthor() {
        return m_author;
    }

    public void setAuthor(String author) {
        this.m_author = author;
    }

    public String getCategoryName() {
        return m_categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.m_categoryName = categoryName;
    }

    public String getMonthFormat() {
        return m_monthFormat;
    }

    public void setMonthFormat(String monthFormat) {
        this.m_monthFormat = monthFormat;
    }

    public String getReportFormat() {
        return m_reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.m_reportFormat = reportFormat;
    }

    public Report getReport() {
        return m_report;
    }

    public void setCalendar(Calendar calendar) {
        this.m_calendar = calendar;
    }

    public Date getPeriodEndDate() {
        return m_periodEndDate;
    }

    public void setPeriodEndDate(Date periodEndDate) {
        this.m_periodEndDate = periodEndDate;
    }

    public AvailabilityReportLocatorService getLocatorService() {
        return m_locatorService;
    }

    public void setLocatorService(AvailabilityReportLocatorService locatorService) {
        m_locatorService = locatorService;
    }

    public String getBaseDir() {
        return m_baseDir;
    }

    public void setBaseDir(String baseDir) {
        m_baseDir = baseDir;
    }

    public void setAvailabilityData(AvailabilityData availabilityData) {
        m_availabilityData = availabilityData;
    }

}
