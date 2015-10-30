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
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.Period;
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

            DateTime start = new DateTime();

            TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

            //load uuids from csv files
            logger.info("Start new ... " );
            Map<UUID, Set<UUID>> uuidMap = new HashMap<UUID, Set<UUID>>();
            Map<UUID, VocabularyEnum> vocTypeMap = new HashMap<UUID, VocabularyEnum>();

            for(VocabularyEnum vocabularyType : VocabularyEnum.values()) {
                UUID vocUUID = termLoader.loadUuids(vocabularyType, uuidMap);
                if (! vocUUID.equals(vocabularyType.getUuid())){
                	throw new IllegalStateException("Vocabulary uuid in csv file and vocabulary type differ for vocabulary type " + vocabularyType.toString());
                }
                vocTypeMap.put(vocUUID, vocabularyType);
            }

            //find and create missing terms and load vocabularies from repository
            logger.info("Create missing terms ... " );
            Map<UUID, TermVocabulary<?>> vocabularyMap = new HashMap<UUID, TermVocabulary<?>>();
            Map<UUID, Set<UUID>> missingTermUuids = new HashMap<UUID, Set<UUID>>();

            vocabularyDao.missingTermUuids(uuidMap, missingTermUuids, vocabularyMap);

            for( VocabularyEnum vocabularyType : VocabularyEnum.values()) {   //required to keep the order (language must be the first vocabulary to load)
            	UUID vocUuid = vocabularyType.getUuid();
            	if (missingTermUuids.keySet().contains(vocabularyType.getUuid())  || vocabularyMap.get(vocUuid) == null ){

            		VocabularyEnum vocType = vocTypeMap.get(vocUuid);  //TODO not really necessary, we could also do VocType.getUuuid();
	            	TermVocabulary<?> voc = vocabularyMap.get(vocUuid);
	            	if (voc == null){
	            		//vocabulary is missing
	            		voc = termLoader.loadTerms(vocType, terms);
	            		vocabularyDao.save(voc);
	            		vocabularyMap.put(voc.getUuid(), voc);
	            	}else{
	            		//single terms are missing
	            		Set<UUID> missingTermsOfVoc = missingTermUuids.get(vocUuid);
		            	Set<? extends DefinedTermBase> createdTerms = termLoader.loadSingleTerms(vocType, voc, missingTermsOfVoc);
	                	vocabularyDao.saveOrUpdate(voc);
	                }
	            }
	            initializeAndStore(vocabularyType, terms, vocabularyMap);  //TODO
        	}

            transactionManager.commit(txStatus);

            DateTime end = new DateTime();
            Period period = new Period(start, end);
            logger.info ("Term loading took " + period.getSeconds() + "." + period.getMillis() + " seconds ");

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
     * @param vocabularyMap
     */
    protected void initializeAndStore(VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms, Map<UUID, TermVocabulary<?>> vocabularyMap) {
    	Class<? extends DefinedTermBase<?>> clazz = vocType.getClazz();
        UUID vocabularyUuid = vocType.getUuid();

        if (logger.isDebugEnabled()){ logger.debug("Loading vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );}

        TermVocabulary<? extends DefinedTermBase> persistedVocabulary;
        if (vocabularyMap == null || vocabularyMap.get(vocabularyUuid) == null ){
        	persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
        }else{
        	persistedVocabulary = vocabularyMap.get(vocabularyUuid);
        }

        if (logger.isDebugEnabled()){ logger.debug("Initializing terms in vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );}
        //not really needed anymore as we do term initializing from the beginning now
        if (persistedVocabulary != null){
            for(DefinedTermBase<?> definedTermBase : persistedVocabulary.getTerms()) {

            	Hibernate.initialize(definedTermBase.getRepresentations());
                for(Representation r : definedTermBase.getRepresentations()) {
                    Hibernate.initialize(r.getLanguage());
                }
                terms.put(definedTermBase.getUuid(), definedTermBase);
            }
        }else{
            logger.error("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
            throw new IllegalStateException("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
        }


        //fill term store
        if (logger.isDebugEnabled()){ logger.debug("Setting defined Terms for class " + clazz.getSimpleName() + ", " + persistedVocabulary.getTerms().size() + " in vocabulary");}
        super.setDefinedTerms(clazz, persistedVocabulary);
        if (logger.isDebugEnabled()){ logger.debug("Second pass - DONE");}

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
        logger.info("Loading terms for '" + vocabularyType.name() + "': " + vocabularyType.getClazz().getName());
        Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();

        for(DefinedTermBase persistedTerm : persistedTerms.values()) {
            terms.put(persistedTerm.getUuid(), persistedTerm);
        }

        TermVocabulary<?> loadedVocabulary  = termLoader.loadTerms(vocabularyType, terms);

        UUID vocabularyUuid = loadedVocabulary.getUuid();


        if (logger.isDebugEnabled()){logger.debug("loading persisted vocabulary " + vocabularyUuid);}
        TermVocabulary<DefinedTermBase> persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
        if(persistedVocabulary == null) { // i.e. there is no persisted vocabulary
            //handle new vocabulary
        	if (logger.isDebugEnabled()){logger.debug("vocabulary " + vocabularyUuid + " does not exist - saving");}
            saveVocabulary(loadedVocabulary);
        }else {
        	//handle existing vocabulary
            if (logger.isDebugEnabled()){logger.debug("vocabulary " + vocabularyUuid + " does exist and already has " + persistedVocabulary.size() + " terms");}
            boolean persistedVocabularyHasMissingTerms = false;
            for(Object t : loadedVocabulary.getTerms()) {
                if(!persistedVocabulary.getTerms().contains(t)) {
                    persistedVocabularyHasMissingTerms = true;
                    persistedVocabulary.addTerm((DefinedTermBase)t);
                }
            }
            if(persistedVocabularyHasMissingTerms) {
            	if (logger.isDebugEnabled()){logger.debug("vocabulary " + vocabularyUuid + " exists but does not have all the required terms - updating");}
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
