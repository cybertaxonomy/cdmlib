// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IGrantedAuthorityService;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.IWorkingSetService;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
/**
 * @author a.mueller
 *
 */
@Component
public class CdmApplicationDefaultConfiguration implements ICdmApplicationConfiguration, ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(CdmApplicationDefaultConfiguration.class);

    protected ApplicationContext applicationContext;


    @Autowired
    //@Qualifier("nameService")
    private INameService nameService;
    @Autowired
    //@Qualifier("taxonService")
    private ITaxonService taxonService;
    @Autowired
    //@Qualifier("classificationService")
    private IClassificationService classificationService;
    @Autowired
    //@Qualifier("referenceService")
    private IReferenceService referenceService;
    @Autowired
    //@Qualifier("agentService")
    private IAgentService agentService;
    @Autowired
    //@Qualifier("databaseService")
    private IDatabaseService databaseService;
    @Autowired
    //@Qualifier("termService")
    private ITermService termService;
    @Autowired
    private HibernateTransactionManager transactionManager;
    @Autowired
    //@Qualifier("descriptionService")
    private IDescriptionService descriptionService;
    @Autowired
    //@Qualifier("occurrenceService")
    private IOccurrenceService occurrenceService;
    @Autowired
    //@Qualifier("mediaService")
    private IMediaService mediaService;
    @Autowired
    //@Qualifier("commonService")
    private ICommonService commonService;
    @Autowired
    private ILocationService locationService;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private ProviderManager authenticationManager;
    @Autowired
    private IUserService userService;
    @Autowired
    private IGrantedAuthorityService grantedAuthorityService;
    @Autowired
    private IGroupService groupService;
    @Autowired
    private ICollectionService collectionService;
    @Autowired
    private IFeatureTreeService featureTreeService;
    @Autowired
    private IFeatureNodeService featureNodeService;
    @Autowired
    private IVocabularyService vocabularyService;
    @Autowired
    private ITaxonNodeService taxonNodeService;
    @Autowired
    private IIdentificationKeyService identificationKeyService;
    @Autowired
    private IPolytomousKeyService polytomousKeyService;
    @Autowired
    private IPolytomousKeyNodeService polytomousKeyNodeService;
    @Autowired
    private PermissionEvaluator permissionEvaluator;


//	@Autowired
    //@Qualifier("mainService")
    private IService<CdmBase> mainService;

    @Autowired
    private IWorkingSetService workingSetService;

//********************** CONSTRUCTOR *********************************************************/

    /**
     * Constructor
     */
    protected CdmApplicationDefaultConfiguration() {
    }

// ****************************** APPLICATION CONTEXT *************************************************/


    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

// ****************************** GETTER *************************************************/


    public final Object getBean(String name){
        return this.applicationContext.getBean(name);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getAgentService()
     */
    public IAgentService getAgentService() {
        return this.agentService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getDatabaseService()
     */
    public IDatabaseService getDatabaseService() {
        return this.databaseService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getNameService()
     */
    public INameService getNameService() {
        return this.nameService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getReferenceService()
     */
    public IReferenceService getReferenceService() {
        return this.referenceService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTaxonService()
     */
    public ITaxonService getTaxonService() {
        return this.taxonService;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getClassificationService()
     */
    public IClassificationService getClassificationService() {
        return this.classificationService;
    }

    public ITaxonNodeService getTaxonNodeService(){
        return this.taxonNodeService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getDescriptionService()
     */
    public IDescriptionService getDescriptionService(){
        return this.descriptionService;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getOccurrenceService()
     */
    public IOccurrenceService getOccurrenceService(){
        return this.occurrenceService;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getMediaService()
     */
    public IMediaService getMediaService(){
        return this.mediaService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTermService()
     */
    public ITermService getTermService() {
        return this.termService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCommonService()
     */
    public ICommonService getCommonService(){
        return this.commonService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getLocationService()
     */
    public ILocationService getLocationService() {
        return this.locationService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getUserService()
     */
    public IUserService getUserService() {
        return this.userService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getUserService()
     */
    public IGrantedAuthorityService getGrantedAuthorityService() {
        return this.grantedAuthorityService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCommonService()
     */
    public IService<CdmBase> getMainService(){
        return this.mainService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getTransactionManager()
     */
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getAuthenticationManager()
     */
    public ProviderManager getAuthenticationManager() {
        return this.authenticationManager;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#NewConversation()
     */
    public ConversationHolder NewConversation() {
        // TODO make this a prototype
        return new ConversationHolder(dataSource, sessionFactory, transactionManager);
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getCollectionService()
     */
    public ICollectionService getCollectionService() {
        return collectionService;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getFeatureTreeService()
     */
    public IFeatureTreeService getFeatureTreeService() {
        return featureTreeService;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getFeatureNodeService()
     */
    public IFeatureNodeService getFeatureNodeService(){
        return featureNodeService;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getVocabularyService()
     */
    public IVocabularyService getVocabularyService() {
        return vocabularyService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getIdentificationKeyService()
     */
    public IIdentificationKeyService getIdentificationKeyService(){
        return identificationKeyService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getPolytomousKeyService()
     */
    public IPolytomousKeyService getPolytomousKeyService(){
        return polytomousKeyService;
    }

    public IPolytomousKeyNodeService getPolytomousKeyNodeService(){
        return polytomousKeyNodeService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getWorkingSetService()
     */
    @Override
    public IWorkingSetService getWorkingSetService() {
        return workingSetService;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getGroupService()
     */
    @Override
    public IGroupService getGroupService() {
        return groupService;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getPermissionEvaluator()
     */
    public PermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#startTransaction()
     */
    @Override
    public TransactionStatus startTransaction() {
        return startTransaction(false);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#startTransaction()
     */
    @Override
    public TransactionStatus startTransaction(Boolean readOnly) {

        PlatformTransactionManager txManager = getTransactionManager();

        DefaultTransactionDefinition defaultTxDef = new DefaultTransactionDefinition();
        defaultTxDef.setReadOnly(readOnly);
        TransactionDefinition txDef = defaultTxDef;

        // Log some transaction-related debug information.
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction name = " + txDef.getName());
            logger.debug("Transaction facets:");
            logger.debug("Propagation behavior = " + txDef.getPropagationBehavior());
            logger.debug("Isolation level = " + txDef.getIsolationLevel());
            logger.debug("Timeout = " + txDef.getTimeout());
            logger.debug("Read Only = " + txDef.isReadOnly());
            // org.springframework.orm.hibernate3.HibernateTransactionManager
            // provides more transaction/session-related debug information.
        }

        TransactionStatus txStatus = txManager.getTransaction(txDef);
        return txStatus;
    }


    public void commitTransaction(TransactionStatus txStatus){
        PlatformTransactionManager txManager = getTransactionManager();
        txManager.commit(txStatus);
        return;
    }



}
