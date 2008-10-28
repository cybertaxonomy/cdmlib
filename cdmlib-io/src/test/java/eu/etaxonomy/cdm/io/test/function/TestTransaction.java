/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.test.function;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
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
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @created 27.10.2008
 */
public class TestTransaction {
	
	private static final String dbName = "cdm_test_jaxb";
	
    private static final int MAX_ENTRIES = 20;
	
	private static final ICdmDataSource db = TestDatabase.CDM_DB(dbName);
	
    private static final Logger logger = Logger.getLogger(TestTransaction.class);
    
    
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
		
		BotanicalName synName1, name1, name2;
		Rank rankGenus = Rank.GENUS();
		Rank rankSpecies = Rank.SPECIES();
		Taxon taxon1, taxon2, child1, child2;
		
		try {
			/* ************** Start Transaction 1 ******************************** */
	    	TransactionStatus txStatOne = appCtr.startTransaction(true);
	    	
			/* ************** Start Transaction 2 ******************************** */
			TransactionStatus txStatTwo = appCtr.startTransaction(true);

	    	List<TeamOrPersonBase> agents = appCtr.getAgentService().getAllAgents(MAX_ENTRIES, 0);
	    	TeamOrPersonBase author = agents.get(0);
	    	synName1 = 
	    		BotanicalName.NewInstance(rankGenus, "Aposeris", null, null, null, author, null, "0", null);
			name1 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "lucida", null, author, null, "1", null);
			name2 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "scabra", null, author, null, "2", null);
	    	List<ReferenceBase> references = appCtr.getReferenceService().getAllReferences(MAX_ENTRIES, 0);
	    	ReferenceBase sec = references.get(0);
	    	List<Taxon> taxa = appCtr.getTaxonService().getRootTaxa(sec);
	    	taxon1 = taxa.get(0);
			child1 = Taxon.NewInstance(name1, sec);
	    	taxon1.addHeterotypicSynonymName(synName1);
			taxon1.addTaxonomicChild(child1, sec, "10");
			appCtr.getTaxonService().saveTaxon(taxon1);

			/* ************** Commit Transaction 1 ******************************** */
	    	appCtr.commitTransaction(txStatOne);
	    	
	    	taxon2 = taxa.get(1);
			child2 = Taxon.NewInstance(name2, sec);
			taxon2.addTaxonomicChild(child2, sec, "10");
			appCtr.getTaxonService().saveTaxon(taxon2);

			/* ************** Commit Transaction 2 ******************************** */
	    	appCtr.commitTransaction(txStatTwo);
	    	
	    	//java.lang.IllegalStateException: Cannot deactivate transaction synchronization - not active
	    	appCtr.close();
			logger.info(""); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
	
	
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
		
		BotanicalName synName1, name1, name2;
		Rank rankGenus = Rank.GENUS();
		Rank rankSpecies = Rank.SPECIES();
		Taxon taxon1, taxon2, child1, child2;
		
		try {
			/* ************** Start Transaction 1 ******************************** */
	    	TransactionStatus txStatOne = appCtr.startTransaction(true);
	    	
			/* ************** Start Transaction 2 ******************************** */
			TransactionStatus txStatTwo = appCtr.startTransaction(true);

	    	List<TeamOrPersonBase> agents = appCtr.getAgentService().getAllAgents(MAX_ENTRIES, 0);
	    	TeamOrPersonBase author = agents.get(0);
	    	synName1 = 
	    		BotanicalName.NewInstance(rankGenus, "Aposeris", null, null, null, author, null, "0", null);
			name1 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "lucida", null, author, null, "1", null);
			name2 = 
				BotanicalName.NewInstance(rankSpecies, "Hyoseris", null, "scabra", null, author, null, "2", null);
	    	List<ReferenceBase> references = appCtr.getReferenceService().getAllReferences(MAX_ENTRIES, 0);
	    	ReferenceBase sec = references.get(0);
	    	List<Taxon> taxa = appCtr.getTaxonService().getRootTaxa(sec);
	    	taxon1 = taxa.get(0);
			child1 = Taxon.NewInstance(name1, sec);
	    	taxon1.addHeterotypicSynonymName(synName1);
			taxon1.addTaxonomicChild(child1, sec, "10");
			appCtr.getTaxonService().saveTaxon(taxon1);

			/* ************** Commit Transaction 1 ******************************** */
	    	appCtr.commitTransaction(txStatOne);
	    	
	    	taxon2 = taxa.get(1);
			child2 = Taxon.NewInstance(name2, sec);
			taxon2.addTaxonomicChild(child2, sec, "10");
			appCtr.getTaxonService().saveTaxon(taxon2);

			/* ************** Commit Transaction 2 ******************************** */
	    	appCtr.commitTransaction(txStatTwo);
	    	
	    	//java.lang.IllegalStateException: Cannot deactivate transaction synchronization - not active
	    	appCtr.close();
			logger.info(""); 
				
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
    	TestDatabase.loadTestData(dbName, appCtrInit);
    	
		modifyDisjunctObjects();
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
