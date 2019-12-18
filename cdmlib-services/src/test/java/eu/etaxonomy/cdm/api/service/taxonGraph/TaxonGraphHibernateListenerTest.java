/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.IRunAs;
import eu.etaxonomy.cdm.api.application.RunAsAdmin;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph.TaxonGraphDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Oct 1, 2018
 */
public class TaxonGraphHibernateListenerTest extends CdmTransactionalIntegrationTestWithSecurity {

    @SpringBeanByType
    protected ITaxonGraphDao taxonGraphDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private IPreferenceDao prefDao;

    @SpringBeanByName
    private AuthenticationProvider runAsAuthenticationProvider;

    @SpringBeanByType
    private SessionFactory sessionFactory;

    private static boolean isRegistered;

    protected static UUID uuid_secRef = UUID.fromString("34e1ff99-63c4-4296-81b6-b20afb98902e");

    protected static UUID uuid_n_euglenophyceae = UUID.fromString("9928147d-4499-4ce9-bcf3-e4eaa13e509e");
    protected static UUID uuid_n_euglena = UUID.fromString("ab59d853-dd4f-4f80-bd7b-cf53bfd42d39");
    protected static UUID uuid_n_trachelomonas = UUID.fromString("5e3d015c-0a5c-4975-a3b0-334b4b47ff79");
    protected static UUID uuid_n_trachelomonas_a  = UUID.fromString("a798721a-e305-420d-aec1-e915ad1971e4");
    protected static UUID uuid_n_trachelomonas_o  = UUID.fromString("a2e7eeff-b844-4b3d-ab75-2a113b44573e");
    protected static UUID uuid_n_trachelomonas_o_var_d  = UUID.fromString("d8a0e3ad-2a4d-45ed-b874-f96616015f91");
    protected static UUID uuid_n_trachelomonas_s  = UUID.fromString("5b90bd58-7f76-45c4-9966-7f65e7bf0bb0");
    protected static UUID uuid_n_trachelomonas_s_var_a = UUID.fromString("192ad8a1-55ca-4379-87a1-3bbd04e8b880");
    protected static UUID uuid_n_trachelomonas_r_s = UUID.fromString("2d6e68bf-aba7-433d-8325-ea15f3e567f4");
    protected static UUID uuid_n_phacus_s = UUID.fromString("d59b8715-1b98-4da4-a42d-efcbe85b323c");

    protected static UUID uuid_t_euglenophyceae = UUID.fromString("4ea17d7a-17a3-41f0-8de6-e924494ecbae");
    protected static UUID uuid_t_euglena = UUID.fromString("1c69afd4-ae58-4913-8706-5c89729d38f4");
    protected static UUID uuid_t_trachelomonas = UUID.fromString("52b9a8e0-9133-4ee0-ba9f-84ca6e28d033");
    protected static UUID uuid_t_trachelomonas_a  = UUID.fromString("04443b64-f2e5-48c5-9069-9354f43ded9f");
    protected static UUID uuid_t_trachelomonas_o  = UUID.fromString("bdf75350-8361-4e33-a614-a4214cc3e90a");
    protected static UUID uuid_t_trachelomonas_o_var_d  = UUID.fromString("f54ad8cf-fe87-499d-826a-2c5a71551fcf");
    protected static UUID uuid_t_trachelomonas_s  = UUID.fromString("5dce8a09-c809-4027-a9ce-b70901e7b820");
    protected static UUID uuid_t_trachelomonas_s_var_a = UUID.fromString("3f14c528-e191-4a6f-b2a9-36c9a3fc7eee");

    private static TaxonGraphHibernateListener taxonGraphHibernateListener = new TaxonGraphHibernateListener();

    @Before
    public void registerListener() throws NoSuchMethodException, SecurityException {

        if(!TaxonGraphHibernateListenerTest.isRegistered){
            EventListenerRegistry listenerRegistry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
            listenerRegistry.appendListeners(EventType.POST_UPDATE, taxonGraphHibernateListener);
            listenerRegistry.appendListeners(EventType.POST_INSERT, taxonGraphHibernateListener);
            TaxonGraphHibernateListenerTest.isRegistered = true;
        }
        //
        taxonGraphHibernateListener.registerProcessClass(TaxonGraphBeforeTransactionCompleteProcess.class, new Object[]{new RunAsAdmin(runAsAuthenticationProvider)}, new Class[]{IRunAs.class});
    }


