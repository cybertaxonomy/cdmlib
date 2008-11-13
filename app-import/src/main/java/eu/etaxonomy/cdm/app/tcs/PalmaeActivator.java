/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class PalmaeActivator {
	private static Logger logger = Logger.getLogger(PalmaeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String tcsSource = TcsSources.arecaceae_local();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_portal_test_localhost();
	
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	static final String sourceSecId = "palm_pub_ed_999999";
	//should the taxonX import run as well?
	static final boolean includeTaxonX = false;
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;

	//static final boolean doTypes = true;
	//static final boolean doNameFacts = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	

	
	private boolean doImport(){
		boolean success = true;
		System.out.println("Start import from Tcs("+ tcsSource.toString() + ") ...");
		
		//make BerlinModel Source
		String source = tcsSource;
		ICdmDataSource destination = cdmDestination;
		
		TcsImportConfigurator tcsImportConfigurator = TcsImportConfigurator.NewInstance(source,  destination);
		
		tcsImportConfigurator.setSecUuid(secUuid);
		tcsImportConfigurator.setSourceSecId(sourceSecId);
		
		tcsImportConfigurator.setDoAuthors(doAuthors);
		tcsImportConfigurator.setDoReferences(doReferences);
		tcsImportConfigurator.setDoTaxonNames(doTaxonNames);
		tcsImportConfigurator.setDoRelNames(doRelNames);
		//tcsImportConfigurator.setDoNameStatus(doNameStatus);
		//tcsImportConfigurator.setDoTypes(doTypes);
		//tcsImportConfigurator.setDoNameFacts(doNameFacts);
		
		tcsImportConfigurator.setDoTaxa(doTaxa);
		tcsImportConfigurator.setDoRelTaxa(doRelTaxa);
		tcsImportConfigurator.setDoFacts(doFacts);
		
		tcsImportConfigurator.setCheck(check);
		tcsImportConfigurator.setDbSchemaValidation(hbm2dll);

		// invoke import
		CdmDefaultImport<TcsImportConfigurator> tcsImport = new CdmDefaultImport<TcsImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		success &= tcsImport.invoke(tcsImportConfigurator);
		System.out.println("End import from TCS ("+ source.toString() + ")...");
		
		return success;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean success = true;
		
		logger.debug("start");
		PalmaeActivator me = new PalmaeActivator();
		me.doImport();
		
		if (includeTaxonX){
			PalmaeTaxonXImportActivator taxonXimporter = new PalmaeTaxonXImportActivator();
			PalmaeTaxonXImportActivator.cdmDestination = cdmDestination;
			PalmaeTaxonXImportActivator.secUuid = secUuid;
			success &= taxonXimporter.runImport();
		}
		
		String strSuccess = "";
		if (success == false){
			strSuccess = "not ";
		}
		System.out.println("Import " + strSuccess + "successful");
		
	}
	
}
