// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.globis;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.CdmImportSources;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.globis.GlobisImportConfigurator;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.ZoologicalName;

/**
 * @author a.mueller
 * @created 14.04.2010
 * @version 1.0
 */
public class GlobisActivator {
	private static final Logger logger = Logger.getLogger(GlobisActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source globisSource = CdmImportSources.GLOBIS_MDB_20120928();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_globis_dev();
	
	
	
	static final UUID classificationUuid = UUID.fromString("8bd27d84-fd4f-4bfa-bde0-3e6b7311b334");
	static final UUID featureTreeUuid = UUID.fromString("33cbf7a8-0c47-4d47-bd11-b7d77a38d0f6");
	//static final Object[] featureKeyList = new Integer[]{1,4,5,10,11,12,13,14, 249, 250, 251, 252, 253};
	
	static final String classificationName = "Globis";
	static final String sourceReferenceTitle = "globis database";
	
	static final String imageBaseUrl = "http://globis-images.insects-online.de/images/";
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	static final int partitionSize = 3000;


	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICZN;

	
	static final boolean doReadMediaData = false;
	
//	//ignore null
	static final boolean ignoreNull = true;
	
// ***************** ALL ************************************************//
	
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	
	//taxa
	static final boolean doCurrentTaxa = true;
	static final boolean doSpecTaxa = true;
	static final boolean doImages = true;
	static final boolean doCommonNames = true;
	
	
//******************** NONE ***************************************//
	

//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	
//	//taxa
//	static final boolean doCurrentTaxa = true;
//	static final boolean doSpecTaxa = false;
//	static final boolean doImages = false;
//	static final boolean doCommonNames = true;

//	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from ("+ globisSource.getDatabase() + ") ...");
		
		//make Globis Source
		Source source = globisSource;
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		GlobisImportConfigurator config = GlobisImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationUuid(classificationUuid);
		config.setNomenclaturalCode(nomenclaturalCode);

		config.setIgnoreNull(ignoreNull);
		config.setDoReadMediaData(doReadMediaData);
		config.setDoReferences(doReferences);
		
		config.setDoCurrentTaxa(doCurrentTaxa);
		config.setDoSpecTaxa(doSpecTaxa);
		config.setDoImages(doImages);
		config.setImageBaseUrl(imageBaseUrl);
		
		config.setDbSchemaValidation(hbm2dll);
		config.setCheck(check);
		config.setRecordsPerTransaction(partitionSize);
		config.setClassificationName(classificationName);
		config.setSourceReferenceTitle(sourceReferenceTitle);

		// invoke import
		CdmDefaultImport<GlobisImportConfigurator> globisImport = new CdmDefaultImport<GlobisImportConfigurator>();
		globisImport.invoke(config);
		
		if (config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			ICdmApplicationConfiguration app = globisImport.getCdmAppController();
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
