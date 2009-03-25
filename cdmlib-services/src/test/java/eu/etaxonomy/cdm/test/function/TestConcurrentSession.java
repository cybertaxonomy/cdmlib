package eu.etaxonomy.cdm.test.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.hibernate.annotation.HibernateSessionFactory;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.conversation.IConversationEnabled;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.hibernate.CdmCrudEvent;
import eu.etaxonomy.cdm.persistence.hibernate.CdmPostCrudObservableListener;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * TODO refactor the Transaction Handling into Conversation Manager
 * 
 * @author n.hoffmann
 *
 */
public class TestConcurrentSession extends CdmIntegrationTest{

	private static final Logger logger = Logger.getLogger(TestConcurrentSession.class);
	
	@HibernateSessionFactory 
	private SessionFactory sessionFactory;
	
	@SpringBeanByType
	private PlatformTransactionManager transactionManager;

	@SpringBeanByType
	private ITaxonService taxonService;
	
	@SpringBeanByType
	private IReferenceService referenceService;
	
	@SpringBeanByType
	private DataSource dataSource;
	
	@SpringBeanByType
	private ITaxonDao taxonDao;
	
	private ConversationHolder conversationHolder1;
	private ConversationHolder conversationHolder2;

	private UUID taxonUuid1 = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
	private UUID taxonUuid2 = UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46");

	private UUID referenceUuid1 = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	private UUID referenceUuid2 = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");
	
	/**
	 * 
	 */
	private TransactionDefinition definition = null;

	private ConversationHolder conversationHolder3;
	


	@Before
	public void setup(){
		conversationHolder1 = new ConversationHolder(dataSource, sessionFactory, transactionManager);
		conversationHolder2 = new ConversationHolder(dataSource, sessionFactory, transactionManager);
		conversationHolder3 = new ConversationHolder(dataSource, sessionFactory, transactionManager);
	}
	
	@After
	public void tearDown(){
		// TODO investigate when resources get cleaned up and why this poses a problem sometimes and sometimes not
//		conversationHolder1.dispose();
//		conversationHolder2.dispose();
	}
	
	/**
	 * Test the general possibility to open two sessions at the same time
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testTwoSessions(){
	
		conversationHolder1.bind();		
		int context1Count = taxonDao.count();
		conversationHolder1.unbind();
		
		conversationHolder2.bind();		
		int context2Count = taxonDao.count();
		conversationHolder2.unbind();
		
		assertNotSame("The contexts sessions should be distinct.", conversationHolder1.getSession(), conversationHolder2.getSession());
		assertEquals("Both contexts should yield the same results(at least if " +
				"there where no write operations in between)", context1Count, context2Count);
	}
	
	/**
	 * Getting the same taxon from two different sessions. However, the resulting objects should not be the same.
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testTwoSessionsEqualTaxon(){
		
		conversationHolder1.bind();	
		TaxonBase taxonBase1 = taxonDao.findByUuid(taxonUuid1);
		conversationHolder1.unbind();
		
		conversationHolder2.bind();		
		TaxonBase taxonBase2 = taxonDao.findByUuid(taxonUuid1);
		conversationHolder2.unbind();
		
		assertEquals("The objects should be equal", taxonBase1, taxonBase2);
		assertNotSame("The objects should not be the same", taxonBase1, taxonBase2);
	}
	
	/**
	 * 
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testTwoSessionsEqualTaxonWithTaxonService(){
		conversationHolder1.bind();	
		TaxonBase taxonBase1 = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder1.unbind();
		
		conversationHolder2.bind();		
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder2.unbind();
		
		assertEquals("The objects should be equal", taxonBase1, taxonBase2);
		assertNotSame("The objects should not be the same", taxonBase1, taxonBase2);
	}
	
	/**
	 * Getting the same taxon from two different sessions. However, the resulting objects should not be the same.
	 */
	@Test
	@Deprecated
	@DataSet("ConcurrentSessionTest.xml")
	public void testOneSessionsTwoTransactionsSameTaxon(){
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		conversationHolder1.bind();	
		TaxonBase taxonBase1 = taxonDao.findByUuid(taxonUuid1);
		conversationHolder1.unbind();
		
		conversationHolder1.bind();		
		TaxonBase taxonBase2 = taxonDao.findByUuid(taxonUuid1);
		conversationHolder1.unbind();
		
		assertEquals("The objects should be equal", taxonBase1, taxonBase2);
		assertSame("The objects should  not be the same", taxonBase1, taxonBase2);
	}
	
