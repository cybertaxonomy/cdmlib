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
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;


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
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final Source berlinModelSource = BerlinModelSources.euroMed_BGBM42();
//	static final Source berlinModelSource = BerlinModelSources.euroMed_PESI3();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_euroMed();
	
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_pesi_euromed();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_euromed();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static final boolean includePesiExport = false;
	
	static final int sourceSecId = 7000000; //500000
	static final UUID classificationUuid = UUID.fromString("314a68f9-8449-495a-91c2-92fde8bcf344");
	static final boolean useSingleClassification = true;
	static final String classificationName = "Euro+Med 2014";	
	static final UUID featureTreeUuid = UUID.fromString("6a5e1c2b-ec0d-46c8-9c7d-a2059267ffb7");
	static final Object[] featureKeyList = new Integer[]{1, 31, 4, 98, 41}; 	
	
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	static final int partitionSize = 2500;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	
	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICNAFP;

	//ignore null
	static final boolean ignoreNull = true;
	
	static final boolean switchSpeciesGroup = true;
	
	static boolean useClassification = true;
	
	static boolean isSplitTdwgCodes = false;
	static boolean useEmAreaVocabulary = true;
	
	private boolean removeHttpMapsAnchor = true;

	
	static final String infrGenericRankAbbrev = "[unranked]";
	static final String infrSpecificRankAbbrev = "[unranked]";
	
	
	static String taxonTable = "v_cdm_exp_taxaAll";
	static String classificationQuery = " SELECT DISTINCT t.PTRefFk, r.RefCache FROM PTaxon t INNER JOIN Reference r ON t.PTRefFk = r.RefId WHERE t.PTRefFk = " + sourceSecId; 
	static String relPTaxonIdQuery = " SELECT TOP (100) PERCENT r.RelPTaxonId " + 
					" FROM RelPTaxon AS r INNER JOIN v_cdm_exp_taxaDirect AS a ON r.PTNameFk2 = a.PTNameFk AND r.PTRefFk2 = a.PTRefFk" +
					" ORDER BY r.RelPTaxonId ";  // AND r.RelQualifierFk =1 
	static String nameIdTable = " v_cdm_exp_namesAll ";
	static String referenceIdTable = " v_cdm_exp_refAll ";
	static String refDetailFilter =  " RefDetailID IN (SELECT RefDetailID FROM v_cdm_exp_RefDetail) ";
	static String factFilter = " factId IN ( SELECT factId FROM v_cdm_exp_factsAll WHERE FactCategoryFk NOT IN (12, 14, 249, 251))";
	static String occurrenceFilter = " occurrenceId IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )";
	static String occurrenceSourceFilter = " occurrenceFk IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )"; 
	static String commonNameFilter = " commonNameId IN ( SELECT commonNameId FROM v_cdm_exp_commonNamesAll )";
	static String webMarkerFilter = " TableNameFk <> 500 OR ( RIdentifierFk IN (SELECT RIdentifier FROM v_cdm_exp_taxaAll)) ";
	static String authorTeamFilter = null; // " authorTeamId IN (SELECT authorTeamId FROM v_cdm_exp_authorTeamsAll) ";
	static String authorFilter = null;  // " authorId IN (SELECT authorId FROM v_cdm_exp_authorsAll) "; 
	

	
// **************** ALL *********************	

	boolean invers =   !(hbm2dll == DbSchemaValidation.CREATE);
	
	static final boolean doUser = true;
