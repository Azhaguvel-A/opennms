package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class IfIndexNullTest {
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:snmpTestData-null.properties")
    public void testNullIfIndex() throws Exception {
        final CountDownLatch eventRecieved = anticipateEvents(EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI );
        
        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/tec_dump.xml"));
        
        List<OnmsNode> nodes = getNodeDao().findAll();
        OnmsNode node = nodes.get(0);
        
        eventRecieved.await();
        
        NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
    }
    
    public void runScan(NodeScan scan) throws InterruptedException, ExecutionException {
        Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }
    
    private CountDownLatch anticipateEvents(String... ueis) {
        final CountDownLatch eventRecieved = new CountDownLatch(1);
        m_eventSubscriber.addEventListener(new EventListener() {

            public void onEvent(Event e) {
                eventRecieved.countDown();
            }

            public String getName() {
                return "Test Initial Setup";
            }
        }, Arrays.asList(ueis));
        return eventRecieved;
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }

    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }
}
