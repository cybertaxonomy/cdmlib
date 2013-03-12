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
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
public class JaxbImportActivator {

	/* SerializeFrom DB **/
	//private static final ICdmDataSource cdmSource = CdmDestinations.localH2Diptera();
	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_test();
	
	
	// Import:
	private static String importFileNameString = 
		//"C:\\workspace\\cdmlib_2.2\\cdmlib-io\\src\\test\\resources\\eu\\etaxonomy\\cdm\\io\\jaxb\\export_test_app_import.xml";
//		"file:/C:/export_test_app_import.xml";
	"file:/C:/localCopy/Data/krähen/201206141338-jaxb_export-cdm.xml";
	

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;

	private static final Logger logger = Logger.getLogger(JaxbImportActivator.class);

	
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

	private void invokeImport(String importFileParamString, ICdmDataSource destination) {
		try {
			JaxbImportConfigurator jaxbImportConfigurator;
			if (importFileParamString !=null && destination != null){
				URI importFileParam;
				importFileParam = new URI(importFileParamString);
				jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileParam, destination);
			}else if (destination != null){			
				URI importFileName = new URI(importFileNameString);
				jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileName, destination);
			} else if (importFileParamString !=null ){
				URI importFileParam = new URI(importFileParamString);
				jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileParam, cdmDestination);
			} else{
				URI importFileName = new URI(importFileNameString);
				jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileName, cdmDestination);
			}
			
			CdmDefaultImport<JaxbImportConfigurator> jaxbImport = 
				new CdmDefaultImport<JaxbImportConfigurator>();
	
	
			// invoke import
			logger.debug("Invoking Jaxb import");
	
			jaxbImport.invoke(jaxbImportConfigurator, destination, true);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	

	}

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.CREATE, false);

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

		JaxbImportActivator jia = new JaxbImportActivator();
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		String file = chooseFile(args)!= null ? chooseFile(args) : importFileNameString;
		CdmApplicationController appCtr = null;
//		appCtr = jia.initDb(destination);
				
		jia.invokeImport(file, destination);
	}

}
