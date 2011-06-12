package org.opennms.util.ilr;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.util.ilr.LogMessage;
import org.opennms.util.ilr.SimpleLogMessage;


public class SimpleLogMessageTest {
	@Test
	public void testGetService() {
		LogMessage log = SimpleLogMessage.create("2010-05-26 12:12:38,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		assertEquals("24/216.216.217.254/SNMP",log.getServiceID());
	}
	//WRITE THIS TEST
	@Test
	public void testGetDate() {
	    
	}
	//WRITE THIS TEST
	@Test
	public void testGetServiceID() {
	    
	}
	//WRITE THIS TEST
	@Test
	public void testGetThread() {
	    
	}
}
