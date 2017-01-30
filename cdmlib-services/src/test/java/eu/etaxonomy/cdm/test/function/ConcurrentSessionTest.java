/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.test.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Set;
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
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.hibernate5.HibernateSystemException;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * This test class is an exhaustive testing ground for conversations (a.k.a long running sessions)
 * implemented in the CDM Library by the {@link eu.etaxonomy.cdm.api.conversation.ConversationHolder ConversationHolder}.
 *
 * @author n.hoffmann
 *
 */
@Transactional(TransactionMode.DISABLED)
public class ConcurrentSessionTest extends CdmIntegrationTest {

    private static final Logger logger = Logger.getLogger(ConcurrentSessionTest.class);

    @SpringBeanByType
    private SessionFactory sessionFactory;

    @SpringBeanByType
    private PlatformTransactionManager transactionManager;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private DataSource dataSource;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    private ConversationHolder conversationHolder1 = null;
    private ConversationHolder conversationHolder2 = null;
    private ConversationHolder conversationHolder3 = null;

    private DataSource targetDataSource;

    private final UUID taxonUuid1 = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
    private final UUID taxonUuid2 = UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46");

    private final UUID referenceUuid1 = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
    private final UUID referenceUuid2 = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");




    @Before
    public void setup(){

        targetDataSource = dataSource instanceof TransactionAwareDataSourceProxy ?
                ((TransactionAwareDataSourceProxy)dataSource).getTargetDataSource():
                dataSource;
    }

    @After
    public void tearDown(){
        if(conversationHolder1 != null) {
            conversationHolder1.close();
        }
        if(conversationHolder2 != null) {
            conversationHolder2.close();
        }
        if(conversationHolder3 != null) {
            conversationHolder3.close();
        }


    }

