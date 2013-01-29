/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.util;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 27.10.2008
 */
public class TestTransaction {
	
	private static final String dbName = "cdm_test_jaxb";
    private static final int MAX_ENTRIES = 20;
	
	private static final ICdmDataSource db = TestDatabase.CDM_DB(dbName);
    private static final Logger logger = Logger.getLogger(TestTransaction.class);

    
    /** Modifies disjunct objects within two transactions of one application context.
     *  Flow:
     *  Start transaction #1. Modify and save taxon #1.
     *  Start transaction #2. Modify taxon #2.
     *  Commit transaction #1.
     *  Save taxon #2.
     *  Commit transaction #2.
     *  
     *  It is possible to commit transaction #2 before committing transaction #1
     *  but it is not possible to modify data after transaction #2 has been committed
     *  (LazyInitializationException). However, it is possible to save data after 
     *  transaction #2 has been committed.
     */    
	private void modifyDisjunctObjects() {
		
		CdmApplicationController appCtr = null;
		logger.info("Test modifying disjunct objects");

		try {
			appCtr = CdmApplicationController.NewInstance(db, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}
		
		BotanicalName name1, name2;
		Rank rankSpecies = Rank.SPECIES();
		Taxon taxon1, taxon2, child1, child2;
		
		try {
			/* ************** Start Transaction #1 ******************************** */
			
	    	TransactionStatus txStatOne = appCtr.startTransaction();
	    	
	    	List<? extends AgentBase> agents = appCtr.getAgentService().list(null, MAX_ENTRIES, 0, null, null);
	    	//List<TeamOrPersonBase> agents = appCtr.getAgentService().getAllAgents(MAX_ENTRIES, 0);
	    	TeamOrPersonBase author = (TeamOrPersonBase) agents.get(0);
	    	List<Reference> references = appCtr.getReferenceService().list(null, MAX_ENTRIES, 0, null, null);
	    	Reference sec = references.get(0);
	    	List<Taxon> taxa = appCtr.getTaxonService().getAllTaxa(MAX_ENTRIES, 0);

			name1 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "lucida", null, author, null, "1", null);
            // Calendula L.
			taxon1 = taxa.get(0);
			child1 = Taxon.NewInstance(name1, sec);
			taxon1.addTaxonomicChild(child1, sec, "D#t1-c1");
			appCtr.getTaxonService().saveOrUpdate(taxon1);
			

			/* ************** Start Transaction #2 ******************************** */
	    	
			TransactionStatus txStatTwo = appCtr.startTransaction();

			name2 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "scabra", null, author, null, "2", null);
            // Sonchus L.
	    	taxon2 = taxa.get(1);
			child2 = Taxon.NewInstance(name2, sec);
			taxon2.addTaxonomicChild(child2, sec, "D#t2-c2");
			
			/* ************** Commit Transaction #1 ******************************** */
			
	    	appCtr.commitTransaction(txStatOne);
	    	
			UUID t2uuid = appCtr.getTaxonService().saveOrUpdate(taxon2);
	    	
			/* ************** Commit Transaction #2 ******************************** */
			
	    	appCtr.commitTransaction(txStatTwo);
	    	
	    	appCtr.close();
			logger.info("End test modifying disjunct objects"); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
	

    /** Modifies shared objects within two transactions of one application context.
     *  Flow:
     *  Start transaction #1. Modify and save taxon #1.
     *  Start transaction #2. Modify taxon #1.
     *  Commit transaction #1.
     *  Save taxon #1.
     *  Commit transaction #2.
     */    
	private void modifySharedObjects() {
		
		CdmApplicationController appCtr = null;
		logger.info("Test modifying shared objects");

		try {
			appCtr = CdmApplicationController.NewInstance(db, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}
		
		BotanicalName name1, name2;
		Rank rankSpecies = Rank.SPECIES();
		Taxon taxon1, taxon2, child1, child2;
		
		try {
			/* ************** Start Transaction #1 ******************************** */
			
	    	TransactionStatus txStatOne = appCtr.startTransaction();
	    	
	    	List<? extends AgentBase> agents = appCtr.getAgentService().list(null, MAX_ENTRIES, 0, null, null);
	    	//List<TeamOrPersonBase> agents = appCtr.getAgentService().getAllAgents(MAX_ENTRIES, 0);
	    	TeamOrPersonBase author = (TeamOrPersonBase) agents.get(0);
	    	List<Reference> references = appCtr.getReferenceService().list(null, MAX_ENTRIES, 0, null, null);
	    	Reference sec = references.get(0);
	    	List<Taxon> taxa = appCtr.getTaxonService().getAllTaxa(MAX_ENTRIES, 0);

			name1 = 
				BotanicalName.NewInstance(rankSpecies, "Launaea", null, "child1", null, author, null, "1", null);
			// Cichorium intybus L.
	    	taxon1 = taxa.get(5);
			child1 = Taxon.NewInstance(name1, sec);
			taxon1.addTaxonomicChild(child1, sec, "S#t1-c1");
			appCtr.getTaxonService().saveOrUpdate(taxon1);
			

			/* ************** Start Transaction #2 ******************************** */
	    	
			TransactionStatus txStatTwo = appCtr.startTransaction();

			name2 = 
				BotanicalName.NewInstance(rankSpecies, "Reichardia", null, "child2", null, author, null, "2", null);
			// Cichorium intybus L.
	    	taxon2 = taxa.get(5);
			child2 = Taxon.NewInstance(name2, sec);
			taxon2.addTaxonomicChild(child2, sec, "S#t1-c2");
			
			/* ************** Commit Transaction #1 ******************************** */
			
	    	appCtr.commitTransaction(txStatOne);
	    	
			UUID t2uuid = appCtr.getTaxonService().saveOrUpdate(taxon2);
	    	
			/* ************** Commit Transaction #2 ******************************** */
			
	    	appCtr.commitTransaction(txStatTwo);
	    	
	    	appCtr.close();
			logger.info("End test modifying shared objects"); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
	

	private void checkTransactionFacets() {
		
		CdmApplicationController appCtr = null;
		logger.info("Test checking transaction facets");
		
		try {
			appCtr = CdmApplicationController.NewInstance(db, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			/* ************** Start Transaction #1 ******************************** */
			
	    	TransactionStatus txStatOne = appCtr.startTransaction();
	    	appCtr.commitTransaction(txStatOne);
	    	// set CdmApplicationController = debug in log4j.properties to see the transaction properties
	    	appCtr.close();
			logger.info("End test ask session for objects"); 
			
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
		
	private void askSessionForObjects() {
		
		CdmApplicationController appCtr = null;
		logger.info("Test asking session for objects");

		try {
			appCtr = CdmApplicationController.NewInstance(db, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}
		
		BotanicalName name1, name1_;
		Rank rankSpecies = Rank.SPECIES();
		Taxon taxon1;
		TaxonBase taxon1_;
		UUID t1uuid;
		
		try {
			/* ************** Start Transaction #1 ******************************** */
			
	    	TransactionStatus txStatOne = appCtr.startTransaction();
	    	
	    	List<? extends AgentBase> agents = appCtr.getAgentService().list(null, MAX_ENTRIES, 0, null, null);
	    	//List<TeamOrPersonBase> agents = appCtr.getAgentService().getAllAgents(MAX_ENTRIES, 0);
	    	//Agent author = agents.get(0);
	    	TeamOrPersonBase author = (TeamOrPersonBase) agents.get(0);
	    	List<Reference> references = appCtr.getReferenceService().list(null, MAX_ENTRIES, 0, null, null);
	    	Reference sec = references.get(0);

			name1 = 
				BotanicalName.NewInstance(rankSpecies, "NewTaxon1", null, "taxon1", null, author, null, "1", null);
	    	taxon1 = Taxon.NewInstance(name1, sec);
			t1uuid = appCtr.getTaxonService().saveOrUpdate(taxon1);
			//t1uuid = appCtr.getTaxonService().saveTaxon(taxon1, txStatOne);

			/* ************** Start Transaction #2 ******************************** */
	    	
			TransactionStatus txStatTwo = appCtr.startTransaction();

			// ask whether object taxon1 is known
			//getSession().
			
			name1_ = 
				BotanicalName.NewInstance(rankSpecies, "NewTaxon1_", null, "taxon1_", null, author, null, "1_", null);
	    	taxon1_ = appCtr.getTaxonService().find(t1uuid);
			
			/* ************** Commit Transaction #1 ******************************** */
			
	    	appCtr.commitTransaction(txStatOne);
	    	
			//UUID t2uuid = appCtr.getTaxonService().saveTaxon(taxon2);
	    	
			/* ************** Commit Transaction #2 ******************************** */
			
	    	appCtr.commitTransaction(txStatTwo);
	    	
	    	appCtr.close();
			logger.info("End test ask session for objects"); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
	

	private void test() { 
		
    	/* Init DB */
		// initDb(ICdmDataSource db, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading)
		CdmApplicationController appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.CREATE, false);

		/* Load test data into DB */
//    	TestDatabase.loadTestData(dbName, appCtrInit);

//		checkTransactionFacets();
//		modifyDisjunctObjects();
//		modifySharedObjects();
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestTransaction ta = new TestTransaction();
    	ta.test();
	}
}
