package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.test.annotation.IfProfileValue;

public class IcmpDetectorTest {
    
    private IcmpDetector m_icmpDetector;
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccess() throws Exception {
        m_icmpDetector = new IcmpDetector();
        assertTrue(m_icmpDetector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }
    
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFail() throws Exception {
        m_icmpDetector = new IcmpDetector();
        assertFalse(m_icmpDetector.isServiceDetected(InetAddress.getByName("0.0.0.0"), new NullDetectorMonitor()));
    }
}
