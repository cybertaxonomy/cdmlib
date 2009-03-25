/**
 * 
 */
package eu.etaxonomy.cdm.api.conversation;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This is an implementation of the session-per-conversation pattern for usage
 * in a Spring context.
 *  
 * @see http://www.hibernate.org/42.html
 * 
 * @author n.hoffmann
 * @created 12.03.2009
 * @version 1.0
 */
public class ConversationHolder {

	/**
	 * This class logger instance 
	 */
	private static final Logger logger = Logger.getLogger(ConversationHolder.class);

	/**
	 * The applications session factory
	 */
	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * The datasource associated with the application context
	 */
	@Autowired
	private DataSource dataSource;
	
	/**
	 * 
	 */
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	/**
	 * The persistence context for this conversation
	 */
	private Session longSession = null;

	/**
	 * Spring communicates with hibernate sessions via a SessionHolder object
	 */
	private SessionHolder sessionHolder = null;

	/**
	 * see TransactionDefinition
	 */
	private TransactionDefinition definition;
	
	/**
	 * This conversations transaction
	 */
	private TransactionStatus transactionStatus;

	
	/**
	 * 
	 */
	private static final ConversationMediator mediator = new ConversationMediator();
	
	/**
	 * Simple constructor
	 */
	public ConversationHolder(){
		logger.trace("Creating new ConversationHolder.");
	}
	
	/**
	 * 
	 * @param dataSource
	 */
	public ConversationHolder(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 
	 * @param dataSource
	 * @param sessionFactory
	 */
	public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory) {
		this(dataSource);
		this.sessionFactory = sessionFactory;
	}

