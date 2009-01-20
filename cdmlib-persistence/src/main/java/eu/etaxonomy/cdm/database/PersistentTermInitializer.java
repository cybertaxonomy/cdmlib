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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.WrongTermTypeException;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DeterminationModifier;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
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
	private ITermVocabularyDao vocabularyDao;
	private IDefinedTermDao termDao;
	/**
	 * After a bit of head-scratching I found section 3.5.1.3. in the current spring 
	 * reference manual - @PostConstruct / afterPropertiesSet() is called 
	 * immediatly after the bean is constructed, prior to any AOP interceptors being 
	 * wrapped round the bean. Thus, we have to use programmatic transactions, not 
	 * annotations or pointcuts.
	 */
	private PlatformTransactionManager transactionManager;
	private DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
	
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
	public void setTermDao(IDefinedTermDao termDao){
		this.termDao = termDao;
	}
	
	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	@PostConstruct
	@Override
	public void initialize(){
		logger.debug("PersistentTermInitializer initialize()");
		if (omit){
			logger.info("PersistentTermInitializer.omit == true, returning without initializing terms");
			return;
		} else {
			Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
			classesToInitialize = new Class[]{Language.class,Continent.class,WaterbodyOrCountry.class,
                    Rank.class,TypeDesignationStatus.class,
                    NomenclaturalStatusType.class,
                    SynonymRelationshipType.class,
                    HybridRelationshipType.class,
                    NameRelationshipType.class,TaxonRelationshipType.class,
                    MarkerType.class,
                    AnnotationType.class,NamedAreaType.class,NamedAreaLevel.class,
                    NomenclaturalCode.class,Feature.class,NamedArea.class,PresenceTerm.class,AbsenceTerm.class,Sex.class,
                    DerivationEventType.class,PreservationMethod.class,DeterminationModifier.class,StatisticalMeasure.class,RightsTerm.class
                    };
			logger.info("PersistentTermInitializer.omit == false, initializing " + terms.size() + " term classes");
			for(Class clazz : classesToInitialize) {
				UUID vocabularyUuid = firstPass(clazz,terms);
				secondPass(clazz,vocabularyUuid,terms);
			}
			
		}
	}	
	
	private void secondPass(Class clazz, UUID vocabularyUuid,Map<UUID,DefinedTermBase> terms) {
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
		TermVocabulary persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
		for(Object obj : persistedVocabulary.getTerms()) {
			DefinedTermBase d = (DefinedTermBase)obj;
			terms.put(d.getUuid(), d);
			logger.debug("Setting defined Terms for class " + clazz.getSimpleName());
			super.setDefinedTerms(clazz, persistedVocabulary);
		}

		transactionManager.commit(txStatus);
	}
 
	/**
	 * T
	 * @param clazz
	 * @param persistedTerms
	 * @return
	 */
	private UUID firstPass(Class clazz, Map<UUID, DefinedTermBase> persistedTerms) {
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
			logger.debug("vocabulary " + vocabularyUuid + " does exists and already has " + persistedVocabulary.size() + " terms");
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