//	//authors
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
	static final boolean doRelTaxa = false;
	static final boolean doFacts = true;
	static final boolean doOccurences = true;
	static final boolean doCommonNames = true;

	//etc.
	static final boolean doMarker = true;

	
	public void importEm2CDM (Source source, ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		//make BerlinModel Source
				
		BerlinModelImportConfigurator config = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationName(classificationName);
		
		config.setClassificationUuid(classificationUuid);
		config.setSourceSecId(sourceSecId);
		config.setNomenclaturalCode(nomenclaturalCode);
		config.setIgnoreNull(ignoreNull);
		
		config.setDoAuthors(doAuthors ^ invers);
		config.setDoReferences(invers ? doReferences.invers() : doReferences);
		config.setDoTaxonNames(doTaxonNames ^ invers);
		config.setDoRelNames(doRelNames ^ invers);
		config.setDoNameStatus(doNameStatus ^ invers);
		config.setDoTypes(doTypes);  //always false
		config.setDoNameFacts(doNameFacts ^ invers);
		config.setDoTaxa(doTaxa ^ invers);
		config.setDoRelTaxa(doRelTaxa ^ invers);
		config.setDoFacts(doFacts ^ invers);
		config.setDoOccurrence(doOccurences ^ invers);
		config.setDoCommonNames(doCommonNames ^ invers);
		
		config.setDoMarker(doMarker ^ invers);
		config.setDoUser(doUser ^ invers);
		
		config.setUseClassification(useClassification);
		config.setSourceRefUuid(BerlinModelTransformer.uuidSourceRefEuroMed);
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
		config.setRefDetailFilter(refDetailFilter);
		config.setCommonNameFilter(commonNameFilter);
		config.setOccurrenceFilter(occurrenceFilter);
		config.setOccurrenceSourceFilter(occurrenceSourceFilter);
		config.setWebMarkerFilter(webMarkerFilter);
		config.setUseSingleClassification(useSingleClassification);
		
		//TDWG codes
		config.setSplitTdwgCodes(isSplitTdwgCodes);
		config.setUseEmAreaVocabulary(useEmAreaVocabulary);
		
		config.setCheck(check);
		config.setEditor(editor);
		config.setRecordsPerTransaction(partitionSize);
		
		config.setSwitchSpeciesGroup(switchSpeciesGroup);
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		bmImport.invoke(config);
		
		if (config.isDoTaxonNames() && (config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)  )  ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			TransactionStatus tx = app.startTransaction();
			try {
				Rank sectBot = (Rank)app.getTermService().find(Rank.SECTION_BOTANY().getUuid());
				Representation repr = sectBot.getRepresentation(Language.ENGLISH());
				repr.setAbbreviatedLabel(repr.getAbbreviatedLabel().replace("(bot.)", "").trim());
				repr.setLabel(repr.getLabel().replace("(Botany)", "").trim());
				sectBot.setTitleCache(null, false);  //to definetely update the titleCache also
				app.getTermService().saveOrUpdate(sectBot);
	
				Rank subSectBot = (Rank)app.getTermService().find(Rank.SECTION_BOTANY().getUuid());
				repr = subSectBot.getRepresentation(Language.ENGLISH());
				repr.setAbbreviatedLabel(repr.getAbbreviatedLabel().replace("(bot.)", "").trim());
				repr.setLabel(repr.getLabel().replace("(Botany)", "").trim());
				subSectBot.setTitleCache(null, false);  //to definetely update the titleCache also
				app.getTermService().saveOrUpdate(subSectBot);
				app.commitTransaction(tx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (config.isDoFacts() && (config.getCheck().equals(CHECK.CHECK_AND_IMPORT)  || config.getCheck().equals(CHECK.IMPORT_WITHOUT_CHECK)  )  ){
			ICdmApplicationConfiguration app = bmImport.getCdmAppController();
			TransactionStatus tx = app.startTransaction();
			
			//make feature tree
			FeatureTree tree = TreeCreator.flatTree(featureTreeUuid, config.getFeatureMap(), featureKeyList);
			FeatureNode imageNode = FeatureNode.NewInstance(Feature.IMAGE());
			tree.getRoot().addChild(imageNode);
			FeatureNode distributionNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
			tree.getRoot().addChild(distributionNode, 1); 
			FeatureNode commonNameNode = FeatureNode.NewInstance(Feature.COMMON_NAME());
			tree.getRoot().addChild(commonNameNode, 2); 
			app.getFeatureTreeService().saveOrUpdate(tree);
			
			//Change common name label
			DefinedTermBase<?> commonNameFeature = app.getTermService().find(Feature.COMMON_NAME().getUuid());
			commonNameFeature.setLabel("Common Names", Language.ENGLISH());
			commonNameFeature.setTitleCache(null, false);  //to definetely update the titleCache also
			app.getTermService().saveOrUpdate(commonNameFeature);
			app.commitTransaction(tx);
		}
		
		if (config.isDoRelTaxa()){
			
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
			//not available from here since E+M was moved to app-import
//			PesiExportActivatorEM exportActivator = new PesiExportActivatorEM();
//			exportActivator.doExport(cdmRepository);
		}

	}

}
