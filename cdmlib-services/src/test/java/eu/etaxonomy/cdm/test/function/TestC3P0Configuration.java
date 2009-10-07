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
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author n.hoffmann
 * @created Sep 25, 2009
 * @version 1.0
 */
public class TestC3P0Configuration{
	private static final Logger logger = Logger
			.getLogger(TestC3P0Configuration.class);
	
	CdmApplicationController appController;
	
	@Before
	public void setup() throws DataSourceNotFoundException, TermNotFoundException{
		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance("localhost", "test", -1, "", "", NomenclaturalCode.ICBN);
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