    /**
     * We want to know if the data really gets persisted.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testProofOfDataPersistency(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        // first conversation
        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        // get a taxon
        TaxonBase taxonBase = taxonService.find(taxonUuid1);
        // get a reference
        Reference reference = referenceService.find(referenceUuid2);
        // make sure
        assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
        // set the reference as the taxons new sec
        taxonBase.setSec(reference);
        // save and commit
        taxonService.save(taxonBase);
        conversationHolder1.commit();


        // second conversation
        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        // load the same taxon in a different session
        TaxonBase taxonBaseInSecondTransaction = taxonService.find(taxonUuid1);
        // load the reference
        Reference referenceInSecondTransaction = referenceService.find(referenceUuid2);
        // we assume that
        assertSame("The reference should be the sec now.", taxonBaseInSecondTransaction.getSec(), referenceInSecondTransaction);
        assertNotSame("The reference should not be the same object as in first transaction.", reference, referenceInSecondTransaction);
    }

    /**
     * Test the general possibility to open two sessions at the same time
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testTwoSessions(){
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        int context1Count = taxonDao.count();

        conversationHolder2.bind();
        int context2Count = taxonDao.count();

        assertEquals("Both contexts should yield the same results(at least if " +
                "there where no write operations in between)", context1Count, context2Count);

    }

    /**
     * Getting the same taxon from two different sessions using the taxon dao.
     * However, the resulting objects should be equal but not be the same.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testTwoSessionsEqualTaxon(){
        conversationHolder1 = new ConversationHolder(dataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(dataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TaxonBase taxonBase1 = taxonDao.findByUuid(taxonUuid1);

        conversationHolder2.bind();
        TaxonBase taxonBase2 = taxonDao.findByUuid(taxonUuid1);

        assertEquals("The objects should be equal.", taxonBase1, taxonBase2);
        assertNotSame("The objects should be the same.", taxonBase1, taxonBase2);
    }

    /**
     * Getting the same taxon from two different sessions using the taxon service.
     * However, the resulting objects should be equal but not be the same.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testTwoSessionsEqualTaxonWithTaxonService(){
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);

        conversationHolder2.bind();
        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);


        assertEquals("The objects should be equal", taxonBase1, taxonBase2);
        assertNotSame("The objects should not be the same", taxonBase1, taxonBase2);
    }

    /**
     * Getting the same taxon from two different sessions using the taxon service.
     * However, the resulting objects should be equal but not be the same.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testTwoSessionsRemoveTaxonBaseTaxonService(){
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);
        TaxonNameBase name = taxonBase1.getName();
        Synonym syn = Synonym.NewInstance(name, null);
        taxonService.save(syn);
        conversationHolder1.commit();
        Set<TaxonBase> taxonBases = taxonBase1.getName().getTaxonBases();
        name.removeTaxonBase(syn);
        taxonService.saveOrUpdate(taxonBase1);
        conversationHolder1.commit();
        conversationHolder2.bind();
        conversationHolder2.startTransaction();


        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);
        taxonBases = taxonBase2.getName().getTaxonBases();

        assertEquals("There should be only one taxon left", taxonBases.size(), 1);
        assertNotSame("The objects should not be the same", taxonBase1, taxonBase2);
    }

    /**
     * Interveaving conversations to test session within and outside conversations.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void interveaveConversations(){
        TaxonBase<?> taxonBase1 = taxonService.find(taxonUuid1);
        Session h4Session1 = taxonService.getSession();

        assertNull(TransactionSynchronizationManager.getResource(sessionFactory));

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        SessionHolder sessionHolder1 = (SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory);
        Session session1 = sessionHolder1.getSession();
        assertNotSame("The sessions should not be the same", h4Session1, session1);
        TaxonBase<?> taxonBase2 = taxonService.find(taxonUuid1);
        SessionHolder sessionHolder2 = (SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory);
        Session session2 = sessionHolder2.getSession();
        assertEquals("The session holders should be equal", sessionHolder1, sessionHolder2);
        assertEquals("The sessions should be equal", session1, session2);
        conversationHolder1.close();

        h4Session1 = taxonService.getSession();
        assertNotSame("The sessions should not be the same", h4Session1, session2);
        TaxonBase<?> taxonBase3 = taxonService.find(taxonUuid1);
        Session h4Session2 = taxonService.getSession();
        TaxonBase<?> taxonBase4 = taxonService.find(taxonUuid1);
        assertNotSame("The sessions should not be the same", h4Session1, h4Session2);

    }


    /**
     * Testing multiple transactions for the same conversation holder.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testLongConversationWithMultipleTransactions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TransactionStatus txStatusOne = conversationHolder1.startTransaction();
        TaxonBase taxonBase1 = taxonDao.findByUuid(taxonUuid1);
        TaxonNameBase taxonName1 = taxonBase1.getName();
        conversationHolder1.commit();


        conversationHolder1.bind();
        TransactionStatus txStatusTwo = conversationHolder1.startTransaction();
        TaxonNameBase taxonName2 = taxonBase1.getName();
        conversationHolder1.commit();

        assertNotSame(txStatusOne, txStatusTwo);
        assertSame("Two objects from different transactions should be the same, because we are still in " +
                "same persistence context", taxonName1, taxonName2);
    }

    /**
     * Switching sessions. Should not throw any exceptions
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testSwitchSessions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        //		TransactionStatus txStatusOne = transactionManager.getTransaction(definition);
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);

        conversationHolder2.bind();
        //		TransactionStatus txStatusTwo = transactionManager.getTransaction(definition );
        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);

        conversationHolder1.bind();
        TaxonBase taxonBase3 = taxonService.find(taxonUuid2);


    }

    /**
     * Lazy loaded objects should be the same in different transactions within the
     * same conversation.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testReaccessingTheSameLazyLoadedObjectInTwoDifferentTransactions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.bind();
        TransactionStatus tx1 = conversationHolder1.startTransaction();
        TaxonBase t1 = taxonService.find(taxonUuid1);

        TaxonNameBase n1 = t1.getName();
        TransactionStatus tx2 = conversationHolder1.commit(true);

        TaxonNameBase n2 = t1.getName();

        assertSame(n1, n2);
        assertNotSame(tx1, tx2);

    }

    /**
     * Objects should be the same in different transactions within the
     * same conversation.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testReaccessingTheSameObjectInTwoDifferentTransactions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.bind();
        TransactionStatus tx1 = conversationHolder1.startTransaction();
        TaxonBase t1 = taxonService.find(taxonUuid1);

        TransactionStatus tx2 = conversationHolder1.commit(true);
        TaxonBase t2 = taxonService.find(taxonUuid1);

        assertSame(t1, t2);
        assertNotSame(tx1, tx2);

    }

    /**
     * Objects should be the same in the same transaction within the
     * same conversation.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testReaccessingTheSameObjectInSameTransaction(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        TaxonBase t1 = taxonService.find(taxonUuid1);

        // I wonder if this breaks
        TaxonBase t2 = taxonService.find(taxonUuid1);

        assertSame(t1, t2);

    }



    /**
     * Load an object, manipulate it and persist it by committing the transaction.
     * When reloading the same object we should still be in the same session
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testSavingAndReaccessingTheSameObject(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        TestConversationEnabled testConversationEnabled = new TestConversationEnabled();

        conversationHolder1.registerForDataStoreChanges(testConversationEnabled);

        conversationHolder1.bind();
        TransactionStatus txStatusOne = conversationHolder1.startTransaction();
        //		Session sessionFirstTransaction = conversationHolder11.getSession();
        TaxonBase taxonBase = taxonService.find(taxonUuid1);
        TaxonNameBase newTaxonName = TaxonNameFactory.NewBotanicalInstance(null);

        conversationHolder1.bind();
        newTaxonName.addTaxonBase(taxonBase);

        conversationHolder1.bind();
        taxonService.save(taxonBase);
        conversationHolder1.commit();


        conversationHolder1.bind();
        TransactionStatus txStatusTwo = conversationHolder1.startTransaction();

        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);
        conversationHolder1.commit();

        assertEquals("The taxa should be equal.", taxonBase, taxonBase2);

        assertEquals("The name objects should be the same.", taxonBase.getName(), taxonBase2.getName());

    }



    /**
     * We load the same taxon in two different sessions. The reference of the first
     * taxon gets manipulated and the taxon saved afterwards.  When trying to persist the other
     * taxon we would expect some form of exception.
     *
     */
    @Test(expected=HibernateSystemException.class)

