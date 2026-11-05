/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * Integration tests for {@link CdmMassIndexer} / {@link ICdmMassIndexer}.
 * Uses {@link ITaxonService#findByFullText} only as a probe that purge and reindex
 * each changed the Lucene index.
 *
 * @author a.babadshanjan, a.kohlbecker
 * @author a.mueller (extracted from TaxonServiceSearchTest)
 */
public class CdmMassIndexerTest extends CdmServicesTransactionalSpringIntegrationTest {

    @Autowired
    private ICdmMassIndexer indexer;

    @Autowired
    private ITaxonService taxonService;

    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/api/service/TaxonServiceSearchTest.xml")
    public final void testPurge() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        boolean includeUnpublished = true;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByFullText(null, "Abies", null, subtree,
                includeUnpublished, null, true, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities after initial reindex");

        indexer.purge(null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entities since the index has been purged");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/api/service/TaxonServiceSearchTest.xml")
    public final void testReindex() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        boolean includeUnpublished = true;

        indexer.purge(null);
        commitAndStartNewTransaction(null);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByFullText(null, "Abies", null, subtree,
                includeUnpublished, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entities since the index has been purged");

        indexer.reindex(indexer.indexedClasses(), null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities after reindex");
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
