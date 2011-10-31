/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TaxonNodeDaoHibernateImplBenchmark extends
        CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    private UUID acherontia_styx_NodeUuid;

    private Reference ref1;

    private Synonym syn1;

    private static final int BENCHMARK_ROUNDS = 20;

    @Before
    public void setUp(){

        acherontia_styx_NodeUuid = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");
        ref1 = ReferenceFactory.newBook();
        syn1 = Synonym.NewInstance(null, null);
        AuditEventContextHolder.clearContext();
    }

    @After
    public void tearDown(){
        AuditEventContextHolder.clearContext();
    }


    @Test
    @DataSet(value="TaxonNodeDaoHibernateImplTest.xml")
    public void testInit() {
        assertNotNull("Instance of ITaxonDao expected",taxonNodeDao);
        assertNotNull("Instance of IReferenceDao expected",classificationDao);
    }


    @Test
    @DataSet(value="TaxonNodeDaoHibernateImplTest.xml")
    public void taxaAddRemove() {
        TaxonNode parentNode = (TaxonNode) taxonNodeDao.findByUuid(acherontia_styx_NodeUuid);

        List<TaxonNode> newTaxonNodes = new ArrayList<TaxonNode>(BENCHMARK_ROUNDS);

        long startMillis = System.currentTimeMillis();
        for(int indx = 0; indx < BENCHMARK_ROUNDS; indx++){
            Taxon child = Taxon.NewInstance(null, null);

            child.setTitleCache("Acherontia lachesis benchmark_" + indx + " Eitschberger, 2003", true);
            TaxonNode childNode = parentNode.addChildTaxon(child, ref1, "p" + indx, syn1);
            newTaxonNodes.add(childNode);
            taxonNodeDao.saveOrUpdate(parentNode);
            logger.debug("[" + indx + "]" + child.getTitleCache());

        }
        double duration = ((double)(System.currentTimeMillis() - startMillis) ) / BENCHMARK_ROUNDS ;
        logger.info("Benchmark result - [add one TaxonNode] : " + duration + "ms (" + BENCHMARK_ROUNDS +" benchmark rounds )");

        startMillis = System.currentTimeMillis();
        for(TaxonNode childnode: newTaxonNodes){
            taxonNodeDao.delete(childnode);
        }
        duration = ((double)(System.currentTimeMillis() - startMillis))  / BENCHMARK_ROUNDS ;
        logger.info("Benchmark result - [delete one TaxonNode] : " + duration + "ms (" + BENCHMARK_ROUNDS +" benchmark rounds )");


    }
}
