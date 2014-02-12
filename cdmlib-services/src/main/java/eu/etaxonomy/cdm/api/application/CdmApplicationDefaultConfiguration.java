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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
@Component
public class CdmApplicationDefaultConfiguration extends CdmApplicationConfiguration implements ICdmApplicationDefaultConfiguration, ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(CdmApplicationDefaultConfiguration.class);

	@Autowired
	private IDatabaseService databaseService;
	
    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private HibernateTransactionManager transactionManager;

    
	/**
	 * 
	 */
	public CdmApplicationDefaultConfiguration() {
	}
	
	

	@Override
	public IDatabaseService getDatabaseService() {
		return this.databaseService;
	}

	
    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
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
            // org.springframework.orm.hibernate4.HibernateTransactionManager
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
	public ConversationHolder NewConversation() {
		return new ConversationHolder(dataSource, sessionFactory, transactionManager);

	}


}
