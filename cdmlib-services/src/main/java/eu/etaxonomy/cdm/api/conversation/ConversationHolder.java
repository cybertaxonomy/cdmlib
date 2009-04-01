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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.ResourceHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.etaxonomy.cdm.persistence.hibernate.CdmPostCrudObservableListener;

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
public class ConversationHolder{

	private static final Logger logger = Logger.getLogger(ConversationHolder.class);

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private DataSource dataSource;
	
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
	 * @see TransactionDefinition
	 */
	private TransactionDefinition definition;
	
	/**
	 * This conversations transaction
	 */
	private TransactionStatus transactionStatus;

	/**
	 * Simple constructor used by Spring only
	 */
	private ConversationHolder(){
	}

	public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory, 
			PlatformTransactionManager transactionManager) {
		this();
		this.dataSource = dataSource;
		this.sessionFactory = sessionFactory;
		this.transactionManager = transactionManager;
	}
	
	/**
	 * This method has to be called when starting a new unit-of-work. All required resources are
	 * bound so that SessionFactory.getCurrentSession() returns the right session for this conversation
	 */
	public void bind() {
		

		if(TransactionSynchronizationManager.hasResource(getSessionFactory())){
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}

		/*
		 * FIXME it is a rather strange behaviour that HibernateTransactionManager 
		 * binds a dataSource and then complains about that later on. At least that 
		 * is what happens in the editor.
		 * 
		 *  With this in the code the tests will not run, but the editor for now.
		 * 
		 */
//		if(TransactionSynchronizationManager.hasResource(getDataSource())){
//			TransactionSynchronizationManager.unbindResource(getDataSource());
//		}
	
		
		if(TransactionSynchronizationManager.isSynchronizationActive()){
			TransactionSynchronizationManager.clearSynchronization();
		}

		
		logger.info("Binding resources for ConversationHolder: [" + this + "]");
		
		// lazy creation of session
		if (longSession == null) {
			longSession = SessionFactoryUtils.getNewSession(getSessionFactory());
			longSession.setFlushMode(FlushMode.MANUAL);

			// TODO set the ConnectionReleaseMode to AFTER_TRANSACTION, possibly in applicationContext
			
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
		
		
		logger.info("Binding Session to TransactionSynchronizationManager.");
		TransactionSynchronizationManager.bindResource(getSessionFactory(), sessionHolder);

		logger.info("Starting new Synchronization in TransactionSynchronizationManager.");
		TransactionSynchronizationManager.initSynchronization();
		
		
	}
	
	/**
	 * @return
	 */
	private DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * @return true if this longSession is bound to the session factory.
	 */
	public boolean isBound(){
		//return sessionHolder != null && longSession != null && longSession.isConnected();
		return longSession != null && getSessionFactory().getCurrentSession() == longSession;
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
	public TransactionStatus commit(boolean restartTransaction){
		if(isTransactionActive()){
			
			// commit the changes
			transactionManager.commit(transactionStatus);
						
			// Reset the transactionStatus.
			transactionStatus = null;
			
			// Committing a transaction frees all resources.
			// Since we are in a conversation we directly rebind those resources and start a new transaction
			bind();
			if(restartTransaction){
				return startTransaction();
			}
		}else{
			logger.warn("No active transaction but commit was called");
		}
		return null;
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
	public SessionFactory getSessionFactory() {
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
	 * Register to get updated after any interaction with the datastore
	 */
	public void registerForDataStoreChanges(IConversationEnabled observer) {
		CdmPostCrudObservableListener.getDefault().register(observer);
	}
}
