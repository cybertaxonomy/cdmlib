/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
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
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    IDefinedTermDao definedTermDao;

    private UUID uuid;
    private UUID sphingidae;
    private UUID acherontia;
    private UUID mimas;
    private UUID rethera;
    private UUID retheraSecCdmtest;
    private UUID atroposAgassiz;
    private UUID atroposLeach;
    private UUID acherontiaLachesis;
    private AuditEvent previousAuditEvent;
    private AuditEvent mostRecentAuditEvent;

    private UUID northernAmericaUuid;
    private UUID southernAmericaUuid;
    private UUID antarcticaUuid;

    private UUID classificationUuid;

    private Taxon taxonAcherontia;


    @Before
    public void setUp() {

        uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
        sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
        acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
        acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
        atroposAgassiz = UUID.fromString("d75b2e3d-7394-4ada-b6a5-93175b8751c1");
        atroposLeach =  UUID.fromString("3da4ab34-6c50-4586-801e-732615899b07");
        rethera = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
        retheraSecCdmtest = UUID.fromString("a9f42927-e507-433a-9629-62073a908aae");


        mimas = UUID.fromString("900052b7-b69c-4e26-a8f0-01c215214c40");
        previousAuditEvent = new AuditEvent();
        previousAuditEvent.setRevisionNumber(1025);
        previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
        mostRecentAuditEvent = new AuditEvent();
        mostRecentAuditEvent.setRevisionNumber(1026);
        mostRecentAuditEvent.setUuid(UUID.fromString("afe8e761-8545-497b-9134-6a6791fc0b0d"));
        AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)

        northernAmericaUuid = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
        southernAmericaUuid = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");
        antarcticaUuid = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");

        classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");

        taxonAcherontia = (Taxon)taxonDao.findByUuid(acherontia);
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
    public void testDelete() {
        assert taxonAcherontia != null : "taxon must exist";
        taxonDao.delete(taxonAcherontia);
        setComplete();
        endTransaction();
    }

//    @Test
    @DataSet(value="TaxonDaoHibernateImplTest.xml")
    public void testAddTaxon() {
        assert taxonAcherontia != null : "taxon must exist";
        taxonDao.delete(taxonAcherontia);
        setComplete();
        endTransaction();
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
