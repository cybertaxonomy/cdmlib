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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.Classification;

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

    protected <T extends CdmBase>void reindex(Class<T> type) {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        logger.info("start indexing " + type.getName());
        Transaction transaction = fullTextSession.beginTransaction();

        Object countResultObj = getSession().createQuery("select count(*) from " + type.getName()).uniqueResult();
        Long countResult = (Long)countResultObj;
        Long numOfBatches = countResult > 0 ? ((countResult-1)/BATCH_SIZE)+1 : 0;

        // Scrollable results will avoid loading too many objects in memory
        ScrollableResults results = fullTextSession.createCriteria(type).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
        int index = 0;
        while (results.next()) {
            index++;
            fullTextSession.index(results.get(0)); // index each element
            if (index % BATCH_SIZE == 0 || index == countResult) {
                fullTextSession.flushToIndexes(); // apply changes to indexes
                fullTextSession.clear(); // clear since the queue is processed
                logger.info("\tbatch " + (((index-1)/BATCH_SIZE)+1) + "/" + numOfBatches + " processed");
                //if(index / BATCH_SIZE > 10 ) break;
            }
        }

        //transaction.commit(); // no need to commit, transaction will be committed automatically
        logger.info("end indexing " + type.getName());
    }

    protected <T extends CdmBase>void purge(Class<T> type) {

        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        logger.info("purging " + type.getName());

        fullTextSession.purgeAll(type);
        //transaction.commit(); // no need to commit, transaction will be committed automatically
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.IMassIndexer#reindex()
     */
    @Override
    public void reindex(){

        for(Class type : indexedClasses()){
            reindex(type);
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.IMassIndexer#purge()
     */
    @Override
    public void purge(){

        for(Class type : indexedClasses()){
            purge(type);
        }
    }

    /**
     * @return
     */
    public Class[] indexedClasses() {
        return new Class[] {
                DescriptionElementBase.class,
                Classification.class,
//                TaxonBase.class
                };
    }
}