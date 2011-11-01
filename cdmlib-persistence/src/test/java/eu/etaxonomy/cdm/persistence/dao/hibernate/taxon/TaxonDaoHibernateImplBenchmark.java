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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplBenchmark extends CdmTransactionalIntegrationTest {

//    @Rule
//    public MethodRule benchmarkRun = new BenchmarkRule();


    @SpringBeanByType
    private ITaxonDao taxonDao;


    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    IDefinedTermDao definedTermDao;

    private UUID acherontiaLachesis;

    private static final int BENCHMARK_ROUNDS = 20;

    @Before
    public void setUp() {
        acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
    }

    @After
    public void tearDown() {
        AuditEventContextHolder.clearContext();
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#TaxonDaoHibernateImpl()}.
     */
    @Test
    @DataSet(value="TaxonDaoHibernateImplTest.xml")
    public void testInit() {
        logger.warn("testInit()");
        assertNotNull("Instance of ITaxonDao expected",taxonDao);
        assertNotNull("Instance of IReferenceDao expected",referenceDao);
    }

    @Test
    @DataSet(value="TaxonDaoHibernateImplTest.xml")
    public void updateTaxon() {

        Taxon taxon = (Taxon) taxonDao.findByUuid(acherontiaLachesis);

        long startMillis = System.currentTimeMillis();
        for(int indx = 0; indx < BENCHMARK_ROUNDS; indx++){
            taxon.setTitleCache("Acherontia lachesis benchmark_" + indx + " Eitschberger, 2003", true);
            taxonDao.saveOrUpdate(taxon);
            logger.debug("[" + indx + "]" + taxon.getTitleCache());
        }
        double duration = ((double)(System.currentTimeMillis() - startMillis) ) / BENCHMARK_ROUNDS ;
        logger.info("Benchmark result - [update one Taxon] : " + duration + "ms (" + BENCHMARK_ROUNDS +" benchmark rounds )");
    }




















}
