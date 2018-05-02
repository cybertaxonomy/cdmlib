/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.test.integration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.babadshanjan
 * @since 15.09.2008
 */
public class TestCdmDbComparator {

	private static final String sourceDbOne = "cdm_test_jaxb";
	private static final String sourceDbTwo = "cdm_test_jaxb2";

	private static final ICdmDataSource sourceOne = TestCdmDbComparator.CDM_DB(sourceDbOne);
	private static final ICdmDataSource sourceTwo = TestCdmDbComparator.CDM_DB(sourceDbTwo);

	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	public static ICdmDataSource CDM_DB(String dbname) {

	String password = AccountStore.readOrStorePassword(dbname, server, username, null);
	ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
	return datasource;
	}

    private final int MAX_ROWS = 60000;
    private final int MAX_TABLES = 150;

    private static final Logger logger = Logger.getLogger(TestCdmDbComparator.class);

	private static final String[] table_list = {
//			"Address",
			"Agent",
//			"Agent_Agent",
//			"Agent_Annotation",
//			"Agent_DefinedTermBase",
//			"Agent_Extension",
//			"Agent_InstitutionalMembership",
//			"Agent_Marker",
//			"Agent_Media",
//			"Agent_OriginalSource",
//			"Agent_Rights",
			"Annotation",
//			"CDM_VIEW",
//			"CDM_VIEW_CDM_VIEW",
//			"Collection",
//			"Collection_Annotation",
//			"Collection_Extension",
//			"Collection_Marker",
//			"Collection_Media",
//			"Collection_OriginalSource",
//			"Collection_Rights",
//			"Contact",
			"DefinedTermBase",
//			"DefinedTermBase_DefinedTermBase",
//			"DefinedTermBase_Media",
//			"DefinedTermBase_Representation",
//			"DefinedTermBase_TermVocabulary",
//			"DerivationEvent",
//			"DerivationEvent_Annotation",
//			"DerivationEvent_Marker",
			"DescriptionBase",
//			"DescriptionBase_Annotation",
//			"DescriptionBase_DefinedTermBase",
//			"DescriptionBase_DescriptionElementBase",
//			"DescriptionBase_Extension",
//			"DescriptionBase_Marker",
//			"DescriptionBase_OriginalSource",
//			"DescriptionBase_Rights",
			"DescriptionElementBase",
//			"DescriptionElementBase_Annotation",
//			"DescriptionElementBase_DefinedTermBase",
//			"DescriptionElementBase_LanguageString",
//			"DescriptionElementBase_Marker",
//			"DescriptionElementBase_Media",
//			"DescriptionElementBase_StatisticalMeasurementValue",
//			"DeterminationEvent",
//			"DeterminationEvent_Annotation",
//			"DeterminationEvent_Marker",
//			"Extension",
			"FeatureNode",
			"FeatureTree",
//			"FeatureTree_Representation",
//			"GatheringEvent",
//			"GatheringEvent_Annotation",
//			"GatheringEvent_Marker",
			"HomotypicalGroup",
//			"HomotypicalGroup_Annotation",
//			"HomotypicalGroup_Marker",
//			"HybridRelationship",
//			"HybridRelationship_Annotation",
//			"HybridRelationship_Marker",
//			"InstitutionalMembership",
			"LanguageString",
//			"Locus",
			"Marker",
			"Media",
			"MediaRepresentation",
			"MediaRepresentationPart",
//			"Media_Annotation",
//			"Media_Marker",
//			"Media_Rights",
//			"Media_Sequence",
//			"MediaKey_CoveredTaxon",
			"NameRelationship",
//			"NameRelationship_Annotation",
//			"NameRelationship_Marker",
			"NomenclaturalStatus",
//			"NomenclaturalStatus_Annotation",
//			"NomenclaturalStatus_Marker",
			"OriginalSource",
//			"OriginalSource_Annotation",
//			"OriginalSource_Marker",
//			"Person_Keyword",
			"Reference",
//			"ReferenceBase_Annotation",
//			"ReferenceBase_Extension",
//			"ReferenceBase_Marker",
//			"ReferenceBase_Media",
//			"ReferenceBase_OriginalSource",
//			"ReferenceBase_Rights",
//			"RelationshipTermBase_inverseRepresentation",
			"Representation",
//			"Rights",
//			"Sequence",
//			"Sequence_Annotation",
//			"Sequence_Extension",
//			"Sequence_Marker",
//			"Sequence_Media",
//			"Sequence_OriginalSource",
//			"Sequence_ReferenceBase",
//			"Sequence_Rights",
			"SpecimenOrObservationBase",
//			"SpecimenOrObservationBase_Annotation",
//			"SpecimenOrObservationBase_DerivationEvent",
//			"SpecimenOrObservationBase_Extension",
//			"SpecimenOrObservationBase_Marker",
//			"SpecimenOrObservationBase_Media",
//			"SpecimenOrObservationBase_OriginalSource",
//			"SpecimenOrObservationBase_Rights",
//			"StateData",
//			"StateData_DefinedTermBase",
//			"StatisticalMeasurementValue",
//			"StatisticalMeasurementValue_DefinedTermBase",
			"TaxonBase",
//			"TaxonBase_Annotation",
//			"TaxonBase_Extension",
//			"TaxonBase_Marker",
//			"TaxonBase_OriginalSource",
//			"TaxonBase_Rights",
			"TaxonName",
//			"TaxonName_Annotation",
//			"TaxonName_Extension",
//			"TaxonName_HybridRelationship",
//			"TaxonName_Marker",
//			"TaxonName_NomenclaturalStatus",
//			"TaxonName_OriginalSource",
//			"TaxonName_Rights",
//			"TaxonName_TypeDesignationBase",
			"TaxonRelationship",
//			"TaxonRelationship_Annotation",
//			"TaxonRelationship_Marker",
			"TermVocabulary",
//			"TermVocabulary_Representation",
			"TypeDesignationBase",
//			"TypeDesignationBase_Annotation",
//			"TypeDesignationBase_Marker",
	};

//	@Autowired
//	@Qualifier("cdmDao")
//	private ICdmEntityDao cdmDao;

//	@Autowired
//	protected void setDao(ICdmEntityDao dao) {
//		logger.debug("setting DAO");
//		this.cdmDao = dao;
//	}

//	@Autowired
//	private SessionFactory factory;
//
//	protected Session getSession(){
//	Session session = factory.getCurrentSession();
//	return session;
//}

//    public ResultSet getResultSet (String query){
//    	ResultSet rs;
//    	try {
//            this.getConnection(); //establish connection
//        	if (query == null){
//        		return null;
//        	}
//            mStmt = mConn.createStatement();
//            rs = mStmt.executeQuery(query);
//            return rs;
//        }catch(SQLException e){
//            logger.error("Problems when creating Resultset for query \n  " + query + " \n" + "Exception: " + e);
//            return null;
//        }
//    }

