/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.conversation;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.etaxonomy.cdm.persistence.hibernate.CdmPostDataChangeObservableListener;

/**
 * This is an implementation of the session-per-conversation pattern for usage in a Spring context.
 *
 * The primary aim of this class is to create and maintain sessions across multiple transactions.
 * It is important to ensure that these (long running) sessions must always behave consistently
 * with regards to session management behaviour expected by Hibernate.
 * <p>
 * This behaviour essentially revolves around the resources map in the {@link org.springframework.transaction.support.TransactionSynchronizationManager TransactionSynchronizationManager}.
 * This resources map contains two entries of interest,
 * <ul>
 *  <li>(Autowired) {@link org.hibernate.SessionFactory} mapped to the {@link org.springframework.orm.hibernate5.SessionHolder}</li>
 *  <li>(Autowired) {@link javax.sql.DataSource} mapped to the {@link org.springframework.jdbc.datasource.ConnectionHolder}</li>
 * </ul>
 * <p>
 * The SessionHolder object itself contains the {@link org.hibernate.Session Session} as well as the {@link org.hibernate.Transaction object.
 * The ConnectionHolder contains the (JDBC) {@link java.sql.Connection Connection} to the database. For every action to do with the
 * transaction object it is required to have both entries present in the resources. Both the session as well as the connection
 * objects must not be null and the corresponding holders must have their 'synchronizedWithTransaction' flag set to true.
 * <p>
 * The default behaviour of the {@link org.springframework.transaction.PlatformTransactionManager PlatformTransactionManager} which in the CDM case is autowired
 * to {@link org.springframework.orm.hibernate5.HibernateTransactionManager HibernateTransactionManager}, is to check these entries
 * when starting a transaction. If this entries do not exist in the resource map then they are created, implying a new session, which
 * is in fact how hibernate implements the default 'session-per-request' pattern internally.
 * <p>
 * Given the above conditions, this class manages long running sessions by providing the following methods,
 * <ul>
 *  <li>{@link #bind()} : binds the session owned by this conversation to the resource map.</li>
 *  <li>{@link #startTransaction()} : starts a transaction.</li>
 *  <li>{@link #commit()} : commits the current transaction, with the option of restarting a new transaction.</li>
 *  <li>{@link #unbind()} : unbinds the session owned by this conversation from the resource map.</li>
 *  <li>{@link #close()} : closes the session owned by this conversation.</li>
 * </ul>
 * <p>
 * With the exception of {@link #unbind()} (which should be called explicitly), the above sequence must be strictly followed to
 * maintain a consistent session state. Even though it is possible to interweave multiple conversations at the same time, for a
 * specific conversation the above sequence must be followed.
 *
 * @see http://www.hibernate.org/42.html
 *
 * @author n.hoffmann,c.mathew
 * @since 12.03.2009
 */
public class ConversationHolder {

    private static final Logger logger = LogManager.getLogger();

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


    private boolean closed = false;

    private FlushMode defaultFlushMode = FlushMode.COMMIT;

    /**
     * Simple constructor used by Spring only
     */
    protected ConversationHolder(){
        closed = false;
    }

