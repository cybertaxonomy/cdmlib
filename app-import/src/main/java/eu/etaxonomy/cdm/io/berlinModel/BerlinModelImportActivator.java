/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImport;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelSources;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImportConfigurator.*;


/**
 * @author a.mueller
 *
 */
public class BerlinModelImportActivator {
	private static final Logger logger = Logger.getLogger(BerlinModelImportActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	//static final Source berlinModelSource = BerlinModelSources.EDIT_Diptera();
	static final Source berlinModelSource = BerlinModelSources.editWP6();	
	//static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_diptera();
    static final ICdmDataSource cdmDestination = CdmDestinations.cdm_portal_test_localhost();
	
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	static final boolean doTypes = false;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = false;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
		CdmApplicationController cdmApp;
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = cdmDestination;
		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		bmImportConfigurator.setDoAuthors(doAuthors);
		bmImportConfigurator.setDoReferences(doReferences);
		bmImportConfigurator.setDoTaxonNames(doTaxonNames);
		bmImportConfigurator.setDoRelNames(doRelNames);
		bmImportConfigurator.setDoNameStatus(doNameStatus);
		bmImportConfigurator.setDoNameStatus(doTypes);
		
		bmImportConfigurator.setDoTaxa(doTaxa);
		bmImportConfigurator.setDoRelTaxa(doRelTaxa);
		bmImportConfigurator.setDoFacts(doFacts);
		bmImportConfigurator.setDbSchemaValidation(DbSchemaValidation.CREATE);
		
		// invoke import
		BerlinModelImport bmImport = new BerlinModelImport();
		bmImport.doCheck(bmImportConfigurator);
		bmImport.doImport(bmImportConfigurator);

		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
	}

}
