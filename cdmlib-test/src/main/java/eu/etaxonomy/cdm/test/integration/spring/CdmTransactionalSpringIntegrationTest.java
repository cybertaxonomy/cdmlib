/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Manual Spring transaction control for integration tests
 * (Unitils {@code CdmTransactionalIntegrationTest} replacement).
 * <p>
 * Does <em>not</em> use Spring's {@code @Transactional} test support so that
 * {@link #commit()}, {@link #commitAndStartNewTransaction()} etc. keep working
 * for lazy-load / dataset patterns.
 */
public abstract class CdmTransactionalSpringIntegrationTest extends CdmSpringIntegrationTest {

    protected static final Logger logger = LogManager.getLogger();

    private boolean defaultRollback = true;
    private boolean complete = false;
    private int transactionsStarted = 0;
    private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    private TransactionStatus transactionStatus;

    protected boolean isDefaultRollback() {
        return this.defaultRollback;
    }

    public void setDefaultRollback(final boolean defaultRollback) {
        this.defaultRollback = defaultRollback;
    }

    protected boolean isRollback() {
        return isDefaultRollback();
    }

    protected void preventTransaction() {
        this.transactionDefinition = null;
    }

    protected void setTransactionDefinition(final TransactionDefinition customDefinition) {
        this.transactionDefinition = customDefinition;
    }

    @BeforeEach
    public void onSetUp() throws Exception {
        this.complete = !this.isRollback();

        if (this.transactionManager == null) {
            logger.info("No transaction manager set: test will NOT run within a transaction");
        } else if (this.transactionDefinition == null) {
            logger.info("No transaction definition set: test will NOT run within a transaction");
        } else {
            onSetUpBeforeTransaction();
            startNewTransaction();
            try {
                onSetUpInTransaction();
            } catch (final Exception ex) {
                endTransaction();
                throw ex;
            }
        }
    }

    @AfterEach
    @BeforeEach
    public void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    protected void onSetUpBeforeTransaction() throws Exception {
    }

    protected void onSetUpInTransaction() throws Exception {
    }

    @AfterEach
    public void onTearDown() throws Exception {
        if (this.transactionStatus != null && !this.transactionStatus.isCompleted()) {
            try {
                onTearDownInTransaction();
            } finally {
                endTransaction();
            }
        }
        if (this.transactionsStarted > 0) {
            onTearDownAfterTransaction();
        }
    }

    protected void onTearDownInTransaction() throws Exception {
    }

    protected void onTearDownAfterTransaction() throws Exception {
    }

    protected void setComplete() {
        if (this.transactionManager == null) {
            throw new IllegalStateException("No transaction manager set");
        }
        this.complete = true;
        logger.debug("set complete = true");
    }

    protected void endTransaction() {
        final boolean commit = this.complete || !isRollback();
        if (this.transactionStatus != null) {
            try {
                if (commit) {
                    this.transactionManager.commit(this.transactionStatus);
                    logger.debug("Committed transaction after execution of test");
                } else {
                    this.transactionManager.rollback(this.transactionStatus);
                    logger.debug("Rolled back transaction after execution of test.");
                }
            } finally {
                this.transactionStatus = null;
            }
        }
    }

    protected void rollback() {
        if (this.transactionStatus != null) {
            try {
                this.transactionManager.rollback(this.transactionStatus);
            } finally {
                this.transactionStatus = null;
            }
        }
    }

    protected void startNewTransaction() throws TransactionException {
        if (this.transactionStatus != null) {
            throw new IllegalStateException(
                    "Cannot start new transaction without ending existing transaction: "
                            + "Invoke endTransaction() before startNewTransaction()");
        }
        if (this.transactionManager == null) {
            throw new IllegalStateException("No transaction manager set");
        }
        this.transactionStatus = this.transactionManager.getTransaction(this.transactionDefinition);
        ++this.transactionsStarted;
        this.complete = !this.isRollback();
    }

    protected void commitAndStartNewTransaction() {
        this.commitAndStartNewTransaction(null);
    }

    protected void commitAndStartNewTransaction(final String[] tableNames) {
        commit();
        if (tableNames != null && logger.isDebugEnabled()) {
            printDataSet(System.out, tableNames);
        }
        startNewTransaction();
    }

    protected void commit() {
        setComplete();
        endTransaction();
    }
}
