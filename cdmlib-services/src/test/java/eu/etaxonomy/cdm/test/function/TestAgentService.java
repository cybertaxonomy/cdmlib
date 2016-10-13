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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 * @author nho
 *
 */
@Ignore /* IGNORE in Suite */
public class TestAgentService {

	static CdmApplicationController appController;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance("localhost", "test", -1, "", "");
		CdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("TestAgentService", "sa", "");
		appController = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.UPDATE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		appController.close();
	}

	@Test
	public void testGetTeamOrPersonBaseUuidAndNomenclaturalTitle(){
		List<UuidAndTitleCache<Team>> result = appController.getAgentService().getTeamUuidAndNomenclaturalTitle();

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() > 0);
	}

	@Test
	public void testGetPersonUuidAndNomenclaturalTitle(){
		List<UuidAndTitleCache<Person>> result = appController.getAgentService().getPersonUuidAndTitleCache();

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() > 0);
	}

}
