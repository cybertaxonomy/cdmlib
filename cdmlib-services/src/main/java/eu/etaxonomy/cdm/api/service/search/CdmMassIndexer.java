/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.indexes.spi.DirectoryBasedIndexManager;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.spi.SearchIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * @author Andreas Kohlbecker
 * @date Dec 15, 2011
 *
 */
@Component
@Transactional
public class CdmMassIndexer implements ICdmMassIndexer {

    private final Set<Class<? extends CdmBase>> indexedClasses = new HashSet<Class<? extends CdmBase>>();
    public static final Logger logger = Logger.getLogger(CdmMassIndexer.class);

    /*
     * flag to enable old hibernate search 3.1 mode
     */
    private static final boolean HS_31_MODE = false;

    public HibernateTransactionManager transactionManager;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = (HibernateTransactionManager)transactionManager;
    }

    protected Session getSession(){
        Session session = transactionManager.getSessionFactory().getCurrentSession();
        return session;
    }

    /**
     * reindex method based on hibernate search  3.1
     *
     * @param type
     * @param monitor
     */
    protected <T extends CdmBase>void reindex_31(Class<T> type, IProgressMonitor monitor) {

        //TODO set the application in maintenance mode: making
        // queries to the index is not recommended when a MassIndexer is busy.
        // fullTextSession.createIndexer().startAndWait();

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        logger.info("start indexing " + type.getName());
        monitor.subTask("indexing " + type.getSimpleName());

        Long countResult = countEntities(type);
        int batchSize = sweetestBatchSize(type);
        int numOfBatches = calculateNumOfBatches(countResult, batchSize);

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask("Indexing " + type.getSimpleName(), numOfBatches);


        // Scrollable results will avoid loading too many objects in memory
        ScrollableResults results = fullTextSession.createCriteria(type).setFetchSize(batchSize).scroll(ScrollMode.FORWARD_ONLY);
        long index = 0;
        int batchesWorked = 0;


        try {
            while (results.next()) {
                index++;
                fullTextSession.index(results.get(0)); // index each element
                if (index % batchSize == 0 || index == countResult) {
                    batchesWorked++;
                    try {
                        fullTextSession.flushToIndexes(); // apply changes to indexes
                    } catch(ObjectNotFoundException e){
                        // TODO report this issue to progress monitor once it can report on errors
                        logger.error("possibly invalid data, thus skipping this batch and continuing with next one", e);
                    } finally {
                        fullTextSession.clear(); // clear since the queue is processed
                        getSession().clear(); // clear session to free memory
                        subMonitor.worked(1);
                        logger.info("\tbatch " + batchesWorked + "/" + numOfBatches + " processed");
                    }
                }
            }
        } catch (RuntimeException e) {
            //TODO better means to notify that the process has been stopped, using the STOPPED_WORK_INDICATOR is only a hack
            monitor.worked(RestServiceProgressMonitor.STOPPED_WORK_INDICATOR);
            monitor.done();
            throw	e;
        }
        logger.info("end indexing " + type.getName());
        subMonitor.done();
    }

    /**
     *
     *
     * @param type
     * @param monitor
     */
    protected <T extends CdmBase> void createDictionary(Class<T> type, IProgressMonitor monitor)  {
        String indexName = null;
        if(type.isAnnotationPresent(org.hibernate.search.annotations.Indexed.class)) {
            indexName = type.getAnnotation(org.hibernate.search.annotations.Indexed.class).index();
        } else {
            //TODO:give some indication that this class is infact not indexed
            return;
        }
        SearchFactory searchFactory = Search.getFullTextSession(getSession()).getSearchFactory();
        IndexManager indexManager = obtainIndexManager(searchFactory, indexName);

        IndexReader indexReader = searchFactory.getIndexReaderAccessor().open(type);
        List<String> idFields = getIndexedDeclaredFields(type);

        monitor.subTask("creating dictionary " + type.getSimpleName());

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask("Creating dictionary " + type.getSimpleName(), 1);

        Directory directory = ((DirectoryBasedIndexManager) indexManager).getDirectoryProvider().getDirectory();
        SpellChecker spellChecker = null;
        try {
            spellChecker = new SpellChecker(directory);
            Iterator<String> itr = idFields.iterator();
            while(itr.hasNext()) {
                String indexedField = itr.next();
                logger.info("creating dictionary for field " + indexedField);
                Dictionary dictionary = new LuceneDictionary(indexReader, indexedField);
                IndexWriterConfig iwc = new IndexWriterConfig(searchFactory.getAnalyzer(type));
                spellChecker.indexDictionary(dictionary, iwc, true);
            }
            subMonitor.internalWorked(1);
        } catch (IOException e) {
            logger.error("IOException when creating dictionary", e);
            //TODO better means to notify that the process has been stopped, using the STOPPED_WORK_INDICATOR is only a hack
            monitor.worked(RestServiceProgressMonitor.STOPPED_WORK_INDICATOR);
            monitor.done();
        } catch (RuntimeException e) {
            logger.error("RuntimeException when creating dictionary", e);
            //TODO better means to notify that the process has been stopped, using the STOPPED_WORK_INDICATOR is only a hack
            monitor.worked(RestServiceProgressMonitor.STOPPED_WORK_INDICATOR);
            monitor.done();
        } finally {
            searchFactory.getIndexReaderAccessor().close(indexReader);
        }
        if (spellChecker != null) {
            try {
                logger.info("closing spellchecker ");
                spellChecker.close();
            } catch (IOException e) {
                logger.error("IOException when closing spellchecker", e);
            }
        }

        logger.info("end creating dictionary " + type.getName());
        subMonitor.done();
    }

    private IndexManager obtainIndexManager(SearchFactory searchFactory, String indexName){
        SearchIntegrator searchIntegrator = searchFactory.unwrap(SearchIntegrator.class );
        IndexManager indexManager = searchIntegrator.getIndexManager(indexName);
        return indexManager;
    }

    private int sweetestBatchSize(Class<? extends CdmBase> type){

        Runtime.getRuntime().gc();
        long freeMemoryMB;
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        if(memoryMXBean != null){
            logger.debug("NonHeapMemoryUsage: "+memoryMXBean.getHeapMemoryUsage());
             MemoryUsage memusage = memoryMXBean.getHeapMemoryUsage();
             freeMemoryMB =( memusage.getMax() - memusage.getUsed()) / (1024 * 1024);
        } else {
            // will be smaller than the actual free mem since Runtime does not
            // know about Committed heap mem
            freeMemoryMB = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        }

        // TODO check for min free:
        // < 600MB => ERROR may fail with out of memory
        // < 750MB => WARNING may be slow
        if(freeMemoryMB < 600) {
            logger.error("The available free heap space appears to be too small (<600MB), the mass indexer may run out of memory!");
        }
        if(freeMemoryMB < 750) {
            logger.warn("The available free heap space appears to be small (<750MB), the mass indexer could be slow!");
        }

        double factor = 0.769; // default
        if(DescriptionElementBase.class.isAssignableFrom(type)) {
            factor = 0.025;
        }

        int batchSize = (int) Math.floor( factor * freeMemoryMB);
        logger.info("calculated batch size sweet spot for indexing " + type.getSimpleName()
                + " with " +  freeMemoryMB +  "MB free mem is " + batchSize);
        return batchSize;
    }

    /**
     * @param countResult
     * @return
     */
    private int calculateNumOfBatches(Long countResult, int batchSize) {
        Long numOfBatches =  countResult > 0 ? ((countResult-1)/batchSize)+1 : 0;
        return numOfBatches.intValue();
    }

    /**
     * @param type
     * @return
     */
    private <T> Long countEntities(Class<T> type) {
        Object countResultObj = getSession().createQuery("select count(*) from " + type.getName()).uniqueResult();
        Long countResult = (Long)countResultObj;
        return countResult;
    }

    protected <T extends CdmBase>void purge(Class<T> type, IProgressMonitor monitor) {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        logger.info("purging " + type.getName());
        fullTextSession.purgeAll(type);

        // TODO
        // toggle on/off flag doSpellIndex introduced for debugging, see ticket:
        //  #3721 (CdmMassIndexer.purge throwing errors due to LockObtainFailedException)
        // remove once this is fixed
        boolean doSpellIndex = false;

        if(doSpellIndex){
            SearchFactory searchFactory = fullTextSession.getSearchFactory();
            IndexManager indexManager = obtainIndexManager(searchFactory, type.getName());
            if(indexManager == null){
                logger.info("No IndexManager found for " + type.getName() + ", thus nothing to purge");
                return;
            }

            Directory directory = ((DirectoryBasedIndexManager) indexManager).getDirectoryProvider().getDirectory();
            SpellChecker spellChecker = null;
            try {
                spellChecker = new SpellChecker(directory);
                spellChecker.clearIndex();
            } catch (IOException e) {
                logger.error("IOException when creating dictionary", e);
                //TODO better means to notify that the process has been stopped, using the STOPPED_WORK_INDICATOR is only a hack
                monitor.worked(RestServiceProgressMonitor.STOPPED_WORK_INDICATOR);
                monitor.done();
            }

            if (spellChecker != null) {
                try {
                    logger.info("closing spellchecker ");
                    spellChecker.close();
                } catch (IOException e) {
                    logger.error("IOException when closing spellchecker", e);
                }
            }
        }
    }

    @Override
    public void reindex(Collection<Class<? extends CdmBase>> types, IProgressMonitor monitor){

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        if(types == null){
            types = indexedClasses();
        }

        if(HS_31_MODE) {

        }

        monitor.setTaskName("CdmMassIndexer");
        int steps = types.size() + (HS_31_MODE ? 1 /* +1 for optimize */ : 0);
        monitor.beginTask("Reindexing " + types.size() + " classes", steps);

        boolean optimize = true;

        long start = System.currentTimeMillis();
        for(Class<? extends CdmBase> type : types){
            long perTypeStart = System.currentTimeMillis();

            if(HS_31_MODE) {
                // TODO remove this mode and all related code once the old reindex method is vanished
                reindex_31(type, monitor);
            } else {
                reindex_55(type, monitor);
                optimize = false;
            }


            logger.info("Indexing of " + type.getSimpleName() + " in " + ((System.currentTimeMillis() - perTypeStart) / 1000) + "s");
        }

        if(HS_31_MODE) {
            monitor.subTask("Optimizing Index");
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
            subMonitor.beginTask("Optimizing Index",1);
            optimize();
            logger.info("end index optimization");
            subMonitor.worked(1);
            subMonitor.done();
        }
        logger.info("reindexing completed in " + ((System.currentTimeMillis() - start) / 1000) + "s");

        //monitor.worked(1);
        monitor.done();

    }

    /**
     * new reindex method which benefits from
     * the mass indexer available in hibernate search 5.5
     *
     * @param type
     * @param monitor
     * @throws InterruptedException
     */
    protected void reindex_55(Class<? extends CdmBase> type, IProgressMonitor monitor) {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());


        logger.info("start indexing " + type.getName());
        monitor.subTask("indexing " + type.getSimpleName());

        Long countResult = countEntities(type);
        int batchSize = sweetestBatchSize(type);
        int numOfBatches = calculateNumOfBatches(countResult * 2, batchSize); // each entity is worked two times 1. document added, 2. document build

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask("Indexing " + type.getSimpleName(), numOfBatches);


        MassIndexerProgressMonitor indexerMonitorWrapper = new MassIndexerProgressMonitorWrapper(subMonitor, batchSize);

        try {
            fullTextSession
            .createIndexer(type)
            .batchSizeToLoadObjects(batchSize)
            .cacheMode(CacheMode.IGNORE)
            .threadsToLoadObjects(4) // optimize http://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/#search-batchindexing-threadsandconnections
            .idFetchSize(150) //TODO optimize
            .progressMonitor(indexerMonitorWrapper)
            .startAndWait();
        } catch (InterruptedException ie) {
            logger.info("Mass indexer has been interrupted");
            subMonitor.isCanceled();
        }
    }

    @Override
    public void createDictionary(IProgressMonitor monitor) {
        if(monitor == null){
            monitor = new NullProgressMonitor();
        }

        monitor.setTaskName("CdmMassIndexer_Dictionary");
        int steps = dictionaryClasses().length; // +1 for optimize
        monitor.beginTask("Creating Dictionary " + dictionaryClasses().length + " classes", steps);

        for(Class type : dictionaryClasses()){
            createDictionary(type, monitor);
        }

        monitor.done();

    }
    protected void optimize() {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        fullTextSession.getSearchFactory().optimize();
        fullTextSession.flushToIndexes();
        fullTextSession.clear();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.IMassIndexer#purge()
     */
    @Override
    public void purge(IProgressMonitor monitor){

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }

        monitor.setTaskName("CdmMassIndexer");
        int steps = indexedClasses().size() + 1; // +1 for optimize
        monitor.beginTask("Purging " + indexedClasses().size() + " classes", steps);

        for(Class<? extends CdmBase> type : indexedClasses()){
            purge(type, monitor);
            monitor.worked(1);
        }
        // need to flush to the index before optimizing
        // the purge method is not doing the flushing by itself
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        fullTextSession.flushToIndexes();

        // optimize
        optimize();
        monitor.worked(1);

        // done
        monitor.done();
    }


    /**
     * Returns a list of declared indexable fields within a class through reflection.
     *
     * @param clazz
     * @return
     */
    private List<String> getIndexedDeclaredFields(Class clazz) {
        List<String> idFields = new ArrayList<String>();
        if(clazz.isAnnotationPresent(org.hibernate.search.annotations.Indexed.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for(int i=0;i<declaredFields.length;i++ ) {
                logger.info("checking field " + declaredFields[i].getName());
                if(declaredFields[i].isAnnotationPresent(org.hibernate.search.annotations.Field.class) ||
                        declaredFields[i].isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
                    idFields.add(declaredFields[i].getName());
                    logger.info("adding field " + declaredFields[i].getName());
                }
            }
        }
        return idFields;
    }
    /**
     * @return
     */
    @Override
    public Set<Class<? extends CdmBase>> indexedClasses() {
        // if no indexed classes have been 'manually' set then
        // the default is the full list
        if(indexedClasses.size() == 0) {
            indexedClasses.add(DescriptionElementBase.class);
            indexedClasses.add(TaxonBase.class);
            indexedClasses.add(Classification.class);
            indexedClasses.add(TaxonNameBase.class);
            indexedClasses.add(SpecimenOrObservationBase.class);
            indexedClasses.add(TaxonRelationship.class);
        }
        return indexedClasses;
    }

    /**
     * @return
     */
    @Override
    public Class[] dictionaryClasses() {
        return new Class[] {
                NonViralName.class
                };
    }

}
