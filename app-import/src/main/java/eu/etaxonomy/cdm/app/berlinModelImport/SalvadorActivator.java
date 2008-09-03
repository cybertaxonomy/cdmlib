/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.berlinModelImport;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class SalvadorActivator {
	private static final Logger logger = Logger.getLogger(SalvadorActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.El_Salvador();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_salvador();
	static final UUID secUuid = UUID.fromString("d03ef02a-f226-4cb1-bdb4-f6c154f08a34");
	static final int sourceSecId = 7331;
	
	static final UUID featureTreeUuid = UUID.fromString("ae9615b8-bc60-4ed0-ad96-897f9226d568");
	static final Object[] featureKeyList = new Integer[]{5,10,11,12}; 
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;


	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN();

//	//authors
//	static final boolean doAuthors = true;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
//	//names
//	static final boolean doTaxonNames = true;
//	static final boolean doRelNames = true;
//	static final boolean doNameStatus = true;
//	static final boolean doTypes = false;  //Types do not exist in El_Salvador DB
//	static final boolean doNameFacts = false;  //Name Facts do not exist in El_Salvador DB
//	
//	//taxa
//	static final boolean doTaxa = true;
//	static final boolean doRelTaxa = true;
//	static final boolean doFacts = true;
//	static final boolean doOccurences = false;
	
	//authors
	static final boolean doAuthors = false;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
	//names
	static final boolean doTaxonNames = false;
	static final boolean doRelNames = false;
	static final boolean doNameStatus = false;
	static final boolean doTypes = false;
	static final boolean doNameFacts = false;
	
	//taxa
	static final boolean doTaxa = false;
	static final boolean doRelTaxa = false;
	static final boolean doFacts = true;
	static final boolean doOccurences = false;
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = cdmDestination;
		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		bmImportConfigurator.setSecUuid(secUuid);
		bmImportConfigurator.setSourceSecId(sourceSecId);
		bmImportConfigurator.setNomenclaturalCode(nomenclaturalCode);

		bmImportConfigurator.setDoAuthors(doAuthors);
		bmImportConfigurator.setDoReferences(doReferences);
		bmImportConfigurator.setDoTaxonNames(doTaxonNames);
		bmImportConfigurator.setDoRelNames(doRelNames);
		bmImportConfigurator.setDoNameStatus(doNameStatus);
		bmImportConfigurator.setDoTypes(doTypes);
		bmImportConfigurator.setDoNameFacts(doNameFacts);
		
		bmImportConfigurator.setDoTaxa(doTaxa);
		bmImportConfigurator.setDoRelTaxa(doRelTaxa);
		bmImportConfigurator.setDoFacts(doFacts);
		bmImportConfigurator.setDoOccurrence(doOccurences);
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);

		bmImportConfigurator.setCheck(check);
		
		// invoke import
		CdmDefaultImport<TcsImportConfigurator> bmImport = new CdmDefaultImport<TcsImportConfigurator>();
		bmImport.invoke(bmImportConfigurator);

		
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
	}

}
