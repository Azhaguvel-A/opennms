package org.opennms.gwt.web.ui.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.gwt.web.ui.asset.server.AssetServiceImpl;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.SecurityContextService;
import org.opennms.web.svclayer.support.SpringSecurityContextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ OpenNMSConfigurationExecutionListener.class,
		TemporaryDatabaseExecutionListener.class,
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath*:/META-INF/opennms/component-dao.xml" })
@JUnitTemporaryDatabase()
public class AssetServiceImplTest {

	@Autowired
	private DistPollerDao m_distPollerDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AssetRecordDao m_assetRecordDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

	private SecurityContextService m_securityContextService;

	private final GrantedAuthority ROLE_ADMIN = new GrantedAuthorityImpl(
			"ROLE_ADMIN");
	private final GrantedAuthority ROLE_PROVISION = new GrantedAuthorityImpl(
			"ROLE_PROVISION");
	private final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl(
			"ROLE_USER");

	private final String USERNAME = "opennms";

	private final String PASS = "r0c|<Z";
	
	private User validAdmin;
	
	private User invalidAdmin;
	
	private User validProvision;
	
	private User invalidProvision;
	
	private User validUser;
	
	private User invalidUser;
	
	private User validPower;
	
	private User invalidPower;
	
	private Authentication m_auth;
	
	private SecurityContext m_context;

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
		this.m_context = new SecurityContextImpl();
		
		this.validAdmin = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN });
		this.invalidAdmin = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN });
		
		this.validProvision = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_PROVISION });
		this.invalidProvision = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_PROVISION });
		
		this.validUser = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER });
		this.invalidUser = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER });

		this.validPower = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN, ROLE_PROVISION });
		this.invalidPower = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER, ROLE_PROVISION });

		this.m_auth = new PreAuthenticatedAuthenticationToken(
				this.validAdmin, new Object());
		this.m_context.setAuthentication(this.m_auth);
		SecurityContextHolder.setContext(this.m_context);
		this.m_securityContextService = new SpringSecurityContextService();
	}

	@Test
	public void testCreateAndGets() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 7");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		// Test findAll method
		Collection<OnmsAssetRecord> assetRecords = m_assetRecordDao.findAll();
		assertEquals(7, assetRecords.size());

		// Test countAll method
		assertEquals(7, m_assetRecordDao.countAll());
	}

	@Test
	public void testAssetServiceImpl() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 7");
		assetRecord.setAdmin("supermario");
		assetRecord.setZip("myzip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode2");
		m_nodeDao.save(onmsNode);
		assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 23");
		assetRecord.setAdmin("mediummario");
		assetRecord.setZip("yourzip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(this.m_nodeDao);
		assetServiceImpl.setAssetRecordDao(this.m_assetRecordDao);

		System.out.println("AssetCommand: "
				+ assetServiceImpl.getAssetByNodeId(7).toString());
		System.out.println("Suggestions: "
				+ assetServiceImpl.getAssetSuggestions());
		assertTrue("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(7).getAllowModify());
	}

//	@Test
//	public void successAllowModifyAssetByAdmin() {
//		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
//		assetServiceImpl.setNodeDao(this.m_nodeDao);
//		assetServiceImpl.setAssetRecordDao(this.m_assetRecordDao);
//		this.m_auth = new PreAuthenticatedAuthenticationToken(
//				this.validAdmin, new Object());
//		this.m_context.setAuthentication(this.m_auth);
//		SecurityContextHolder.setContext(this.m_context);
//		this.m_securityContextService = new SpringSecurityContextService();
//		assertTrue("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(7).getAllowModify());
//	}
//
//	@Test
//	public void failAllowModifyAssetByAdmin() {
//		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
//		assetServiceImpl.setNodeDao(this.m_nodeDao);
//		assetServiceImpl.setAssetRecordDao(this.m_assetRecordDao);
//		this.m_auth = new PreAuthenticatedAuthenticationToken(
//				this.invalidAdmin, new Object());
//		this.m_context.setAuthentication(this.m_auth);
//		SecurityContextHolder.setContext(this.m_context);
//		this.m_securityContextService = new SpringSecurityContextService();
//		assertFalse("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(7).getAllowModify());
//	}
	
	
	@Test
	public void testSaveOrUpdate() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 7");
		assetRecord.setAdmin("supermario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.setZip("myzip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetCommand assetCommand = new AssetCommand();
		BeanUtils.copyProperties(assetRecord, assetCommand);

		System.out.println("AssetCommand (Source): " + assetCommand);
		System.out.println("Asset to Save (Target): " + assetRecord);

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(this.m_nodeDao);
		assetServiceImpl.setAssetRecordDao(this.m_assetRecordDao);
		System.out.println();
		assertTrue(assetServiceImpl.saveOrUpdateAssetByNodeId(7, assetCommand));
	}

	@Test
	public void testAssetSuggestion() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("your Node");
		onmsNode.setSysObjectId("mySysOid");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 666");
		assetRecord.setAdmin("medium mario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.setZip("his zip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("his Node");
		m_nodeDao.save(onmsNode);
		assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 999");
		assetRecord.setAdmin("super mario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.setZip("your zip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(this.m_nodeDao);
		assetServiceImpl.setAssetRecordDao(this.m_assetRecordDao);
		System.out.println("Asset: " + assetServiceImpl.getAssetByNodeId(7));
	}
}
