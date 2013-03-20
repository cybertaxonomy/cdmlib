// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.berlinModelImport;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @date 03.12.2010
 *
 */
public class IldisActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IldisActivator.class);
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.ILDIS();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_ildis_dev();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	

	static final UUID secUuid = UUID.fromString("a7f29364-ce98-4c1d-ad2e-3d889cc5885c");
	static final UUID classificationUuid = UUID.fromString("c7a4e447-ca1e-46e9-adb9-037dab039ccc");
	static final int sourceSecId = 8500000;
	
	static final UUID featureTreeUuid = UUID.fromString("9703afa5-3104-4b3b-b498-f549c0df2d2a");
	static final Object[] featureKeyList = new Integer[]{1, 43, 31, 4, 12, 98, 253, 301, 302, 303}; 	
	
	// set to zero for unlimited nameFacts
	static final int recordsPerTransaction = 2000;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	
	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;
	
// **************** ALL *********************	

	static final boolean doUser = true;
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	static final boolean doTypes = true;
	static final boolean doNameFacts = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	static final boolean doOccurences = true;
	static final boolean doCommonNames = true;

	//etc.
	static final boolean doMarker = true;

	
// **************** SELECTED *********************
//
//	static final boolean doUser = false;
//	//authors
//	static final boolean doAuthors = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	//names
//	static final boolean doTaxonNames = false;
//	static final boolean doRelNames = false;
//	static final boolean doNameStatus = false;
//	static final boolean doTypes = false;
//	static final boolean doNameFacts = false;
//	
//	//taxa 
//	static final boolean doTaxa = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doCommonNames = false;
//	static final boolean doFacts = false;
//	static final boolean doOccurences = false;
//	
//	//etc.
//	static final boolean doMarker = false;
	
	
	private boolean doInvoke(ICdmDataSource destination){
		boolean success = true;
		Source source = berlinModelSource;
				
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		bmImportConfigurator.setClassificationUuid(classificationUuid);
		bmImportConfigurator.setSourceSecId(sourceSecId);
		
		bmImportConfigurator.setNomenclaturalCode(nomenclaturalCode);

		bmImportConfigurator.setIgnoreNull(ignoreNull);
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
		bmImportConfigurator.setDoCommonNames(doCommonNames);
		
		bmImportConfigurator.setDoMarker(doMarker);
		bmImportConfigurator.setDoUser(doUser);
		bmImportConfigurator.setEditor(editor);
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);
		bmImportConfigurator.setRecordsPerTransaction(recordsPerTransaction);
		
		bmImportConfigurator.setCheck(check);
		bmImportConfigurator.setEditor(editor);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		success &= bmImport.invoke(bmImportConfigurator);
		
		if (doFacts && (bmImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || bmImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK) )   ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			
			//make feature tree
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, bmImportConfigurator.getFeatureMap(), featureKeyList);
			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
			tree.getRoot().addChild(imageNode);
			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
			tree.getRoot().addChild(distributionNode, 2); 
			app.getFeatureTreeService().saveOrUpdate(tree);
		}
		
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");

		return success;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + destination.getDatabase() + " ...");
		IldisActivator me = new IldisActivator();
		me.doInvoke(destination);
		
	}
}
