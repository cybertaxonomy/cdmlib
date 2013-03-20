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

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

public class MTStandardlisteActivator {
	

	

	


	/**
	 * TODO add the following to a wiki page:
	 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
	 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
	 * 
	 * 
	 * @author a.mueller
	 *
	 */

		private static final Logger logger = Logger.getLogger(AlgaTerraActivator.class);

		//database validation status (create, update, validate ...)
		static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		static final Source berlinModelSource = BerlinModelSources.MT_Standardliste();
		static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//		static final ICdmDataSource cdmDestination = CdmDestinations.cdm_mt_standardliste();
		
		static final UUID treeUuid = UUID.fromString("70549f1a-3d30-42ae-8257-c8367e2703b0");
		static final int sourceSecId = 7331;
		static final UUID sourceRefUuid = UUID.fromString("33baaf62-f5c4-4260-aacb-090fe4d24206");
		
		static final UUID featureTreeUuid = UUID.fromString("2b592057-de3a-4782-a6f3-90a87e2a004d");
		static final Object[] featureKeyList = new Integer[]{7,201,202,203,204,205,206,207}; 
		
		//check - import
		static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;


		//NomeclaturalCode
		static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	// ****************** ALL *****************************************
		
		//authors
		static final boolean doAuthors = true;
		//references
		static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
		//names
		static final boolean doTaxonNames = true;
		static final boolean doRelNames = true;
		static final boolean doNameStatus = true;
		static final boolean doTypes = true;  
		static final boolean doNameFacts = false;   
		
		//taxa
		static final boolean doTaxa = true;
		static final boolean doRelTaxa = true;
		static final boolean doFacts = true;
		static final boolean doOccurences = false;
		static final boolean doCommonNames = false; 

	// ************************ NONE **************************************** //
		
//		//authors
//		static final boolean doAuthors = false;
//		//references
//		static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//		//names
//		static final boolean doTaxonNames = false;
//		static final boolean doRelNames = false;
//		static final boolean doNameStatus = false;
//		static final boolean doTypes = false;
//		static final boolean doNameFacts = false;
	//	
//		//taxa
//		static final boolean doTaxa = false;
//		static final boolean doRelTaxa = false;
//		static final boolean doFacts = false;
//		static final boolean doOccurences = false;
//		static final boolean doCommonNames = false;
		
		
		public void invoke(String[] args){
			System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
			logger.debug("Start");
			//make BerlinModel Source
			Source source = berlinModelSource;
			ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
			
			BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
			
			bmImportConfigurator.setClassificationUuid(treeUuid);
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
			bmImportConfigurator.setDoCommonNames(doCommonNames);
			bmImportConfigurator.setSourceRefUuid(sourceRefUuid);
			
			bmImportConfigurator.setDbSchemaValidation(hbm2dll);

			bmImportConfigurator.setCheck(check);
			
			// invoke import
			CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
			bmImport.invoke(bmImportConfigurator);

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
		}
		
		
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			MTStandardlisteActivator activator = new MTStandardlisteActivator();
			activator.invoke(args);
		}

	}

