/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;


import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.ILocationService.NamedAreaVocabularyType;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 * @author n.hoffman
 * @created 12.05.2009
 * @version 1.0
 */
@Ignore
public class TestLocationServiceImpl  extends CdmIntegrationTest{
	private static final Logger logger = Logger
			.getLogger(TestLocationServiceImpl.class);

	@SpringBeanByType
	private ILocationService locationService;

	@Ignore
	@Test
	public void testGetTopLevelContinentAreas(){
		locationService.getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType.CONTINENT);
	}

	@Test
	public void testGetTopLevelTdwgAreas(){
		locationService.getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType.TDWG_AREA);
	}

	@Ignore
	@Test
	public void testGetTopLevelCoutryAreas(){
		locationService.getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType.COUNTRY);
	}

	@Ignore
	@Test
	public void testGetTopLevelWaterbodyAreas(){
		locationService.getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType.WATERBODY);
	}

	public void testNewDatasourceClass(){
//			String server = "192.168.2.10";
//			String database = "cdm_test_andreasM";
//			String username = "edit";
//			String password = CdmUtils.readInputLine("Password: ");
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;

//			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance("test", "sa", "", null);
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

		ConversationHolder conversation = appCtr.NewConversation();
		conversation.bind();

		Taxon taxon = Taxon.NewInstance(null, null);

	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }
}