    @After
    public void inactivateListener() {
        taxonGraphHibernateListener.unRegisterProcessClass(TaxonGraphBeforeTransactionCompleteProcess.class);
    }

    /**
     *
     */
    protected void setUuidPref() {
        PrefKey key = TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID;
        prefDao.set(new CdmPreference(key.getSubject(), key.getPredicate(), uuid_secRef.toString()));
        commitAndStartNewTransaction();
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testNewTaxonName() throws TaxonGraphException{

        try{
            setUuidPref();

            Reference refX = ReferenceFactory.newBook();
            refX.setTitleCache("Ref-X", true);

            TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
            n_t_argentinensis = nameDao.save(n_t_argentinensis);
            commitAndStartNewTransaction();

            n_t_argentinensis = nameDao.load(n_t_argentinensis.getUuid());
            Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_t_argentinensis, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals(1, edges.size());
            Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testNewGenusName() throws TaxonGraphException{

        try{
            setUuidPref();

            Reference refX = ReferenceFactory.newBook();
            refX.setTitleCache("Ref-X", true);

            TaxonName n_phacus = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Phacus", null, null, null, null, refX, null, null);
            nameDao.save(n_phacus);
            commitAndStartNewTransaction();

            n_phacus = nameDao.load(n_phacus.getUuid());
            Assert.assertTrue("a taxon should have been created", n_phacus.getTaxa().size() > 0);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(nameDao.load(uuid_n_phacus_s), n_phacus, true);
            Assert.assertEquals(1, edges.size());
            Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeNomRef() throws TaxonGraphException{
        try {
            setUuidPref();

            Reference refX = ReferenceFactory.newBook();
            refX.setTitleCache("Ref-X", true);

            // printDataSet(System.err,"TaxonRelationship");
            TaxonName n_trachelomonas_a = nameDao.load(uuid_n_trachelomonas_a);
            n_trachelomonas_a.setNomenclaturalReference(refX);
            nameDao.saveOrUpdate(n_trachelomonas_a);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(nameDao.load(uuid_n_trachelomonas_a), nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals(1, edges.size());
            Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeRank() throws TaxonGraphException{

        try {
            setUuidPref();

            TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());

            n_trachelomonas_o_var_d.setRank(Rank.SPECIES());
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
            Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeGenus() throws TaxonGraphException{

        try {
            setUuidPref();

            TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
            edges = taxonGraphDao.edges(null, nameDao.load(uuid_n_euglena), true);
            Assert.assertEquals("No edges to 'Euglena' expected", 0, edges.size());

            n_trachelomonas_o_var_d.setGenusOrUninomial("Euglena");
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
            Assert.assertEquals("The edge to 'Trachelomonas oviformis' should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_euglena), true);
            Assert.assertEquals("The edge to 'Euglena' should have been created", 1, edges.size());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeSpecificEpithet_of_InfraSpecific() throws TaxonGraphException{

        try {
            setUuidPref();

            TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());

            n_trachelomonas_o_var_d.setSpecificEpithet("alabamensis");
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
            Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_a), true);
            Assert.assertEquals("The edge to Trachelomonas alabamensis should have been created", 1, edges.size());

        } finally {
            rollback();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeSpecificEpithet_of_Species() throws TaxonGraphException{

        try {
            setUuidPref();

            TaxonName n_trachelomonas_o = nameDao.load(uuid_n_trachelomonas_o);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(nameDao.load(uuid_n_trachelomonas_o_var_d), n_trachelomonas_o, true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o, nameDao.load(uuid_n_trachelomonas) , true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis' to 'Trachelomonas' expected", 1, edges.size());

            n_trachelomonas_o.setSpecificEpithet("robusta");
            nameDao.saveOrUpdate(n_trachelomonas_o);
            commitAndStartNewTransaction();
            n_trachelomonas_o = nameDao.load(n_trachelomonas_o.getUuid());

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o, nameDao.load(uuid_n_trachelomonas) , true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(nameDao.load(uuid_n_trachelomonas_o_var_d), n_trachelomonas_o, true);
            Assert.assertEquals("The edge from 'Trachelomonas oviformis var. duplex' should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(nameDao.load(uuid_n_trachelomonas_r_s), n_trachelomonas_o, true);
            Assert.assertEquals("The edge to 'Trachelomonas robusta var. sparsiornata' should have been created", 1, edges.size());

        } finally {
            rollback();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // DataSet create by createTestDataSet()
    }
}
