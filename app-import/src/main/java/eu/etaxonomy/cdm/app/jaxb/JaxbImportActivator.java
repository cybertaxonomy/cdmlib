/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.jaxb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportConfigurator;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
public class JaxbImportActivator {

	/* SerializeFrom DB **/
	private static final ICdmDataSource cdmSource = CdmDestinations.localH2Diptera();
	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_jaxb();
	
	// Import:
	private static String importFileName = 
		//"C:\\workspace\\cdmlib_2.2\\cdmlib-io\\src\\test\\resources\\eu\\etaxonomy\\cdm\\io\\jaxb\\export_test_app_import.xml";
		"file:/C:/Dokumente%20und%20Einstellungen/k.luther/Eigene%20Dateien/Neuer%20Ordner/cdmlib/cdmlib-io/target/classes/schema/cdm/export_test_app_import.xml";
	

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

	private void invokeImport(String importFileParam, ICdmDataSource destination) {
		JaxbImportConfigurator jaxbImportConfigurator;
		if (importFileParam !=null && destination != null){
			jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileParam, destination);
		}else if (destination != null){			
		jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileName, destination);
		} else if (importFileParam !=null ){
			jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileParam, cdmDestination);
		} else{
			jaxbImportConfigurator = JaxbImportConfigurator.NewInstance(importFileName, cdmDestination);
		}
		
		CdmDefaultImport<JaxbImportConfigurator> jaxbImport = 
			new CdmDefaultImport<JaxbImportConfigurator>();


		// invoke import
		logger.debug("Invoking Jaxb import");

		jaxbImport.invoke(jaxbImportConfigurator, destination, true);

	}

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.CREATE, true);

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

		JaxbImportActivator sc = new JaxbImportActivator();
		ICdmDataSource source = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmSource;
		String file = chooseFile(args)!= null ? chooseFile(args) : importFileName;
		CdmApplicationController appCtr = null;
		appCtr = sc.initDb(source);
				
		sc.invokeImport(file, source);
	}

}
