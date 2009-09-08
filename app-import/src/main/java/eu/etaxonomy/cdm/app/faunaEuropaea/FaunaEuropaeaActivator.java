/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.faunaEuropaea;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 */
public class FaunaEuropaeaActivator {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaActivator.class);

	static final Source faunaEuropaeaSource = FaunaEuropaeaSources.faunEu();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_anahit();
	
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.UPDATE;
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICZN;

// ****************** ALL *****************************************
	
	static final int limitSave = 1000;
	
	static final boolean doAuthors = true;
	static final boolean doTaxa = true;
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	static final boolean doDistributions = true;
	
	static final boolean doBasionyms = false;
	static final boolean doTaxonomicallyIncluded = true;
	static final boolean doMisappliedNames = true;
	static final boolean doHeterotypicSynonyms = true;

// ************************ NONE **************************************** //
		
//	static final boolean doAuthors = false;
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	static final boolean doTaxa = false;
//	static final boolean doSynonyms = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doDistributions = false;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from Fauna Europaea ("+ faunaEuropaeaSource.getDatabase() + ") ...");
		
		ICdmDataSource destination = cdmDestination;
		
		FaunaEuropaeaImportConfigurator fauEuImportConfigurator = 
			FaunaEuropaeaImportConfigurator.NewInstance(faunaEuropaeaSource,  destination);
		
		fauEuImportConfigurator.setDbSchemaValidation(dbSchemaValidation);
		fauEuImportConfigurator.setNomenclaturalCode(nomenclaturalCode);
		fauEuImportConfigurator.setCheck(check);

		fauEuImportConfigurator.setDoAuthors(doAuthors);
		fauEuImportConfigurator.setDoTaxa(doTaxa);
		fauEuImportConfigurator.setDoReferences(doReferences);
		fauEuImportConfigurator.setDoOccurrence(doDistributions);
		fauEuImportConfigurator.setDoTaxonomicallyIncluded(doTaxonomicallyIncluded);
		fauEuImportConfigurator.setDoBasionyms(doBasionyms);
		fauEuImportConfigurator.setDoMisappliedNames(doMisappliedNames);
		fauEuImportConfigurator.setDoHeterotypicSynonyms(doHeterotypicSynonyms);
		fauEuImportConfigurator.setLimitSave(limitSave);

		// invoke import
		CdmDefaultImport<FaunaEuropaeaImportConfigurator> fauEuImport = 
			new CdmDefaultImport<FaunaEuropaeaImportConfigurator>();
		fauEuImport.invoke(fauEuImportConfigurator);

		
		System.out.println("End import from Fauna Europaea ("+ faunaEuropaeaSource.getDatabase() + ")...");
	}

}
