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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.scope.LuceneIndexScope;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
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
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * @author Andreas Kohlbecker
 * @since Dec 15, 2011
 */
@Component
@Transactional
public class CdmMassIndexer implements ICdmMassIndexer {

    private static final Logger logger = LogManager.getLogger();

    private final Set<Class<? extends CdmBase>> indexedClasses = new HashSet<>();

    private HibernateTransactionManager transactionManager;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = (HibernateTransactionManager)transactionManager;
    }

    protected Session getSession(){
        Session session = transactionManager.getSessionFactory().getCurrentSession();
        return session;
    }

    protected <T extends CdmBase> void createDictionary(Class<T> type, IProgressMonitor monitor)  {
        if(!type.isAnnotationPresent(Indexed.class)) {
            //TODO:give some indication that this class is in fact not indexed
            return;
        }

        SearchMapping mapping = Search.mapping(
                transactionManager.getSessionFactory());
        LuceneIndexScope scope = mapping.scope(type).extension(LuceneExtension.get());
        IndexReader indexReader = scope.openIndexReader();
        List<String> idFields = getIndexedDeclaredFields(type);

        monitor.subTask("creating dictionary " + type.getSimpleName());

        SubProgressMonitor subMonitor = SubProgressMonitor.NewInstance(monitor, 1);
        subMonitor.beginTask("Creating dictionary " + type.getSimpleName(), 1);

        SpellChecker spellChecker = null;
        try {
            // Use a RAM directory for the spell checker index; the former HS5
            // DirectoryBasedIndexManager API is no longer available.
            Directory directory = org.apache.lucene.store.NIOFSDirectory.open(
                    java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"),
                            "cdm-spellcheck", type.getSimpleName()));
            spellChecker = new SpellChecker(directory);
            Iterator<String> itr = idFields.iterator();
            while(itr.hasNext()) {
                String indexedField = itr.next();
                logger.info("creating dictionary for field " + indexedField);
                Dictionary dictionary = new LuceneDictionary(indexReader, indexedField);
                IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
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
            try {
                indexReader.close();
            } catch (IOException e) {
                logger.error("IOException when closing index reader", e);
            }
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
        logger.info("calculated batch size sweet spot for indexing " + type.getSimpleName()
                + " with " +  freeMemoryMB +  "MB free mem is " + batchSize);
        return batchSize;
    }

    private int calculateNumOfBatches(Long countResult, int batchSize) {
        Long numOfBatches =  countResult > 0 ? ((countResult-1)/batchSize)+1 : 0;
        return numOfBatches.intValue();
    }

    private <T> Long countEntities(Class<T> type) {
        Object countResultObj = getSession().createQuery("select count(*) from " + type.getName()).uniqueResult();
        Long countResult = (Long)countResultObj;
        return countResult;
    }

    protected <T extends CdmBase>void purge(Class<T> type, IProgressMonitor monitor) {

        logger.info("purging " + type.getName());
        Search.session(getSession()).workspace(type).purge();

        // Spell-check index purge relied on HS5 DirectoryBasedIndexManager; disabled under HS6.
        boolean doSpellIndex = false;

        if(doSpellIndex){
            logger.warn("Spell index purge is not supported under Hibernate Search 6");
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
     * Reindex method which uses
     * the mass indexer available in hibernate search 5.5
     *
     * @param type
     * @param monitor
     * @throws InterruptedException
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
            Search.session(getSession())
                .massIndexer(type)
                .batchSizeToLoadObjects(batchSize)
                .cacheMode(CacheMode.IGNORE)
                .threadsToLoadObjects(4)
                .idFetchSize(150)
                .monitor(indexerMonitorWrapper)
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

        for(Class<?> type : dictionaryClasses()){
            createDictionary((Class)type, monitor);
        }

        monitor.done();

    }
    protected void optimize() {
        Search.session(getSession()).workspace().mergeSegments();
        getSession().clear();
    }

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

        // optimize
        optimize();
        monitor.worked(1);

        // done
        monitor.done();
    }


    /**
     * Returns a list of declared indexable fields within a class through reflection.
     */
    private List<String> getIndexedDeclaredFields(Class<?> clazz) {
        List<String> idFields = new ArrayList<String>();
        if(clazz.isAnnotationPresent(Indexed.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for(int i=0;i<declaredFields.length;i++ ) {
                logger.info("checking field " + declaredFields[i].getName());
                if(declaredFields[i].isAnnotationPresent(FullTextField.class) ||
                        declaredFields[i].isAnnotationPresent(KeywordField.class) ||
                        declaredFields[i].isAnnotationPresent(GenericField.class)) {
                    idFields.add(declaredFields[i].getName());
                    logger.info("adding field " + declaredFields[i].getName());
                }
            }
        }
        return idFields;
    }

    @Override
    public Set<Class<? extends CdmBase>> indexedClasses() {
        // if no indexed classes have been 'manually' set then
        // the default is the full list
        if(indexedClasses.size() == 0) {
            indexedClasses.add(DescriptionElementBase.class);
            indexedClasses.add(TaxonBase.class);
            indexedClasses.add(Classification.class);
            indexedClasses.add(TaxonName.class);
            indexedClasses.add(SpecimenOrObservationBase.class);
            indexedClasses.add(TaxonRelationship.class);

        }
        return indexedClasses;
    }

    @Override
    public Class<?>[] dictionaryClasses() {
        return new Class[] {
                TaxonName.class
        };
    }
}