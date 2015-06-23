package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class Level2ValidationEventListenerTest extends CdmTransactionalIntegrationTest {

	protected static final Logger logger = Logger.getLogger(Level2ValidationEventListenerTest.class);

	private UUID uuid;
	private TaxonBase<?> cdmBase;

	@SpringBeanByType
	private ITaxonDao cdmEntityDaoBase;

	@SpringBeanByType
	private AuthenticationManager authenticationManager;

	@SpringBeanByType
	private IUserDao userDao;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		logger.info("begin setUp()");
		uuid = UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66");
		cdmBase = Taxon.NewInstance(null, null);
		cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));
		logger.info("end setUp()");
	}


	/************ TESTS ********************************/

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#CdmEntityDaoBase(java.lang.Class)}
	 * .
	 *
	 * @throws Exception
	 */
	@Test
	public void testCdmEntityDaoBase() throws Exception
	{
		assertNotNull("cdmEntityDaoBase should exist", cdmEntityDaoBase);
	}


	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}
	 * .
	 */
	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testSaveOrUpdate()
	{
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setDoubtful(true);
		cdmEntityDaoBase.saveOrUpdate(cdmBase);
		commit();
	}


	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}
	 * .
	 */
	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testSave() throws Exception
	{
		cdmEntityDaoBase.save(cdmBase);
		commit();
	}


	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#update(eu.etaxonomy.cdm.model.common.CdmBase)}
	 * .
	 */
	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testUpdate()
	{
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setDoubtful(true);
		cdmEntityDaoBase.update(cdmBase);
		commit();
	}


	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}
	 * .
	 */
	//@Test
	//@DataSet("CdmEntityDaoBaseTest.xml")
	//@ExpectedDataSet
	public void testDelete()
	{
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		assertNotNull(cdmBase);
		cdmEntityDaoBase.delete(cdmBase);
	}


	@Override
	public void createTestDataSet() throws FileNotFoundException {
		// TODO Auto-generated method stub
	}

}
