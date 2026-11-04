/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.entity.SearchIndexedEntity;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.persistence.hibernate.CdmSessionUtils;
import jakarta.persistence.EntityManager;

/**
 * @author Andreas Kohlbecker
 * @since Dec 15, 2011
 */
@Component
@Transactional
public class CdmMassIndexer implements ICdmMassIndexer {

    private static final Logger logger = LogManager.getLogger();

    private final Set<Class<? extends CdmBase>> indexedClasses = new HashSet<>();

    @Autowired
    private SessionFactory sessionFactory;

    private EntityManager getEntityManager(){
        return CdmSessionUtils.getTransactionalEntityManager(sessionFactory);
    }

    private SearchMapping getSearchMapping() {
        return Search.mapping(sessionFactory);
    }

    /**
     * Logs the configured Lucene index storage location (once per purge/reindex call).
     * Index files for each entity live under {@code <root>/<indexName>/}.
     */
    private void logIndexDirectory() {
        Map<String, Object> props = sessionFactory.getProperties();
        Object directoryType = props.get("hibernate.search.backend.directory.type");
        Object directoryRoot = props.get("hibernate.search.backend.directory.root");
        if (directoryRoot != null) {
            String absolute = Paths.get(directoryRoot.toString()).toAbsolutePath().normalize().toString();
            logger.info("Hibernate Search index location: type={}, root={} (absolute={})",
                    directoryType, directoryRoot, absolute);
        } else {
            logger.info("Hibernate Search index location: type={}, root=<not set>", directoryType);
        }
    }

    private int sweetestBatchSize(Class<? extends CdmBase> type){

        Runtime.getRuntime().gc();
        long freeMemoryMB;
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        if(memoryMXBean != null){
            logger.debug("NonHeapMemoryUsage: "+memoryMXBean.getHeapMemoryUsage());
             MemoryUsage memusage = memoryMXBean.getHeapMemoryUsage();
             freeMemoryMB = (memusage.getMax() - memusage.getUsed()) / (1024 * 1024);
        } else {
            // will be smaller than the actual free mem since Runtime does not
            // know about Committed heap mem
            freeMemoryMB = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        }

        // TODO check for min free:
        // < 600MB => WARNING may fail with out of memory
        // < 750MB => INFO may be slow
        if(freeMemoryMB < 600) {
            logger.warn("The available free heap space appears to be too small (<600MB), the mass indexer may run out of memory!");
        } else if(freeMemoryMB < 750) {
            logger.info("The available free heap space appears to be small (<750MB), the mass indexer could be slow!");
        }

        double factor = 0.769; // default
        if(DescriptionElementBase.class.isAssignableFrom(type)) {
            factor = 0.025;
        }

        int batchSize = (int) Math.floor( factor * freeMemoryMB);
        logger.info("Calculated batch size sweet spot for indexing " + type.getSimpleName()
                + " with " +  freeMemoryMB +  "MB free mem is " + batchSize);
        return batchSize;
    }

    private int calculateNumOfBatches(Long countResult, int batchSize) {
        Long numOfBatches =  countResult > 0 ? ((countResult-1)/batchSize)+1 : 0;
        return numOfBatches.intValue();
    }

    private <T> Long countEntities(Class<T> type) {
        return getEntityManager()
                .createQuery("SELECT count(*) FROM " + type.getName(), Long.class)
                .getSingleResult();
    }

    protected <T extends CdmBase>void purge(Class<T> type, IProgressMonitor monitor) {
        logger.info("purging " + type.getName());
        Search.session(getEntityManager()).workspace(type).purge();
    }

    @Override
    public void reindex(Collection<Class<? extends CdmBase>> types, IProgressMonitor monitor){

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        if(types == null){
            types = indexedClasses();
        }

        logIndexDirectory();

        monitor.setTaskName("CdmMassIndexer");
        int nSteps = types.size();
        monitor.beginTask("Reindexing " + nSteps + " classes", nSteps);

        var start = Instant.now();
        for(Class<? extends CdmBase> type : types){
            var perTypeStart = Instant.now();

            reindexSingleClass(type, monitor);

            logger.info("Indexing of " + type.getSimpleName() + " in " + Duration.between(perTypeStart, Instant.now()).toSeconds() + "s");
        }

        logger.info("reindexing completed in " + Duration.between(start, Instant.now()).toSeconds() + "s");

        //monitor.worked(1);
        monitor.done();

    }

    /**
     * Reindex one entity type via the Hibernate Search mass indexer.
     */
    protected void reindexSingleClass(Class<? extends CdmBase> type, IProgressMonitor monitor) {

        logger.info("start indexing " + type.getName());
        monitor.subTask("indexing " + type.getSimpleName());

        Long countResult = countEntities(type);
        int batchSize = sweetestBatchSize(type);
        int numOfBatches = calculateNumOfBatches(countResult * 2, batchSize); // each entity is worked two times 1. document added, 2. document build

        SubProgressMonitor subMonitor = SubProgressMonitor.NewInstance(monitor, 1);
        subMonitor.beginTask("Indexing " + type.getSimpleName(), numOfBatches);

        MassIndexingMonitor indexerMonitorWrapper = new MassIndexerProgressMonitorWrapper(subMonitor, batchSize);

        try {
            Search.session(getEntityManager())
                .massIndexer(type)
                .batchSizeToLoadObjects(batchSize)
                .cacheMode(CacheMode.IGNORE)
                .threadsToLoadObjects(4)
                .idFetchSize(resolveIdFetchSize())
                .monitor(indexerMonitorWrapper)
                .startAndWait();
        } catch (InterruptedException ie) {
            logger.info("Mass indexer has been interrupted");
            subMonitor.isCanceled();
        }
    }

    /**
     * MySQL/MariaDB need {@link Integer#MIN_VALUE} so the JDBC driver streams IDs
     * instead of preloading the whole result set. Other databases (e.g. H2) reject
     * negative fetch sizes.
     */
    private int resolveIdFetchSize() {
        Dialect dialect = sessionFactory.unwrap(SessionFactoryImplementor.class)
                .getJdbcServices().getDialect();
        if (dialect instanceof MySQLDialect) {
            return Integer.MIN_VALUE;
        }
        return 150;
    }

    protected void optimize() {
        Search.session(getEntityManager()).workspace().mergeSegments();
        getEntityManager().clear();
    }

    @Override
    public void purge(IProgressMonitor monitor){

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }

        logIndexDirectory();

        monitor.setTaskName("CdmMassIndexer");
        int steps = indexedClasses().size() + 1; // +1 for optimize
        monitor.beginTask("Purging " + indexedClasses().size() + " classes", steps);

        for(Class<? extends CdmBase> type : indexedClasses()){
            purge(type, monitor);
            monitor.worked(1);
        }

        // optimize
        optimize();
        monitor.worked(1);

        // done
        monitor.done();
    }

    @Override
    public Set<Class<? extends CdmBase>> indexedClasses() {

        // if no indexed classes have been 'manually' set then
        // the default is the full list
        if(indexedClasses.size() == 0) {
            SearchMapping mapping = getSearchMapping();
            Collection<? extends SearchIndexedEntity<?>> all = mapping.allIndexedEntities();
            all.forEach(sie->indexedClasses.add((Class<? extends CdmBase>)sie.javaClass()));
        }
        return indexedClasses;
    }
}
