/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.util.OptionalLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingTypeGroupMonitor;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingTypeGroupMonitorContext;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingTypeGroupMonitorCreateContext;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * Adapts Hibernate Search {@link MassIndexingMonitor} to the CDM {@link IProgressMonitor}.
 *
 * @author a.kohlbecker
 * @since Dec 7, 2015
 */
public class MassIndexerProgressMonitorWrapper implements MassIndexingMonitor {

    private static final Logger logger = LogManager.getLogger();

    private static final MassIndexingTypeGroupMonitor NO_OP_TYPE_GROUP_MONITOR = new MassIndexingTypeGroupMonitor() {
        @Override
        public void documentsIndexed(long increment) {
            // progress is driven by MassIndexingMonitor.documentsAdded/documentsBuilt
        }

        @Override
        public void indexingStarted(MassIndexingTypeGroupMonitorContext context) {
            // no-op
        }

        @Override
        public void indexingCompleted(MassIndexingTypeGroupMonitorContext context) {
            // no-op
        }
    };

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
    public MassIndexingTypeGroupMonitor typeGroupMonitor(MassIndexingTypeGroupMonitorCreateContext context) {
        OptionalLong totalCount = context.totalCount();
        if (totalCount.isPresent()) {
            logger.debug("Mass indexing total count: {}", totalCount.getAsLong());
        }
        return NO_OP_TYPE_GROUP_MONITOR;
    }

    @Override
    public void indexingCompleted() {
        monitor.done();
    }
}