	private Map<String, List<String>> doLoadDataFromDb(String dbname, Source source) {

		Map<String, List<String>> dbTables = new HashMap<String, List<String>>();

		logger.info("Loading data from DB " + dbname);

		CdmApplicationController appCtr = null;


		String password = AccountStore.readOrStorePassword(dbname, server, username, null);

		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);


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

	private Map<String, List<CdmBase>> doLoadDataFromDb_(String dbname) {

		Map<String, List<CdmBase>> dbTables = new HashMap<String, List<CdmBase>>();

		logger.info("Loading data from DB " + dbname);

		CdmApplicationController appCtr = null;


		String password = AccountStore.readOrStorePassword(dbname, server, username, null);

		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);


    	TransactionStatus txStatus = appCtr.startTransaction(true);

    	// get data from DB

    	try {

    		dbTables = retrieveAllTables_(appCtr);

    	} catch (Exception e) {
    		logger.error("error setting data");
    		e.printStackTrace();
    	}
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();

    	return dbTables;

    }

	private Map<String, List<String>> doLoadDataFromDb__(String dbname, Source source) {

		Map<String, List<String>> dbTables = new HashMap<String, List<String>>();

		logger.info("Loading data from DB " + dbname);

    	try {

    		dbTables = retrieveAllTables__(source);

    	} catch (Exception e) {
    		logger.error("error setting data");
    		e.printStackTrace();
    	}
    	return dbTables;

    }

    private Map<String, List<CdmBase>> retrieveAllTables_(CdmApplicationController appCtr) {

		Map<String, List<CdmBase>> tables_ = new HashMap<String, List<CdmBase>>(table_list.length);

		List<String> tableRows = new ArrayList<String>(MAX_ROWS);

		//List<Agent> agents = appCtr.getAgentService().getAllAgents(MAX_ROWS, 0);

		try {
			//get data from database
			for (int i = 0; i < table_list.length; i++) {

	    		logger.debug("Retrieving table '" + table_list[i] + "'");
	    		System.out.println("Retrieving table '" + table_list[i] + "'");

				List<CdmBase> rows = new ArrayList<CdmBase>(MAX_ROWS);

				rows = appCtr.getMainService().rows(table_list[i], MAX_ROWS, 0);

    			tables_.put(table_list[i], rows);

			}

		} catch (Exception e) {
    		logger.error("error retrieving data");
    		e.printStackTrace();
		}
//		return tables;
		return tables_;
    }

    private Map<String, List<String>> retrieveAllTables___(Source source) {

		Map<String, List<String>> tables = new HashMap<String, List<String>>(table_list.length);
		List<String> tableRows = new ArrayList<String>(MAX_ROWS);

		try {
			//get data from database
			for (int i = 0; i < table_list.length; i++) {

				List<String> rows = new ArrayList<String>(MAX_ROWS);

//					Session session = factory.getCurrentSession();

//					if ( sessionObject != null ) {
//						session.update(sessionObject);
//					}
//					Query query = session.createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
//					query.setParameter("label", queryString);

//					Query query = session.createQuery("select from " + table_list[i]);
//				    rows = query.list();

				//FIXME: NullPointerException (cdmDao is null)
				    //rows = cdmDao.list(MAX_ROWS, 0);
					tables.put(table_list[i], rows);

				}

		} catch (Exception e) {
    		logger.error("error retrieving data");
    		e.printStackTrace();
		}
		return tables;
    }

    private Map<String, List<String>> retrieveAllTables__(Source source) {

//		IImportConfigurator config = new BerlinModelImportConfigurator;
//		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
//		Source source = bmiConfig.getSource();
//		ResultSet rs = berlinModelSource.getResultSet();

		Map<String, List<String>> tables = new HashMap<String, List<String>>(table_list.length);
		List<String> tableRows = new ArrayList<String>(MAX_ROWS);

		try {
			//get data from database
			for (int i = 0; i < table_list.length; i++) {
				String strQuery =
					" SELECT * FROM " + table_list[i];
				logger.debug("SQL Statement: " +  strQuery);
				//ResultSet rs = berlinModelSource.getResultSet();
				//ResultSet rs = source.getResultSet(strQuery) ;
				ResultSet rs = source.getResultSet(strQuery) ;
				List<String> rows = new ArrayList<String>(MAX_ROWS);

				while (rs.next()) {
					rows.add(rs.toString());
				}
				tables.put(table_list[i], rows);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
		}
		return tables;
    }

    private Map<String, List<String>> retrieveAllTables(CdmApplicationController appCtr) {

		Map<String, List<String>> tables = new HashMap<String, List<String>>(table_list.length);

		List<String> agentTableContent = new ArrayList<String>(MAX_ROWS);
		List<? extends AgentBase> agents = appCtr.getAgentService().list(null,MAX_ROWS, 0,null,null);
		for (AgentBase agent: agents ) {
			//TODO: Want the entire row as string not just toString() of the object.
			agentTableContent.add(agent.toString());
		}
		tables.put("agents", agentTableContent);

		//List<Annotation> annotations = appCtr.getTermService().getAllAnnotations(MAX_ROWS, 0);

		List<String> definedTermBaseTableContent = new ArrayList<String>(MAX_ROWS);
		List<DefinedTermBase> definedTermBases = appCtr.getTermService().list(null,MAX_ROWS, 0,null,null);
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
		List<Reference> references = appCtr.getReferenceService().list(null,MAX_ROWS, 0,null,null);
		List<Representation> representations = appCtr.getTermService().getAllRepresentations(MAX_ROWS, 0);
		List<SpecimenOrObservationBase> specimenOrObservationBases = appCtr.getOccurrenceService().list(null,MAX_ROWS, 0,null,null);
//		List<TaxonBase> taxonBases = appCtr.getTaxonService().getAllTaxa(MAX_ROWS, 0);
//		List<TaxonName> taxonNames = appCtr.getNameService().getAllNames(MAX_ROWS, 0);
		//List<TaxonRelationship> taxonRelationships = appCtr.getTaxonService().getAllTaxonRelationships(MAX_ROWS, 0);
		List<TermVocabulary> termVocabularies = appCtr.getVocabularyService().list(null,MAX_ROWS, 0,null,null);
		List<TypeDesignationBase> typeDesignationBases = appCtr.getNameService().getAllTypeDesignations(MAX_ROWS, 0);

		return tables;
	}

     private void compareTables(String tableName, List<CdmBase> tablesDbOne, List<CdmBase> tablesDbTwo) {

		int tableOneSize = tablesDbOne.size();
		int tableTwoSize = tablesDbTwo.size();
		int tableMinSize = 0;
		int tableMaxSize = 0;

		if (tableOneSize != tableTwoSize) {
			logger.warn("Table '" + tableName + "', Rows differ: " + tablesDbOne.size() + ", " + tablesDbTwo.size());
            tableMinSize = Math.min(tableOneSize, tableTwoSize);
            tableMaxSize = Math.max(tableOneSize, tableTwoSize);
		} else {
			logger.info("Table '" + tableName + "': " + tablesDbOne.size());
		}

		int different = 0;

		try {
		for (int i = 0; i < tableMinSize; i++) {

			CdmBase obj1 = tablesDbOne.get(i);
			CdmBase obj2 = tablesDbTwo.get(i);

			// This compares only whether both tables contain the same objects.
			// It doesn't check whether all field values are the same.
			logger.debug("Row # " + i + ":");
			if (obj1.equals(obj2) != true) {
				different++;
				logger.debug("Table 1 = " + obj1);
				logger.debug("Table 2 = " + obj2);
			} else {
				logger.debug("Entry = " + obj1);
			}
		}
		if (different > 0) {
			logger.info("# Rows identical: " + (tableMaxSize - different));
			logger.warn("# Rows different: " + different);
		}
		} catch (org.hibernate.LazyInitializationException e){
			logger.error("LazyInitializationException");
		}
	}

    	private void doCompareDatabases(Map<String, List<CdmBase>> tablesDbOne, Map<String, List<CdmBase>> tablesDbTwo) {
//        public void doCompareDatabases(Map<String, List<String>> tablesDbOne, Map<String, List<String>> tablesDbTwo) {

		logger.debug("# Tables in DB 1: " + tablesDbOne.size());
		logger.debug("# Tables in DB 2: " + tablesDbTwo.size());

		for (String tableName: tablesDbOne.keySet()) {

			logger.info("Comparing table '" + tableName + "'");

//			List<String> dbOneTableRows = new ArrayList<String>();
//			List<String> dbTwoTableRows = new ArrayList<String>();
			List<CdmBase> dbOneTableRows = new ArrayList<CdmBase>();
			List<CdmBase> dbTwoTableRows = new ArrayList<CdmBase>();

			dbOneTableRows = tablesDbOne.get(tableName);
			dbTwoTableRows = tablesDbTwo.get(tableName);

//			Collections.sort(dbOneTableRows);
//			Collections.sort(dbTwoTableRows);

			int different = 0;
			int tableSize = dbOneTableRows.size();

			for (int i = 0; i < tableSize; i++) {

//				String str1 = dbOneTableRows.get(i);
//				String str2 = dbTwoTableRows.get(i);
				CdmBase str1 = dbOneTableRows.get(i);
				CdmBase str2 = dbTwoTableRows.get(i);

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

//	private void test_(){
//
//		Map<String, List<CdmBase>> tablesDbOne = doLoadDataFromDb_(sourceDbOne);
//		Map<String, List<CdmBase>> tablesDbTwo = doLoadDataFromDb_(sourceDbTwo);
//	    doCompareDatabases(tablesDbOne, tablesDbTwo);
//
//	}

	private void test(){

		CdmApplicationController appCtrOne = null;
		CdmApplicationController appCtrTwo = null;
		logger.info("Comparing '" + sourceDbOne + "' and '" + sourceDbTwo + "'");

		try {
			appCtrOne = CdmApplicationController.NewInstance(sourceOne, DbSchemaValidation.VALIDATE, true);
			appCtrTwo = CdmApplicationController.NewInstance(sourceTwo, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			//get data from database
	    	TransactionStatus txStatOne = appCtrOne.startTransaction(true);
	    	TransactionStatus txStatTwo = appCtrTwo.startTransaction(true);
			for (int i = 0; i < table_list.length; i++) {

				List<CdmBase> rowsDbOne = new ArrayList<CdmBase>(MAX_ROWS);
				List<CdmBase> rowsDbTwo = new ArrayList<CdmBase>(MAX_ROWS);
				rowsDbOne = appCtrOne.getMainService().rows(table_list[i], MAX_ROWS, 0);
				rowsDbTwo = appCtrTwo.getMainService().rows(table_list[i], MAX_ROWS, 0);
				compareTables(table_list[i], rowsDbOne, rowsDbTwo);
			}
	    	appCtrTwo.commitTransaction(txStatTwo);
	    	appCtrOne.commitTransaction(txStatOne);
	    	//java.lang.IllegalStateException: Cannot deactivate transaction synchronization - not active
	    	appCtrOne.close();
	    	appCtrTwo.close();
			logger.info("End database comparison");

		} catch (Exception e) {
    		logger.error("Error retrieving or comparing data");
    		e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestCdmDbComparator diff = new TestCdmDbComparator();
    	diff.test();
	}
}
