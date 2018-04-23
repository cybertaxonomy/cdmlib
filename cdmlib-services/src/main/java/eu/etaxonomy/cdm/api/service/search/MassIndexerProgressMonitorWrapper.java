/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.batchindexing.impl.SimpleIndexingProgressMonitor;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * @author a.kohlbecker
 * @since Dec 7, 2015
 *
 */
public class MassIndexerProgressMonitorWrapper implements MassIndexerProgressMonitor {

    MassIndexerProgressMonitor massIndexerMonitor ;
    private final IProgressMonitor monitor;
    private final int batchSize;
    private long tickCount = 0;

    public IProgressMonitor monitor() {
        return monitor;
    }
    /**
     * @param monitor
     */
    public MassIndexerProgressMonitorWrapper(IProgressMonitor monitor, int batchSize) {
        this.monitor = monitor;
        this.batchSize = batchSize;
        this.massIndexerMonitor = new SimpleIndexingProgressMonitor(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void documentsAdded(long increment) {
        // all current implementations always pass 1l as parameter
        massIndexerMonitor.documentsAdded(increment);
        updatePerBatchMonitor((int)increment);

    }
    /**
     * @param increment
     *
     */
    private void updatePerBatchMonitor(int increment) {
        tickCount += increment;
        if(tickCount % (batchSize * 2) == 0) {
            // one batch worked
            monitor.worked(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void documentsBuilt(int number) {
        // unused as of implementing this
        massIndexerMonitor.documentsBuilt(number);
        updatePerBatchMonitor(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void entitiesLoaded(int size) {
        massIndexerMonitor.entitiesLoaded(size);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToTotalCount(long count) {
        massIndexerMonitor.addToTotalCount(count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexingCompleted() {
        massIndexerMonitor.indexingCompleted();
        monitor.done();
    }

}
