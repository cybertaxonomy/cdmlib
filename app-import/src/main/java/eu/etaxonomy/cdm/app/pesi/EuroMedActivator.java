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

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.berlinModelImport.TreeCreator;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
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
public class EuroMedActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EuroMedActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final Source berlinModelSource = BerlinModelSources.euroMed();
	static final Source berlinModelSource = BerlinModelSources.EDIT_CICHORIEAE();
//	static final Source berlinModelSource = BerlinModelSources.EDIT_Diptera();
	
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM();
	static final int sourceSecId = 7000000; //500000
	
	static final UUID taxonomicTreeUuid = UUID.fromString("314a68f9-8449-495a-91c2-92fde8bcf344");
	
	static final UUID featureTreeUuid = UUID.fromString("6a5e1c2b-ec0d-46c8-9c7d-a2059267ffb7");
	static final Object[] featureKeyList = new Integer[]{1, 31, 4, 98, 41}; 	
	
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	static final int partitionSize = 2000;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	
	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;
	
	static boolean useTaxonomicTree = true;


// **************** ALL *********************	
//
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

//	static final boolean doUser = true;
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
//	static final boolean doFacts = false;
//	static final boolean doOccurences = false;
//	
//	//etc.
//	static final boolean doMarker = false;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
				
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		bmImportConfigurator.setTaxonomicTreeUuid(taxonomicTreeUuid);
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
		bmImportConfigurator.setUseTaxonomicTree(useTaxonomicTree);
		
		bmImportConfigurator.setDoTaxa(doTaxa);
		bmImportConfigurator.setDoRelTaxa(doRelTaxa);
		bmImportConfigurator.setDoFacts(doFacts);
		bmImportConfigurator.setDoOccurrence(doOccurences);
		bmImportConfigurator.setDoCommonNames(doCommonNames);
		
		bmImportConfigurator.setDoMarker(doMarker);
		bmImportConfigurator.setDoUser(doUser);
		bmImportConfigurator.setEditor(editor);
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);
		
		// maximum number of name facts to import
		bmImportConfigurator.setMaximumNumberOfNameFacts(maximumNumberOfNameFacts);
		
		bmImportConfigurator.setCheck(check);
		bmImportConfigurator.setEditor(editor);
		bmImportConfigurator.setRecordsPerTransaction(partitionSize);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		bmImport.invoke(bmImportConfigurator);
		
		if (doFacts && bmImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || bmImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			CdmApplicationController app = bmImport.getCdmAppController();
			
			//make feature tree
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, bmImportConfigurator.getFeatureMap(), featureKeyList);
			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
			tree.getRoot().addChild(imageNode);
			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
			tree.getRoot().addChild(distributionNode, 2); 
			app.getFeatureTreeService().saveOrUpdate(tree);
		}
		
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");

	}

}
