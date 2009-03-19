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

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @created 18.03.2009
 * @version 1.0
 */
public class CacheStrategyGeneratorTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CacheStrategyGeneratorTest.class);

	private UUID uuid;
	private TaxonBase cdmBase;
	
	@SpringBeanByType
	private ITaxonNameDao cdmEntityDaoBase;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		uuid = UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66");
		cdmBase = Taxon.NewInstance(null, null);
		cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));
	}

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
	@DataSet("CacheStrategyGeneratorTest.xml")
	@ExpectedDataSet
	public void testOnSaveOrUpdate() {
		BotanicalName name =  (BotanicalName)cdmEntityDaoBase.findByUuid(UUID.fromString("a49a3963-c4ea-4047-8588-2f8f15352730"));;
		name.setTitleCache(null, false);
		name.setNameCache(null, false);
		name.setGenusOrUninomial("Abies");
		name.setAuthorshipCache("Mill.", true);
		cdmEntityDaoBase.saveOrUpdate(name);
	}

	
}	
	

