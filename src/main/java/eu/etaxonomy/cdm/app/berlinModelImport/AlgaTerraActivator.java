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
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.algaterra.AlgaTerraImportConfigurator;
import eu.etaxonomy.cdm.io.algaterra.AlgaTerraImportTransformer;
import eu.etaxonomy.cdm.io.algaterra.AlgaTerraSpecimenImportBase;
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
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class AlgaTerraActivator {
	private static final Logger logger = Logger.getLogger(AlgaTerraActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.AlgaTerra();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();

//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_algaterra_preview();
	
	
	static final UUID treeUuid = UUID.fromString("1f617402-78dc-4bf1-ac77-d260600a8879");
	static final int sourceSecId = 7331;
	static final UUID sourceRefUuid = UUID.fromString("7e1a2500-93a5-40c2-ba34-0213d7822379");
	
	static final UUID featureTreeUuid = UUID.fromString("a970168a-36fd-4c7c-931e-87214a965c14");
	static final Object[] featureKeyList = new Integer[]{7,201,202,203,204,205,206,207}; 
	static final UUID specimenFeatureTreeUuid = UUID.fromString("ba86246e-d4d0-419f-832e-86d70b1e4bd7");
	
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	private boolean ignoreNull = true;
	
	private boolean includeFlatClassifications = true;
	private boolean includeAllNonMisappliedRelatedClassifications = true;
	
	private EDITOR editor = EDITOR.EDITOR_AS_EDITOR;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;
	
	static String factFilter = " factCategoryFk NOT IN (7, 202, 1000 ) ";
	
	
// ****************** ALL *****************************************
	
//	//authors
//	static final boolean doAuthors = true;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
//	//names
//	static final boolean doTaxonNames = true;
//	static final boolean doRelNames = true;
//	static final boolean doNameStatus = true;
//	static final boolean doTypes = true;  
//	
//	//taxa
//	static final boolean doTaxa = true;
//	static final boolean doRelTaxa = true;
//	static final boolean doFacts = true;
//	
//	//alga terra specific
//	static final boolean ecoFacts = true;
//	static final boolean doFactEcology = true;
//	static final boolean doImages = true;

// ************************ NONE **************************************** //
	
	//authors
	static final boolean doAuthors = false;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = false;
	static final boolean doRelNames = false;
	static final boolean doNameStatus = false;
	static final boolean doTypes = false;
	static final boolean doNameFacts = false;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = false;
	static final boolean doFacts = true;
	
  //alga terra specific
	static final boolean ecoFacts = true;
	static final boolean doFactEcology = false;
	static final boolean doImages = true;
	
	
	public void invoke(String[] args){
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
		logger.debug("Start");
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		AlgaTerraImportConfigurator config = AlgaTerraImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationUuid(treeUuid);
		config.setSourceSecId(sourceSecId);
		config.setNomenclaturalCode(nomenclaturalCode);

		config.setDoAuthors(doAuthors);
		config.setDoReferences(doReferences);
		config.setDoTaxonNames(doTaxonNames);
		config.setDoRelNames(doRelNames);
		config.setDoNameStatus(doNameStatus);
		config.setDoTypes(doTypes);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoFacts(doFacts);
		config.setDoEcoFacts(ecoFacts);
		config.setDoImages(doImages);
		config.setDoFactEcology(doFactEcology);
		
		config.setSourceRefUuid(sourceRefUuid);
		config.setIgnoreNull(ignoreNull);
		
		config.setIncludeFlatClassifications(includeFlatClassifications);
		config.setIncludeAllNonMisappliedRelatedClassifications(includeAllNonMisappliedRelatedClassifications);
		config.setFactFilter(factFilter);
		
		config.setDbSchemaValidation(hbm2dll);

		config.setCheck(check);
		config.setEditor(editor);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		bmImport.invoke(config);

		if (doFacts && (config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK) )   ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			
			//make feature tree
			makeTaxonFeatureTree(config, app);

			//make specimen feature tree
			//TODO more specimen specific
			makeSpecimenFeatureTree(config, app);

		}
		
		
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
	}


	/**
	 * @param config
	 * @param app
	 */
	private void makeTaxonFeatureTree(AlgaTerraImportConfigurator config, ICdmApplicationConfiguration app) {
		FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, config.getFeatureMap(), featureKeyList);
		tree.setTitleCache("AlgaTerra Taxon Feature Tree", true);
		
		FeatureNode node = FeatureNode.NewInstance(Feature.HABITAT());
		tree.getRoot().addChild(node);
		
		node = FeatureNode.NewInstance(Feature.OBSERVATION());
		tree.getRoot().addChild(node);
		
		node = FeatureNode.NewInstance(Feature.SPECIMEN());
		tree.getRoot().addChild(node);
		
		node = FeatureNode.NewInstance(Feature.INDIVIDUALS_ASSOCIATION());
		tree.getRoot().addChild(node);
		
		//needed ??
		FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		tree.getRoot().addChild(distributionNode, 2);
		
		//needed ??
		FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
		tree.getRoot().addChild(imageNode);
		
		app.getFeatureTreeService().saveOrUpdate(tree);
	}


	/**
	 * @param config
	 * @param app
	 * @param tree
	 */
	private void makeSpecimenFeatureTree(AlgaTerraImportConfigurator config, ICdmApplicationConfiguration app) {
		ITermService termService = app.getTermService();
		FeatureTree specimenTree = FeatureTree.NewInstance(specimenFeatureTreeUuid);
//		FeatureTree specimenTree = TreeCreator.flatTree(specimenFeatureTreeUuid, config.getFeatureMap(), featureKeyList);
		specimenTree.setTitleCache("AlgaTerra Specimen Feature Tree", true);
		FeatureNode root = specimenTree.getRoot();
		
		
		FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
		root.addChild(imageNode);
		
		addFeatureNodeByUuid(root, termService, AlgaTerraSpecimenImportBase.uuidFeatureAlgaTerraClimate);
		FeatureNode node = FeatureNode.NewInstance(Feature.HABITAT());
		root.addChild(node);
		addFeatureNodeByUuid(root, termService, AlgaTerraSpecimenImportBase.uuidFeatureHabitatExplanation);
		addFeatureNodeByUuid(root, termService, AlgaTerraSpecimenImportBase.uuidFeatureAlgaTerraLifeForm);
		
		addFeatureNodeByUuid(root, termService, AlgaTerraSpecimenImportBase.uuidFeatureAdditionalData);
		addFeatureNodeByUuid(root, termService, AlgaTerraSpecimenImportBase.uuidFeatureSpecimenCommunity);
		
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeaturePH);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureConductivity);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureWaterTemperature);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureSilica);
		FeatureNode nitrogenNode = makeNitrogenNode(root, termService);
		addFeatureNodeByUuid(nitrogenNode, termService, AlgaTerraImportTransformer.uuidFeatureNitrate);
		addFeatureNodeByUuid(nitrogenNode, termService, AlgaTerraImportTransformer.uuidFeatureNitrite);
		addFeatureNodeByUuid(nitrogenNode, termService, AlgaTerraImportTransformer.uuidFeatureAmmonium);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeaturePhosphate);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureOrthoPhosphate);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureNPRation);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureDIN);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureSRP);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureOxygenSaturation);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureCl);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureSecchiDepth);
		addFeatureNodeByUuid(root, termService, AlgaTerraImportTransformer.uuidFeatureCommunity);
		app.getFeatureTreeService().saveOrUpdate(specimenTree);
	}
	
	private FeatureNode makeNitrogenNode(FeatureNode root, ITermService termService) {
		Feature nFeature = Feature.NewInstance("Supra feature for all Nitrogen related subfeatures", "Nitrogen", "N");
		termService.save(nFeature);
		FeatureNode nNode = FeatureNode.NewInstance(nFeature);
		root.addChild(nNode);
		return nNode;
	}


//	private FeatureNode addFeataureNodesByUuidList(UUID[] featureUuidList, FeatureNode root, ITermService termService){
//		FeatureNode lastChild = null;
//		for (UUID featureUuid : featureUuidList){
//			addFeatureNodeByUuid(root, termService, featureUuid);
//		}
//
//		return lastChild;
//	}


	/**
	 * @param root
	 * @param termService
	 * @param featureUuid
	 */
	private void addFeatureNodeByUuid(FeatureNode root, ITermService termService, UUID featureUuid) {
		Feature feature = (Feature)termService.find(featureUuid);
		if (feature != null){
			FeatureNode child = FeatureNode.NewInstance(feature);
			root.addChild(child);	
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AlgaTerraActivator activator = new AlgaTerraActivator();
		activator.invoke(args);
	}

}
