/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.RevisionStatus;
import eu.etaxonomy.cdm.model.common.RevisionStatusInfo;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author muellera
 * @since 09.10.2026
 * @see #11040
 */
public class RevisionStatusChangeListenerTest extends CdmTransactionalSpringIntegrationTest {

    @Autowired
    private TaxonDaoHibernateImpl taxonDao;

    @Autowired
    private IDefinedTermDao definedTermDao;

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testAutoSetChangedOnStatusChangeOnly() {

        RevisionStatus completed = (RevisionStatus) definedTermDao.findByUuid(RevisionStatus.uuidCompleted);
        RevisionStatus inProcess = (RevisionStatus) definedTermDao.findByUuid(RevisionStatus.uuidInProcess);
        assertNotNull(completed);
        assertNotNull(inProcess);

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
        assertNotNull(taxon.getRevisionStatus());
        assertEquals(completed.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        ZonedDateTime firstChanged = taxon.getRevisionStatus().getChanged();
        assertNotNull(firstChanged, "changed must be set automatically when status is set");
        assertFalse(firstChanged.toInstant().isBefore(beforeFirstChange.toInstant()), "changed should be at/after the status change");

        // unrelated update must not touch changed
        taxon.setConceptId("concept-1");
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        assertEquals("concept-1", taxon.getConceptId());
        assertEquals(firstChanged.toInstant(), taxon.getRevisionStatus().getChanged().toInstant(), "changed must stay unchanged on unrelated updates");

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
        assertEquals(inProcess.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        ZonedDateTime secondChanged = taxon.getRevisionStatus().getChanged();
        assertNotNull(secondChanged);
        assertFalse(secondChanged.toInstant().isBefore(beforeSecondChange.toInstant()));
        assertTrue(secondChanged.toInstant().isAfter(firstChanged.toInstant()), "changed must be updated when status changes");

        // explicit changed timestamp must be preserved
        ZonedDateTime explicit = ZonedDateTime.parse("2020-01-15T10:00:00+01:00[Europe/Berlin]");
        taxon.setRevisionStatus(RevisionStatusInfo.NewInstance(completed, explicit));
        taxonDao.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = (Taxon) taxonDao.findByUuid(taxon.getUuid());
        assertEquals(completed.getUuid(), taxon.getRevisionStatus().getStatus().getUuid());
        assertEquals(explicit.toInstant(), taxon.getRevisionStatus().getChanged().toInstant(), "explicitly provided changed must be preserved");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
