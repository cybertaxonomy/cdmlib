/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.cichorieae;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.wp6.CommonNameImportConfigurator;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class CichorieaeCommonNameActivator {
	private static final Logger logger = Logger.getLogger(CichorieaeCommonNameActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cichorieae_preview();
	static String source = "C:/localCopy/Data/wp6/cich/Common names-Ueberarbeitung_AM.xls";

// **************** ALL *********************	

	private boolean doInvoke(ICdmDataSource destination){
		boolean success = true;

		URI sourceUri = null;
		File file = new File(source);
		if (! file.exists()){
			logger.warn("File does not exist");
			System.exit(-1);
		}
		sourceUri = file.toURI();
		
		CommonNameImportConfigurator config;
		
		config = CommonNameImportConfigurator.NewInstance(sourceUri, destination);
			
		// invoke import
		CdmDefaultImport<CommonNameImportConfigurator> myImport = new CdmDefaultImport<CommonNameImportConfigurator>();
		success &= myImport.invoke(config);
		String successString = success ? "successful" : " with errors ";
		System.out.println("End updating caches for "+ destination.getDatabase() + "..." +  successString);
		return success;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		System.out.println("Start updating caches for "+ destination.getDatabase() + "...");
		CichorieaeCommonNameActivator me = new CichorieaeCommonNameActivator();
		me.doInvoke(destination);
		
	}

}
