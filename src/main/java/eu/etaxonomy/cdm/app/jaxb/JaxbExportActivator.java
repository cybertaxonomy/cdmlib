/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.jaxb;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportConfigurator;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
public class JaxbExportActivator {

	/* SerializeFrom DB **/
	private static final ICdmDataSource cdmSource = CdmDestinations.localH2Diptera();
	
	// Export:
	private static String exportFileName = "C:\\export_test_app_import.xml";

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;

	private static final Logger logger = Logger.getLogger(JaxbImportActivator.class);

	private void invokeExport(ICdmDataSource sourceParam, URI uri) {
		JaxbExportConfigurator jaxbExportConfigurator;
		if (uri !=null && sourceParam != null){
			jaxbExportConfigurator = JaxbExportConfigurator.NewInstance(sourceParam, uri);
		}else if (sourceParam != null){			
			jaxbExportConfigurator = JaxbExportConfigurator.NewInstance(sourceParam, URI.create(exportFileName));
		} else if (uri !=null ){
			jaxbExportConfigurator = JaxbExportConfigurator.NewInstance(cdmSource, uri);
		} else{
			jaxbExportConfigurator = JaxbExportConfigurator.NewInstance(cdmSource, URI.create(exportFileName));
		}
		
		
		CdmDefaultExport<JaxbExportConfigurator> jaxbExport = 
			new CdmDefaultExport<JaxbExportConfigurator>();
		

		// invoke export
		logger.debug("Invoking Jaxb export");
		jaxbExport.invoke(jaxbExportConfigurator);

	}
	public static String chooseFile(String[] args) {
		if(args == null)
			return null;
		for (String dest: args){
			if (dest.endsWith(".xml")){
				return args[0];
			}
		}
		return null;
	}

	

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, false);

		return appCtrInit;
	}

	
	// Load test data to DB
	private void loadTestData(CdmApplicationController appCtrInit) {
		TestDatabase.loadTestData("", appCtrInit);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JaxbExportActivator sc = new JaxbExportActivator();
		ICdmDataSource source = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmSource;
		String file = chooseFile(args);
		URI uri = URI.create(file);
		CdmApplicationController appCtr = null;
		appCtr = sc.initDb(source);
				
		sc.invokeExport(source, uri);
		
	}

}
