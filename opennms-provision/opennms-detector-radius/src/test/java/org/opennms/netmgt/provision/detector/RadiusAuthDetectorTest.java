package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.radius.RadiusAuthDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class RadiusAuthDetectorTest implements ApplicationContextAware{
    
    @Autowired
    public RadiusAuthDetector m_detector;

    @Before
    public void setUp(){
         MockLogAppender.setupLogging();
    }
    
	@Test
	public void testDetectorFail() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("asdfjlaks;dfjklas;dfj");
	    m_detector.setAuthType("chap");
	    m_detector.setPassword("invalid");
	    m_detector.setSecret("service");
	    m_detector.setUser("1273849127348917234891720348901234789012374");
	    m_detector.onInit();
		assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.100"), new NullDetectorMonitor()));
	}

	@Test
	@Ignore("have to have a radius server set up")
	public void testDetectorPass() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("0");
	    m_detector.setAuthType("mschapv2");
	    m_detector.setPassword("password");
	    m_detector.setSecret("testing123");
	    m_detector.setUser("testing");
	    m_detector.onInit();
		assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.211.11"), new NullDetectorMonitor()));
	}

	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
    }
	
}