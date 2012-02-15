package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class HibernateCriteriaConverterTest {
    @Autowired
    DatabasePopulator m_populator;
    
    @Autowired
    NodeDao m_nodeDao;

    private static boolean m_populated = false;
    
    @Before
    public void setUp() {
    	MockLogAppender.setupLogging(true);
        try {
            if (!m_populated) {
                m_populator.populateDatabase();
            }
        } catch (final Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

	@Test
	public void testNode() {
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
		cb.isNotNull("id");
		
		List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
		assertEquals(6, nodes.size());
		
		cb.eq("label", "node1").join("ipInterfaces", "ipInterface").eq("ipInterface.ipAddress", "192.168.1.1");
		nodes = m_nodeDao.findMatching(cb.toCriteria());
		assertEquals(1, nodes.size());
	}
}
