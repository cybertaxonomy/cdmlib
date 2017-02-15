/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.example;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This is an example class to show how to connect to a CDM Database via a
 * {@link CdmApplicationController}. Please don't change this file. Rather
 * copy&paste it to use it for your own purposes.
 *
 * @author a.mueller
 *
 */
public class ApplicationExample {
	//change to VALIDATE to connect to an existing CDM database
	static DbSchemaValidation schemaValidation = DbSchemaValidation.CREATE;


	private void testH2(){

		//H2
		String database = "test";
		String username = "sa";
		ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa");

		ICdmRepository app = CdmApplicationController.NewInstance(dataSource, schemaValidation);
		Taxon taxon = Taxon.NewInstance(null, null);
		app.getTaxonService().save(taxon);
	}

	private void testMySql(){

		//please adapt the following parameters to your local conditions
		String server = "localhost";
		String database = "test";
		String username = "edit";
		//password will be asked for in console and stored in your home directory
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

		ICdmRepository app = CdmApplicationController.NewInstance(dataSource, schemaValidation);
		Taxon taxon = Taxon.NewInstance(null, null);
		app.getTaxonService().save(taxon);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ApplicationExample().testH2();
//		new ApplicationExample().testMySql();
	}

}
