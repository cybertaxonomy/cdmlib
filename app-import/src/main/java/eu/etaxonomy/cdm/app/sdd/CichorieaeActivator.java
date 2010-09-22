/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.sdd;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.sdd.in.SDDImportConfigurator;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
public class CichorieaeActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CichorieaeActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;
	
	/********************************************************************************
	 * IMPORTANT:
	 * 
	 * execute the following sql statements before runniung the import
	 * 
	 * ALTER TABLE `statedata_definedtermbase`  DROP INDEX `modifiers_id`;
	 * ALTER TABLE `statisticalmeasurementvalue_definedtermbase`  DROP INDEX `modifiers_id`;
	 * 
	 ********************************************************************************/
	static final ICdmDataSource cdmDestination = CdmDestinations.local_cdm_edit_cichorieae_b();
	static final String sddSource = SDDSources.Cichorieae_DA_export_sdd();
	//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_portal_test_localhost();

	static final String sourceSecId = "cichorieae-crepis-sdd-import";

	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;

	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;

	static final boolean doMatchTaxa = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		logger.info("Start import from SDD ("+ sddSource.toString() + ") ...");

		String source = sddSource;
		ICdmDataSource destination = cdmDestination;

		SDDImportConfigurator sddImportConfigurator = SDDImportConfigurator.NewInstance(source,  destination);

		sddImportConfigurator.setSourceSecId(sourceSecId);

		sddImportConfigurator.setDoAuthors(doAuthors);
		sddImportConfigurator.setDoReferences(doReferences);
		sddImportConfigurator.setDoTaxonNames(doTaxonNames);
		sddImportConfigurator.setDoRelNames(doRelNames);

		sddImportConfigurator.setDoTaxa(doTaxa);
		sddImportConfigurator.setDoRelTaxa(doRelTaxa);
		sddImportConfigurator.setDoFacts(doFacts);


		sddImportConfigurator.setCheck(check);
		sddImportConfigurator.setDbSchemaValidation(hbm2dll);
		
		sddImportConfigurator.setDoMatchTaxa(doMatchTaxa);
		

		// invoke import
		CdmDefaultImport<SDDImportConfigurator> sddImport = new CdmDefaultImport<SDDImportConfigurator>();
		sddImport.invoke(sddImportConfigurator);

		logger.info("End import from SDD ("+ source.toString() + ")...");
	}


}
