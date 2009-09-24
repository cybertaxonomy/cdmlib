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

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.berlinModelImport.TreeCreator;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_import_cichorieae();

	org.h2.jdbc.JdbcSQLException h;
	static final UUID treeUuid = UUID.fromString("ba7120ce-4fab-49dc-aaa4-f36276426aa8");
	static final int sourceSecId = 7700000;
	
	static final UUID featureTreeUuid = UUID.fromString("ab007336-d853-4f2f-a490-7c8232eafe7b");
	static final Object[] featureKeyList = new Integer[]{1, 31, 4, 98, 41}; 	
	
	//TODO update for Taraxacum
	static final String mediaUrlString = "http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/";
	//Mac
	//static final File mediaPath = new File("/Volumes/protolog/protolog/");
	//Windows
	static final File mediaPath = new File("\\\\media\\editwp6\\protolog");
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	
	//check - import
	//static final CHECK check = CHECK.CHECK_ONLY;
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;


// **************** ALL *********************	
////	authors
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

	
// **************** SELECTED *********************

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
	public void doImport() {
		logger.info("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		ICdmDataSource destination = cdmDestination;
		
		BerlinModelImportConfigurator bmImportConfigurator = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
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
		bmImportConfigurator.setDbSchemaValidation(hbm2dll);

		// mediaResourceLocations
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
		bmImport.invoke(bmImportConfigurator);
		
		if (bmImportConfigurator.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || bmImportConfigurator.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			CdmApplicationController app = bmImport.getCdmAppController();
			TransactionStatus tx = app.startTransaction();
			//make feature tree
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, bmImportConfigurator.getFeatureMap(), featureKeyList);
			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
			tree.getRoot().addChild(imageNode);
			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
			tree.getRoot().addChild(distributionNode, 2);
			app.getDescriptionService().saveFeatureTree(tree);
			mergeIntoCichorieae(app);
			app.commitTransaction(tx);
		}
		
		logger.info("End import from BerlinModel ("+ source.getDatabase() + ")...");
	}
	
	public void mergeIntoCichorieae(CdmApplicationController app){
		
		String taraxTaraxacum = "9a7bced0-fa1a-432e-9cca-57b62219cde6";
		String cichTaraxacum = "c946ac62-b6c6-493b-8ed9-278fa38b931a";
		UUID taraxacumCichUUID = UUID.fromString(cichTaraxacum);
		Taxon taraxacumInCich = (Taxon)app.getTaxonService().findByUuid(taraxacumCichUUID);
		if (taraxacumInCich != null) {
			logger.info("Merge Taraxacum");
			Taxon parent = taraxacumInCich.getTaxonomicParent();
			Set<TaxonNode> parentNodes = parent.getTaxonNodes();
			if(parentNodes.size() != 1){
				throw new RuntimeException("Exactly one Taxon Node expected, but found " + parentNodes.size());
			}
			TaxonNode parentNode = parentNodes.iterator().next();
			UUID taraxacumTaraxUUID = UUID.fromString(taraxTaraxacum);
			Taxon taraxacumInTarax = (Taxon)app.getTaxonService().findByUuid(taraxacumTaraxUUID);
	
			//TODO reference
			ReferenceBase citation = null;
			String microcitation = null;
			parentNode.addChild(taraxacumInTarax, citation, microcitation);
			app.getTaxonService().save(parent);
			TaxonNode taraxacumInCichNode = parentNode.getTaxonomicTree().getNode(taraxacumInCich);
			parentNode.removeChild(taraxacumInCichNode);
			app.getTaxonService().delete(taraxacumInCich);
		}
	}
	
	public static void main(String[] args) {
		TaraxacumActivator ta = new TaraxacumActivator();
		ta.doImport();
		
	}

}
