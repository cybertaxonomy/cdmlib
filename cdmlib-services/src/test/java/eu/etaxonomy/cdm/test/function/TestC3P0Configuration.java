// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched my mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 *
 * @author n.hoffmann
 * @created Sep 25, 2009
 * @version 1.0
 */
@Ignore /* IGNORE in Suite */
public class TestC3P0Configuration{
	private static final Logger logger = Logger.getLogger(TestC3P0Configuration.class);

	CdmApplicationController appController;

	@Before
	public void setup() throws DataSourceNotFoundException, TermNotFoundException{
		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance("localhost", "test", -1, "edit", "wp5", NomenclaturalCode.ICNAFP);
		appController = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.CREATE);
	}

	@Test
	public void testLongSession() throws InterruptedException{
		appController.NewConversation();

		appController.getTaxonService().list(null, null, null, null,null);

		Thread.sleep(70 * 1000);

		appController.getTaxonService().list(null, null, null, null,null);
	}
}
