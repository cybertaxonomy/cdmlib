/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.search;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Integration tests for {@link CdmMassIndexer} / {@link ICdmMassIndexer}.
 * Uses {@link ITaxonService#findByFullText} only as a probe that purge and reindex
 * each changed the Lucene index.
 *
 * @author a.babadshanjan, a.kohlbecker
 * @author a.mueller (extracted from TaxonServiceSearchTest)
 */
public class CdmMassIndexerTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ICdmMassIndexer indexer;

    @SpringBeanByType
    private ITaxonService taxonService;

    @Test
    @DataSet(value = "/eu/etaxonomy/cdm/api/service/TaxonServiceSearchTest.xml",
            loadStrategy = CleanSweepInsertLoadStrategy.class)
    public final void testPurge() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        boolean includeUnpublished = true;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByFullText(null, "Abies", null, subtree,
                includeUnpublished, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 8 entities after initial reindex", 8, pager.getCount().intValue());

        indexer.purge(null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
        Assert.assertEquals("Expecting no entities since the index has been purged", 0, pager.getCount().intValue());
    }

    @Test
    @DataSet(value = "/eu/etaxonomy/cdm/api/service/TaxonServiceSearchTest.xml",
            loadStrategy = CleanSweepInsertLoadStrategy.class)
    public final void testReindex() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        boolean includeUnpublished = true;

        indexer.purge(null);
        commitAndStartNewTransaction(null);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByFullText(null, "Abies", null, subtree,
                includeUnpublished, null, true, null, null, null, null);
        Assert.assertEquals("Expecting no entities since the index has been purged", 0, pager.getCount().intValue());

        indexer.reindex(indexer.indexedClasses(), null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 8 entities after reindex", 8, pager.getCount().intValue());
    }

    private void refreshLuceneIndex() {
        commit();
        endTransaction();
        indexer.purge(DefaultProgressMonitor.NewInstance());
        indexer.reindex(indexer.indexedClasses(), DefaultProgressMonitor.NewInstance());
        startNewTransaction();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
