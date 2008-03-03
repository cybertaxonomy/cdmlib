/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=false)   //TODO rollback=true does not work yet because of Terms
@Transactional
public class CdmEntityDaoBaseTest {
	private static final Logger logger = Logger.getLogger(CdmEntityDaoBaseTest.class);
	
	private static UUID taxonUUID;
	private static boolean isInitialized;
	
	@Autowired
	private TaxonDaoHibernateImpl cdmEntityDaoBaseTester;

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
		if (! isInitialized){  //as long as rollback does not work
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = new BotanicalName(genus);
			botanicalName.setGenusOrUninomial("GenusName");
			Journal journal = new Journal();
			journal.setTitle("JournalTitel");
			
			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
			UUID uuid = cdmEntityDaoBaseTester.saveOrUpdate(taxon);
			taxonUUID = UUID.fromString(uuid.toString());
			logger.debug("setUpEnd");
			isInitialized = true;
		}
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
	@Test
	public void testCdmEntityDaoBase() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#saveCdmObj(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSaveCdmObj() {
		CdmBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSaveOrUpdate() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		UUID oldUuid = cdmBase.getUuid();
		cdmBase.setUuid(UUID.randomUUID());
		UUID newUuid = cdmEntityDaoBaseTester.saveOrUpdate(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBaseTester.findByUuid(newUuid);
		assertEquals(cdmBase.getId(), cdmBase2.getId());
		
		//oldValue
		cdmBase.setUuid(oldUuid);
		cdmEntityDaoBaseTester.saveOrUpdate(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testSave() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		int oldId = cdmBase.getId();
		
		UUID oldUuid = cdmBase.getUuid();
		UUID newUuid = UUID.randomUUID();
		cdmBase.setUuid(newUuid);
		cdmEntityDaoBaseTester.save(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBaseTester.findByUuid(newUuid);
		logger.warn("semantic unclear");
		//assertFalse(cdmBase.getId() == cdmBase2.getId());
		//oldValue
		cdmBase.setUuid(oldUuid);
		cdmEntityDaoBaseTester.saveOrUpdate(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#update(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testUpdate() {
		logger.warn("testUpdate - Not yet implemented"); // TODO
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#findById(int)}.
	 */
	@Test
	public void testFindById() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		TaxonBase cdmBase2 = cdmEntityDaoBaseTester.findById(cdmBase.getId());
		assertSame(cdmBase, cdmBase2);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#findByUuid(java.util.UUID)}.
	 */
	@Test
	public void testFindByUuid() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#exists(java.util.UUID)}.
	 */
	@Test
	public void testExists() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		boolean exists = cdmEntityDaoBaseTester.exists(cdmBase.getUuid());
		assertTrue(exists);
		boolean existsRandom = cdmEntityDaoBaseTester.exists(UUID.randomUUID());
		assertFalse(existsRandom);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#list(int, int)}.
	 */
	@Test
	public void testList() {
		List<TaxonBase> list = cdmEntityDaoBaseTester.list(1000, 0);
		assertNotNull(list);
		assertTrue(list.size()>0);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public void testDelete() {
		TaxonBase cdmBase = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		assertNotNull(cdmBase);
		cdmEntityDaoBaseTester.delete(cdmBase);
		TaxonBase cdmBase2 = cdmEntityDaoBaseTester.findByUuid(taxonUUID);
		assertNull(cdmBase2);
		//oldValue
//		cdmBase.setId(0);
//		cdmEntityDaoBaseTester.saveOrUpdate(cdmBase);
	}

}
