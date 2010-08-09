/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.eflora;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.floraMalesiana.FloraMalesianaImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.floraMalesiana.FloraMalesianaTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class FloraMalesianaActivator {
	private static final Logger logger = Logger.getLogger(FloraMalesianaActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String fmSource1 = EfloraSources.fm_sapindaceae_local();
	static final String fmSource2 = EfloraSources.fm_sapindaceae2_local();
	static final String fmSource13_1 = EfloraSources.fm_13_1_local();
	static final String fmSource13_2 = EfloraSources.fm_13_2_local();
	
	
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_malesiana_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_malesiana_production();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("168df0c6-6429-484c-b26f-ded1f7e44bd9");
	
	//classification
	static final UUID classificationUuid = UUID.fromString("ca4e4bcb-a1d1-4124-a358-a3d3c41dd450");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doPrintKeys = false;
	
	//taxa
	static final boolean doTaxa = true;

	private boolean includeSapindaceae1 = true;
	private boolean includeSapindaceae2 = true;
	private boolean includeVol13_1 = true;
	private boolean includeVol13_2 = true;

	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make BerlinModel Source
		String source = fmSource1;
		FloraMalesianaImportConfigurator floraMalesianaConfig= FloraMalesianaImportConfigurator.NewInstance(source, cdmDestination);
		floraMalesianaConfig.setTaxonomicTreeUuid(classificationUuid);
		floraMalesianaConfig.setDoTaxa(doTaxa);
		floraMalesianaConfig.setCheck(check);
		floraMalesianaConfig.setDoPrintKeys(doPrintKeys);
		floraMalesianaConfig.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport<FloraMalesianaImportConfigurator> myImport = new CdmDefaultImport<FloraMalesianaImportConfigurator>();

		
		//Sapindaceae1
		if (includeSapindaceae1){
			System.out.println("Start import from ("+ fmSource1.toString() + ") ...");
			myImport.invoke(floraMalesianaConfig);
			System.out.println("End import from ("+ fmSource1.toString() + ")...");
		}
		
		//Sapindaceae2
		if (includeSapindaceae2){
			System.out.println("\nStart import from ("+ fmSource2.toString() + ") ...");
			source = fmSource2;
			floraMalesianaConfig.setSource(source);
			myImport.invoke(floraMalesianaConfig);
			System.out.println("End import from ("+ fmSource2.toString() + ")...");
		}
		
		//Vol13_1
		if (includeVol13_1){
			System.out.println("\nStart import from ("+ fmSource13_1.toString() + ") ...");
			source = fmSource13_1;
			floraMalesianaConfig.setSource(source);
			myImport.invoke(floraMalesianaConfig);
			System.out.println("End import from ("+ fmSource13_1.toString() + ")...");
		}

		//Vol13_2
		if (includeVol13_2){
			System.out.println("\nStart import from ("+ fmSource13_2.toString() + ") ...");
			source = fmSource13_2;
			floraMalesianaConfig.setSource(source);
			myImport.invoke(floraMalesianaConfig);
			System.out.println("End import from ("+ fmSource13_2.toString() + ")...");
		}
		
		FeatureTree tree = makeFeatureNode(myImport.getCdmAppController().getTermService());
		myImport.getCdmAppController().getFeatureTreeService().saveOrUpdate(tree);
		
		//check keys
		if (doPrintKeys){
			TransactionStatus tx = myImport.getCdmAppController().startTransaction();
			List<FeatureTree> keys = myImport.getCdmAppController().getFeatureTreeService().list(PolytomousKey.class, null, null, null, null);
			for(FeatureTree key : keys){
				((PolytomousKey)key).print(System.out);
				System.out.println();
			}
			myImport.getCdmAppController().commitTransaction(tx);
		}
		
	}
	
	private FeatureTree makeFeatureNode(ITermService service){
		
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		FeatureNode root = result.getRoot();
		FeatureNode newNode;
		
		newNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		root.addChild(newNode);
		FloraMalesianaTransformer transformer = new FloraMalesianaTransformer();
		addFeataureNodesByStringList(descriptionFeatureList, newNode, transformer, service);
		
		newNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.ECOLOGY());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.USES());
		root.addChild(newNode);
		
		newNode = FeatureNode.NewInstance(Feature.ANATOMY());
		root.addChild(newNode);	



		return result;
	}

	private static String [] descriptionFeatureList = new String[]{
		"Leaflets",  
		"Leaves",  
		"Branchlets",  
		"lifeform",  
		"Inflorescences",  
		"Flowers",  
		"Sepals",  
		"Outer Sepals",  
		"Anthers",  
		"Petals",  
		"Petal",  
		"Disc",  
		"Stamens",  
		"Fruits",  
		"Indumentum",  
		"figure",  
		"fig",  
		"figs",  
		"Seeds",  
		"Flowering",  
		"Bracts",  
		"Pedicels",  
		"Pistil",  
		"Ovary",  
		"Twigs",  
		"Pedicels",  
		"Infructescences",  
		"Branches",  
		"Flower",  
		"Ovules",  
		"Female",  
		"Style",  
		"Arillode",  
		"Fruit",  
		"Branch",  
		"Inflorescence",  
		"Calyx",  
		"Seedling",  
		"Staminodes",  
		"Filaments",  
		"Pistillode",  
		"Stigma",  
		"Petiole",  
		"Buds",  
		"Stems",  
		"Trees",  
		"Chromosomes",  
		"Axillary",  
		"Petiolules",  
		"Male flowers",  
		"Young inflorescences",  
		"Sepal",  
		"Thyrses",  
		"Thyrsus",  
		"Bark",  
		"endophytic body",  
		"flowering buds",  
		"flower buds",  
		"perianth",  
		"scales",  
		"perigone tube",  
		"corolla",  
		"annulus",  
		"female flowers",  
		"cymes",  
		"nutlets",  
		"stem",  
		"pollen",  
		"secondary xylem",  
		"chromosome number",  
		"stem leaves",  
		"flower tube",  
	
		"seed",  
		"drupes",  
		"fruiting axes",  
		"androecium",  
		"gynoecium",  
	
		"anther",  
		"flower-bearing stems",  
		"Flowering branchlets",  
	
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
		FloraMalesianaActivator me = new FloraMalesianaActivator();
		me.doImport(cdmDestination);
	}
	
}
