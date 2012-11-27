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

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
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

    private static final int BATCH_SIZE = 100;

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
        Transaction transaction = fullTextSession.beginTransaction();

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
        //transaction.commit(); // no need to commit, transaction will be committed automatically
        logger.info("end indexing " + type.getName());
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
        fullTextSession.getSearchFactory().optimize();
        //transaction.commit(); // no need to commit, transaction will be committed automatically
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
        int steps = indexedClasses().length + 1; // +1 for optimize
        monitor.beginTask("Purging " + indexedClasses().length + " classes", steps);

        for(Class type : indexedClasses()){
            purge(type, monitor);
            monitor.worked(1);
        }
        optimize(monitor);

        monitor.done();
    }

    /**
     * @return
     */
    public Class[] indexedClasses() {
        return new Class[] {
                DescriptionElementBase.class,
                Classification.class,
                TaxonBase.class
                };
    }
}