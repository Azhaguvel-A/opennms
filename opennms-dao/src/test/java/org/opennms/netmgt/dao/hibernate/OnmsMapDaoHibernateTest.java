package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OnmsMapDaoHibernateTest {
	@Autowired
	private OnmsMapDao m_onmsMapDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
	@Test
	@Transactional
    public void testSaveOnmsMap() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap", "admin");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
    	m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
    	assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap2() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap2", "admin",969,726);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode().trim(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());

    }

	@Test
	@Transactional
    public void testSaveOnmsMap3() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap3", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner().trim());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap4() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap4", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.USER_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap5() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap5", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.AUTOMATICALLY_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.AUTOMATICALLY_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap6() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap6", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.AUTOMATICALLY_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        
        map2.setType(OnmsMap.AUTOMATIC_SAVED_MAP);
        m_onmsMapDao.save(map2);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
       
        OnmsMap map3 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map2, map3);
        assertEquals(map2.getName(), map3.getName());
        assertEquals(map2.getOwner(), map3.getOwner());
        assertEquals(map2.getType(), OnmsMap.AUTOMATIC_SAVED_MAP);
        assertEquals(map2.getAccessMode(), map3.getAccessMode().trim());
        assertEquals(map2.getUserLastModifies(), map3.getUserLastModifies());
        assertEquals(map2.getLastModifiedTime(), map3.getLastModifiedTime());
        assertEquals(map2.getCreateTime(), map3.getCreateTime());
        assertEquals(map2.getWidth(), map3.getWidth());
        assertEquals(map2.getHeight(), map3.getHeight());
    }

	@Test
	@Transactional
    public void testFindById() {
		m_databasePopulator.populateDatabase();
		
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of this object then this ID may change and this test will fail.
        //
        int id = 62;
        OnmsMap map = m_onmsMapDao.findMapById(id);
        if (map == null) {
            List<OnmsMap> maps = m_onmsMapDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (OnmsMap current : maps) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No OnmsMap record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }
        assertNotNull(map);
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByName() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByName("DB_Pop_Test_Map");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByNameAndTypeOk() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.USER_GENERATED_MAP);

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByNameAndTypeKo() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.AUTOMATICALLY_GENERATED_MAP);

        assertEquals(0, maps.size());
    }


	@Test
	@Transactional
    public void testFindMapsLike() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsLike("Pop_Test");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByType() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByType("X");
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindAutoMaps() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findAutoMaps();
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindSaveMaps() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findSaveMaps();
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindUserMaps() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findUserMaps();
        assertEquals(1, maps.size());
    }

	@Test
	@Transactional
    public void testDeleteOnmsMap() {
		m_databasePopulator.populateDatabase();
		
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of this object then this ID may change and this test will fail.
        //
        int id = 62;
        OnmsMap map = m_onmsMapDao.findMapById(id);
        if (map == null) {
            List<OnmsMap> maps = m_onmsMapDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (OnmsMap current : maps) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No OnmsMap record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }

        assertNotNull(map);
        m_onmsMapDao.delete(map);

        assertNull(m_onmsMapDao.findMapById(61));
    }

	@Test
	@Transactional
    public void testFindMapByOwner() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByOwner("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }
    
	@Test
	@Transactional
    public void testFindMapbyGroup() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByGroup("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());        
    }

	@Test
	@Transactional
    public void testFindMapbyGroup1() {
		m_databasePopulator.populateDatabase();
		
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByGroup("");
        assertEquals(0, maps.size());
    }

    
	@Test
	@Transactional
    public void testFindVisibleMapByGroup() {
		m_databasePopulator.populateDatabase();
		
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
        Collection<OnmsMap> maps = m_onmsMapDao.findVisibleMapsByGroup("testGroup");
        assertEquals(2, maps.size());
    }

	@Test
	@Transactional
    public void testFindVisibleMapByGroup2() {
		m_databasePopulator.populateDatabase();
		
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
        Collection<OnmsMap> maps = m_onmsMapDao.findVisibleMapsByGroup("wrongGroup");
        assertEquals(1, maps.size());
    }

    
}
