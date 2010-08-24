/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.eflora;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.CdmImportSources;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist.CentralAfricaChecklistImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.floraMalesiana.FloraMalesianaTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class CentralAfricaChecklistActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CentralAfricaChecklistActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source mySource = CdmImportSources.AFRICA_CHECKLIST_ACCESS();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("ebe558b5-d04d-41d5-83d9-b61c56e6e34a");
	
	public static final String sourceReference = "Flora of Central Africa";
	
	//classification
	static final UUID classificationUuid = UUID.fromString("ce1d035a-79a9-4a3a-95bf-26641ecb4fbe");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
//	static boolean doPrintKeys = false;
	
	//taxa
	static final boolean doTaxa = true;

	private void doImport(ICdmDataSource cdmDestination){
		
		//make Source
		Source source = mySource;
		
		CentralAfricaChecklistImportConfigurator config= CentralAfricaChecklistImportConfigurator.NewInstance(source, cdmDestination);
		config.setTaxonomicTreeUuid(classificationUuid);
		config.setDoTaxa(doTaxa);
		config.setCheck(check);
		config.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport<CentralAfricaChecklistImportConfigurator> myImport = new CdmDefaultImport<CentralAfricaChecklistImportConfigurator>();

		System.out.println("Start import from ("+ source.toString() + ") ...");
		config.setSourceReference(getSourceReference(sourceReference));
		myImport.invoke(config);
		System.out.println("End import from ("+ source.toString() + ")...");
		

		
		FeatureTree tree = makeFeatureNode(myImport.getCdmAppController().getTermService());
		myImport.getCdmAppController().getFeatureTreeService().saveOrUpdate(tree);
		
		//check keys
//		if (doPrintKeys){
//			TransactionStatus tx = myImport.getCdmAppController().startTransaction();
//			List<FeatureTree> keys = myImport.getCdmAppController().getFeatureTreeService().list(PolytomousKey.class, null, null, null, null);
//			for(FeatureTree key : keys){
//				((PolytomousKey)key).print(System.out);
//				System.out.println();
//			}
//			myImport.getCdmAppController().commitTransaction(tx);
//		}
		
	}
	
	private ReferenceBase getSourceReference(String string) {
		ReferenceBase result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}

	private FeatureTree makeFeatureNode(ITermService service){
		FloraMalesianaTransformer transformer = new FloraMalesianaTransformer();
		
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		result.setTitleCache("Flora Malesiana Presentation Feature Tree");
		FeatureNode root = result.getRoot();
		FeatureNode newNode;
		
		newNode = FeatureNode.NewInstance(Feature.CITATION());
		root.addChild(newNode);
		
		newNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		root.addChild(newNode);
		
		return result;
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CentralAfricaChecklistActivator me = new CentralAfricaChecklistActivator();
		me.doImport(cdmDestination);
	}
	
}
