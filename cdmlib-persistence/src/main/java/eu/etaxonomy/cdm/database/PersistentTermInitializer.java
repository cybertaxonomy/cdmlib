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
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
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
	
	public boolean isOmit() {
		return omit;
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
	public void initialize() {
		super.initialize();
	}
	

	@Override
	public void doInitialize(){
		logger.info("PersistentTermInitializer initialize start ...");
		if (omit){
			logger.info("PersistentTermInitializer.omit == true, returning without initializing terms");
			return;
		} else {
			Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
			logger.info("PersistentTermInitializer.omit == false, initializing " + VocabularyEnum.values().length + " term classes");
			
			TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
			for(VocabularyEnum vocabularyType : VocabularyEnum.values()) {
				//Class<? extends DefinedTermBase<?>> clazz = vocabularyType.getClazz();
				UUID vocabularyUuid = firstPass(vocabularyType,terms);
				secondPass(vocabularyType.getClazz(),vocabularyUuid,terms);
			}
			transactionManager.commit(txStatus);
		}
		logger.info("PersistentTermInitializer initialize end ...");
	}	
	
	/**
	 * Initializes the static fields of the <code>TermVocabulary</code> classes.
	 * 
	 * @param clazz the <code>Class</code> of the vocabulary
	 * @param vocabularyUuid the <code>UUID</code> of the vocabulary
	 * @param terms a <code>Map</code> containing all already 
	 * 						 loaded terms with their <code>UUID</code> as key
	 */
	protected void secondPass(Class clazz, UUID vocabularyUuid, Map<UUID,DefinedTermBase> terms) {
		logger.debug("Initializing vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );
		
		TermVocabulary persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
		
		if (persistedVocabulary != null){
			for(Object object : persistedVocabulary.getTerms()) {
				DefinedTermBase definedTermBase = (DefinedTermBase) object;
				Hibernate.initialize(definedTermBase.getRepresentations());
				for(Representation r : definedTermBase.getRepresentations()) {
					Hibernate.initialize(r.getLanguage());
				}
				terms.put(definedTermBase.getUuid(), definedTermBase);			
			}
		}else{
			logger.error("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
			throw new NullPointerException("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
		}
		logger.debug("Setting defined Terms for class " + clazz.getSimpleName());
		super.setDefinedTerms(clazz, persistedVocabulary);
	}
 
	/**
	 * This method loads the vocabularies from CSV files and compares them to the vocabularies
	 * already in database. Non-existing vocabularies will be created and vocabularies with missing 
	 * terms will be updated.
	 * 
	 * @param clazz the <code>Class</code> of the vocabulary
	 * @param persistedTerms a <code>Map</code> containing all already 
	 * 						 loaded terms with their <code>UUID</code> as key
	 * @return the <code>UUID</code> of the loaded vocabulary as found in CSV file
	 */
	public UUID firstPass(VocabularyEnum vocabularyType, Map<UUID, DefinedTermBase> persistedTerms) {
		logger.info("Loading terms for " + vocabularyType.getClazz().getSimpleName());
		Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
		
		for(DefinedTermBase d : persistedTerms.values()) {
			terms.put(d.getUuid(), d);
		}

		TermVocabulary loadedVocabulary  = termLoader.loadTerms(vocabularyType, terms);

		UUID vocabularyUuid = loadedVocabulary.getUuid();
		
		
		logger.debug("loading vocabulary " + vocabularyUuid);
		TermVocabulary persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
		if(persistedVocabulary == null) { // i.e. there is no persisted vocabulary
			logger.debug("vocabulary " + vocabularyUuid + " does not exist - saving");
			saveVocabulary(loadedVocabulary);
		}else {
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