    @DataSet("ConcurrentSessionTest.xml")

    public void testWhatHappensWhenEncounteringStaleData(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder3 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TransactionStatus txStatusOne = conversationHolder1.startTransaction();
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);


        conversationHolder2.bind();
        TransactionStatus txStatusTwo = conversationHolder2.startTransaction();
        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);


        conversationHolder1.bind();
        Reference reference1 = referenceService.find(referenceUuid1);
        assertSame("This should be the sec", taxonBase1.getSec(), reference1);

        Reference reference2 = referenceService.find(referenceUuid2);
        taxonBase1.setSec(reference2);
        taxonService.save(taxonBase1);
        conversationHolder1.commit();

        conversationHolder2.bind();
        taxonBase2.setSec(reference1);
        taxonService.save(taxonBase2);
        conversationHolder2.commit();

        conversationHolder3.bind();
        TransactionStatus txStatusThree = conversationHolder3.startTransaction();
        TaxonBase taxonBase3 = taxonService.find(taxonUuid1);
        assertNull(taxonBase3.getSec());
    }



    /**
     * Persist data in two different , successive transactions from different conversations
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testProofOfDataPersistencyNewTransactionModel(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        // first conversation
        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        // get a taxon
        TaxonBase taxonBase = taxonService.find(taxonUuid1);
        // get a reference
        Reference reference = referenceService.find(referenceUuid2);
        // make sure
        assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
        // set the reference as the taxons new sec
        taxonBase.setSec(reference);
        // save and commit
        taxonService.save(taxonBase);
        conversationHolder1.commit(false);


        // second conversation
        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        // load the same taxon in a different session
        TaxonBase taxonBaseInSecondTransaction = taxonService.find(taxonUuid1);
        // load the reference
        Reference referenceInSecondTransaction = referenceService.find(referenceUuid2);
        // we assume that
        assertSame("The reference should be the sec now.", taxonBaseInSecondTransaction.getSec(), referenceInSecondTransaction);
        assertNotSame("The reference should not be the same object as in first transaction.", reference, referenceInSecondTransaction);

    }

    /**
     * We manipulate an object in one session and see how these change get propagated to the other session.
     */

    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testIfDataGetsPersistedWhenFiringCommit(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        // first conversation
        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        // get a taxon
        TaxonBase taxonBase = taxonService.find(taxonUuid1);
        // get a reference
        Reference reference = referenceService.find(referenceUuid2);
        // make sure
        assertTrue(! taxonBase.getSec().equals(reference));
        assertNotSame("this reference should not be the taxons sec.", taxonBase.getSec(), reference);
        // set the reference as the taxons new sec
        taxonBase.setSec(reference);
        // save and commit
        taxonService.save(taxonBase);

        // second conversation
        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        // load the same taxon in a different session, since we did not commit the first transaction,
        // the reference change did not make its way to the database and the references should be distinct
        TaxonBase taxonBaseInSecondTransaction = taxonService.find(taxonUuid1);
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
     * Testing multiple transaction in a single conversation (session)
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testMultipleTransactionsInOneSession() {

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);
        conversationHolder1.commit();

        conversationHolder1.startTransaction();
        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);
        conversationHolder1.commit();

        assertSame("The objects should be the same", taxonBase1, taxonBase2);
        assertEquals("The objects should be equal", taxonBase1, taxonBase2);
    }

    /**
     * Testing multiple transaction in a multiple conversations (session)
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testMultipleTransactionsInMultipleSessions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder3 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        conversationHolder1.startTransaction();

        conversationHolder2.bind();
        conversationHolder2.startTransaction();

        conversationHolder3.bind();
        conversationHolder3.startTransaction();
    }

    /**
     * Testing both the saving of a newly created and an already existing DefinedTerm
     * in different sessions.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testSavingTheSameObjectInTwoSessions(){
        //new session
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        //save newly created DefinedTerm
        DefinedTerm term = DefinedTerm.NewKindOfUnitInstance("XXXkindofUnitXXX", "XXXlabelXXX", "XXXlabelAbbrevXXX");
        persistTerm(term, termService, conversationHolder1);
        conversationHolder1.commit();

        //new session
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.startTransaction();
        conversationHolder1.bind();

        //save newly createy term from last session
        persistTerm(term, termService, conversationHolder1);
        conversationHolder1.commit();

        //new session
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.startTransaction();
        conversationHolder1.bind();

        //save already existing DefinedTerm
        NamedArea namedArea = NamedArea.ANTARCTICA();
        persistTerm(term, termService, conversationHolder1);
        conversationHolder1.commit();

        //new session
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.startTransaction();
        conversationHolder1.bind();

        //save already existing term from last session
        persistTerm(namedArea, termService, conversationHolder1);
        conversationHolder1.commit();
    }

    private void persistTerm(DefinedTermBase<?> term, ITermService termService, ConversationHolder conversation){
        if(term!=null){
            //if the term does not exist in the DB save it
            if(termService.find(term.getUuid())==null){
                termService.saveOrUpdate(term);
            }
            //if it does exist but is not bound to the current session re-load and save it
            else if(!conversation.getSession().contains(term)){
                term = termService.load(term.getUuid());
                termService.saveOrUpdate(term);
            }
        }
    }

    /**
     * Testing inserting new data.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testInsert(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        TestConversationEnabled testConversationEnabled = new TestConversationEnabled();

        conversationHolder1.registerForDataStoreChanges(testConversationEnabled);

        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        TaxonBase newTaxon = Taxon.NewInstance(null, null);
        taxonService.save(newTaxon);
        conversationHolder1.commit();
    }

    /**
     * Testing updating already existing data.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testUpdate(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        TestConversationEnabled testConversationEnabled = new TestConversationEnabled();

        conversationHolder1.registerForDataStoreChanges(testConversationEnabled);

        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);
        Reference reference = referenceService.find(referenceUuid2);
        taxonBase1.setSec(reference);
        taxonService.save(taxonBase1);
        conversationHolder1.commit();
    }

    /**
     * Switch sessions randomly and expect retrieved objects to be same but not equal.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testMultipleSessionSwitching(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder3 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        TaxonBase taxon1 = taxonService.find(taxonUuid1);
        assertSame(conversationHolder1.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        conversationHolder2.bind();
        TaxonBase taxon2 = taxonService.find(taxonUuid2);
        assertSame(conversationHolder2.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        conversationHolder3.bind();
        TaxonBase taxon3 = taxonService.find(taxonUuid1);
        assertSame(conversationHolder3.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        conversationHolder2.bind();
        TaxonNameBase name2 = taxon2.getName();
        assertSame(conversationHolder2.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        conversationHolder3.bind();
        TaxonNameBase name3 = taxon3.getName();
        assertSame(conversationHolder3.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        // Lazy loading somehow works without binding the session first
        TaxonNameBase name1 = taxon1.getName();
        assertNotSame(name1, name3);
        assertEquals(name1, name3);
        assertNotSame(conversationHolder1.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

    }

    /**
     * Switch sessions with multiple transactions randomly and expect retrieved objects to be same but not equal.
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testMultipleSessionSwitchingInTransactions(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder3 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        conversationHolder1.bind();
        conversationHolder1.startTransaction();
        TaxonBase taxon1 = taxonService.find(taxonUuid1);
        assertSame(conversationHolder1.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());
        conversationHolder1.commit();

        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        TaxonBase taxon2 = taxonService.find(taxonUuid2);
        assertSame(conversationHolder2.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());
        conversationHolder2.commit();

        conversationHolder3.bind();
        conversationHolder3.startTransaction();
        TaxonBase taxon3 = taxonService.find(taxonUuid1);
        assertSame(conversationHolder3.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());
        conversationHolder3.commit();

        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        TaxonNameBase name2 = taxon2.getName();
        assertSame(conversationHolder2.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());
        conversationHolder2.commit();

        conversationHolder3.bind();
        conversationHolder3.startTransaction();
        TaxonNameBase name3 = taxon3.getName();
        assertSame(conversationHolder3.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

        // Lazy loading somehow works without binding the session first
        TaxonNameBase name1 = taxon1.getName();
        assertNotSame(name1, name3);
        assertEquals(name1, name3);
        assertNotSame(conversationHolder1.getSession(), conversationHolder1.getSessionFactory().getCurrentSession());

    }


    /**
     * Testing of locking mechanism
     */
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testLocking(){

        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder1.bind();
        // first session, first transaction
        conversationHolder1.startTransaction();
        TaxonBase taxonBase = taxonService.find(taxonUuid1);
        // leave the first transaction without committing it

        // start a new session with a new transaction
        conversationHolder2.bind();
        conversationHolder2.startTransaction();
        TaxonBase taxonBase2 = taxonService.find(taxonUuid1);
        taxonBase.setSec(null);
        conversationHolder2.commit();
        // transaction of the second session got committed

        // return to the first session and commit its transaction
        conversationHolder1.bind();
        conversationHolder1.commit();

        conversationHolder1.startTransaction();
        conversationHolder1.lock(taxonBase, LockMode.READ);

    }

    @Ignore
    @Test
    @DataSet("ConcurrentSessionTest.xml")
    public void testSaveOrUpdate() {
        // this test deals with calling a save or saveOrUpdate after binding a conversation holder

        // initialise the conversation holders
        conversationHolder1 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);
        conversationHolder2 = new ConversationHolder(targetDataSource, sessionFactory, transactionManager);

        // bind the first one
        conversationHolder1.bind();
        // This is the call that makes sure that the 'taxonService.saveOrUpdate(taxonBase1);' call
        // later on does not flush the entire session.
        // The method to look at is 'processCommit(DefaultTransactionStatus status)' from
        // the class 'org.springframework.transaction.support.AbstractPlatformTransactionManager'
        // Without the following call the 'doCommit' method is called if 'status.isNewTransaction()' is true
        conversationHolder1.startTransaction();

        // load two taxon base objects
        TaxonBase taxonBase1 = taxonService.find(taxonUuid1);
        TaxonBase taxonBase2 = taxonService.find(taxonUuid2);

        // update taxon base object 1
        String titleCache1 = taxonBase1.getTitleCache();
        logger.info("Taxon 1 Title Cache : " + titleCache1);
        taxonBase1.setTitleCache(titleCache1 + "updated", false);
        logger.info("Taxon 1 Title Cache Updated: " + taxonBase1.getTitleCache());

        // update taxon base object 2
        String titleCache2 = taxonBase2.getTitleCache();
        logger.info("Taxon 2 Title Cache : " + titleCache2);
        taxonBase2.setTitleCache(titleCache2 + "updated", false);
        logger.info("Taxon 2 Title Cache Updated: " + taxonBase2.getTitleCache());

        // NOTE : Without the earlier 'conversationHolder1.startTransaction()' call, the
        // saveOrUpdate method will flush the entire session, in this case both
        // taxonBase1 and taxonBase2
        taxonService.saveOrUpdate(taxonBase1);

        conversationHolder2.bind();

        // Including the 'conversationHolder1.startTransaction()' call solves the
        // problem of the entire session being flushed, but throws the exception,
        // 'org.springframework.transaction.IllegalTransactionStateException:
        // Pre-bound JDBC Connection found! HibernateTransactionManager does
        // not support running within DataSourceTransactionManager if told to manage
        // the DataSource itself. It is recommended to use a single HibernateTransactionManager
        // for all transactions on a single DataSource, no matter whether Hibernate or JDBC access'.
        TaxonBase taxonBase1updated = taxonService.find(taxonUuid1);
        logger.info("Title Cache 1 New  Session: " + taxonBase1updated.getTitleCache());

        TaxonBase taxonBase2updated = taxonService.find(taxonUuid2);
        logger.info("Title Cache 2 New Session: " + taxonBase2updated.getTitleCache());

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }




}