    /**
     * Create a new Conversation holder and bind it immediately.
     *
     * @param dataSource
     * @param sessionFactory
     * @param transactionManager
     */
    public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory,
            PlatformTransactionManager transactionManager) {
        this(dataSource, sessionFactory, transactionManager, true);
    }

    /**
     * Create a new Conversation holder and optionally bind it immediately.
     */
    public ConversationHolder(DataSource dataSource, SessionFactory sessionFactory,
            PlatformTransactionManager transactionManager, boolean bindNow) {
        this();
        this.dataSource = dataSource;
        this.sessionFactory = sessionFactory;
        this.transactionManager = transactionManager;

        if(bindNow) {
            bind();
            if(TransactionSynchronizationManager.hasResource(getDataSource())){
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }
        }
    }

    /**
     * This method has to be called when starting a new unit-of-work. All required resources are
     * bound so that SessionFactory.getCurrentSession() returns the right session for this conversation
     */
    public void bind() {

        logger.info("Binding resources for ConversationHolder");

        if(TransactionSynchronizationManager.isSynchronizationActive()){
            logger.trace("Clearing active  transaction synchronization");
            TransactionSynchronizationManager.clearSynchronization();
        }

        try{

            logger.info("Starting new Synchronization in TransactionSynchronizationManager");
            TransactionSynchronizationManager.initSynchronization();


            if(TransactionSynchronizationManager.hasResource(getSessionFactory())){
                logger.trace("Unbinding resource from TransactionSynchronizationManager with key: " + getSessionFactory());
                TransactionSynchronizationManager.unbindResource(getSessionFactory());
            }

            if(logger.isTraceEnabled()){
                logger.trace("Binding Session to TransactionSynchronizationManager:" + getSessionHolder() + " Session [" + getSessionHolder().getSession().hashCode() + "] with key: " + getSessionFactory());
            } else {
                logger.info("Binding Session to TransactionSynchronizationManager: Session: " + getSessionHolder());
            }
            TransactionSynchronizationManager.bindResource(getSessionFactory(), getSessionHolder());



        } catch(Exception e){
            logger.error("Error binding resources for session", e);
        }

    }

    /**
     * This method has to be called when suspending the current unit of work. The conversation can be later bound again.
     */
    public void unbind() {

        logger.info("Unbinding resources for ConversationHolder");

        if(TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.clearSynchronization();
        }


        if(isBound()) {
            // unbind the current session.
            // there is no need to bind a new session, since HibernateTransactionManager will create a new one
            // if the resource map does not contain one (ditto for the datasource-to-connection entry).
            if(logger.isTraceEnabled()){
                logger.trace("Unbinding SessionFactory [" + getSessionFactory().hashCode() + "]");
            }
            TransactionSynchronizationManager.unbindResource(getSessionFactory());
            if(TransactionSynchronizationManager.hasResource(getDataSource())){
                if(logger.isTraceEnabled()){
                    logger.trace("Unbinding DataSource [" + getDataSource().hashCode() + "]");
                }
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }
        }
    }

    public SessionHolder getSessionHolder(){
        if(this.sessionHolder == null){
            this.sessionHolder = new SessionHolder(getSession());
            logger.info("Creating new SessionHolder:" + sessionHolder);
        }
        return this.sessionHolder;
    }

    private DataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isBound(){
        //return sessionHolder != null && longSession != null && longSession.isConnected();
        SessionHolder currentSessionHolder = (SessionHolder)TransactionSynchronizationManager.getResource(getSessionFactory());
        return longSession != null && currentSessionHolder != null && getSessionFactory().getCurrentSession().equals(longSession);
    }

    /**
     * Creates an instance of TransactionStatus and binds it to this conversation manager.
     * At the moment we allow only one transaction per conversation holder.
     *
     * @return the transaction status bound to this conversation holder
     */
    public TransactionStatus startTransaction(){
        if (isTransactionActive()){
            logger.warn("We allow only one transaction at the moment but startTransaction " +
                    "was called a second time.\nReturning the transaction already associated with this " +
                    "ConversationManager");
        }else{
            //always safe to remove the datasource-to-connection entry since we
            // know that HibernateTransactionManager will create a new one
            if(TransactionSynchronizationManager.hasResource(getDataSource())){
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }

            transactionStatus = transactionManager.getTransaction(definition);

            logger.info("Transaction started: " + transactionStatus);
        }
        return transactionStatus;
    }

    /**
     * @return if there is a running transaction
     */
    public boolean isTransactionActive(){
        return transactionStatus != null && !transactionStatus.isCompleted();
    }

    public void evict(Object object){
        getSession().evict(object);
    }

    public void refresh(Object object){
        getSession().refresh(object);
    }

    public void clear(){
        getSession().clear();
    }

    /**
     * Commit the running transaction.
     */
    public void commit(){
        commit(true);
    }

    /**
     * Commit the running transaction but optionally start a
     * new one right away.
     *
     * @param restartTransaction whether to start a new transaction
     */
    public TransactionStatus commit(boolean restartTransaction){
        if(isTransactionActive()){

            if(getSessionHolder().isRollbackOnly()){
                logger.error("Commiting this session will not work. It has been marked as rollback only.");
            }
            // if a datasource-to-connection entry already exists in the resource map
            // then its setSynchronizedWithTransaction should be true, since hibernate has added
            // this entry.
            // if the datasource-to-connection entry does not exist then we need to create one
            // and explicitly setSynchronizedWithTransaction to true.
            TransactionSynchronizationManager.getResource(getDataSource());
            if(!TransactionSynchronizationManager.hasResource(getDataSource())){
                try {
                    ConnectionHolder ch = new ConnectionHolder(getDataSource().getConnection());
                    ch.setSynchronizedWithTransaction(true);
                    TransactionSynchronizationManager.bindResource(getDataSource(),ch);

                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // commit the changes
            transactionManager.commit(transactionStatus);
			logger.info("Committing  Session: " + getSessionHolder());
            // propagate transaction end
            CdmPostDataChangeObservableListener.getDefault().delayedNotify();

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
     * @return the session associated with this conversation manager
     */
    public Session getSession() {

        String whatStr;

        if(longSession == null){
            longSession = getNewSession();
            whatStr = "Creating";
        } else {
            whatStr = "Reusing";
        }
        if(logger.isDebugEnabled()){
            logger.debug(whatStr + " Session: [" + longSession.hashCode() + "] " + longSession);
        } else {
            logger.info(whatStr + " Session: [" + longSession.hashCode() + "] ");
        }
        return longSession;
    }

    /**
     * @return a new session to be managed by this conversation
     */
    private Session getNewSession() {

        // Interesting: http://stackoverflow.com/questions/3526556/session-connection-deprecated-on-hibernate
        // Also, http://blog-it.hypoport.de/2012/05/10/hibernate-4-migration/

        // This will create a new session which must be explicitly managed by this conversation, which includes
        // binding / unbinding / closing session as well as starting / committing transactions.
        Session session = sessionFactory.openSession();
        session.setFlushMode(getDefaultFlushMode());

        return session;
    }




    /**
     * @return the session factory that is bound to this conversation manager
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void delete(Object object){
        this.getSession().delete(object);
    }

    /**
     * Facades Session.lock()
     */
    public void lock(Object persistentObject, LockMode lockMode) {
        getSession().lock(persistentObject, lockMode);
    }

    public void lock(String entityName, Object persistentObject, LockMode lockMode){
        getSession().lock(entityName, persistentObject, lockMode);
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
        CdmPostDataChangeObservableListener.getDefault().register(observer);
    }

    /**
     * Register to get updated after any interaction with the datastore
     */
    public void unregisterForDataStoreChanges(IConversationEnabled observer) {
        CdmPostDataChangeObservableListener.getDefault().unregister(observer);
    }

    /**
     * Free resources bound to this conversationHolder
     */
    public void close(){
        if(getSession().isOpen()) {
            getSession().close();
            unbind();
        }
        longSession = null;
        sessionHolder = null;
        closed = true;
    }

    public boolean isClosed(){
        return closed;
    }

    public boolean isCompleted(){
        return transactionStatus == null || transactionStatus.isCompleted();
    }

    /**
     * @return the defaultFlushMode
     */
    public FlushMode getDefaultFlushMode() {
        return defaultFlushMode;
    }

    /**
     * @param defaultFlushMode the defaultFlushMode to set
     */
    public void setDefaultFlushMode(FlushMode defaultFlushMode) {
        this.defaultFlushMode = defaultFlushMode;
    }


}
