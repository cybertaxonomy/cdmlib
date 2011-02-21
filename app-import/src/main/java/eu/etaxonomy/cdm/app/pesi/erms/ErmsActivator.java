/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.pesi.erms;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.pesi.FaunaEuropaeaSources;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsImportConfigurator;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.ZoologicalName;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data base under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class ErmsActivator {
	private static final Logger logger = Logger.getLogger(ErmsActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source ermsSource = FaunaEuropaeaSources.PESI_ERMS();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Erms();
	static final UUID treeUuid = UUID.fromString("8bd27d84-fd4f-4bfa-bde0-3e6b7311b334");
	static final UUID featureTreeUuid = UUID.fromString("33cbf7a8-0c47-4d47-bd11-b7d77a38d0f6");
	//static final Object[] featureKeyList = new Integer[]{1,4,5,10,11,12,13,14, 249, 250, 251, 252, 253}; 
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	static final int partitionSize = 2000;


	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICZN;

	//ignore null
	static final boolean ignoreNull = true;
	
// ***************** ALL ************************************************//
	
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doLinks = true;
	static final boolean doOccurences = true;
	static final boolean doImages = true;
	
	
//******************** NONE ***************************************//
	

//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	
//	//taxa
//	static final boolean doTaxa = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doLinks = false;
//	static final boolean doOccurences = false;
//	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from ("+ ermsSource.getDatabase() + ") ...");
		
		//make ERMS Source
		Source source = ermsSource;
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		ErmsImportConfigurator ermsImportConfigurator = ErmsImportConfigurator.NewInstance(source,  destination);
		
		ermsImportConfigurator.setClassificationUuid(treeUuid);
		ermsImportConfigurator.setNomenclaturalCode(nomenclaturalCode);

		ermsImportConfigurator.setIgnoreNull(ignoreNull);
		ermsImportConfigurator.setDoReferences(doReferences);
		
		ermsImportConfigurator.setDoTaxa(doTaxa);
		ermsImportConfigurator.setDoRelTaxa(doRelTaxa);
		ermsImportConfigurator.setDoLinks(doLinks);
		ermsImportConfigurator.setDoOccurrence(doOccurences);
		ermsImportConfigurator.setDbSchemaValidation(hbm2dll);

		ermsImportConfigurator.setCheck(check);
		ermsImportConfigurator.setRecordsPerTransaction(partitionSize);

		// invoke import
		CdmDefaultImport<ErmsImportConfigurator> ermsImport = new CdmDefaultImport<ErmsImportConfigurator>();
		ermsImport.invoke(ermsImportConfigurator);
		
		if (ermsImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || ermsImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			CdmApplicationController app = ermsImport.getCdmAppController();
			ISourceable obj = app.getCommonService().getSourcedObjectByIdInSource(ZoologicalName.class, "1000027", null);
			logger.info(obj);
			
//			//make feature tree
//			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, ermsImportConfigurator.getFeatureMap(), featureKeyList);
//			app = ermsImport.getCdmAppController();
//			app.getFeatureTreeService().saveOrUpdate(tree);
		}
		System.out.println("End import from ("+ source.getDatabase() + ")...");
	}

}
