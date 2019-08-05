/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.integration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringBeanByType;


/**
 * Abstract base class for integration testing a spring / hibernate application using
 * the unitils testing framework and dbunit.
 *
 * This base class extends the {@link CdmItegrationTest} by transactions management features.
 *
 */
@Transactional(TransactionMode.DISABLED) // NOTE: we are handling transaction by ourself in this class, thus we prevent unitils from creating transactions
public abstract class CdmTransactionalIntegrationTest extends CdmIntegrationTest {

    protected static final Logger logger = Logger.getLogger(CdmTransactionalIntegrationTest.class);

    /**
     * The transaction manager to use
     */
    @SpringBeanByType
    PlatformTransactionManager transactionManager;

    /**
     * Should we roll back by default?
     */
    private boolean defaultRollback = true;

    /**
     * Should we commit the current transaction?
     */
    private boolean	complete = false;

    /**
     * Number of transactions started
     */
    private int	transactionsStarted = 0;

    /**
     * Transaction definition used by this test class: by default, a plain
     * DefaultTransactionDefinition. Subclasses can change this to cause
     * different behavior.
     */
    protected TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    /**
     * TransactionStatus for this test. Typical subclasses won't need to use it.
     */
    protected TransactionStatus	transactionStatus;

    /**
     * Get the <em>default rollback</em> flag for this test.
     *
     * @see #setDefaultRollback(boolean)
     * @return The <em>default rollback</em> flag.
     */
    protected boolean isDefaultRollback() {
        return this.defaultRollback;
    }

    /**
     * Subclasses can set this value in their constructor to change the default,
     * which is always to roll the transaction back.
     */
    public void setDefaultRollback(final boolean defaultRollback) {
        this.defaultRollback = defaultRollback;
    }

    /**
     * <p>
     * Determines whether or not to rollback transactions for the current test.
     * </p>
     * <p>
     * The default implementation delegates to {@link #isDefaultRollback()}.
     * Subclasses can override as necessary.
     * </p>
     *
     * @return The <em>rollback</em> flag for the current test.
     */
    protected boolean isRollback() {
        return isDefaultRollback();
    }

    /**
     * Call this method in an overridden {@link #runBare()} method to prevent
     * transactional execution.
     */
    protected void preventTransaction() {
        this.transactionDefinition = null;
    }

    /**
     * Call this method in an overridden {@link #runBare()} method to override
     * the transaction attributes that will be used, so that {@link #setUp()}
     * and {@link #tearDown()} behavior is modified.
     *
     * @param customDefinition the custom transaction definition
     */
    protected void setTransactionDefinition(final TransactionDefinition customDefinition) {
        this.transactionDefinition = customDefinition;
    }

    @BeforeClass
    public static void beforeClass() {
        logger.debug("before test class");
    }

    /**
     * This implementation creates a transaction before test execution.
     * <p>
     * Override {@link #onSetUpBeforeTransaction()} and/or
     * {@link #onSetUpInTransaction()} to add custom set-up behavior for
     * transactional execution. Alternatively, override this method for general
     * set-up behavior, calling <code>super.onSetUp()</code> as part of your
     * method implementation.
     *
     * @throws Exception simply let any exception propagate
     * @see #onTearDown()
     */
    @Before
    public void onSetUp() throws Exception {

        this.complete = !this.isRollback();

        if (this.transactionManager == null) {
            logger.info("No transaction manager set: test will NOT run within a transaction");
        }
        else if (this.transactionDefinition == null) {
            logger.info("No transaction definition set: test will NOT run within a transaction");
        }
        else {
            onSetUpBeforeTransaction();
            startNewTransaction();
            try {
                onSetUpInTransaction();
            }
            catch (final Exception ex) {
                endTransaction();
                throw ex;
            }
        }
    }

