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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IAnnotationService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionElementService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.IEntityConstraintViolationService;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IEventBaseService;
import eu.etaxonomy.cdm.api.service.IGrantedAuthorityService;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.IMetadataService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IPreferenceService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.IRightsService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermCollectionService;
import eu.etaxonomy.cdm.api.service.ITermNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.geo.IDistributionService;
import eu.etaxonomy.cdm.api.service.longrunningService.ILongRunningTasksService;
import eu.etaxonomy.cdm.api.service.media.MediaInfoFactory;
import eu.etaxonomy.cdm.api.service.molecular.IAmplificationService;
import eu.etaxonomy.cdm.api.service.molecular.IPrimerService;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.api.service.security.IAccountRegistrationService;
import eu.etaxonomy.cdm.api.service.security.IPasswordResetService;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;

/**
 * This class actually is the central access point to all cdm api services and thus to all the
 * entities stored in the cdm. From this point of view this class provides a "repository" view
 * on the cdm by which you can access everything important for client development.
 *
 * @author a.mueller
 * @since 21.05.2008
 */
@Component
public class CdmRepository implements ICdmApplication, ApplicationContextAware {

    private static final Logger logger = LogManager.getLogger();

	protected ApplicationContext applicationContext;

	@Autowired
    //@Qualifier("nameService")
    private IAnnotationService annotationService;

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
	private IAccountRegistrationService accountRegistrationService;
	@Autowired
	//@Qualifier("agentService")
	private IAgentService agentService;
	@Autowired
	//@Qualifier("databaseService")
	private IDatabaseService databaseService;
	@Autowired
	//@Qualifier("termService")
	private ITermService termService;
	//@Autowired
	private HibernateTransactionManager transactionManager;
	@Autowired
	//@Qualifier("descriptionService")
	private IDescriptionService descriptionService;
    @Autowired
    //@Qualifier("distributionService")
    private IDistributionService distributionService;
	@Autowired
	//@Qualifier("descriptionElementService")
	private IDescriptionElementService descriptionElementService;
	@Autowired
	//@Qualifier("occurrenceService")
	private IOccurrenceService occurrenceService;
	@Autowired
	//@Qualifier("primerService")
	private IPrimerService primerService;
	@Autowired
	//@Qualifier("amplificationService")
	private IAmplificationService amplificationService;
	@Autowired
	//@Qualifier("sequenceService")
	private ISequenceService sequenceService;
	@Autowired
	//@Qualifier("eventBaseService")
	private IEventBaseService eventBaseService;
	@Autowired
	//@Qualifier("mediaService")
	private IMediaService mediaService;
    @Autowired
    //@Qualifier("mediaService")
    private IMetadataService metadataService;
	@Autowired
	//@Qualifier("commonService")
	private ICommonService commonService;
	@Autowired
	private ILocationService locationService;
	//@Autowired
	private SessionFactory sessionFactory;
	//@Autowired
	private DataSource dataSource;
	@Autowired
	@Lazy
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
	private ITermTreeService termTreeService;
	@Autowired
	private ITermCollectionService termCollectionService;
	@Autowired
	private ITermNodeService termNodeService;
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
	private IProgressMonitorService progressMonitorService;
	@Autowired
	private IEntityValidationService entityValidationService;
	@Autowired
    private IPreferenceService preferenceService;
	@Autowired
    private IRightsService rightsService;
    @Autowired
    private IRegistrationService registrationService;
    @Autowired
    private IRegistrationWorkingSetService registrationWorkingSetService;
    @Autowired
    private ILongRunningTasksService longRunningTasksService;
	@Autowired
	private IEntityConstraintViolationService entityConstraintViolationService;
	@Autowired
	private ICdmPermissionEvaluator permissionEvaluator;
    @Autowired
    private MediaInfoFactory mediaInfoFactory; // FIXME define and use interface
	@Autowired
    private SessionFactory factory;
	@Autowired
	private IDescriptiveDataSetService descriptiveDataSetService;
    @Autowired
    private IPasswordResetService passwordResetService;

	//********************** CONSTRUCTOR *********************************************************/

	protected CdmRepository(){}

