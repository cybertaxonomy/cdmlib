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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
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

    @Before
    public void setSecRef(){
        taxonGraphDao.setSecReferenceUUID(TaxonGraphTest.uuid_secRef);
    }

    @Before
    public void registerListener() {
        taxonGraphDao.enableHibernateListener(true);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testnewTaxonName() throws TaxonGraphException{

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
        n_t_argentinensis = nameDao.save(n_t_argentinensis);
        commitAndStartNewTransaction();

         // printDataSet(System.err,"TaxonRelationship");
        n_t_argentinensis = nameDao.load(n_t_argentinensis.getUuid());
        Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_t_argentinensis, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeNomRef() throws TaxonGraphException{

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);


        printDataSet(System.err,"TaxonRelationship");
        TaxonName n_trachelomonas_a = nameDao.load(TaxonGraphTest.uuid_n_trachelomonas_a);
        n_trachelomonas_a.setNomenclaturalReference(refX);
        nameDao.saveOrUpdate(n_trachelomonas_a);
        commitAndStartNewTransaction();

        printDataSet(System.err,"TaxonRelationship");

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_a, nameDao.load(TaxonGraphTest.uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeRank() throws TaxonGraphException{

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
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeGenus() throws TaxonGraphException{

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
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
    public void testChangeSepcificEpithet() throws TaxonGraphException{

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
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // DataSet create by TaxonGraphTest.createTestDataSet()
    }
}