	public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory, 
			PlatformTransactionManager transactionManager) {
		this(dataSource, sessionFactory);
		this.transactionManager = transactionManager;
	}
	
	/**
	 * 
	 * @param dataSource
	 * @param sessionFactory
	 * @param session
	 */
	public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory,
			PlatformTransactionManager transactionManager, Session session) {
		this(dataSource, sessionFactory, transactionManager);
		longSession = session;
	}
	



	/**
	 * This method has to be called when starting a new unit-of-work. All required resources are
	 * bound so that SessionFactory.getCurrentSession() returns the right session for this conversation
	 */
	public void bind() {
		
		// do nothing if this conversation is already bound
		if(isBound()) return;
		
		logger.info("Binding resources for ConversationHolder: [" + this + "]");
		
		// lazy creation of session
		if (longSession == null) {
			longSession = SessionFactoryUtils.getNewSession(getSessionFactory());
			longSession.setFlushMode(FlushMode.MANUAL);

			logger.info("Creating Session: [" + longSession + "]");
		}

		// lazy creation of session holder
		if(sessionHolder == null){
			sessionHolder = new SessionHolder(longSession);
			logger.info("Creating SessionHolder: [" + sessionHolder + "]");
		}
		
		// connect dataSource with session
		if (!longSession.isConnected()){
			longSession.reconnect(DataSourceUtils.getConnection(dataSource));
			logger.info("Reconnecting DataSource: [" + dataSource + "]" );
		}
		
		
//		// FIXME in case this gets called something went wrong and resources were not freed correctly before
//		// right now we handle this gracefully by unbinding the resources here, but I think this will 
//		// lead to lots of trouble 
//		if(TransactionSynchronizationManager.hasResource(getSessionFactory())){
//			logger.info("Session Factory was already bound to TransactionSynchronizationManager. Unbinding it.");
//			TransactionSynchronizationManager.unbindResource(getSessionFactory());
//		}		
//		if(TransactionSynchronizationManager.isSynchronizationActive()){
//			logger.info("Synchronization was already bound to TransactionSynchronizationManager. Unbinding it.");
//			TransactionSynchronizationManager.clearSynchronization();
//		}
//		
//		logger.info("Binding SessionFactory to TransactionSynchronizationManager: [" + TransactionSynchronizationManager.class + "]");
//		TransactionSynchronizationManager.bindResource(getSessionFactory(),
//				sessionHolder);
//		
//		logger.info("Binding Synchronization to TransactionSynchronizationManager: [" + TransactionSynchronizationManager.class + "]");
//		TransactionSynchronizationManager.initSynchronization();
		if( ! TransactionSynchronizationManager.hasResource(getSessionFactory())){
			logger.info("Session Factory not bound to TransactionSynchronizationManager. Binding it.");
			TransactionSynchronizationManager.bindResource(getSessionFactory(), sessionHolder);
		}	
		if( ! TransactionSynchronizationManager.isSynchronizationActive()){
			logger.info("Synchronization not bound to TransactionSynchronizationManager. Binding it.");
			TransactionSynchronizationManager.initSynchronization();
		}
	}
	
	public boolean isBound(){
		return sessionHolder != null && longSession != null && longSession.isConnected();
	}
	
	/**
	 * API change! use bind() instead
	 * 
	 * @deprecated
	 */
	public void preExecute(){
		bind();
	}

	/**
	 * This method is to be run to free up resources after the unit-of-work has completed 
	 * 
	 * TODO 
	 * we do not need this in a test environment as the junit magic will try to 
	 * clear up resources after the tests are run and if the resources are unbound 
	 * manually beforehand, it will result in exceptions.
	 * maybe we need this in a live environment
	 * 
	 * @deprecated it looks like we don't need this at all
	 */
	public void unbind() {
		logger.info("Freeing resources bound to ConversationHolder: [" + this + "]");
		
		TransactionSynchronizationManager.unbindResource(getSessionFactory());
		TransactionSynchronizationManager.clearSynchronization();
	}
	
	/**
	 * API change! use unbind() instead.
	 * 
	 * @deprecated
	 */
	public void postExecute(){
		unbind();
	}
	
	/**
	 * Creates an instance of TransactionStatus and binds it to this conversation manager.
	 * At the moment we allow only on transaction per conversation holder.
	 * 
	 * @return the transaction status bound to this conversation holder
	 */
	public TransactionStatus startTransaction(){
		if (isTransactionActive()){
			logger.warn("We allow only one transaction at the moment but startTransaction " +
					"was called a second time.\nReturning the transaction already associated with this " +
					"ConversationManager");
		}else{	
			transactionStatus = transactionManager.getTransaction(definition);
			logger.info("Transaction started: [" + transactionStatus + "]");
		}
		return transactionStatus;
	}
	
	/** 
	 * @return if there is a running transaction
	 */
	public boolean isTransactionActive(){
		return transactionStatus != null;
	}
	
	/**
	 * Commit the running transaction.
	 */
	public void commit(){
		commit(false);
	}
	
	/**
	 * Commit the running transaction but optionally start a
	 * new one right away.
	 * 
	 * @param restartTransaction whether to start a new transaction
	 */
	public void commit(boolean restartTransaction){
		if(isTransactionActive()){
			
			// before we commit we have to get the dirty objects for mediation
			ConversationMediationEvent event = new ConversationMediationEvent();
			
			// TODO implements this FIXME implement this
			PersistenceContext persistenceContext = ((SessionImplementor) longSession).getPersistenceContext();
			
			//event.addObject(object);
			
			// commit the changes
			transactionManager.commit(transactionStatus);
			
			
			
			
			// Reset the transactionStatus.
			transactionStatus = null;
			// Commiting a transaction frees all resources.
			// Since we are in a conversation we directly rebind those resources and start a new transaction
			bind();
			if(restartTransaction){
				startTransaction();
			}
		}else{
			logger.warn("No active transaction but commit was called");
		}
	}
	

	/**
	 * close the session if it is still open
	 */
	public void dispose() {
		if (longSession != null && longSession.isOpen()){
			longSession.close();
		}
	}

	/**
	 * @return the session associated with this conversation manager 
	 */
	public Session getSession() {
		return longSession;
	}
	
	/** 
	 * @return the session factory that is bound to this conversation manager
	 */
	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Facades Session.lock()
	 */
	public void lock(Object persistentObject, LockMode lockMode) {
		longSession.lock(persistentObject, lockMode);
	}
	
	public void lock(String entityName, Object persistentObject, LockMode lockMode){
		longSession.lock(entityName, persistentObject, lockMode);
	}

	/**
	 * @return the definition
	 */
	public TransactionDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(TransactionDefinition definition) {
		this.definition = definition;
	}

	/**
	 * @param event
	 */
	public void propagateEvent(ConversationMediationEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public static ConversationMediator getMediator(){
		return mediator;
	}
}
