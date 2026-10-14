/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.RevisionStatus;
import eu.etaxonomy.cdm.model.common.RevisionStatusInfo;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author muellera
 * @since 09.10.2026
 * @see #11040
 */
public class RevisionStatusChangeListenerTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private TaxonDaoHibernateImpl taxonDao;

    @SpringBeanByType
    private IDefinedTermDao definedTermDao;

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
    })
    public void testAutoSetChangedOnStatusChangeOnly() {

        RevisionStatus completed = (RevisionStatus) definedTermDao.findByUuid(RevisionStatus.uuidCompleted);
        RevisionStatus inProcess = (RevisionStatus) definedTermDao.findByUuid(RevisionStatus.uuidInProcess);
        Assert.assertNotNull(completed);
        Assert.assertNotNull(inProcess);

        Taxon taxon = Taxon.NewInstance(null, null);
        taxonDao.save(taxon);
        commitAndStartNewTransaction(null);

        // update with status but without changed → listener sets changed
        ZonedDateTime beforeFirstChange = ZonedDateTime.now().minusMinutes(1);
        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        taxon.setRevisionStatus(RevisionStatusInfo.NewInstance(completed, null));
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        Assert.assertNotNull(taxon.getRevisionStatus());
        Assert.assertEquals(completed.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        ZonedDateTime firstChanged = taxon.getRevisionStatus().getChanged();
        Assert.assertNotNull("changed must be set automatically when status is set", firstChanged);
        Assert.assertFalse("changed should be at/after the status change",
                firstChanged.toInstant().isBefore(beforeFirstChange.toInstant()));

        // unrelated update must not touch changed
        taxon.setConceptId("concept-1");
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        Assert.assertEquals("concept-1", taxon.getConceptId());
        Assert.assertEquals("changed must stay unchanged on unrelated updates",
                firstChanged.toInstant(), taxon.getRevisionStatus().getChanged().toInstant());

        // status change without explicit changed → listener updates changed
        try {
            Thread.sleep(1100); // ensure timestamp differs at second precision
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ZonedDateTime beforeSecondChange = ZonedDateTime.now().minusMinutes(1);
        taxon.getRevisionStatus().setStatus(inProcess);
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        Assert.assertEquals(inProcess.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        ZonedDateTime secondChanged = taxon.getRevisionStatus().getChanged();
        Assert.assertNotNull(secondChanged);
        Assert.assertFalse(secondChanged.toInstant().isBefore(beforeSecondChange.toInstant()));
        Assert.assertTrue("changed must be updated when status changes",
                secondChanged.toInstant().isAfter(firstChanged.toInstant()));

        // explicit changed timestamp must be preserved
        ZonedDateTime explicit = ZonedDateTime.parse("2020-01-15T10:00:00+01:00[Europe/Berlin]");
        taxon.setRevisionStatus(RevisionStatusInfo.NewInstance(completed, explicit));
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        Assert.assertEquals(completed.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        Assert.assertEquals("explicitly provided changed must be preserved",
                explicit.toInstant(), taxon.getRevisionStatus().getChanged().toInstant());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
