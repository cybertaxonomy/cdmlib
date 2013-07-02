/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.cichorieae;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class TaraxacumActivator {
	private static final Logger logger = Logger.getLogger(TaraxacumActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;
	static final Source berlinModelSource = BerlinModelSources.EDIT_Taraxacum();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Cichorieae();

	static final UUID treeUuid = UUID.fromString("019c4b4d-736b-4d2e-883c-e3244167080a");
//	static final UUID treeUuid = UUID.fromString("00db28a7-50e1-4abc-86ec-b2a8ce870de9");
//	static final UUID treeUuid = UUID.fromString("534e190f-3339-49ba-95d9-fa27d5493e3e");
	static final int sourceSecId = 7800000;
	
	static final UUID featureTreeUuid = UUID.fromString("ab007336-d853-4f2f-a490-7c8232eafe7b");
	static final Object[] featureKeyList = new Integer[]{1, 31, 4, 98, 41}; 	
	
	//TODO update for Taraxacum
	static final String mediaUrlString = "http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/";
	//Mac
	//static final File mediaPath = new File("/Volumes/protolog/protolog/");
	//Windows
	//static final File mediaPath = new File("\\\\media\\editwp6\\protolog");
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	
	//check - import
	//static final CHECK check = CHECK.CHECK_ONLY;
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;


// **************** ALL *********************	
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

	
// **************** SELECTED *********************
//
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
	
	/**
	 * @param args
	 */
	public boolean doImport(ICdmDataSource destination, DbSchemaValidation hbm2dll) {
		boolean success = true;
		logger.info("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		bmImportConfigurator.setClassificationUuid(treeUuid);
//		bmImportConfigurator.setSecUuid(secUuid);
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
		
		bmImportConfigurator.setDoTaxa(doTaxa);
		bmImportConfigurator.setDoRelTaxa(doRelTaxa);
		bmImportConfigurator.setDoFacts(doFacts);
		bmImportConfigurator.setDoOccurrence(doOccurences);
		bmImportConfigurator.setDoCommonNames(doCommonNames);
		
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);

		
		// mediaResourceLocations
		File mediaPath = CichorieaeActivator.protologuePath;
		if ( mediaPath.exists() && mediaPath.isDirectory()){
			bmImportConfigurator.setMediaUrl(mediaUrlString);
			bmImportConfigurator.setMediaPath(mediaPath);
		}else{
			logger.warn("Could not configure mediaResourceLocations");
		}
		
		// maximum number of name facts to import
		bmImportConfigurator.setMaximumNumberOfNameFacts(maximumNumberOfNameFacts);
		
		
		bmImportConfigurator.setCheck(check);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		success &= bmImport.invoke(bmImportConfigurator);
		
		if (bmImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || bmImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			TransactionStatus tx = app.startTransaction();
			//make feature tree
//			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, bmImportConfigurator.getFeatureMap(), featureKeyList);
//			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
//			tree.getRoot().addChild(imageNode);
//			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
//			FeatureNode featureNode = tree.getRoot();
//			tree.getRoot().addChild(distributionNode, featureNode.getChildCount() + 1);
//			app.getFeatureTreeService().saveOrUpdate(tree);
			mergeIntoCichorieae(app);
			app.commitTransaction(tx);
		}
		
		logger.info("End import from BerlinModel ("+ source.getDatabase() + ")...");
		return success;
	}
	
	
	public boolean mergeIntoCichorieae(ICdmApplicationConfiguration app){
		boolean success = true;
	//	String taraxTaraxacumUuidStr = "9a7bced0-fa1a-432e-9cca-57b62219cde6";
		String taraxTaraxacumUuidStr = "b86f1156-091c-494d-a9c9-c84d71058f98";
		UUID taraxTaraxacumUUID = UUID.fromString(taraxTaraxacumUuidStr);

		String cichTaraxacumUuidStr = "c946ac62-b6c6-493b-8ed9-278fa38b931a";
		UUID cichTaraxacumUUID = UUID.fromString(cichTaraxacumUuidStr);
		
		Taxon taraxacumInCichTaxon = (Taxon)app.getTaxonService().find(cichTaraxacumUUID);
		if (taraxacumInCichTaxon != null) {
			logger.info("Merge Taraxacum");
			Set<TaxonNode> taxonNodesInCich = taraxacumInCichTaxon.getTaxonNodes();
			TaxonNode taxonNodeInCich = null;
			TaxonNode parentNodeInCich = null;
			Taxon parentInCich = null;
			TaxonNode taxonNodeInTarax = null;
			
			if (taxonNodesInCich == null || taxonNodesInCich.isEmpty()) {
				logger.error("No taxon nodes found for Taraxacum in cichorieae database");
				success = false;
			} else {
				logger.info(taxonNodesInCich.size()+ " taxon node(s) found for Taraxacum in Cich DB");
				taxonNodeInCich = taxonNodesInCich.iterator().next();
				parentNodeInCich = (TaxonNode) taxonNodeInCich.getParent();
				parentInCich = parentNodeInCich.getTaxon();
			}
			
			Taxon taraxacumInTaraxTaxon = (Taxon)app.getTaxonService().find(taraxTaraxacumUUID);
			
			Set<TaxonNode> taxonNodesInTarax = taraxacumInTaraxTaxon.getTaxonNodes();
			
			Classification treeInTaraxacum = null;
			if (taxonNodesInTarax == null || taxonNodesInTarax.isEmpty()) {
				logger.warn("No taxon nodes found for Taraxacum in taraxacum database");
				success = false;
			}else{
				taxonNodeInTarax = taxonNodesInTarax.iterator().next();
				treeInTaraxacum = taxonNodeInTarax.getClassification();
			}
	
			//TODO reference
			Reference citation = null;
			String microcitation = null;
			
			taxonNodeInTarax = parentNodeInCich.addChildNode(taxonNodeInTarax, citation, microcitation, null);
			//parentNodeInCich.getClassification().addParentChild(parentInCich, taraxacumInTaraxTaxon, null, null);
		
			parentNodeInCich.deleteChildNode(taxonNodeInCich);
			
			app.getTaxonService().save(parentInCich);
			app.getTaxonService().delete(taraxacumInCichTaxon);
			try {
//				app.getClassificationService().delete(treeInTaraxacum); //throws exception
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}else{
			logger.warn("Taraxacum in cichorieae not found");
			success = false;
		}
		return success;
	}
	
	public static void main(String[] args) {
		TaraxacumActivator ta = new TaraxacumActivator();
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		ta.doImport(destination, hbm2dll);
		
	}

}
