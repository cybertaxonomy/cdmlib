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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.markup.MarkupImportConfigurator;
import eu.etaxonomy.cdm.io.markup.MarkupTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 */
public class FloreGabonActivator extends EfloraActivatorBase {
	private static final Logger logger = Logger.getLogger(FloreGabonActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final URI source = EfloraSources.fdg_sample();
	static final URI fdg1 = EfloraSources.fdg_1();
	static final URI fdg2 = EfloraSources.fdg_2();
	static final URI fdg3 = EfloraSources.fdg_3();
	static final URI fdg4 = EfloraSources.fdg_4();
	static final URI fdg5 = EfloraSources.fdg_5();
	static final URI fdg5bis = EfloraSources.fdg_5bis();
	static final URI fdg6 = EfloraSources.fdg_6();
	static final URI fdg7 = EfloraSources.fdg_7();
	static final URI fdg8 = EfloraSources.fdg_8();
	static final URI fdg9 = EfloraSources.fdg_9();
	static final URI fdg10 = EfloraSources.fdg_10();
	static final URI fdg11 = EfloraSources.fdg_11();
	static final URI fdg21 = EfloraSources.fdg_21();
	static final URI fdg22 = EfloraSources.fdg_22();
	static final URI fdg27 = EfloraSources.fdg_27();
	static final URI fdg28 = EfloraSources.fdg_28();
	static final URI fdg30 = EfloraSources.fdg_30();
	
	
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flore_gabon_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flore_gabon_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	

	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("ee688973-2595-4d4d-b11e-6df71e96a5c2");
	private static final String featureTreeTitle = "Flore Gabon Presentation Feature Tree";
	
	//classification
	static final UUID classificationUuid = UUID.fromString("2f892452-ff49-48cf-834f-52ca29600719");
	static final String classificationTitle = "Flore du Gabon";
	
	
	//check - import
	private boolean h2ForCheck = false;
	static CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doPrintKeys = false;

	
	private boolean replaceStandardKeyTitles = true;

	//taxa
	static final boolean doTaxa = true;
	
	static final boolean reuseState = true;
	
	
	//if true, use inverse include information
	private boolean inverseInclude = false;
	
	private boolean includeFdg1 = true;
	private boolean includeFdg2 = true;
	private boolean includeFdg3 = true;
	private boolean includeFdg4 = true;
	private boolean includeFdg5 = true;
	private boolean includeFdg5bis = true;
	private boolean includeFdg6 = true;
	private boolean includeFdg7 = true;
	private boolean includeFdg8 = true;
	private boolean includeFdg9 = true;
	private boolean includeFdg10 = true;
	private boolean includeFdg11 = true;
	private boolean includeFdg21 = true;
	private boolean includeFdg22 = true;
	private boolean includeFdg27 = true;
	private boolean includeFdg28 = true;
	private boolean includeFdg30 = true;
	
// **************** NO CHANGE **********************************************/
	
	private IIoObserver observer = new LoggingIoObserver();
	private Set<IIoObserver> observerList = new HashSet<IIoObserver>();
	
	private MarkupImportConfigurator config;
	private CdmDefaultImport<MarkupImportConfigurator> myImport;
	
	private void doImport(ICdmDataSource cdmDestination){
		observerList.add(observer);
		if (h2ForCheck && cdmDestination.getDatabaseType().equals(CdmDestinations.localH2().getDatabaseType())){
			check = CHECK.CHECK_ONLY;
		}
		
		//make config
		URI source = fdg1;
		config = MarkupImportConfigurator.NewInstance(source, cdmDestination);
		config.setClassificationUuid(classificationUuid);
		config.setDoTaxa(doTaxa);
		config.setCheck(check);
		config.setDoPrintKeys(doPrintKeys);
		config.setDbSchemaValidation(hbm2dll);
		config.setObservers(observerList);
		config.setReplaceStandardKeyTitles(replaceStandardKeyTitles);
		config.setSourceReference(getSourceReference("Flore du Gabon"));
		config.setClassificationName(classificationTitle);
		config.setReuseExistingState(reuseState);
		
		myImport = new CdmDefaultImport<MarkupImportConfigurator>(); 
		
		//Vol1
		executeVolume( fdg1, includeFdg1 ^ inverseInclude);
		
		//Vol2
		executeVolume(fdg2, includeFdg2 ^ inverseInclude);
		
		//Vol3
		executeVolume(fdg3, includeFdg3 ^ inverseInclude);

		//Vol4
		executeVolume(fdg4, includeFdg4 ^ inverseInclude);

		//Vol5
		executeVolume(fdg5, includeFdg5 ^ inverseInclude);
		
		//Vol5bis
		executeVolume(fdg5bis, includeFdg5bis ^ inverseInclude);
		
		//Vol6
		executeVolume(fdg6, includeFdg6 ^ inverseInclude);
		
		//Vol7
		executeVolume(fdg7, includeFdg7 ^ inverseInclude);
		
		//Vol8
		executeVolume(fdg8, includeFdg8 ^ inverseInclude);
		
		//Vol9
		executeVolume(fdg9, includeFdg9 ^ inverseInclude);
		
		//Vol10
		executeVolume(fdg10, includeFdg10 ^ inverseInclude);

		//Vol11
		executeVolume(fdg11, includeFdg11 ^ inverseInclude);

		//Vol21
		executeVolume(fdg21, includeFdg21 ^ inverseInclude);
		//Vol22
		executeVolume(fdg22, includeFdg22 ^ inverseInclude);
		//Vol27
		executeVolume(fdg27, includeFdg27 ^ inverseInclude);
		//Vol28
		executeVolume(fdg28, includeFdg28 ^ inverseInclude);
		//Vol30
		executeVolume(fdg30, includeFdg30 ^ inverseInclude);
		
		FeatureTree tree = makeFeatureNode(myImport.getCdmAppController().getTermService());
		myImport.getCdmAppController().getFeatureTreeService().saveOrUpdate(tree);
		
		makeAutomatedFeatureTree(myImport.getCdmAppController(), config.getState(),
				featureTreeUuid, featureTreeTitle);

		
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
		
	}

	/**
	 * @param markupConfig
	 * @param myImport
	 */
	private void executeVolume(URI source, boolean include) {
		if (include){
			System.out.println("\nStart import from ("+ source.toString() + ") ...");
			config.setSource(source);
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
	}
	
	private Reference<?> getSourceReference(String string) {
		Reference<?> result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}
	
	


	private FeatureTree makeFeatureNode(ITermService service){
		MarkupTransformer transformer = new MarkupTransformer();
		
		FeatureTree result = FeatureTree.NewInstance();
		result.setTitleCache("Old feature tree", true);
		FeatureNode root = result.getRoot();
		FeatureNode newNode;
		
		newNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		root.addChild(newNode);
		
		addFeataureNodesByStringList(descriptionFeatureList, newNode, transformer, service);

		addFeataureNodesByStringList(generellDescriptionsUpToAnatomyList, root, transformer, service);
		newNode = FeatureNode.NewInstance(Feature.ANATOMY());
		addFeataureNodesByStringList(anatomySubfeatureList, newNode, transformer, service);
		
		newNode = addFeataureNodesByStringList(generellDescriptionsFromAnatomyToPhytoChemoList, root, transformer, service);
		addFeataureNodesByStringList(phytoChemoSubFeaturesList, newNode, transformer, service);

		newNode = addFeataureNodesByStringList(generellDescriptionsFromPhytoChemoList, root, transformer, service);
		
		newNode = FeatureNode.NewInstance(Feature.COMMON_NAME());
		root.addChild(newNode);
		
		newNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		root.addChild(newNode);

		newNode = FeatureNode.NewInstance(Feature.ECOLOGY());
		root.addChild(newNode);
		addFeataureNodesByStringList(habitatEcologyList, root, transformer, service);
		
		newNode = FeatureNode.NewInstance(Feature.USES());
		root.addChild(newNode);
		
		addFeataureNodesByStringList(chomosomesList, root, transformer, service);

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
	
	
	private static String [] generellDescriptionsUpToAnatomyList = new String[]{
		"Fossils",
		"Morphology and anatomy",
		"Morphology", 
		"Vegetative morphology and anatomy",
	};

	
	private static String [] anatomySubfeatureList = new String[]{
		"Leaf anatomy",
		"Wood anatomy"
	};

	private static String [] generellDescriptionsFromAnatomyToPhytoChemoList = new String[]{
		"Flower morphology",
		"Palynology",  
		"Pollination",  
		"Pollen morphology",
		"embryology",
		"cytology",
		"Life cycle",
		"Fruits and embryology",
		"Dispersal",
		"Chromosome numbers", 
		"Phytochemistry and Chemotaxonomy",
	};
	
	
	private static String [] phytoChemoSubFeaturesList = new String[]{
		"Alkaloids",
		"Iridoid glucosides",
		"Leaf phenolics",
		"Storage products of seeds",
		"Aluminium",
		"Chemotaxonomy",
	};
	

	private static String [] generellDescriptionsFromPhytoChemoList = new String[]{
		"Phytochemistry",
		"Taxonomy",
		"history",
		"cultivation",
		"Notes"
	};

	
	private static String [] descriptionFeatureList = new String[]{
		"lifeform", 
		"Juvenile parts",
		"Bark",
		//new
		"wood",
		"Indumentum",  
		"endophytic body",  
		"apical buds",
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
		"extraxylary sclerenchyma",
		"flower-bearing stems",  
		"Petiole",  
		"Petiolules",  
		"Leaflets", 
		"Lamina",
		"Veins",
		"Lateral veins",
		"secondary veins",
		"Intersecondary veins",
		"veinlets",
		"Thyrsus",  
		"Thyrses",  
		"Inflorescences",  
		"Inflorescence",
		"Young inflorescences", 
		"Male inflorescences", 
		"Female inflorescences", 
		"rachises",
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
		"Androgynophore",
		"Petal",  
		"Petals",
		"perigone",
		"perigone lobes",
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
		"Androphore",
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
	
	public FeatureNode addFeataureNodesByStringList(String[] featureStringList, FeatureNode root, IInputTransformer transformer, ITermService termService){
		FeatureNode lastChild = null;
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
		return lastChild;
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FloreGabonActivator me = new FloreGabonActivator();
		me.doImport(cdmDestination);
	}
}