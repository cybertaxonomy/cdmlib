/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.jaxb;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.jaxb.CdmExporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
 
/**
 * @author a.babadshanjan
 * @created 25.09.2008
 */
public class CdmExportActivator {
	
	private static final Logger logger = Logger.getLogger(CdmExportActivator.class);
	
    /* SerializeFrom DB **/
	private static final String sourceDbName = "cdm_test_jaxb";
	private static final String destinationDbName = "cdm_test_jaxb2";
	
	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;
	
	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}
	
	private static final ICdmDataSource sourceDb = CdmExportActivator.CDM_DB(sourceDbName);
	private static final ICdmDataSource destinationDb = CdmExportActivator.CDM_DB(destinationDbName);
	
	private static boolean doAgents = true;
	private static boolean doAgentData = true;
	private static boolean doLanguageData = true;
	private static boolean doFeatureData = true;
	private static boolean doDescriptions = true;
	private static boolean doMedia = true;
	private static boolean doOccurrences = true;
	private static boolean doReferences = true;
	private static boolean doReferencedEntities = true;
	private static boolean doRelationships = true;
	private static boolean doSynonyms = true;
	private static boolean doTaxonNames = true;
	private static boolean doTaxa = true;
	private static boolean doTerms = true;
	private static boolean doTermVocabularies = true;
	private static boolean doHomotypicalGroups = true;
	
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private JaxbExportImportConfigurator expImpConfigurator = null;

    public CdmExportActivator() {	

		expImpConfigurator = new JaxbExportImportConfigurator();
		
		expImpConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);
		
		expImpConfigurator.setDoAgents(doAgents);
		expImpConfigurator.setDoAgentData(doAgentData);
		expImpConfigurator.setDoLanguageData(doLanguageData);
		expImpConfigurator.setDoFeatureData(doFeatureData);
		expImpConfigurator.setDoDescriptions(doDescriptions);
		expImpConfigurator.setDoMedia(doMedia);
		expImpConfigurator.setDoOccurrences(doOccurrences);
		expImpConfigurator.setDoReferences(doReferences);
		expImpConfigurator.setDoReferencedEntities(doReferencedEntities);
		expImpConfigurator.setDoRelationships(doRelationships);
		expImpConfigurator.setDoSynonyms(doSynonyms);
		expImpConfigurator.setDoTaxonNames(doTaxonNames);
		expImpConfigurator.setDoTaxa(doTaxa);
		expImpConfigurator.setDoTerms(doTerms);
		expImpConfigurator.setDoTermVocabularies(doTermVocabularies);
		expImpConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);
		
		expImpConfigurator.setCdmSource(sourceDb);
		expImpConfigurator.setCdmDestination(destinationDb);

   }

    
    private void invoke(){

    	CdmExporter cdmExporter = new CdmExporter();

    	/* ********* INIT ****************************/
    	
    	// Init source DB
		CdmApplicationController appCtrInit = null;
		
		// initDb(ICdmDataSource db, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading)
		appCtrInit = TestDatabase.initDb(sourceDb, DbSchemaValidation.CREATE, false);

		// Load some test data to source DB
		TestDatabase.loadTestData(sourceDbName, appCtrInit);
    	
//    	testMakeTaxonSynonym(appCtrInit);
//    	testRemoveNameRelationship(appCtrInit);
    	
    	// Init destination DB
//    	expImpConfigurator.setCdmDestSchemaValidation(DbSchemaValidation.CREATE);
//		CdmApplicationController appCtr = 
//			expImpConfigurator.getDestinationAppController(expImpConfigurator.getCdmDestination(), true);

    	/* ********* SERIALIZE ***********************/
    	
    	// Set DbSchemaValidation
    	//expImpConfigurator.setCdmSourceSchemaValidation(DbSchemaValidation.VALIDATE);
    	
    	// Retrieve taxa, synonyms, and relationships through traversing the taxonomic tree.
    	//cdmExporter.doSerializeTaxonTree(expImpConfigurator, marshOutOne);

    	// Retrieve data, including taxa, synonyms, and relationships via services.
     	cdmExporter.doSerialize(expImpConfigurator, marshOutOne);

    	/* ********* DESERIALIZE *********************/
    	
     	cdmExporter.doDeserialize(expImpConfigurator, marshOutOne);

    }

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CdmExportActivator sc = new CdmExportActivator();
    	sc.invoke();
	}

	
    // TODO: move to cdmlib-services: cdm.test.integration
	private void testMakeTaxonSynonym(CdmApplicationController appCtr) {
		
		logger.info("Testing makeTaxonSynonym()");
		TransactionStatus txStatus = appCtr.startTransaction();
		
		Taxon oldTaxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("83a87f0c-e2c4-4b41-b603-4e77e7e53158"));
		Taxon newAcceptedTaxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("0b423190-fcca-4228-86a9-77974477f160"));
		SynonymRelationshipType synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		
		ReferenceBase citation;
		citation = Book.NewInstance();
		Agent linne = appCtr.getAgentService().getAgentByUuid(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
		citation.setAuthorTeam((TeamOrPersonBase)linne);
		citation.setTitleCache("Make Taxon Synonym Test");
		String microRef = "123";
		appCtr.getReferenceService().saveReference(citation);
		
		appCtr.getTaxonService().makeTaxonSynonym(oldTaxon, newAcceptedTaxon, synonymType, citation, microRef);

		appCtr.commitTransaction(txStatus);
		appCtr.close();
		
	}

    // TODO: move to cdmlib-services: cdm.test.integration
	private void testRemoveNameRelationship(CdmApplicationController appCtr) {
		
		logger.info("Testing testRemoveNameRelationship()");
		TransactionStatus txStatus = appCtr.startTransaction();
		
		BotanicalName name1, name2;
		Agent linne = appCtr.getAgentService().getAgentByUuid(UUID.fromString("f6272e48-5b4e-40c1-b4e9-ee32334fa19f"));
		name1 = BotanicalName.NewInstance(Rank.SPECIES(),"Name1",null,"arvensis",null,(TeamOrPersonBase)linne,null,"p.1", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Name2",null,"lanzae",null,(TeamOrPersonBase)linne,null,"p.2", null);
		
		name1.addRelationshipToName(name2, NameRelationshipType.BASIONYM(), "ruleTo");
		name2.addRelationshipFromName(name1, NameRelationshipType.BASIONYM(), "ruleFrom");
			
		appCtr.getNameService().saveTaxonName(name1);
		appCtr.getNameService().saveTaxonName(name2);
		
		logger.info("Removing Name Relationships");
		
		Set<NameRelationship> name1FromRelations = name1.getRelationsFromThisName();
		NameRelationship nameRel = null;
		
		for (NameRelationship name1Rel: name1FromRelations) {
			nameRel = name1Rel;
		}

        name1.removeNameRelationship(nameRel);
//		name1.removeTaxonName(name2);
		appCtr.getNameService().saveTaxonName(name1);
        
		Taxon taxon = (Taxon)appCtr.getTaxonService().getTaxonByUuid(UUID.fromString("6a8be65b-94b6-4136-919a-02002e409158"));
		Set<Synonym> synonyms = taxon.getSynonyms();
		
//		List<TaxonBase> taxa = appCtr.getTaxonService().getAllTaxa(100, 0);
//		Set<Synonym> synonyms = null;
//		for (TaxonBase taxonBase: taxa) {
//			synonyms = taxonBase.getSynonyms();
//		}
		
		Synonym syn = null;
		for (Synonym synonym: synonyms) {
			if (synonym.getUuid().toString().equals("f7ad5713-70ce-42af-984f-865c1f126460")) {
				syn = synonym;
			}
		}
		taxon.removeSynonym(syn);
		appCtr.getTaxonService().saveTaxon(taxon);
		
//		name1FromRelations.removeAll(name1FromRelations);
		
//		Set<NameRelationship> name2ToRelations = name2.getRelationsToThisName();
//		for (NameRelationship name2Rel: name2ToRelations) {
//			name2.removeNameRelationship(name2Rel);
//		}
		
		appCtr.commitTransaction(txStatus);
		appCtr.close();

	}
		
    // TODO: move to cdmlib-services: cdm.test.integration
	private void createNameRelationship(CdmApplicationController appCtr) {
		
		TransactionStatus txStatus = appCtr.startTransaction();

		appCtr.commitTransaction(txStatus);
		appCtr.close();
		
	}
	
}
