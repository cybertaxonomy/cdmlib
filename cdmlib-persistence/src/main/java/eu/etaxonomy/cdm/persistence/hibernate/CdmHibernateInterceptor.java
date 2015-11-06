/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 *
 */
@Component
public class CdmHibernateInterceptor extends EmptyInterceptor {
        private static final long serialVersionUID = 2536017420460052854L;
        private static final Logger logger = Logger.getLogger(CdmHibernateInterceptor.class);

        //FIXME problem is circular dependency (see VocabularyStoreImpl.staticInitialized
//      @Autowired
//      VocabularyStoreImpl vocabularyStore;

        private int updates;
        private int creates;
        private int loads;

    @Override
    public void onDelete(Object entity,
           Serializable id,
           Object[] state,
           String[] propertyNames,
           Type[] types) {
        // do nothing
    }

    @Override
    public boolean onFlushDirty(Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {
		if (logger.isDebugEnabled()) {
            logger.debug("onFlushDirty...");
        }
        boolean result = false;
        if ( entity instanceof CdmBase ) {
                updates++;
                //result &= checkTransientDefinedTerms(currentState);
        }
        return result;
    }

    @Override
    public boolean onLoad(Object entity,
                    Serializable id,
                    Object[] state,
                    String[] propertyNames,
                    Type[] types) {
        if ( entity instanceof CdmBase ) {
            if (logger.isDebugEnabled()) {
                logger.debug("id = " +id);
            }
            loads++;
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity,
                    Serializable id,
                    Object[] state,
                    String[] propertyNames,
                    Type[] types) {
        if (logger.isDebugEnabled()) {
            logger.debug("onSave...");
        }
        boolean result = false;
        if ( entity instanceof CdmBase ) {
                creates++;
                //result &= checkTransientDefinedTerms(state);
        }
        return result;
    }


    private boolean checkTransientDefinedTerms(Object[] state){
        boolean result = false;
//	                if (VocabularyStoreImpl.isInitialized()){
//	                        //logger.debug("Save: " + entity);
//	                        int i = -1;
//	                        for (Object singleState : state){
//	                                i++;
//	                                if (singleState instanceof DefinedTermBase){
//	                                        DefinedTermBase term = ((DefinedTermBase)singleState);
// 	                                        if (term.getId() != 0){
// 	                                                continue;
// 	                                        }else{
// 	                                                //logger.debug(" " + singleState.getClass());
// 	                                                UUID uuid = term.getUuid();
// 	                                                DefinedTermBase storedTermBase = VocabularyStoreImpl.getCurrentVocabularyStore().getTermByUuid(uuid);
// 	                                                if (storedTermBase == null){
// 	                                                        logger.warn("DefinedTermBase with uuid "+ uuid +" could not be found in vocabulary store. Term stays transient.");
// 	                                                }else if (uuid.equals(storedTermBase.getUuid())){
// 	                                                        logger.debug("Changed transient term");
// 	                                                        state[i] = storedTermBase;
// 	                                                        result = true;
// 	                                                }else{
// 	                                                        throw new IllegalStateException("UUID is not equal.");
// 	                                                }
// 	                                        }
//
// 	                                }
// 	                        }
// 	                }else{ //not initialized
//
// 	                }
            return result;
    }

    @Override
    public void afterTransactionCompletion(Transaction tx) {
            if ( tx.getStatus() == TransactionStatus.COMMITTED ) {
                    logger.debug("Creations: " + creates + ", Updates: " + updates + ", Loads: " + loads);
            }
            updates=0;
            creates=0;
            loads=0;
    }

}

