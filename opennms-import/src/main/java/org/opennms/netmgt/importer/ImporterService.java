/*
 This file is part of the OpenNMS(R) Application.

 OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
 OpenNMS(R) is a derivative work, containing both original code, included code and modified
 code that was published under the GNU General Public License. Copyrights for modified 
 and included code are below.

 OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

 Modifications:

 2007 Aug 25: Implement SpringServiceDaemon. - dj@opennms.org
 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
 2006 May 11: Added Event parameter support for setting the URL and foreignSource
 2004 Dec 27: Changed SQL_RETRIEVE_INTERFACES to omit interfaces that have been
              marked as deleted.
 2004 Feb 12: Rebuild the package to ip list mapping while a new discoveried interface
              to be scheduled.
 2003 Jan 31: Cleaned up some unused imports.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.                                                            

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
       
 For more information contact: 
      OpenNMS Licensing       <license@opennms.org>
      http://www.opennms.org/
      http://www.opennms.com/

*/

package org.opennms.netmgt.importer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.importer.operations.AbstractSaveOrUpdateOperation;
import org.opennms.netmgt.importer.operations.ImportOperation;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.operations.ImportStatistics;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class ImporterService extends BaseImporter implements SpringServiceDaemon, DisposableBean, EventListener {
	
	public static final String NAME = "ModelImporter";

	private volatile Resource m_importResource;
	private volatile EventIpcManager m_eventManager;
	private volatile ImporterStats m_stats;

            
	public void doImport() {
	    doImport(null);
	}
        
    /**
     * Begins importing from resource specified in model-importer.properties file or
     * in event parameter: url.  Import Resources are managed with a "key" called 
     * "foreignSource" specified in the XML retreived by the resource and can be overridden 
     * as a parameter of an event.
     * @param event
     */
    private void doImport(Event event) {
        Resource resource = null;
        try {
            m_stats = new ImporterStats();
            resource = ((event != null && getEventUrl(event) != null) ? new UrlResource(getEventUrl(event)) : m_importResource); 
            sendImportStarted(resource);
			importModelFromResource(resource, m_stats, event);
            log().info("Finished Importing: "+m_stats);
            sendImportSuccessful(m_stats, resource);
        } catch (IOException e) {
            String msg = "IOException importing "+resource;
			log().error(msg, e);
            sendImportFailed(msg+": "+e.getMessage(), resource);
        } catch (ModelImportException e) {
            String msg = "Error parsing import data from "+resource;
			log().error(msg, e);
            sendImportFailed(msg+": "+e.getMessage(), resource);
        }
    }

    private String getEventUrl(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_URL);
    }
    
    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private void sendImportSuccessful(ImporterStats stats, Resource resource) {
        EventBuilder builder = new EventBuilder(EventConstants.IMPORT_SUCCESSFUL_UEI, NAME);
        builder.addParam(EventConstants.PARM_IMPORT_RESOURCE, resource.toString());
        builder.addParam(EventConstants.PARM_IMPORT_STATS, stats.toString());
		m_eventManager.sendNow(builder.getEvent());
	}
    
	private void sendImportFailed(String msg, Resource resource) {
        EventBuilder builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, NAME);
        builder.addParam(EventConstants.PARM_IMPORT_RESOURCE, resource.toString());
        builder.addParam(EventConstants.PARM_FAILURE_MESSAGE, msg);
		m_eventManager.sendNow(builder.getEvent());
	}

	private void sendImportStarted(Resource resource) {
        EventBuilder builder = new EventBuilder(EventConstants.IMPORT_STARTED_UEI, NAME);
        builder.addParam(EventConstants.PARM_IMPORT_RESOURCE, resource.toString());
		m_eventManager.sendNow(builder.getEvent());
	}

    public void setImportResource(Resource resource) {
        m_importResource = resource;
    }

	public EventIpcManager getEventManager() {
	    return m_eventManager;
	}

	public void setEventManager(EventIpcManager eventManager) {
		m_eventManager = eventManager;
	}

	protected ImportOperationsManager createImportOperationsManager(Map<String, Integer> assetNumbersToNodes, ImportStatistics stats) {
		ImportOperationsManager opsMgr = super.createImportOperationsManager(assetNumbersToNodes, stats);
		opsMgr.setEventMgr(m_eventManager);
		return opsMgr;
	}

	public void afterPropertiesSet() throws Exception {
		m_eventManager.addEventListener(this, EventConstants.RELOAD_IMPORT_UEI);
	}

	public void destroy() throws Exception {
		m_eventManager.removeEventListener(this, EventConstants.RELOAD_IMPORT_UEI);
		
	}

	public String getName() {
		return NAME;
	}

	public void onEvent(Event e) {
	    ThreadCategory.setPrefix(NAME);

		if (!EventConstants.RELOAD_IMPORT_UEI.equals(e.getUei())) {
                    return;
                }
		
		doImport(e);
	}
	
	public class ImporterStats implements ImportStatistics {

		private Duration m_importDuration = new Duration("Importing");
		private Duration m_auditDuration = new Duration("Auditing");
		private Duration m_loadingDuration = new Duration("Loading");
		private Duration m_processingDuration = new Duration("Processing");
		private Duration m_preprocessingDuration = new Duration("Scanning");
		private Duration m_relateDuration = new Duration("Relating");
		private WorkEffort m_preprocessingEffort = new WorkEffort("Scan Effort");
		private WorkEffort m_processingEffort = new WorkEffort("Write Effort");
		private WorkEffort m_eventEffort = new WorkEffort("Event Sending Effort");
		private int m_deleteCount;
		private int m_insertCount;
		private int m_updateCount;
		private int m_eventCount;

		public void beginProcessingOps() {
			m_processingDuration.start();
		}

		public void finishProcessingOps() {
			m_processingDuration.end();
		}

		public void beginPreprocessingOps() {
			m_preprocessingDuration.start();
		}

		public void finishPreprocessingOps() {
			m_preprocessingDuration.end();
		}

		public void beginPreprocessing(ImportOperation oper) {
			if (oper instanceof AbstractSaveOrUpdateOperation) {
				m_preprocessingEffort.begin();
			}
		}

		public void finishPreprocessing(ImportOperation oper) {
			if (oper instanceof AbstractSaveOrUpdateOperation) {
				m_preprocessingEffort.end();
			}
		}

		public void beginPersisting(ImportOperation oper) {
			m_processingEffort.begin();
			
		}

		public void finishPersisting(ImportOperation oper) {
			m_processingEffort.end();
		}

		public void beginSendingEvents(ImportOperation oper, List<Event> events) {
			if (events != null) m_eventCount += events.size();
			m_eventEffort.begin();
		}

		public void finishSendingEvents(ImportOperation oper, List<Event> events) {
			m_eventEffort.end();
		}

		public void beginLoadingResource(Resource resource) {
			m_loadingDuration.setName("Loading Resource: "+resource);
			m_loadingDuration.start();
		}

		public void finishLoadingResource(Resource resource) {
			m_loadingDuration.end();
		}

		public void beginImporting() {
			m_importDuration.start();
		}

		public void finishImporting() {
			m_importDuration.end();
		}

		public void beginAuditNodes() {
			m_auditDuration.start();
		}

		public void finishAuditNodes() {
			m_auditDuration.end();
		}
		
		public void setDeleteCount(int deleteCount) {
			m_deleteCount = deleteCount;
		}

		public void setInsertCount(int insertCount) {
			m_insertCount = insertCount;
		}

		public void setUpdateCount(int updateCount) {
			m_updateCount = updateCount;
		}

		public void beginRelateNodes() {
			m_relateDuration.start();
		}

		public void finishRelateNodes() {
			m_relateDuration.end();
		}
		
		public String toString() {
			StringBuffer stats = new StringBuffer();
			stats.append("Deletes: ").append(m_deleteCount).append(' ');
			stats.append("Updates: ").append(m_updateCount).append(' ');
			stats.append("Inserts: ").append(m_insertCount).append('\n');
			stats.append(m_importDuration).append(' ');
			stats.append(m_loadingDuration).append(' ');
			stats.append(m_auditDuration).append('\n');
			stats.append(m_preprocessingDuration).append(' ');
			stats.append(m_processingDuration).append(' ');
			stats.append(m_relateDuration).append(' ');
			stats.append(m_preprocessingEffort).append(' ');
			stats.append(m_processingEffort).append(' ');
			stats.append(m_eventEffort).append(' ');
			if (m_eventCount > 0) {
				stats.append("Avg ").append((double)m_eventEffort.getTotalTime()/(double)m_eventCount).append(" ms per event");
			}
			
			return stats.toString();
		}

	}
		
	public class Duration {

		private String m_name = null;
		private long m_start = -1L;
		private long m_end = -1L;
		
		public Duration() {
			this(null);
		}

		public Duration(String name) {
			m_name = name;
		}
		
		public void setName(String name) {
			m_name = name;
		}

		public void start() {
			m_start = System.currentTimeMillis();
		}

		public void end() {
			m_end = System.currentTimeMillis();
		}
		
		public long getLength() {
			if (m_start == -1L) return 0L;
			long end = (m_end == -1L ? System.currentTimeMillis() : m_end);
			return end - m_start;
		}

		public String toString() {
			return (m_name == null ? "" : m_name+": ")+(m_start == -1L ? "has not begun": elapsedTime());
		}

		private String elapsedTime() {

			long duration = getLength();

			long hours = duration / 3600000L;
			duration = duration % 3600000L;
			long mins = duration / 60000L;
			duration = duration % 60000L;
			long secs = duration / 1000L;
			long millis = duration % 1000L;

			StringBuffer elapsed = new StringBuffer();
			if (hours > 0)
				elapsed.append(hours).append("h ");
			if (mins > 0)
				elapsed.append(mins).append("m ");
			if (secs > 0)
				elapsed.append(secs).append("s ");
			if (millis > 0)
				elapsed.append(millis).append("ms");

			return elapsed.toString();

		}

	}

	public class WorkEffort {
		
		private String m_name;
		private long m_totalTime;
		private long m_sectionCount;
		private ThreadLocal<Duration> m_pendingSection = new ThreadLocal<Duration>();
		
		public WorkEffort(String name) {
			m_name = name;
		}

		public void begin() {
			Duration pending = new Duration();
			pending.start();
			m_pendingSection.set(pending);
		}

		public void end() {
			Duration pending = m_pendingSection.get();
			m_sectionCount++;
			m_totalTime += pending.getLength();
		}
		
		public long getTotalTime() {
			return m_totalTime;
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("Total ").append(m_name).append(": ");
			buf.append((double)m_totalTime/(double)1000L).append(" thread-seconds ");
			if (m_sectionCount > 0) {
				buf.append("Avg ").append(m_name).append(": ");
				buf.append((double)m_totalTime/(double)m_sectionCount).append(" ms per node");
			}
			return buf.toString();
		}

	}

    public void start() throws Exception {
        // nothing to do -- we're event-driven
    }
}
