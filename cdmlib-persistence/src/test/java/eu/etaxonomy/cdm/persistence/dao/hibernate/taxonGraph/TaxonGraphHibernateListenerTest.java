/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.io.FileNotFoundException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Oct 1, 2018
 *
 */
public class TaxonGraphHibernateListenerTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    protected ITaxonGraphDao taxonGraphDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private IPreferenceDao prefDao;

    @SpringBeanByType
    private SessionFactory sessionFactory;

    private static boolean isRegistered;

    private static TaxonGraphHibernateListener taxonGraphHibernateListener = new TaxonGraphHibernateListener();

    @Before
    public void registerListener() {

        if(!TaxonGraphHibernateListenerTest.isRegistered){
            EventListenerRegistry listenerRegistry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
            listenerRegistry.appendListeners(EventType.POST_UPDATE, taxonGraphHibernateListener);
            listenerRegistry.appendListeners(EventType.POST_INSERT, taxonGraphHibernateListener);
            TaxonGraphHibernateListenerTest.isRegistered = true;
        }
        taxonGraphHibernateListener.setActive(true);
    }


    @After
    public void inactivateListener() {
        taxonGraphHibernateListener.setActive(false);
    }

    /**
     *
     */
    protected void setUuidPref() {
        PrefKey key = TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID;
        prefDao.set(new CdmPreference(key.getSubject(), key.getPredicate(), TaxonGraphTest.uuid_secRef.toString()));
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

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_t_argentinensis, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
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

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(nameDao.load(TaxonGraphTest.uuid_n_phacus_s), n_phacus, true);
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
            TaxonName n_trachelomonas_a = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_a);
            n_trachelomonas_a.setNomenclaturalReference(refX);
            nameDao.saveOrUpdate(n_trachelomonas_a);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_a, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
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

            TaxonName n_trachelomonas_o_var_d = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());

            n_trachelomonas_o_var_d.setRank(Rank.SPECIES());
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o), true);
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

            TaxonName n_trachelomonas_o_var_d = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
            edges = taxonGraphDao.edges(null, nameDao.load(TaxonGraphTest.uuid_n_euglena), true);
            Assert.assertEquals("No edges to 'Euglena' expected", 0, edges.size());

            n_trachelomonas_o_var_d.setGenusOrUninomial("Euglena");
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o), true);
            Assert.assertEquals("The edge to 'Trachelomonas oviformis' should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_euglena), true);
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

            TaxonName n_trachelomonas_o_var_d = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o_var_d);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o), true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());

            n_trachelomonas_o_var_d.setSpecificEpithet("alabamensis");
            nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
            commitAndStartNewTransaction();

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o), true);
            Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_a), true);
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

            TaxonName n_trachelomonas_o = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o);

            List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o_var_d), n_trachelomonas_o, true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
            edges = taxonGraphDao.edges(n_trachelomonas_o, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas) , true);
            Assert.assertEquals("One edge from 'Trachelomonas oviformis' to 'Trachelomonas' expected", 1, edges.size());

            n_trachelomonas_o.setSpecificEpithet("robusta");
            nameDao.saveOrUpdate(n_trachelomonas_o);
            commitAndStartNewTransaction();
            n_trachelomonas_o = nameDao.load(n_trachelomonas_o.getUuid());

            // printDataSet(System.err,"TaxonRelationship");
            edges = taxonGraphDao.edges(n_trachelomonas_o, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas) , true);
            Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
            edges = taxonGraphDao.edges(nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_o_var_d), n_trachelomonas_o, true);
            Assert.assertEquals("The edge from 'Trachelomonas oviformis var. duplex' should have been deleted", 0, edges.size());
            edges = taxonGraphDao.edges(nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_r_s), n_trachelomonas_o, true);
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
        // DataSet create by TaxonGraphTest.createTestDataSet()
    }
}
