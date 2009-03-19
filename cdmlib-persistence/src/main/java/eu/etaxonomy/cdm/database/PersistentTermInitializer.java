/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * Spring bean class to initialize the {@link IVocabularyStore IVocabularyStore}.
 * To initialize the store the {@link TermLoader TermLoader} and the {@link IVocabularyStore IVocabularyStore}
 * are injected via spring and the initializeTerms method is called as an init-method (@PostConstruct). 

 * @author a.mueller
 */

@Component
public class PersistentTermInitializer extends DefaultTermInitializer {
	private static final Logger logger = Logger.getLogger(PersistentTermInitializer.class);
	
	private boolean omit = false;
	protected ITermVocabularyDao vocabularyDao;

	protected PlatformTransactionManager transactionManager;
	protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
	
	public PersistentTermInitializer() {
		txDefinition.setName("PersistentTermInitializer.initialize()");
		txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	}
	
	public void setOmit(boolean omit) {
		this.omit = omit;
	}
	
	@Autowired
	public void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	/*
	 * After a bit of head-scratching I found section 3.5.1.3. in the current spring 
	 * reference manual - @PostConstruct / afterPropertiesSet() is called 
	 * immediatly after the bean is constructed, prior to any AOP interceptors being 
	 * wrapped round the bean. Thus, we have to use programmatic transactions, not 
	 * annotations or pointcuts.
	 */
	@PostConstruct
	@Override
	public void initialize(){
		logger.debug("PersistentTermInitializer initialize start ...");
		if (omit){
			logger.info("PersistentTermInitializer.omit == true, returning without initializing terms");
			return;
		} else {
			Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
			logger.info("PersistentTermInitializer.omit == false, initializing " + terms.size() + " term classes");
			for(Class clazz : classesToInitialize) {
				UUID vocabularyUuid = firstPass(clazz,terms);
				secondPass(clazz,vocabularyUuid,terms);
			}
			
		}
		logger.debug("PersistentTermInitializer initialize end ...");
	}	
	
	protected void secondPass(Class clazz, UUID vocabularyUuid,Map<UUID,DefinedTermBase> terms) {
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
		TermVocabulary persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
		
		for(Object obj : persistedVocabulary.getTerms()) {
			DefinedTermBase d = (DefinedTermBase)obj;
			Hibernate.initialize(d.getRepresentations());
			terms.put(d.getUuid(), d);			
		}
		
		logger.debug("Setting defined Terms for class " + clazz.getSimpleName());
		super.setDefinedTerms(clazz, persistedVocabulary);

		transactionManager.commit(txStatus);
	}
 
	/**
	 * T
	 * @param clazz
	 * @param persistedTerms
	 * @return
	 */
	public UUID firstPass(Class clazz, Map<UUID, DefinedTermBase> persistedTerms) {
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		logger.debug("loading terms for " + clazz.getSimpleName());
		Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
		
		for(DefinedTermBase d : persistedTerms.values()) {
			terms.put(d.getUuid(), d);
		}

		TermVocabulary loadedVocabulary  = termLoader.loadTerms((Class<? extends DefinedTermBase>)clazz, terms);
		
		UUID vocabularyUuid = loadedVocabulary.getUuid();
		
		logger.debug("loading vocabulary " + vocabularyUuid);
		TermVocabulary persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
		if(persistedVocabulary == null) { // i.e. there is no persisted vocabulary
			logger.debug("vocabulary " + vocabularyUuid + " does not exist - saving");
			saveVocabulary(loadedVocabulary);
		} else {
			logger.debug("vocabulary " + vocabularyUuid + " does exist and already has " + persistedVocabulary.size() + " terms");
		    boolean persistedVocabularyHasMissingTerms = false;
		    for(Object t : loadedVocabulary.getTerms()) {				
		    	if(!persistedVocabulary.getTerms().contains(t)) {
		    		persistedVocabularyHasMissingTerms = true;
		    		persistedVocabulary.addTerm((DefinedTermBase)t);
		    	}
		    }				    
		    if(persistedVocabularyHasMissingTerms) {
		    	logger.debug("vocabulary " + vocabularyUuid + " exists but does not have all the required terms - updating");
		    	updateVocabulary(persistedVocabulary);
		    }
		}
		transactionManager.commit(txStatus);
		return vocabularyUuid;
	}

	private void updateVocabulary(TermVocabulary vocabulary) {
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		vocabularyDao.update(vocabulary);
		transactionManager.commit(txStatus);		
	}

	private void saveVocabulary(TermVocabulary vocabulary) {
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		vocabularyDao.save(vocabulary);
		transactionManager.commit(txStatus);
	}
}
