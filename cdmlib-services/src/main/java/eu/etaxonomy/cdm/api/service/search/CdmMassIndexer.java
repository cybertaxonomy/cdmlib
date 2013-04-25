// $Id$
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.indexes.impl.DirectoryBasedIndexManager;
import org.hibernate.search.store.DirectoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author Andreas Kohlbecker
 * @date Dec 15, 2011
 *
 */
@Component
@Transactional
public class CdmMassIndexer implements ICdmMassIndexer {

    public static final Logger logger = Logger.getLogger(CdmMassIndexer.class);

    private static final int BATCH_SIZE = 10000;

    public HibernateTransactionManager transactionManager;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = (HibernateTransactionManager)transactionManager;
    }

    protected Session getSession(){
        Session session = transactionManager.getSessionFactory().getCurrentSession();
        return session;
    }

    protected <T extends CdmBase>void reindex(Class<T> type, IProgressMonitor monitor) {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        logger.info("start indexing " + type.getName());
        monitor.subTask("indexing " + type.getSimpleName());

        Long countResult = countEntities(type);
        int numOfBatches = calculateNumOfBatches(countResult);

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, numOfBatches);
        subMonitor.beginTask("Indexing " + type.getSimpleName(), numOfBatches);

        // Scrollable results will avoid loading too many objects in memory
        ScrollableResults results = fullTextSession.createCriteria(type).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
        long index = 0;
        int batchesWorked = 0;

        try {
            while (results.next()) {
                index++;
                fullTextSession.index(results.get(0)); // index each element
                if (index % BATCH_SIZE == 0 || index == countResult) {
                    batchesWorked++;
                    fullTextSession.flushToIndexes(); // apply changes to indexes
                    fullTextSession.clear(); // clear since the queue is processed
                    //                calculateNumOfBatches(index == countResult ? countResult : index);
                    logger.info("\tbatch " + batchesWorked + "/" + numOfBatches + " processed");
                    subMonitor.internalWorked(1);
                    //if(index / BATCH_SIZE > 10 ) break;
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
        SearchFactoryImplementor searchFactory = (SearchFactoryImplementor)Search.getFullTextSession(getSession()).getSearchFactory();
        IndexManager indexManager = searchFactory.getAllIndexesManager().getIndexManager(indexName);
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
    			IndexWriterConfig iwc = new IndexWriterConfig(Configuration.luceneVersion, searchFactory.getAnalyzer(type));
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
    
    /**
     * @param countResult
     * @return
     */
    private int calculateNumOfBatches(Long countResult) {
        Long numOfBatches =  countResult > 0 ? ((countResult-1)/BATCH_SIZE)+1 : 0;
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

        
        SearchFactoryImplementor searchFactory = (SearchFactoryImplementor)Search.getFullTextSession(getSession()).getSearchFactory();
        IndexManager indexManager = searchFactory.getAllIndexesManager().getIndexManager(type.getName());        
        Directory directory = ((DirectoryBasedIndexManager) indexManager).getDirectoryProvider().getDirectory();
        SpellChecker spellChecker = null;
    	try {    		
    		spellChecker = new SpellChecker(directory);
    		spellChecker.clearIndex();
    	} catch (IOException e) {
    		logger.error("IOException when creating dictionary", e);
    		//TODO better means to notify that the process has been stopped, using the STOPPED_WORK_INDICATOR is only a hack
            //monitor.worked(RestServiceProgressMonitor.STOPPED_WORK_INDICATOR);
            //monitor.done();
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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.IMassIndexer#reindex()
     */
    @Override
    public void reindex(IProgressMonitor monitor){

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }

        monitor.setTaskName("CdmMassIndexer");
        int steps = totalBatchCount() + 1; // +1 for optimize
        monitor.beginTask("Reindexing " + indexedClasses().length + " classes", steps);

        for(Class type : indexedClasses()){
            reindex(type, monitor);
        }
        optimize(monitor);
        monitor.done();
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
    protected void optimize(IProgressMonitor monitor) {

        monitor.subTask("optimizing");
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        fullTextSession.getSearchFactory().optimize();

        subMonitor.beginTask("optimizing", 1);
        subMonitor.done();
    }

    /**
     * @return
     */
    private int totalBatchCount() {
        int totalNumOfBatches = 0;
        for(Class type : indexedClasses()){
            totalNumOfBatches += calculateNumOfBatches(countEntities(type));
        }
        return totalNumOfBatches;
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
        int steps = indexedClasses().length; // +1 for optimize
        monitor.beginTask("Purging " + indexedClasses().length + " classes", steps);

        for(Class type : indexedClasses()){
            purge(type, monitor);
            monitor.worked(1);
        }

//        // need to commit and start new transaction before optimizing
//        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
//        Transaction tx = fullTextSession.getTransaction();
//        tx.commit();
//        fullTextSession.beginTransaction(); // will be committed automatically at the end of this method since this class is transactional

//        optimize(monitor);

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
    public Class[] indexedClasses() {
        return new Class[] {
                DescriptionElementBase.class,
                Classification.class,
                TaxonBase.class,
                TaxonNameBase.class
                };
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