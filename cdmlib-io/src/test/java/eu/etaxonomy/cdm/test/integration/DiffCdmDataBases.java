/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.test.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.test.function.TestJaxb;

/**
 * @author a.babadshanjan
 * @created 15.09.2008
 */
public class DiffCdmDataBases {
	
    private final int MAX_ROWS = 50000;
    private final int MAX_TABLES = 150;

    private static final Logger logger = Logger.getLogger(DiffCdmDataBases.class);
	
	private static final String loadFromDbOne = "cdm_test_jaxb";
	private static final String loadFromDbTwo = "cdm_test_jaxb2";
	
	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	private static final String[] tables = {
			"Address",
			"Agent",
			"Agent_Agent",
			"Agent_Annotation",
			"Agent_DefinedTermBase", 
			"Agent_Extension", 
			"Agent_InstitutionalMembership", 
			"Agent_Marker", 
			"Agent_Media", 
			"Agent_OriginalSource", 
			"Agent_Rights", 
			"Annotation", 
			"CDM_VIEW", 
			"CDM_VIEW_CDM_VIEW", 
			"Collection", 
			"Collection_Annotation", 
			"Collection_Extension", 
			"Collection_Marker", 
			"Collection_Media", 
			"Collection_OriginalSource", 
			"Collection_Rights", 
			"Contact", 
			"DefinedTermBase", 
			"DefinedTermBase_DefinedTermBase",
			"DefinedTermBase_Media", 
			"DefinedTermBase_Representation", 
			"DefinedTermBase_TermVocabulary", 
			"DerivationEvent", 
			"DerivationEvent_Annotation", 
			"DerivationEvent_Marker", 
			"DescriptionBase", 
			"DescriptionBase_Annotation", 
			"DescriptionBase_DefinedTermBase",
			"DescriptionBase_DescriptionElementBase",
			"DescriptionBase_Extension", 
			"DescriptionBase_Marker", 
			"DescriptionBase_OriginalSource", 
			"DescriptionBase_Rights", 
			"DescriptionElementBase", 
			"DescriptionElementBase_Annotation",
			"DescriptionElementBase_DefinedTermBase",
			"DescriptionElementBase_LanguageString",
			"DescriptionElementBase_Marker", 
			"DescriptionElementBase_Media", 
			"DescriptionElementBase_StatisticalMeasurementValue",
			"DeterminationEvent", 
			"DeterminationEvent_Annotation", 
			"DeterminationEvent_Marker", 
			"Extension", 
			"FeatureNode", 
			"FeatureTree", 
			"FeatureTree_Representation", 
			"GatheringEvent", 
			"GatheringEvent_Annotation", 
			"GatheringEvent_Marker", 
			"GenBankAccession", 
			"HomotypicalGroup", 
			"HomotypicalGroup_Annotation", 
			"HomotypicalGroup_Marker", 
			"HybridRelationship", 
			"HybridRelationship_Annotation", 
			"HybridRelationship_Marker", 
			"InstitutionalMembership", 
			"LanguageString", 
			"Locus", 
			"Marker", 
			"Media", 
			"MediaRepresentation", 
			"MediaRepresentationPart", 
			"Media_Annotation", 
			"Media_Marker", 
			"Media_Rights", 
			"Media_Sequence", 
			"Media_TaxonBase", 
			"NameRelationship", 
			"NameRelationship_Annotation", 
			"NameRelationship_Marker", 
			"NomenclaturalStatus", 
			"NomenclaturalStatus_Annotation", 
			"NomenclaturalStatus_Marker", 
			"OriginalSource", 
			"OriginalSource_Annotation", 
			"OriginalSource_Marker", 
			"Person_Keyword", 
			"ReferenceBase", 
			"ReferenceBase_Annotation", 
			"ReferenceBase_Extension", 
			"ReferenceBase_Marker", 
			"ReferenceBase_Media", 
			"ReferenceBase_OriginalSource", 
			"ReferenceBase_Rights", 
			"RelationshipTermBase_inverseRepresentation",
			"Representation",
			"Rights",
			"Sequence",
			"Sequence_Annotation", 
			"Sequence_Extension", 
			"Sequence_GenBankAccession", 
			"Sequence_Marker", 
			"Sequence_Media", 
			"Sequence_OriginalSource", 
			"Sequence_ReferenceBase", 
			"Sequence_Rights", 
			"SpecimenOrObservationBase", 
			"SpecimenOrObservationBase_Annotation",
			"SpecimenOrObservationBase_DerivationEvent",
			"SpecimenOrObservationBase_Extension",
			"SpecimenOrObservationBase_Marker",
			"SpecimenOrObservationBase_Media",
			"SpecimenOrObservationBase_OriginalSource",
			"SpecimenOrObservationBase_Rights",
			"SpecimenOrObservationBase_Sequence",
			"StateData",
			"StateData_DefinedTermBase", 
			"StatisticalMeasurementValue", 
			"StatisticalMeasurementValue_DefinedTermBase",
			"SynonymRelationship", 
			"SynonymRelationship_Annotation", 
			"SynonymRelationship_Marker", 
			"TaxonBase", 
			"TaxonBase_Annotation", 
			"TaxonBase_Extension", 
			"TaxonBase_Marker", 
			"TaxonBase_OriginalSource", 
			"TaxonBase_Rights", 
			"TaxonNameBase", 
			"TaxonNameBase_Annotation", 
			"TaxonNameBase_Extension", 
			"TaxonNameBase_HybridRelationship",
			"TaxonNameBase_Marker", 
			"TaxonNameBase_NomenclaturalStatus",
			"TaxonNameBase_OriginalSource", 
			"TaxonNameBase_Rights", 
			"TaxonNameBase_TypeDesignationBase",
			"TaxonRelationship", 
			"TaxonRelationship_Annotation", 
			"TaxonRelationship_Marker", 
			"TermVocabulary", 
			"TermVocabulary_Representation", 
			"TypeDesignationBase", 
			"TypeDesignationBase_Annotation", 
			"TypeDesignationBase_Marker", 
			"TypeDesignationBase_TaxonNameBase"
	};

