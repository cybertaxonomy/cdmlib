/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.faunaEuropaea;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.berlinModelImport.TreeCreator;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaImportConfigurator;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 */
public class FaunaEuropaeaActivator {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaActivator.class);

	static final Source faunaEuropaeaSource = FaunaEuropaeaSources.faunEu();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_anahit();
	
//	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.UPDATE;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICZN;

// ****************** ALL *****************************************
	
	static final int limitSave = 2000;
	
	static final boolean doAuthors = true;
	static final boolean doTaxa = true;
	static final boolean doBasionyms = true;
	static final boolean doTaxonomicallyIncluded = true;
	static final boolean doMisappliedNames = true;
	static final boolean doHeterotypicSynonyms = true;
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	static final boolean doDistributions = true;
	static final boolean makeFeatureTree = true;

// ************************ NONE **************************************** //
		
//	static final boolean doAuthors = false;
//	static final boolean doTaxa = false;
//	static final boolean doBasionyms = false;
//	static final boolean doTaxonomicallyIncluded = false;
//	static final boolean doMisappliedNames = false;
//	static final boolean doHeterotypicSynonyms = true;
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	static final boolean doDistributions = false;
//	static final boolean makeFeatureTree = false;
	
	
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

		//make feature tree
		
		if (makeFeatureTree == true) {
			FeatureTree featureTree = FeatureTree.NewInstance(UUID.fromString("ff59b9ad-1fb8-4aa4-a8ba-79d62123d0fb"));
			FeatureNode root = featureTree.getRoot();

			CdmApplicationController app = fauEuImport.getCdmAppController();
			Feature citationFeature = (Feature)app.getTermService().getTermByUuid(UUID.fromString("99b2842f-9aa7-42fa-bd5f-7285311e0101"));
			FeatureNode citationNode = FeatureNode.NewInstance(citationFeature);
			root.addChild(citationNode);
			Feature distributionFeature = (Feature)app.getTermService().getTermByUuid(UUID.fromString("9fc9d10c-ba50-49ee-b174-ce83fc3f80c6"));
			FeatureNode distributionNode = FeatureNode.NewInstance(distributionFeature);
			root.addChild(distributionNode);

			app.getDescriptionService().saveFeatureTree(featureTree);
		}
		
		System.out.println("End import from Fauna Europaea ("+ faunaEuropaeaSource.getDatabase() + ")...");
	}

}