	/**
	 * The session should still be open after committing a transaction.
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testLongConversationWithMultipleTransactions(){
		
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		conversationHolder1.bind();	
		TaxonBase taxonBase1 = taxonDao.findByUuid(taxonUuid1);
		TaxonNameBase taxonName1 = taxonBase1.getName();
		transactionManager.commit(txStatusOne);
		
		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition);
		conversationHolder1.bind();	
		TaxonNameBase taxonName2 = taxonBase1.getName();
		transactionManager.commit(txStatusTwo);
		
		assertSame("Two objects from different transactions should be the same, because we are still in " +
				"same persistence context", taxonName1, taxonName2);
	}

	/**
	 * Switching sessions. Should not throw any exceptions
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testSwitchSessions(){
		
		conversationHolder1.bind();	
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		TaxonBase taxonBase1 = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder1.unbind();

		conversationHolder2.bind();		
		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition );
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder2.unbind();
		
		conversationHolder1.bind();	
		TaxonBase taxonBase3 = taxonService.getTaxonByUuid(taxonUuid2);
		conversationHolder1.unbind();
		
		
	}
	
	/**
	 * Load an object, manipulate it and persist it by committing the transaction.
	 * When reloading the same object we should still be in the same session 
	 */
	@Ignore
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testSavingAndReaccessingTheSameObject(){
		
		conversationHolder1.bind();	
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		Session sessionFirstTransaction = conversationHolder1.getSession();
		TaxonBase taxonBase = taxonService.getTaxonByUuid(taxonUuid1);
		TaxonNameBase newTaxonName = BotanicalName.NewInstance(null);
		conversationHolder1.unbind();
		
		conversationHolder1.bind();
		newTaxonName.addTaxonBase(taxonBase);
		conversationHolder1.unbind();
		
		conversationHolder1.bind();
		taxonService.save(taxonBase);
		conversationHolder1.unbind();
		transactionManager.commit(txStatusOne);
		
		
		conversationHolder1.bind();	
		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition);
		Session sessionSecondTransaction = conversationHolder1.getSession();
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid1);
		transactionManager.commit(txStatusTwo);

		assertSame("The taxa should be the same", taxonBase, taxonBase2);
		assertSame("The sessions should be the same", sessionFirstTransaction, sessionSecondTransaction);
		assertSame("The name objects should be the same", newTaxonName, taxonBase2.getName());
	}
	
	/**
	 * We load the same taxon in two different sessions. The reference of the first
	 * taxon gets manipulated and the taxon saved afterwards.  When trying to persist the other 
	 * taxon we would expect some form of exception.
	 * 
	 * TODO it is not quite clear to me what really should happen here. Right now it seems like
	 * we do not have any locking at all. Needs further investigation.
	 * 
	 * UPDATE we indeed have no locking. Last write wins!
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testWhatHappensWhenEncounteringStaleData(){
		conversationHolder1.bind();
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		TaxonBase taxonBase1 = taxonService.getTaxonByUuid(taxonUuid1);
		
		conversationHolder2.bind();
		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition);
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid1);
		
		conversationHolder1.bind();
		ReferenceBase reference1 = referenceService.getReferenceByUuid(referenceUuid1);
		assertEquals("This should be the sec", taxonBase1.getSec(), reference1);
		
		ReferenceBase reference2 = referenceService.getReferenceByUuid(referenceUuid2);
		taxonBase1.setSec(reference2);		
		taxonService.save(taxonBase1);
		transactionManager.commit(txStatusOne);
		
		conversationHolder2.bind();
		taxonBase2.setSec(null);
		taxonService.save(taxonBase2);
		transactionManager.commit(txStatusTwo);
		
		conversationHolder3.bind();
		TaxonBase taxonBase3 = taxonService.getTaxonByUuid(taxonUuid1);
		assertNull(taxonBase3.getSec());
	}
	
	/**
	 * We want to know if the data really gets persisted.
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testProofOfDataPersistency(){
		// first conversation
		conversationHolder1.bind();
		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
		// get a taxon
		TaxonBase taxonBase = taxonService.getTaxonByUuid(taxonUuid1);
		// get a reference
		ReferenceBase reference = referenceService.getReferenceByUuid(referenceUuid2);
		// make sure 
		assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
		// set the reference as the taxons new sec
		taxonBase.setSec(reference);
		// save and commit
		taxonService.save(taxonBase);
		transactionManager.commit(txStatusOne);
		conversationHolder1.unbind();
		
		
		// second conversation
		conversationHolder2.bind();
		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition);
		// load the same taxon in a different session
		TaxonBase taxonBaseInSecondTransaction = taxonService.getTaxonByUuid(taxonUuid1);
		// load the reference
		ReferenceBase referenceInSecondTransaction = referenceService.getReferenceByUuid(referenceUuid2);
		// we assume that
		assertSame("The reference should be the sec now.", taxonBaseInSecondTransaction.getSec(), referenceInSecondTransaction);
		assertNotSame("The reference should not be the same object as in first transaction.", reference, referenceInSecondTransaction);		
		conversationHolder2.unbind();
	}
	
	/**
	 * The same test as before but uses the conversation managers transaction 
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testProofOfDataPersistencyNewTransactionModel(){
		// first conversation
		conversationHolder1.bind();
		conversationHolder1.startTransaction();
		// get a taxon
		TaxonBase taxonBase = taxonService.getTaxonByUuid(taxonUuid1);
		// get a reference
		ReferenceBase reference = referenceService.getReferenceByUuid(referenceUuid2);
		// make sure 
		assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
		// set the reference as the taxons new sec
		taxonBase.setSec(reference);
		// save and commit
		taxonService.save(taxonBase);
		conversationHolder1.commit();
		conversationHolder1.unbind();
		
		
		// second conversation
		conversationHolder2.bind();
		conversationHolder2.startTransaction();
		// load the same taxon in a different session
		TaxonBase taxonBaseInSecondTransaction = taxonService.getTaxonByUuid(taxonUuid1);
		// load the reference
		ReferenceBase referenceInSecondTransaction = referenceService.getReferenceByUuid(referenceUuid2);
		// we assume that
		assertSame("The reference should be the sec now.", taxonBaseInSecondTransaction.getSec(), referenceInSecondTransaction);
		assertNotSame("The reference should not be the same object as in first transaction.", reference, referenceInSecondTransaction);		
		conversationHolder2.unbind();
	}
	
	/**
	 * We manipulate an object in one session and see how these change get propagated to the other session. 
	 */
	@Ignore
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testIfDataGetsPersistedWhenFiringCommit(){
		// first conversation
		conversationHolder1.bind();
		conversationHolder1.startTransaction();
		// get a taxon
		TaxonBase taxonBase = taxonService.getTaxonByUuid(taxonUuid1);
		// get a reference
		ReferenceBase reference = referenceService.getReferenceByUuid(referenceUuid2);
		// make sure 
		assertTrue(! taxonBase.getSec().equals(reference));
		assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
		// set the reference as the taxons new sec
		taxonBase.setSec(reference);
		// save and commit
		taxonService.save(taxonBase);
		conversationHolder1.unbind();
		
		// second conversation
		conversationHolder2.bind();
		// load the same taxon in a different session, since we did not commit the first transaction,
		// the reference change did not make its way to the database and the references should be distinct
		TaxonBase taxonBaseInSecondTransaction = taxonService.getTaxonByUuid(taxonUuid1);
		assertFalse(taxonBase.getSec().equals(taxonBaseInSecondTransaction.getSec()));
		
		// commit the first transaction
		conversationHolder1.bind();
		conversationHolder1.commit();
		
		// as the taxonBaseInSecondTransaction still has it's data from before the first transaction was committed
		// we assume that the references are still not equal
		assertFalse(taxonBase.getSec().equals(taxonBaseInSecondTransaction.getSec()));
		
		// we call a refresh on the taxonBaseInSecondTransaction to synchronize its state with the database
		conversationHolder2.bind();
		taxonService.refresh(taxonBaseInSecondTransaction);
		
		// the objects should now be equal
		
		assertTrue(taxonBase.getSec().equals(taxonBaseInSecondTransaction.getSec()));
	}
	
	/**
	 * This should not throw exceptions
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testMultipleTransactionsInOneSession(){
		conversationHolder1.bind();
		conversationHolder1.startTransaction();
		TaxonBase taxonBase1 = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder1.commit();
		
		conversationHolder1.startTransaction();
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid2);
		conversationHolder1.commit();		
	}
	
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testInsert(){
		TestConversationEnabled testConversationEnabled = new TestConversationEnabled();
		
		CdmPostCrudObservableListener mediator = CdmPostCrudObservableListener.getDefault();
		mediator.register(testConversationEnabled);
		
		conversationHolder1.bind();
		conversationHolder1.startTransaction();
		TaxonBase newTaxon = Taxon.NewInstance(null, null);
		taxonService.save(newTaxon);
		conversationHolder1.commit();
	}
	
	
	/**
	 * this is a locking playground
	 */
	@Test
	@DataSet("ConcurrentSessionTest.xml")
	public void testLocking(){
		conversationHolder1.bind();
		// first session, first transaction
		conversationHolder1.startTransaction();
		TaxonBase taxonBase = taxonService.getTaxonByUuid(taxonUuid1);
		conversationHolder1.unbind();
		// leave the first transaction without committing it
		
		// start a new session with a new transaction
		conversationHolder2.bind();
		conversationHolder2.startTransaction();
		TaxonBase taxonBase2 = taxonService.getTaxonByUuid(taxonUuid1);
		taxonBase.setSec(null);
		conversationHolder2.commit();
		conversationHolder2.unbind();
		// transaction of the second session got committed
		
		// return to the first session and commit its transaction
		conversationHolder1.bind();
		conversationHolder1.commit();
		
		conversationHolder1.startTransaction();
		conversationHolder1.lock(taxonBase, LockMode.READ);
		conversationHolder1.unbind();
		
	}
	

}
