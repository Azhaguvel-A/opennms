package org.opennms.netmgt.provision;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitDNSServerExecutionListener;
import org.opennms.core.test.annotations.DNSEntry;
import org.opennms.core.test.annotations.DNSZone;
import org.opennms.core.test.annotations.JUnitDNSServer;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitDNSServerExecutionListener.class
})
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "importer.adapter.dns.server=127.0.0.1:9153",
        "importer.adapter.dns.privatekey=hmac-md5/test.example.com./QBMBi+8THN8iyAuGIhniB+fiURwQjrrpwFuq1L6NmHcya7QdKqjwp6kLIczPjsAUDcqiLAdQJnQUhCPThA4XtQ=="
})
@JUnitTemporaryDatabase
public class DnsProvisioningAdapterTest {
    @Autowired
    private DnsProvisioningAdapter m_adapter;
    
    @Autowired
    private NodeDao m_nodeDao;

    private AdapterOperation m_addOperation;
    private AdapterOperation m_deleteOperation;
    
    @Before
    @Transactional
    public void setUp() throws Exception {
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("test.example.com").setForeignSource("dns").setForeignId("1");
        nb.addInterface("192.168.0.1");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        // Call afterPropertiesSet() again so that the adapter is 
        // aware of the node that we just added.
        m_adapter.afterPropertiesSet();

        m_addOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("dns", "1").getId(),
            AdapterOperationType.ADD,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
        
        m_deleteOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("dns", "1").getId(),
            AdapterOperationType.DELETE,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
    }

    @Test
    @Transactional
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="test", address="192.168.0.1")
            })
    })
    public void testAdd() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.addNode(n.getId());
        m_adapter.processPendingOperationForNode(m_addOperation);
    }
    
    @Test
    @Transactional
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="test", address="192.168.0.1")
            })
    })
    public void testDelete() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.deleteNode(n.getId());
        m_adapter.processPendingOperationForNode(m_deleteOperation);
    }
}
