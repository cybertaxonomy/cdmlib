/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.io.File;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.wp6.palmae.config.PalmaeProtologueImportConfigurator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class PalmaeProtologueImportActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PalmaeProtologueImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;
	
	static final String protologueSource = "\\\\Media\\EditWP6\\palmae\\protologe";
	private static final String urlString = "http://wp5.e-taxonomy.eu/media/palmae/protologe/";

	
	
	//static File source  = TcsSources.taxonX_localDir();
	static File source = new File ("\\\\media\\EditWP6\\palmae\\protologe");
	static ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();
	
	static UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	
	//check - import
	static CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doProtologues = true;
	
	public boolean runImport(){
		boolean success = true;
		//make destination
		ICdmDataSource destination = cdmDestination;
		
		PalmaeProtologueImportConfigurator protologConfig = PalmaeProtologueImportConfigurator.NewInstance(protologueSource, destination, urlString);
		// invoke import
		CdmDefaultImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
		
		//protologConfig.setDoFacts(doDescriptions);
		protologConfig.setCheck(check);
		protologConfig.setDbSchemaValidation(hbm2dll);

		cdmImport.startController(protologConfig, destination);
		
		protologConfig.setSource(source);
		success &= cdmImport.invoke(protologConfig);
		
		return success;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from Source("+ source.toString() + ") ...");
		PalmaeProtologueImportActivator importer = new PalmaeProtologueImportActivator();
		importer.runImport();
		System.out.println("End import from Source ("+ source.toString() + ")...");
	}

	
}
