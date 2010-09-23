/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.testUpdate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.eflora.EfloraSources;
import eu.etaxonomy.cdm.common.DefaultProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.eflora.EfloraImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae.CentralAfricaEricaceaeImportConfigurator;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class EricaceaeTestUpdateActivator {
	private static final Logger logger = Logger.getLogger(EricaceaeTestUpdateActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final URI source = EfloraSources.ericacea_local();

	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();
	

	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7");
	
	public static UUID defaultLanguageUuid = Language.uuidEnglish;
	
	//classification
	static final UUID classificationUuid = UUID.fromString("10e5efcc-6e13-4abc-ad42-e0b46e50cbe7");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doPrintKeys = false;
	
	//taxa
	static final boolean doTaxa = false;

	private boolean includeEricaceae = true;


	
	private void doImport(ICdmDataSource cdmDestination){
		
		CdmUpdater updater = new CdmUpdater();
		updater.updateToCurrentVersion(cdmDestination, DefaultProgressMonitor.NewInstance());
		
		//make Source
		CentralAfricaEricaceaeImportConfigurator config= CentralAfricaEricaceaeImportConfigurator.NewInstance(source, cdmDestination);
		config.setTaxonomicTreeUuid(classificationUuid);
		config.setDoTaxa(doTaxa);
		config.setCheck(check);
		config.setDefaultLanguageUuid(defaultLanguageUuid);
		config.setDoPrintKeys(doPrintKeys);
		config.setDbSchemaValidation(hbm2dll);
		
		
		CdmDefaultImport<EfloraImportConfigurator> myImport = new CdmDefaultImport<EfloraImportConfigurator>();
		
		CdmApplicationController app = myImport.getCdmAppController();
		
		
		//
		if (includeEricaceae){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		app = myImport.getCdmAppController();
		
		TransactionStatus tx = app.startTransaction();
		List<FeatureTree> featureTrees = app.getFeatureTreeService().list(null, null, null, null, null);
		for (FeatureTree tree :featureTrees){
			if (tree.getClass().getSimpleName().equalsIgnoreCase("FeatureTree")){
				moveChild(app, tree);
			}
		}
		app.commitTransaction(tx);
		
		
		
	}

	/**
	 * @param app
	 * @param tree
	 */
	private void moveChild(CdmApplicationController app, FeatureTree tree) {
		FeatureNode root = tree.getRoot();
		int count = root.getChildCount();
		FeatureNode lastChild = root.getChildAt(count - 1);
		root.removeChild(lastChild);
		root.addChild(lastChild, 1);
		app.getFeatureTreeService().saveOrUpdate(tree);
	}
	
	private ReferenceBase getSourceReference(String string) {
		ReferenceBase result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EricaceaeTestUpdateActivator me = new EricaceaeTestUpdateActivator();
		me.doImport(cdmDestination);
	}
	
}
