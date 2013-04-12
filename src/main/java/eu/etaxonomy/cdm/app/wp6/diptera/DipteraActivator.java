/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.diptera;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.berlinModelImport.TreeCreator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonImport.PublishMarkerChooser;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
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
public class DipteraActivator {
	private static final Logger logger = Logger.getLogger(DipteraActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.EDIT_Diptera();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Diptera();

	static final UUID secUuid = UUID.fromString("06fd671f-1226-4e3b-beca-1959b3b32e20");
	static final UUID treeUuid = UUID.fromString("1e3093f6-c761-4e96-8065-2c1334ddd0c1");
	static final int sourceSecId = 1000000;
	static final UUID featureTreeUuid = UUID.fromString("ae9615b8-bc60-4ed0-ad96-897f9226d568");
	static final Object[] featureKeyList = new Integer[]{1, 4, 5, 10, 11, 12, 99};
	
	static boolean useClassification = true;
	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	//taxon publish marker
	static final PublishMarkerChooser taxonPublish = PublishMarkerChooser.NO_MARKER;

	static final boolean doDistributionParser = true;  //also run DipteraDistributionParser

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICZN;

//	//ignore null
	static final boolean ignoreNull = true;


	
	//update citations ?
	static final boolean updateCitations = true;
	
	//include collections and add to specimen
	static final boolean updateCollections = true;
	
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
	static final boolean doOccurences = false; //There are no occurrence data in diptera
	static final boolean doCommonNames = false; //no common names in diptera
	
	//etc.
	static final boolean doMarker = true;
	static final boolean doUser = true;

// **************** SELECTED *********************
	
//	//authors
//	static final boolean doAuthors = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	//names
//	static final boolean doTaxonNames = true;
//	static final boolean doRelNames = false;
//	static final boolean doNameStatus = false;
//	static final boolean doTypes = true;
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
//	static final boolean doUser = true;	

	
	/**
	 * @param destination 
	 * @param args
	 */
	public boolean doImport(ICdmDataSource destination) {
		boolean success = true;
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;

		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		bmImportConfigurator.setClassificationUuid(treeUuid);
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
		bmImportConfigurator.setUseClassification(useClassification);
		
		bmImportConfigurator.setDoTaxa(doTaxa);
		bmImportConfigurator.setDoRelTaxa(doRelTaxa);
		bmImportConfigurator.setDoFacts(doFacts);
		bmImportConfigurator.setDoOccurrence(doOccurences);
		bmImportConfigurator.setDoCommonNames(doCommonNames);
		
		bmImportConfigurator.setDoMarker(doMarker);
		bmImportConfigurator.setDoUser(doUser);
		bmImportConfigurator.setEditor(editor);
		bmImportConfigurator.setTaxonPublishMarker(taxonPublish);
		try {
			Method nameTypeDesignationStatusMethod = DipteraActivator.class.getDeclaredMethod("nameTypeDesignationStatueMethod", String.class);
			bmImportConfigurator.setNameTypeDesignationStatusMethod(nameTypeDesignationStatusMethod);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);

		bmImportConfigurator.setCheck(check);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		success &= bmImport.invoke(bmImportConfigurator);
		
		if (bmImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || bmImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			
			//parse distributions
			if (doDistributionParser){
				DipteraDistributionParser dipDist = new DipteraDistributionParser();
				dipDist.doDistribution(app);
			}
			//make feature tree
			app = bmImport.getCdmAppController();
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, bmImportConfigurator.getFeatureMap(), featureKeyList);
			// add image
			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
			tree.getRoot().addChild(imageNode);
			// add distribution
			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
			tree.getRoot().addChild(distributionNode);
			app.getFeatureTreeService().saveOrUpdate(tree);
		}
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
		return success;
	}
	
	public static void main(String[] args) {
		boolean success = true;
		logger.debug("start");
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;	
		DipteraActivator me = new DipteraActivator();
		success &= me.doImport(destination);
		
		DipteraPostImportUpdater updater = new DipteraPostImportUpdater();
		if (updateCitations){
			success &= updater.updateCitations(destination);
		}
		
		if (updateCollections){
			success &= updater.updateCollections(destination);
		}

	
	}
	
	
	
	

	private static NameTypeDesignationStatus nameTypeDesignationStatueMethod(String note){
		if (CdmUtils.isEmpty(note)){
			return null;
		}
		note = note.trim();
		if (note.equalsIgnoreCase("aut.") || note.equalsIgnoreCase("automatic")){
			return NameTypeDesignationStatus.AUTOMATIC();
		}else if (note.equalsIgnoreCase("subs. mon.") ){
			return NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY();
		}else if (note.startsWith("mon.") ){
			return NameTypeDesignationStatus.MONOTYPY();
		}else if (note.startsWith("orig. des") ){
			return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
		}else if (note.startsWith("des") ){
			return NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION();
		}else{
			logger.warn("NameTypeDesignationStatus could not be defined for: " + note);
			return null;
		}
		
		
	}
	
}
