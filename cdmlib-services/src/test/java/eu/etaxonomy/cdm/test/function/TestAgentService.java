/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;


import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author nho
 *
 */
public class TestAgentService {

	static CdmApplicationController appController;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance("localhost", "test", -1, "", "", NomenclaturalCode.ICBN);
//		CdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
		appController = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.UPDATE);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		appController.close();
	}
	
	@Test
	public void testGetTeamOrPersonBaseUuidAndNomenclaturalTitle(){
		List<UuidAndTitleCache<TeamOrPersonBase>> result = appController.getAgentService().getTeamOrPersonBaseUuidAndNomenclaturalTitle();
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() > 0);
	}
	
	@Test
	public void testGetPersonUuidAndNomenclaturalTitle(){
		List<UuidAndTitleCache<Person>> result = appController.getAgentService().getPersonUuidAndNomenclaturalTitle();
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() > 0);
	}

}
