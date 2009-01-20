/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class CdmEntityDaoBaseTest extends CdmIntegrationTest {
	
	private UUID uuid;
	private CdmBase cdmBase;
	
	@SpringBeanByType
	private CdmEntityDaoBaseTestClass cdmEntityDaoBase;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {	
		uuid = UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66");
		cdmBase = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);;
		cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));
	}
	
/************ TESTS ********************************/


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#CdmEntityDaoBase(java.lang.Class)}.
	 * @throws Exception 
	 */
	@Test
	public void testCdmEntityDaoBase() throws Exception {
		assertNotNull("cdmEntityDaoBase should exist",cdmEntityDaoBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	@ExpectedDataSet
	public void testSaveOrUpdate() {
		CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setUuid(UUID.fromString("61410dd0-c774-11dd-ad8b-0800200c9a66"));
		cdmEntityDaoBase.saveOrUpdate(cdmBase);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	@ExpectedDataSet
	public void testSave() {
		cdmEntityDaoBase.save(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#update(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	@ExpectedDataSet
	public void testUpdate() {
		CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setUuid(UUID.fromString("65bc7d70-c76c-11dd-ad8b-0800200c9a66"));
		cdmEntityDaoBase.update(cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#findById(int)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	public void testFindById() {		
		CdmBase cdmBase = cdmEntityDaoBase.findById(1);
		assertNotNull("There should be an entity with an id of 1",cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#findByUuid(java.util.UUID)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	public void testFindByUuid() {
		CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		assertNotNull("testFindByUuid() an entity with a uuid of " + uuid.toString(),cdmBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#exists(java.util.UUID)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	public void testExists() {
		boolean exists = cdmEntityDaoBase.exists(uuid);
		assertTrue("exists() should return true for uuid " + uuid.toString(), exists);
		boolean existsRandom = cdmEntityDaoBase.exists(UUID.randomUUID());
		assertFalse("exists() should return false for any other uuid", existsRandom);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#list(int, int)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	public void testList() {
		List<TaxonBase> list = cdmEntityDaoBase.list(1000, 0);
		assertNotNull("list() should not return null",list);
		assertEquals("list() should return a list with two entities in it",list.size(),2);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CdmEntityDaoBaseTest.xml")
	@ExpectedDataSet
	public void testDelete() {
		CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		assertNotNull(cdmBase);
		cdmEntityDaoBase.delete(cdmBase);
	}

}
