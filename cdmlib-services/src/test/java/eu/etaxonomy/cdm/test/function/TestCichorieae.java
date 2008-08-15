/**
 * 
 */
package eu.etaxonomy.cdm.test.function;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.jaxb.CdmDocumentBuilder;
import eu.etaxonomy.cdm.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AgentDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DefinedTermDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.name.TaxonNameDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.reference.ReferenceDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

/**
 * @author a.babadshanjan
 *
 */
public class TestCichorieae {

	private static final Logger logger = Logger.getLogger(TestCichorieae.class);
	
	//private static final String serializeFromDb = "cdm_test_jaxb";
	private static final String serializeFromDb = "cdm_test_anahit";
	private static final String deserializeToDb = "cdm_test_jaxb2";
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private String marshOutTwo = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_roundtrip.xml");

	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public void testSerialize(String dbname, String filename) {
    	
		logger.info("Serializing DB " + dbname + " to file " + filename);

		CdmApplicationController appCtr = null;

    	try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
    		
    		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
    		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, serializeFromDb, username, password);
    		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

    	} catch (DataSourceNotFoundException e) {
    		logger.error("datasource error");
    	} catch (TermNotFoundException e) {
    		logger.error("defined terms not found");
    	}
    	
    	TransactionStatus txStatus = appCtr.startTransaction();

    	IAgentDao agentDao = new AgentDaoImpl();
    	IDefinedTermDao definedTermDao = new DefinedTermDaoImpl();
    	IReferenceDao referenceDao = new ReferenceDaoHibernateImpl();
    	ITaxonNameDao nameDao = new TaxonNameDaoHibernateImpl();
    	ITaxonDao taxonDao = new TaxonDaoHibernateImpl();
    	
    	DataSet dataSet = new DataSet();
    	List<Agent> agents = null;
    	List<DefinedTermBase> terms = null;
    	List<TaxonNameBase> taxonomicNames = null;
    	List<TaxonBase> taxa = null;

    	// get data from DB

    	try {
    		logger.info("Load data from DB ...");

    		// get all DAO data

    		agents = agentDao.list(10000, 0);
    		terms = definedTermDao.list(5000, 0);
    		taxonomicNames = nameDao.list(8000, 0);
    		taxa = taxonDao.getAllTaxa(10000, 0);
    		
    	} catch (Exception e) {
    		logger.info("error while fetching taxa");
    	}

    	try {
    		dataSet.setAgents(agents);
//    		dataSet.setReferences(references);
    		dataSet.setTerms(terms);
    		dataSet.setTaxonomicNames(taxonomicNames);
//    		dataSet.setTaxa(taxa);
    		
    		dataSet.setSynonyms(new ArrayList<Synonym>());
    		dataSet.setRelationships(new HashSet<RelationshipBase>());
    		dataSet.setHomotypicalGroups(new HashSet<HomotypicalGroup>());
    		
    	} catch (Exception e) {
    		logger.info("error setting root data");
    	}

    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		cdmDocumentBuilder.marshal(dataSet, new FileWriter(filename));

    	} catch (Exception e) {
    		logger.error("marshalling error");
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    
	private void test(){
		
	    testSerialize(serializeFromDb, marshOutOne);
		}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestCichorieae sc = new TestCichorieae();
    	sc.test();
	}
}