	// ****************************** APPLICATION CONTEXT *************************************************/

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
		this.applicationContext = applicationContext;
	}

	// ****************************** GETTER *************************************************/

	@Override
	public Object getBean(String name){
	    return this.applicationContext.getBean(name);
	}

    @Override
    public IAccountRegistrationService getAccountRegistrationService() {
        return accountRegistrationService;
    }

	@Override
	public IAnnotationService getAnnotationService(){
	    return this.annotationService;
	}

	@Override
	public IAgentService getAgentService(){
		return this.agentService;
	}

	@Override
	public IDatabaseService getDatabaseService(){
		return this.databaseService;
	}


	@Autowired
	public void setDataSource(DataSource dataSource){
		this.dataSource = dataSource;
	}

	@Override
	public INameService getNameService(){
		return this.nameService;
	}

	@Override
	public IReferenceService getReferenceService(){
		return this.referenceService;
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	@Override
	public ITaxonService getTaxonService(){
		return this.taxonService;
	}

	@Override
	public IClassificationService getClassificationService(){
		return this.classificationService;
	}

	@Override
	public ITaxonNodeService getTaxonNodeService(){
		return this.taxonNodeService;
	}

	@Override
	public IDescriptionService getDescriptionService(){
		return this.descriptionService;
	}

    @Override
    public IDistributionService getDistributionService(){
        return this.distributionService;
    }

    @Override
    public IDescriptionElementService getDescriptionElementService(){
        return this.descriptionElementService;
    }

	@Override
	public IOccurrenceService getOccurrenceService(){
		return this.occurrenceService;
	}

	@Override
	public IPrimerService getPrimerService(){
		return this.primerService;
	}

	@Override
	public IAmplificationService getAmplificationService(){
		return this.amplificationService;
	}

	@Override
	public ISequenceService getSequenceService(){
		return this.sequenceService;
	}

	@Override
	public IEventBaseService getEventBaseService() {
	    return this.eventBaseService;
	}

	@Override
	public IMediaService getMediaService(){
		return this.mediaService;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public IMetadataService getMetadataService() {
        return this.metadataService;
    }

	@Override
	public ITermService getTermService(){
		return this.termService;
	}

	@Override
	public ICommonService getCommonService(){
		return this.commonService;
	}

	@Override
	public ILocationService getLocationService(){
		return this.locationService;
	}

	@Override
	public IUserService getUserService(){
		return this.userService;
	}

	@Override
	public IGrantedAuthorityService getGrantedAuthorityService(){
		return this.grantedAuthorityService;
	}

	@Override
	public PlatformTransactionManager getTransactionManager(){
		return this.transactionManager;
	}

	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager){
		this.transactionManager = (HibernateTransactionManager) transactionManager;
	}

	@Override
	public ProviderManager getAuthenticationManager(){
		return this.authenticationManager;
	}

	@Override
	public ConversationHolder NewConversation(){
		// TODO make this a prototype
		return new ConversationHolder(dataSource, sessionFactory, transactionManager);
	}

	@Override
	public ICollectionService getCollectionService(){
		return collectionService;
	}

	@Override
	public ITermTreeService getTermTreeService(){
	    return termTreeService;
	}

    @Override
    public ITermCollectionService getTermCollectionService() {
        return termCollectionService;
    }

    @Override
    public ITermNodeService getTermNodeService(){
        return termNodeService;
    }

	@Override
    public IPreferenceService getPreferenceService(){
        return preferenceService;
    }

	@Override
	public IVocabularyService getVocabularyService(){
		return vocabularyService;
	}

	@Override
	public IIdentificationKeyService getIdentificationKeyService(){
		return identificationKeyService;
	}

	@Override
	public IPolytomousKeyService getPolytomousKeyService(){
		return polytomousKeyService;
	}

	@Override
	public IPolytomousKeyNodeService getPolytomousKeyNodeService(){
		return polytomousKeyNodeService;
	}

    @Override
    public IProgressMonitorService getProgressMonitorService() {
        return progressMonitorService;
    }

	@Override
	public IDescriptiveDataSetService getDescriptiveDataSetService(){
		return descriptiveDataSetService;
	}

	@Override
	public IGroupService getGroupService(){
		return groupService;
	}

	@Override
	public IEntityValidationService getEntityValidationService(){
		return entityValidationService;
	}

	@Override
	public IEntityConstraintViolationService getEntityConstraintViolationService(){
		return entityConstraintViolationService;
	}

	@Override
	public ICdmPermissionEvaluator getPermissionEvaluator(){
		return permissionEvaluator;
	}

    @Override
    public MediaInfoFactory getMediaInfoFactory() { // FIXME define and use interface
        return mediaInfoFactory;
    }

    @Override
    public IRightsService getRightsService() {
        return rightsService;
    }

    @Override
    public IRegistrationService getRegistrationService() {
        return registrationService;
    }

    @Override
    public IRegistrationWorkingSetService getRegistrationWorkingSetService() {
        return registrationWorkingSetService;
    }

    @Override
    public IPasswordResetService getPasswordResetService() {
        return passwordResetService;
    }

    @Override
    public ILongRunningTasksService getLongRunningTasksService() {
        return longRunningTasksService;
    }

	@Override
	public TransactionStatus startTransaction(){
		return startTransaction(false);
	}

	@Override
	public TransactionStatus startTransaction(Boolean readOnly){

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
			// org.springframework.orm.hibernate5.HibernateTransactionManager
			// provides more transaction/session-related debug information.
		}

		TransactionStatus txStatus = txManager.getTransaction(txDef);
		return txStatus;
	}

	@Override
	public void commitTransaction(TransactionStatus txStatus){
		PlatformTransactionManager txManager = getTransactionManager();
		txManager.commit(txStatus);
		return;
	}

    @Override
    public void rollbackTransaction(TransactionStatus txStatus){
        PlatformTransactionManager txManager = getTransactionManager();
        txManager.rollback(txStatus);
        return;
    }

	@Override
	public void authenticate(String username, String password){
		UsernamePasswordAuthenticationToken tokenForUser = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = this.getAuthenticationManager().authenticate(tokenForUser);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
	}

    public SessionFactory getSessionFactory() {
        return factory;
    }

    /**
     * Returns the current session as obtained from the session factory.
     *
     * @throws HibernateException
     *             In case a session bound to the current thread cannot be found
     * @return the session bound to the current thread
     */
    public Session getSession() throws HibernateException {
        return factory.getCurrentSession();
    }

    /**
     * clears the current Session
     */
    public void clearSession() {
        try {
            getSession().clear();
        } catch (HibernateException e) {
            // no current session: nothing to clear!
        }
    }
}