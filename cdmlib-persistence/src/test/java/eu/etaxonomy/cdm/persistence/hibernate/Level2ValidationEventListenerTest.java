/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class Level2ValidationEventListenerTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

	private UUID uuid;
	private TaxonBase<?> cdmBase;

	@SpringBeanByType
	private ITaxonDao cdmEntityDaoBase;

	@SpringBeanByType
	private AuthenticationManager authenticationManager;

	@SpringBeanByType
	private IUserDao userDao;

	@Before
	public void setUp() throws Exception {
		logger.info("begin setUp()");
		uuid = UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66");
		cdmBase = Taxon.NewInstance(null, null);
		cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));
		logger.info("end setUp()");
	}

	/************ TESTS ********************************/

	@Test
	public void testCdmEntityDaoBase() throws Exception	{
		assertNotNull("cdmEntityDaoBase should exist", cdmEntityDaoBase);
	}

	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testSaveOrUpdate(){
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setDoubtful(true);
		cdmEntityDaoBase.saveOrUpdate(cdmBase);
		commit();
	}

	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testSave() throws Exception{
		cdmEntityDaoBase.save(cdmBase);
		commit();
	}

	//@Test
	//@DataSet
	//@ExpectedDataSet
	public void testUpdate(){
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		cdmBase.setDoubtful(true);
		cdmEntityDaoBase.update(cdmBase);
		commit();
	}

	//@Test
	//@DataSet("CdmEntityDaoBaseTest.xml")
	//@ExpectedDataSet
	public void testDelete(){
		TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
		assertNotNull(cdmBase);
		cdmEntityDaoBase.delete(cdmBase);
	}

	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}