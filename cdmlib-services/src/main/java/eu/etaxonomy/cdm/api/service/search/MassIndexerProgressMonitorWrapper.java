/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * Adapts Hibernate Search 6 {@link MassIndexingMonitor} to the CDM {@link IProgressMonitor}.
 *
 * @author a.kohlbecker
 * @since Dec 7, 2015
 */
public class MassIndexerProgressMonitorWrapper implements MassIndexingMonitor {

    private static final Logger logger = LogManager.getLogger();

    private final IProgressMonitor monitor;
    private final int batchSize;
    private long tickCount = 0;

    public IProgressMonitor monitor() {
        return monitor;
    }

    public MassIndexerProgressMonitorWrapper(IProgressMonitor monitor, int batchSize) {
        this.monitor = monitor;
        this.batchSize = batchSize;
    }

    @Override
    public void documentsAdded(long increment) {
        updatePerBatchMonitor((int) increment);
    }

    private void updatePerBatchMonitor(int increment) {
        tickCount += increment;
        if(tickCount % (batchSize * 2L) == 0) {
            monitor.worked(1);
        }
    }

    @Override
    public void documentsBuilt(long number) {
        updatePerBatchMonitor((int) number);
    }

    @Override
    public void entitiesLoaded(long size) {
        // no-op; progress is driven by documentsAdded/documentsBuilt
    }

    @Override
    public void addToTotalCount(long count) {
        logger.debug("Mass indexing total count: {}", count);
    }

    @Override
    public void indexingCompleted() {
        monitor.done();
    }
}
