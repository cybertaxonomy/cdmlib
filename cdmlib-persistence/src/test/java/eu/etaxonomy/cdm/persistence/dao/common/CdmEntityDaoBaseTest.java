/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

/**
 * @author a.mueller
 *
 */
public class CdmEntityDaoBaseTest extends CdmUnitTestBase{
	private static final Logger logger = Logger.getLogger(CdmEntityDaoBaseTest.class);
	
	private static UUID taxonUUID;
	private static boolean isInitialized;
	
	@Autowired
	private TaxonDaoHibernateImpl cdmEntityDaoBase;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.info("setUpBeforeClass");
	}
	

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger.debug("setUp");
		Rank genus = Rank.GENUS();
		BotanicalName botanicalName = new BotanicalName(genus);
		botanicalName.setGenusOrUninomial("GenusName");
		Journal journal = new Journal();
		journal.setTitle("JournalTitel");
		
		Taxon taxon = Taxon.NewInstance(botanicalName, journal);
		UUID uuid = cdmEntityDaoBase.saveOrUpdate(taxon);
		taxonUUID = UUID.fromString(uuid.toString());
		logger.debug("setUpEnd");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
/************ TESTS ********************************/


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#CdmEntityDaoBase(java.lang.Class)}.
	 */
	//@Test
	public void testCdmEntityDaoBase() {
		logger.debug("testCdmEntityDaoBase");
		assertNotNull(cdmEntityDaoBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#saveCdmObj(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSaveCdmObj() {
		logger.debug("testSaveCdmObj");
		CdmBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSaveOrUpdate() {
		logger.debug("testSaveOrUpdate");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		cdmBase.setUuid(UUID.randomUUID());
		UUID newUuid = cdmEntityDaoBase.saveOrUpdate(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBase.findByUuid(newUuid);
		assertEquals(cdmBase.getId(), cdmBase2.getId());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSave() {
		logger.debug("testSave");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		UUID newUuid = UUID.randomUUID();
		cdmBase.setUuid(newUuid);
		cdmEntityDaoBase.save(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBase.findByUuid(newUuid);
		logger.warn("semantic unclear");
		//assertFalse(cdmBase.getId() == cdmBase2.getId());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#update(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testUpdate() {
		logger.debug("testUpdate");
		logger.warn("testUpdate - Not yet implemented"); // TODO
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#findById(int)}.
	 */
	@Test
	public void testFindById() {
		logger.debug("testFindById");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		TaxonBase cdmBase2 = cdmEntityDaoBase.findById(cdmBase.getId());
		assertSame(cdmBase, cdmBase2);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#findByUuid(java.util.UUID)}.
	 */
	@Test
	public void testFindByUuid() {
		logger.debug("testFindByUuid");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#exists(java.util.UUID)}.
	 */
	@Test
	public void testExists() {
		logger.debug("testExists");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		boolean exists = cdmEntityDaoBase.exists(cdmBase.getUuid());
		assertTrue(exists);
		boolean existsRandom = cdmEntityDaoBase.exists(UUID.randomUUID());
		assertFalse(existsRandom);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#list(int, int)}.
	 */
	@Test
	public void testList() {
		logger.debug("testList");
		List<TaxonBase> list = cdmEntityDaoBase.list(1000, 0);
		assertNotNull(list);
		assertTrue(list.size()>0);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testDelete() {
		logger.debug("testDelete");
		TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
		cdmEntityDaoBase.delete(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBase.findByUuid(taxonUUID);
		assertNull(cdmBase2);
	}

}