	public Map<String, List<String>> doLoadDataFromDb(String dbname) {
    	
		Map<String, List<String>> dbTables = new HashMap<String, List<String>>();
		
		logger.info("Loading data from DB " + dbname);

		CdmApplicationController appCtr = null;

    	try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
    		
    		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
    		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
    		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);

    	} catch (DataSourceNotFoundException e) {
    		logger.error("datasource error");
    	} catch (TermNotFoundException e) {
    		logger.error("defined terms not found");
    	}
    	
    	TransactionStatus txStatus = appCtr.startTransaction(true);

    	// get data from DB

    	try {

    		dbTables = retrieveAllTables(appCtr);
    		
    	} catch (Exception e) {
    		logger.error("error setting data");
    		e.printStackTrace();
    	}
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    	return dbTables;
    	
    }
	
    private Map<String, List<String>> retrieveAllTables(CdmApplicationController appCtr) {
		
		Map<String, List<String>> tables = new HashMap<String, List<String>>(MAX_TABLES);
		
		List<String> agentTableContent = new ArrayList<String>(MAX_ROWS);
		List<Agent> agents = appCtr.getAgentService().getAllAgents(MAX_ROWS, 0);
		for (Agent agent: agents ) {
			//TODO: Want the entire row as string not just toString() of the object.
			agentTableContent.add(agent.toString());
		}
		tables.put("agents", agentTableContent);

		//List<Annotation> annotations = appCtr.getTermService().getAllAnnotations(MAX_ROWS, 0);
		
		List<String> definedTermBaseTableContent = new ArrayList<String>(MAX_ROWS);
		List<DefinedTermBase> definedTermBases = appCtr.getTermService().getAllDefinedTerms(MAX_ROWS, 0);
		for (DefinedTermBase definedTermBase: definedTermBases ) {
			definedTermBaseTableContent.add(definedTermBase.toString());
		}
		tables.put("definedTermBases", definedTermBaseTableContent);

		//List<DescriptionBase> descriptionBases = appCtr.getDescriptionService().getAllDescriptionBases(MAX_ROWS, 0);
		//List<DescriptionElementBase> descriptionElementBases = appCtr.getDescriptionService().getAllDescriptionElementBases(MAX_ROWS, 0);
		//List<HomotypicalGroup> homotypicalGroups = appCtr.getNameService().getAllHomotypicalGroups(MAX_ROWS, 0);
		List<LanguageString> languageStrings = appCtr.getTermService().getAllLanguageStrings(MAX_ROWS, 0);
		//List<Marker> markers = appCtr.getTermService().getAllMarkers(MAX_ROWS, 0);
		//List<NameRelationship> nameRelationships = appCtr.getNameService().getAllNameRelationships(MAX_ROWS, 0);
		List<NomenclaturalStatus> nomenclaturalStatus = appCtr.getNameService().getAllNomenclaturalStatus(MAX_ROWS, 0);
		//List<OriginalSource> originalSources = appCtr.getNameService().getAllOriginalSources(MAX_ROWS, 0);
		List<ReferenceBase> referenceBases = appCtr.getReferenceService().getAllReferences(MAX_ROWS, 0);
		List<Representation> representations = appCtr.getTermService().getAllRepresentations(MAX_ROWS, 0);
		List<SpecimenOrObservationBase> specimenOrObservationBases = appCtr.getOccurrenceService().getAllSpecimenOrObservationBases(MAX_ROWS, 0);
		//List<SynonymRelationship> synonymRelationships = appCtr.getTaxonService().getAllSynonymRelationships(MAX_ROWS, 0);
//		List<TaxonBase> taxonBases = appCtr.getTaxonService().getAllTaxa(MAX_ROWS, 0);
//		List<TaxonNameBase> taxonNameBases = appCtr.getNameService().getAllNames(MAX_ROWS, 0);
		//List<TaxonRelationship> taxonRelationships = appCtr.getTaxonService().getAllTaxonRelationships(MAX_ROWS, 0);
		List<TermVocabulary<DefinedTermBase>> termVocabularies = appCtr.getTermService().getAllTermVocabularies(MAX_ROWS, 0);
		List<TypeDesignationBase> typeDesignationBases = appCtr.getNameService().getAllTypeDesignations(MAX_ROWS, 0);
		
		return tables;
	}
	
    public void doCompareDatabases(Map<String, List<String>> tablesDbOne, Map<String, List<String>> tablesDbTwo) {
		
		logger.debug("# Tables in DB 1: " + tablesDbOne.size());
		logger.debug("# Tables in DB 2: " + tablesDbTwo.size());
		
		for (String tableName: tablesDbOne.keySet()) {

			logger.info("Comparing table '" + tableName + "'");
			
			List<String> dbOneTableRows = new ArrayList<String>();
			List<String> dbTwoTableRows = new ArrayList<String>();
			
			dbOneTableRows = tablesDbOne.get(tableName);
			dbTwoTableRows = tablesDbTwo.get(tableName);
			
			Collections.sort(dbOneTableRows);
			Collections.sort(dbTwoTableRows);

			int different = 0;
			int tableSize = dbOneTableRows.size();
			
			for (int i = 0; i < tableSize; i++) {

				String str1 = dbOneTableRows.get(i);
				String str2 = dbTwoTableRows.get(i);

				if (str1.equals(str2) != true) {

					different++;
					logger.debug("Rows differ:");
					logger.debug("Table 1 Row = " + str1); 
					logger.debug("Table 2 Row = " + str2); 

				}
				i++;
			}
			if (different > 0) {
				logger.info("Compared table '" + tableName + "':");
				logger.info("# Rows total: " + tableSize);
				logger.info("# Rows identical: " + (tableSize - different)); 
				logger.warn("# Rows different: " + different); 
			}
		}
		logger.info("End database comparison"); 
	}

	private void test(){
		
		Map<String, List<String>> tablesDbOne = doLoadDataFromDb(loadFromDbOne);
		Map<String, List<String>> tablesDbTwo = doLoadDataFromDb(loadFromDbTwo);
	    doCompareDatabases(tablesDbOne, tablesDbTwo);

	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		DiffCdmDataBases diff = new DiffCdmDataBases();
    	diff.test();
	}
}