    @After
    @Before
    public void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Subclasses can override this method to perform any setup operations, such
     * as populating a database table, <i>before</i> the transaction created by
     * this class. Only invoked if there <i>is</i> a transaction: that is, if
     * {@link #preventTransaction()} has not been invoked in an overridden
     * {@link #runTest()} method.
     *
     * @throws Exception simply let any exception propagate
     */
    protected void onSetUpBeforeTransaction() throws Exception {

    }

    /**
     * Subclasses can override this method to perform any setup operations, such
     * as populating a database table, <i>within</i> the transaction created by
     * this class.
     * <p>
     * <b>NB:</b> Not called if there is no transaction management, due to no
     * transaction manager being provided in the context.
     * <p>
     * If any {@link Throwable} is thrown, the transaction that has been started
     * prior to the execution of this method will be
     * {@link #endTransaction() ended} (or rather an attempt will be made to
     * {@link #endTransaction() end it gracefully}); The offending
     * {@link Throwable} will then be rethrown.
     *
     * @throws Exception simply let any exception propagate
     */
    protected void onSetUpInTransaction() throws Exception {

    }

    /**
     * This implementation ends the transaction after test execution.
     * <p>
     * Override {@link #onTearDownInTransaction()} and/or
     * {@link #onTearDownAfterTransaction()} to add custom tear-down behavior
     * for transactional execution. Alternatively, override this method for
     * general tear-down behavior, calling <code>super.onTearDown()</code> as
     * part of your method implementation.
     * <p>
     * Note that {@link #onTearDownInTransaction()} will only be called if a
     * transaction is still active at the time of the test shutdown. In
     * particular, it will <i>not</i> be called if the transaction has been
     * completed with an explicit {@link #endTransaction()} call before.
     *
     * @throws Exception simply let any exception propagate
     * @see #onSetUp()
     */
    @After
    public void onTearDown() throws Exception {

        // Call onTearDownInTransaction and end transaction if the transaction
        // is still active.
        if (this.transactionStatus != null && !this.transactionStatus.isCompleted()) {
            try {
                onTearDownInTransaction();
            }
            finally {
                endTransaction();
            }
        }
        // Call onTearDownAfterTransaction if there was at least one
        // transaction, even if it has been completed early through an
        // endTransaction() call.
        if (this.transactionsStarted > 0) {
            onTearDownAfterTransaction();
        }
    }

    /**
     * Subclasses can override this method to run invariant tests here. The
     * transaction is <i>still active</i> at this point, so any changes made in
     * the transaction will still be visible. However, there is no need to clean
     * up the database, as a rollback will follow automatically.
     * <p>
     * <b>NB:</b> Not called if there is no actual transaction, for example due
     * to no transaction manager being provided in the application context.
     *
     * @throws Exception simply let any exception propagate
     */
    protected void onTearDownInTransaction() throws Exception {

    }

    /**
     * Subclasses can override this method to perform cleanup after a
     * transaction here. At this point, the transaction is <i>not active anymore</i>.
     *
     * @throws Exception simply let any exception propagate
     */
    protected void onTearDownAfterTransaction() throws Exception {

    }

    /**
     * Cause the transaction to commit for this test method, even if the test
     * method is configured to {@link #isRollback() rollback}.
     *
     * @throws IllegalStateException if the operation cannot be set to complete
     *         as no transaction manager was provided
     */
    protected void setComplete() {

        if (this.transactionManager == null) {
            throw new IllegalStateException("No transaction manager set");
        }
        this.complete = true;
        logger.debug("set complete = true");
    }

    /**
     * Immediately force a commit or rollback of the transaction, according to
     * the <code>complete</code> and {@link #isRollback() rollback} flags.
     * <p>
     * Can be used to explicitly let the transaction end early, for example to
     * check whether lazy associations of persistent objects work outside of a
     * transaction (that is, have been initialized properly).
     *
     * @see #setComplete()
     */
    protected void endTransaction() {

        final boolean commit = this.complete || !isRollback();

        if (this.transactionStatus != null) {
            try {
                logger.debug("Trying to commit or rollback");
                if (commit) {
                    this.transactionManager.commit(this.transactionStatus);
                    logger.debug("Committed transaction after execution of test");
                }
                else {
                    this.transactionManager.rollback(this.transactionStatus);
                    logger.debug("Rolled back transaction after execution of test.");
                }
            }
            finally {
                logger.debug("Clearing transactionStatus");
                this.transactionStatus = null;
            }
        }
    }

    protected void rollback() {

        if (this.transactionStatus != null) {
            try {
                logger.debug("trying to rollback");
                this.transactionManager.rollback(this.transactionStatus);
                logger.debug("Rolled back transaction after execution of test.");
            }
            finally {
                logger.debug("Clearing transactionStatus");
                this.transactionStatus = null;
            }
        }
    }


    /**
     * Start a new transaction. Only call this method if
     * {@link #endTransaction()} has been called. {@link #setComplete()} can be
     * used again in the new transaction. The fate of the new transaction, by
     * default, will be the usual rollback.
     *
     * @throws TransactionException if starting the transaction failed
     */
    protected void startNewTransaction() throws TransactionException {

        if (this.transactionStatus != null) {
            throw new IllegalStateException("Cannot start new transaction without ending existing transaction: "
                    + "Invoke endTransaction() before startNewTransaction()");
        }
        if (this.transactionManager == null) {
            throw new IllegalStateException("No transaction manager set");
        }

        this.transactionStatus = this.transactionManager.getTransaction(this.transactionDefinition);
        ++this.transactionsStarted;
        this.complete = !this.isRollback();

        if (logger.isDebugEnabled()) {
            logger.debug("Began transaction (" + this.transactionsStarted + "): transaction manager ["
                    + this.transactionManager + "]; rollback [" + this.isRollback() + "].");
        }
    }

    protected void commitAndStartNewTransaction() {
        this.commitAndStartNewTransaction(null);
    }

    /**
     * @param tableNames the tables supplied by this array will be <b>printed after</b> the transaction has committed
     * and ended <b>only if the logging level is set to debug</b>, e.g.:
     * <pre>
     *  log4j.logger.eu.etaxonomy.cdm.test.integration=DEBUG
     * </pre>
     */
    protected void commitAndStartNewTransaction(final String[] tableNames) {
        commit();
        if(logger.isEnabledFor(Level.DEBUG)){
            printDataSet(System.out, tableNames);
//          careful, the following will overwrite existing files:
//          writeDbUnitDataSetFile(tableNames);
        }
        startNewTransaction();
    }

    /**
     * Commit and end transaction
     */
    protected void commit() {
        setComplete();
        endTransaction();
    }

}
