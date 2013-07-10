/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.eflora;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.CdmImportBase.TermMatchMode;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.EfloraImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae.CentralAfricaEricaceaeImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae.CentralAfricaEricaceaeTransformer;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenCdmExcelImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class CentralAfricaEricaceaeActivator {
	private static final Logger logger = Logger.getLogger(CentralAfricaEricaceaeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final URI source = EfloraSources.ericacea_local();
	
	static final URI specimenSource = EfloraSources.ericacea_specimen_local();

	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM3();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_central_africa_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_jaxb();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_cichorieae_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_campanulaceae_production();
	
	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7");
	
	public static UUID defaultLanguageUuid = Language.uuidFrench;
	
	//classification
	static final UUID classificationUuid = UUID.fromString("10e5efcc-6e13-4abc-ad42-e0b46e50cbe7");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doPrintKeys = false;
	
	//taxa
	private boolean includeEricaceae = true;
	static final boolean doTaxa = true;
	static final boolean doDeduplicate = false;

	
	private boolean doNewNamedAreas = false;
	private boolean doFeatureTree = false;
	
	private boolean doSpecimen = false;
	private TermMatchMode specimenAreaMatchMode = TermMatchMode.UUID_ABBREVLABEL;

	
	private void doImport(ICdmDataSource cdmDestination){
		
		CdmUpdater su = CdmUpdater.NewInstance();
		IProgressMonitor monitor = DefaultProgressMonitor.NewInstance();
//		
//		try {
//			su.updateToCurrentVersion(cdmDestination, monitor);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (true){
//			return;
//		}
		
		//make Source
		CentralAfricaEricaceaeImportConfigurator config= CentralAfricaEricaceaeImportConfigurator.NewInstance(source, cdmDestination);
		config.setClassificationUuid(classificationUuid);
		config.setDoTaxa(doTaxa);
		config.setCheck(check);
		config.setDefaultLanguageUuid(defaultLanguageUuid);
		config.setDoPrintKeys(doPrintKeys);
		config.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport<EfloraImportConfigurator> myImport = new CdmDefaultImport<EfloraImportConfigurator>();

		
		//Ericaceae
		if (includeEricaceae){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		if (doFeatureTree){
			FeatureTree tree = makeFeatureNode(myImport.getCdmAppController().getTermService());
			myImport.getCdmAppController().getFeatureTreeService().saveOrUpdate(tree);
		}
		
		//check keys
		if (doPrintKeys){
			TransactionStatus tx = myImport.getCdmAppController().startTransaction();
			List<PolytomousKey> keys = myImport.getCdmAppController().getPolytomousKeyService().list(PolytomousKey.class, null, null, null, null);
			for(PolytomousKey key : keys){
				key.print(System.out);
				System.out.println();
			}
			myImport.getCdmAppController().commitTransaction(tx);
		}
		
		//deduplicate
		if (doDeduplicate){
			ICdmApplicationConfiguration app = myImport.getCdmAppController();
			if (app == null){
				app = CdmApplicationController.NewInstance(cdmDestination, hbm2dll, false);
			}
			app.getAgentService().updateTitleCache(Team.class, null, null, null);
			return;
//			int count = app.getAgentService().deduplicate(Person.class, null, null);
//			
//			logger.warn("Deduplicated " + count + " persons.");
////			count = app.getAgentService().deduplicate(Team.class, null, null);
////			logger.warn("Deduplicated " + count + " teams.");
//			count = app.getReferenceService().deduplicate(Reference.class, null, null);
//			logger.warn("Deduplicated " + count + " references.");
		}
		
		if(doNewNamedAreas){
			newNamedAreas(myImport);
		}

		if (doSpecimen){
			logger.warn("Start specimen import");
			ICdmApplicationConfiguration app = myImport.getCdmAppController();
			SpecimenCdmExcelImportConfigurator specimenConfig= SpecimenCdmExcelImportConfigurator.NewInstance(specimenSource, cdmDestination);
			specimenConfig.setCdmAppController((CdmApplicationController)app);
			specimenConfig.setAreaMatchMode(specimenAreaMatchMode);
			
			config.setDbSchemaValidation(DbSchemaValidation.VALIDATE);
			specimenConfig.setSourceReference(getSourceReference(specimenConfig.getSourceReferenceTitle()));
			
			CdmDefaultImport<SpecimenCdmExcelImportConfigurator> specimenImport = new CdmDefaultImport<SpecimenCdmExcelImportConfigurator>();
			specimenImport.setCdmAppController(app);
			specimenImport.invoke(specimenConfig);
			
			
		}
		return;
	

		
	}

	private void newNamedAreas(CdmDefaultImport<EfloraImportConfigurator> myImport) {
		ICdmApplicationConfiguration app = myImport.getCdmAppController();
		if (app == null){
			app = CdmApplicationController.NewInstance(cdmDestination, hbm2dll, false);
		}
		TransactionStatus tx = app.startTransaction();
		
		OrderedTermVocabulary<NamedArea> voc = OrderedTermVocabulary.NewInstance("Phytogeographic Regions of Central Africa", "Phytogeographic Regions of Central Africa", "FdAC regions", null);
		app.getVocabularyService().save(voc);
		
		NamedAreaLevel level = NamedAreaLevel.NewInstance("Phytogeographic Regions of Central Africa", "Phytogeographic Regions of Central Africa", "FdAC regions");
		ITermService termService = app.getTermService();
		
		termService.save(level);
		
		NamedArea area = NamedArea.NewInstance("C\u00F4tier", "C\u00F4tier", "I");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);
		

		area = NamedArea.NewInstance("Mayumbe", "Mayumbe", "II");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Bas-Congo", "Bas-Congo", "III");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Kasai", "Kasai", "IV");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Bas-Katanga", "Bas-Katanga", "V");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Forestier Central", "Forestier Central", "VI");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Ubangi-Uele", "Ubangi-Uele", "VII");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Lac Albert", "Lac Albert", "VIII");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Lacs \u00C9douard et Kivu", "Lacs \u00C9douard et Kivu", "IX");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Rwanda-Burundi", "Rwanda-Burundi", "X");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);

		area = NamedArea.NewInstance("Haut-Katanga", "Haut-Katanga", "XI");
		area.setLevel(level);
		area.setType(NamedAreaType.NATURAL_AREA());
		voc.addTerm(area);
		termService.save(area);
		
		app.getVocabularyService().save(voc);
		
		app.commitTransaction(tx);

	}

	private Reference getSourceReference(String string) {
		Reference result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}

	private FeatureTree makeFeatureNode(ITermService service){
		CentralAfricaEricaceaeTransformer transformer = new CentralAfricaEricaceaeTransformer();
		
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		result.setTitleCache("Central Africa Ericaceae Feature Tree");
		FeatureNode root = result.getRoot();
		FeatureNode newNode;
		
		newNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		root.addChild(newNode);
		
		addFeataureNodesByStringList(descriptionFeatureList, newNode, transformer, service);

		addFeataureNodesByStringList(generellDescriptionsList, root, transformer, service);

		
		newNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		root.addChild(newNode);

		newNode = FeatureNode.NewInstance(Feature.ECOLOGY());
		root.addChild(newNode);
		addFeataureNodesByStringList(habitatEcologyList, root, transformer, service);
		
		newNode = FeatureNode.NewInstance(Feature.USES());
		root.addChild(newNode);
		
		addFeataureNodesByStringList(chomosomesList, root, transformer, service);

		newNode = FeatureNode.NewInstance(Feature.COMMON_NAME());
		root.addChild(newNode);
		
		newNode = FeatureNode.NewInstance(Feature.CITATION());
		root.addChild(newNode);
		
		return result;
	}
	
	private static String [] chomosomesList = new String[]{
		"Chromosomes", 
	};

	
	private static String [] habitatEcologyList = new String[]{
		"Habitat",
		"Habitat & Ecology"
	};
	
	
	private static String [] generellDescriptionsList = new String[]{
		"Fossils",
		"Morphology and anatomy",
		"Morphology", 
		"Vegetative morphology and anatomy",
		"Flower morphology",
		"Palynology",  
		"Pollination",  
		"Pollen morphology",
		"Life cycle",
		"Fruits and embryology",
		"Dispersal",
		"Wood anatomy",  
		"Leaf anatomy",  
		"Chromosome numbers", 
		"Phytochemistry and Chemotaxonomy",
		"Phytochemistry",
		"Taxonomy",	
	};

	private static String [] descriptionFeatureList = new String[]{
		"lifeform", 
		"Bark",  
		"Indumentum",  
		"endophytic body",  
		"flowering buds",  
		"Branchlets",  
		"Branches",  
		"Branch",  
		"Flowering branchlets",
		"Trees",  
		"Twigs",  
		"stem",  
		"Stems",  
		"stem leaves", 
		"Leaves",
		"flower-bearing stems",  
		"Petiole",  
		"Petiolules",  
		"Leaflets", 
		"Thyrsus",  
		"Thyrses",  
		"Inflorescences",  
		"Inflorescence",
		"Young inflorescences", 
		"Bracts",  
		"Pedicels",  
		"flowering buds",  
		"scales",  
		"Buds",  
		"Flowers",  
		"Flower",  
		"Flowering",
		"Stigma",  
		"perianth",  
		"Sepals",  
		"Sepal",  
		"Outer Sepals",  
		"Axillary",  
		"cymes",  
		"Calyx",  
		"Petal",  
		"Petals",  
		"perigone tube",
		"Disc",  
		"corolla",  
		"Stamens",  
		"Staminodes",  
		"Ovary",  
		"Anthers",
		"anther",  
		"Pistil",  
		"Pistillode",  
		"Ovules",  
		"androecium",  
		"gynoecium",  
		"Filaments",  		
		"Style",  
		"annulus",  
		"female flowers",  
		"Male flowers",  
		"Female",  
		"Infructescences",    //order not consistent (sometimes before "Flowers")  
		"Fruit",  
		"Fruits",  
		"fruiting axes",  
		"drupes",  
		"Arillode",  
		"seed",  
		"Seeds",  
		"Seedling",  
		"flower tube", 
		"nutlets",  
		"pollen",  
		"secondary xylem",  
		"chromosome number",  
	
		"figure",  
		"fig",  
		"figs",  



		
	};
	
	public void addFeataureNodesByStringList(String[] featureStringList, FeatureNode root, IInputTransformer transformer, ITermService termService){
		try {
			for (String featureString : featureStringList){
			UUID featureUuid;
			featureUuid = transformer.getFeatureUuid(featureString);
			Feature feature = (Feature)termService.find(featureUuid);
			if (feature != null){
				FeatureNode child = FeatureNode.NewInstance(feature);
				root.addChild(child);	
			}
		}
		} catch (UndefinedTransformerMethodException e) {
			logger.error("getFeatureUuid is not implemented in transformer. Features could not be added");
		}
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CentralAfricaEricaceaeActivator me = new CentralAfricaEricaceaeActivator();
		me.doImport(cdmDestination);
	}
	
}
