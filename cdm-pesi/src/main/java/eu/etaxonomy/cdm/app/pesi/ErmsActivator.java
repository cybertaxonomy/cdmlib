/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.pesi;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsImportConfigurator;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.common.ISourceable;
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
	static final Source ermsSource = PesiSources.PESI3_ERMS();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_pesi_erms();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_erms();
	static final UUID treeUuid = UUID.fromString("6fa988a9-10b7-48b0-a370-2586fbc066eb");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	static final int partitionSize = 5000;

	//ignore null
	static final boolean ignoreNull = true;
	
	static final boolean includeExport = true;
	
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
	
	
	private void doImport(Source source, ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from ("+ ermsSource.getDatabase() + ") ...");
		
		//make ERMS Source
		
		ErmsImportConfigurator config = ErmsImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationUuid(treeUuid);
		
		config.setIgnoreNull(ignoreNull);
		config.setDoReferences(doReferences);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoLinks(doLinks);
		config.setDoOccurrence(doOccurences);
		config.setDbSchemaValidation(hbm2dll);

		config.setCheck(check);
		config.setRecordsPerTransaction(partitionSize);
		config.setSourceRefUuid(PesiTransformer.uuidSourceRefErms);

		// invoke import
		CdmDefaultImport<ErmsImportConfigurator> ermsImport = new CdmDefaultImport<ErmsImportConfigurator>();
		ermsImport.invoke(config);
		
		if (config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			ICdmApplicationConfiguration app = ermsImport.getCdmAppController();
			ISourceable obj = app.getCommonService().getSourcedObjectByIdInSource(ZoologicalName.class, "1000027", null);
			logger.info(obj);
			
//			//make feature tree
//			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, ermsImportConfigurator.getFeatureMap(), featureKeyList);
//			app = ermsImport.getCdmAppController();
//			app.getFeatureTreeService().saveOrUpdate(tree);
		}
		System.out.println("End import from ("+ source.getDatabase() + ")...");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource cdmDB = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		ErmsActivator ermsImport = new ErmsActivator();
		ermsImport.doImport(ermsSource, cdmDB, hbm2dll);
		
		if (includeExport){
			PesiExportActivatorERMS ermsExport = new PesiExportActivatorERMS();
			ermsExport.doExport(cdmDB);
		}
	}

}
