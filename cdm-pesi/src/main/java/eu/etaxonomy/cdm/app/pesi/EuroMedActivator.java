/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.pesi;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.BerlinModelSources;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.TreeCreator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
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
public class EuroMedActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EuroMedActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final Source berlinModelSource = BerlinModelSources.euroMed();
	static final Source berlinModelSource = BerlinModelSources.PESI3_euroMed();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_pesi_euroMed();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static final boolean includePesiExport = false;
	
	static final int sourceSecId = 7000000; //500000
	static final UUID classificationUuid = UUID.fromString("314a68f9-8449-495a-91c2-92fde8bcf344");
	static final boolean useSingleClassification = true;
	
	static final UUID featureTreeUuid = UUID.fromString("6a5e1c2b-ec0d-46c8-9c7d-a2059267ffb7");
	static final Object[] featureKeyList = new Integer[]{1, 31, 4, 98, 41}; 	
	
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	static final int partitionSize = 5000;
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	
	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;
	
	static final boolean switchSpeciesGroup = true;
	
	static boolean useClassification = true;
	
	static boolean 	isSplitTdwgCodes = false;
	
	private boolean removeHttpMapsAnchor = true;

	
	static final String infrGenericRankAbbrev = "[unranked]";
	static final String infrSpecificRankAbbrev = "[unranked]";
	
	
	static String taxonTable = "v_cdm_exp_taxaAll";
	static String classificationQuery = " SELECT DISTINCT t.PTRefFk, r.RefCache FROM PTaxon t INNER JOIN Reference r ON t.PTRefFk = r.RefId WHERE t.PTRefFk = " + sourceSecId; 
	static String relPTaxonIdQuery = " SELECT r.RelPTaxonId " + 
					" FROM RelPTaxon AS r INNER JOIN v_cdm_exp_taxaDirect AS a ON r.PTNameFk2 = a.PTNameFk AND r.PTRefFk2 = a.PTRefFk ";
	static String nameIdTable = " v_cdm_exp_namesAll ";
	static String referenceIdTable = " v_cdm_exp_refAll ";
	static String factFilter = " factId IN ( SELECT factId FROM v_cdm_exp_factsAll )";
	static String occurrenceFilter = " occurrenceId IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )";
	static String occurrenceSourceFilter = " occurrenceFk IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )"; 
	static String commonNameFilter = " commonNameId IN ( SELECT commonNameId FROM v_cdm_exp_commonNamesAll )";
	static String webMarkerFilter = " TableNameFk <> 500 OR ( RIdentifierFk IN (SELECT RIdentifier FROM v_cdm_exp_taxaAll)) ";
	static String authorTeamFilter = null; // " authorTeamId IN (SELECT authorTeamId FROM v_cdm_exp_authorTeamsAll) ";
	static String authorFilter = null;  // " authorId IN (SELECT authorId FROM v_cdm_exp_authorsAll) "; 
	

	
// **************** ALL *********************	

	static final boolean doUser = true;
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	static final boolean doTypes = false;  //serious types do not exist in E+M
	static final boolean doNameFacts = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	static final boolean doOccurences = true;
	static final boolean doCommonNames = false; //FIXME currently common names throw errors and make later imports not work

	//etc.
	static final boolean doMarker = true;

	
// **************** SELECTED *********************

//	static final boolean doUser = true;
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
//	static final boolean doCommonNames = false;
//	
//	//etc.
//	static final boolean doMarker = false;
	
	
	public void importEm2CDM (Source source, ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		//make BerlinModel Source
				
		BerlinModelImportConfigurator config = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationUuid(classificationUuid);
		config.setSourceSecId(sourceSecId);
		
		config.setNomenclaturalCode(nomenclaturalCode);

		config.setIgnoreNull(ignoreNull);
		config.setDoAuthors(doAuthors);
		config.setDoReferences(doReferences);
		config.setDoTaxonNames(doTaxonNames);
		config.setDoRelNames(doRelNames);
		config.setDoNameStatus(doNameStatus);
		config.setDoTypes(doTypes);
		config.setDoNameFacts(doNameFacts);
		config.setUseClassification(useClassification);
		config.setSourceRefUuid(PesiTransformer.uuidSourceRefEuroMed);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoFacts(doFacts);
		config.setDoOccurrence(doOccurences);
		config.setDoCommonNames(doCommonNames);
		
		config.setDoMarker(doMarker);
		config.setDoUser(doUser);
		config.setEditor(editor);
		config.setDbSchemaValidation(hbm2dll);
		
		// maximum number of name facts to import
		config.setMaximumNumberOfNameFacts(maximumNumberOfNameFacts);
		
		config.setInfrGenericRankAbbrev(infrGenericRankAbbrev);
		config.setInfrSpecificRankAbbrev(infrSpecificRankAbbrev);
		config.setRemoveHttpMapsAnchor(removeHttpMapsAnchor);
		
//		filter
		config.setTaxonTable(taxonTable);
		config.setClassificationQuery(classificationQuery);
		config.setRelTaxaIdQuery(relPTaxonIdQuery);
		config.setNameIdTable(nameIdTable);
		config.setReferenceIdTable(referenceIdTable);
		config.setAuthorTeamFilter(authorTeamFilter);
		config.setAuthorFilter(authorFilter);
		config.setFactFilter(factFilter);
		config.setCommonNameFilter(commonNameFilter);
		config.setOccurrenceFilter(occurrenceFilter);
		config.setOccurrenceSourceFilter(occurrenceSourceFilter);
		config.setWebMarkerFilter(webMarkerFilter);
		config.setUseSingleClassification(useSingleClassification);
		
		//TDWG codes
		config.setSplitTdwgCodes(isSplitTdwgCodes);
		
		
		config.setCheck(check);
		config.setEditor(editor);
		config.setRecordsPerTransaction(partitionSize);
		
		config.setSwitchSpeciesGroup(switchSpeciesGroup);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		bmImport.invoke(config);
		
		if (doFacts && config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)    ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			
			//make feature tree
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, config.getFeatureMap(), featureKeyList);
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
		EuroMedActivator importActivator = new EuroMedActivator();
		Source source = berlinModelSource;
		ICdmDataSource cdmRepository = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		importActivator.importEm2CDM(source, cdmRepository, hbm2dll);
		if (includePesiExport){
			PesiExportActivatorEM exportActivator = new PesiExportActivatorEM();
			exportActivator.doExport(cdmRepository);
		}

	}

}
