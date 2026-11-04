/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.conversation;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.etaxonomy.cdm.persistence.hibernate.CdmPostDataChangeObservableListener;
import eu.etaxonomy.cdm.persistence.hibernate.CdmSessionUtils;
import jakarta.persistence.EntityManager;

/**
 * Session-per-conversation for Spring + JPA ({@code JpaTransactionManager}).
 * <p>
 * Holds an application-managed (extended) persistence context across multiple
 * short transactions by binding an {@link EntityManagerHolder} under the
 * {@link SessionFactory} key. {@code JpaTransactionManager} reuses that holder
 * when it is not yet synchronized with a transaction; DAOs resolve the Session
 * via {@link CdmSessionUtils}.
 * <p>
 * Lifecycle: {@link #bind()} → {@link #startTransaction()} → work →
 * {@link #commit()} / {@link #unbind()} → {@link #close()}.
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
     * Long-lived Hibernate Session (also the JPA EntityManager) for this conversation.
     */
    private Session longSession = null;

    /**
     * Bound into {@link TransactionSynchronizationManager} for JpaTransactionManager /
     * {@link CdmSessionUtils}.
     */
    private EntityManagerHolder entityManagerHolder = null;

    /**
     * @see TransactionDefinition
     */
    private TransactionDefinition definition;

    /**
     * This conversation's transaction
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
            // Never unbind DataSource while Spring TX synchronizations may still
            // reference it (ConnectionSynchronization.suspend would then fail).
        }
    }

    /**
     * Bind this conversation's persistence context so DAO / service code sees it
     * (via {@link CdmSessionUtils} / the bound {@link EntityManagerHolder}).
     */
    public void bind() {

        logger.info("Binding resources for ConversationHolder");

        try{
            if(TransactionSynchronizationManager.hasResource(getSessionFactory())){
                Object existing = TransactionSynchronizationManager.getResource(getSessionFactory());
                if (existing != getEntityManagerHolder()) {
                    logger.trace("Unbinding resource from TransactionSynchronizationManager with key: "
                            + getSessionFactory());
                    TransactionSynchronizationManager.unbindResource(getSessionFactory());
                } else {
                    logger.trace("Conversation EntityManagerHolder already bound");
                    return;
                }
            }

            if(logger.isTraceEnabled()){
                logger.trace("Binding EntityManager to TransactionSynchronizationManager:"
                        + getEntityManagerHolder() + " Session ["
                        + getSession().hashCode() + "] with key: " + getSessionFactory());
            } else {
                logger.info("Binding EntityManager to TransactionSynchronizationManager: "
                        + getEntityManagerHolder());
            }
            TransactionSynchronizationManager.bindResource(getSessionFactory(), getEntityManagerHolder());

        } catch(Exception e){
            logger.error("Error binding resources for session", e);
        }

    }

    /**
     * Suspend this conversation; it can be {@link #bind() bound} again later.
     */
    public void unbind() {

        logger.info("Unbinding resources for ConversationHolder");

        Object resource = TransactionSynchronizationManager.getResource(getSessionFactory());
        if (resource != null && (resource == entityManagerHolder || isBound())) {
            if(logger.isTraceEnabled()){
                logger.trace("Unbinding SessionFactory [" + getSessionFactory().hashCode() + "]");
            }
            TransactionSynchronizationManager.unbindResourceIfPossible(getSessionFactory());
        }
        // Do not touch DataSource / clearSynchronization here: JpaTransactionManager
        // and ConnectionSynchronization own that lifecycle. Manual unbind caused
        // "No value for key [DataSource] bound to thread" on the next getTransaction.
    }

    /**
     * @return the holder bound for JPA / {@link CdmSessionUtils}
     */
    public EntityManagerHolder getEntityManagerHolder(){
        if(this.entityManagerHolder == null){
            // Session implements EntityManager in Hibernate 6
            this.entityManagerHolder = new EntityManagerHolder(getSession());
            logger.info("Creating new EntityManagerHolder:" + entityManagerHolder);
        }
        return this.entityManagerHolder;
    }

    /**
     * @deprecated use {@link #getEntityManagerHolder()}; kept for transitional callers
     */
    @Deprecated
    public EntityManagerHolder getSessionHolder(){
        return getEntityManagerHolder();
    }

    public boolean isBound(){
        Object resource = TransactionSynchronizationManager.getResource(getSessionFactory());
        if (longSession == null || !(resource instanceof EntityManagerHolder)) {
            return false;
        }
        EntityManager em = ((EntityManagerHolder) resource).getEntityManager();
        if (em == null || !em.isOpen()) {
            return false;
        }
        return longSession.equals(em.unwrap(Session.class));
    }

    /**
     * Creates an instance of TransactionStatus and binds it to this conversation manager.
     * At the moment we allow only one transaction per conversation holder.
     *
     * @return the transaction status bound to this conversation holder
     */
    public TransactionStatus startTransaction(){
        if (isTransactionActive()){
            logger.warn("We allow only one transaction at the moment but startTransaction "
                    + "was called a second time.\nReturning the transaction already associated with this "
                    + "ConversationManager");
        }else{
            if (!isBound()) {
                bind();
            }
            // Do not touch DataSource/ConnectionHolder here: JpaTransactionManager (and its
            // ConnectionSynchronization) owns that lifecycle. Unbinding first causes
            // "No value for key [DataSource] bound to thread" on suspend/resume.

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

            if(getEntityManagerHolder().isRollbackOnly()){
                logger.error("Commiting this session will not work. It has been marked as rollback only.");
            }

            // commit the changes (JpaTM keeps a non-new EntityManagerHolder bound and open)
            transactionManager.commit(transactionStatus);
            logger.info("Committing Session: " + getEntityManagerHolder());
            // propagate transaction end
            CdmPostDataChangeObservableListener.getDefault().delayedNotify();

            // Reset the transactionStatus.
            transactionStatus = null;

            // Ensure conversation EM is still the thread-bound resource after commit cleanup
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
        // Application-managed Session (= extended PC). Must stay open across TX boundaries;
        // JpaTransactionManager must see it via EntityManagerHolder (not create a TX-scoped EM).
        Session session = sessionFactory.openSession();
        session.setHibernateFlushMode(getDefaultFlushMode());
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
        // Roll back any still-active Spring TX before releasing the Session / connection.
        // Overlapping dual-TX tests otherwise leave checked-out connections and hang the suite.
        if (isTransactionActive()) {
            try {
                transactionManager.rollback(transactionStatus);
            } catch (Exception e) {
                logger.warn("Error rolling back conversation transaction on close", e);
            } finally {
                transactionStatus = null;
            }
        }
        // Unbind while EM is still open (isBound() unwraps the Session).
        try {
            unbind();
        } catch (Exception e) {
            logger.warn("Error unbinding conversation resources on close", e);
        }
        if(longSession != null && longSession.isOpen()) {
            longSession.close();
        }
        // Clear a leftover closed holder if unbind did not remove it
        if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
            Object resource = TransactionSynchronizationManager.getResource(getSessionFactory());
            if (resource == entityManagerHolder
                    || (resource instanceof EntityManagerHolder
                            && !((EntityManagerHolder) resource).getEntityManager().isOpen())) {
                TransactionSynchronizationManager.unbindResource(getSessionFactory());
            }
        }
        longSession = null;
        entityManagerHolder = null;
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
