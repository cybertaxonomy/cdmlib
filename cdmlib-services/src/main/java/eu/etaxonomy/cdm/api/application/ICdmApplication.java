/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.application;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;

/**
 * @author a.mueller
 * @since 21.05.2008
 */
public interface ICdmApplication extends ICdmRepository {

    /**
     * Starts a read only transaction
     */
    public TransactionStatus startTransaction();

    public TransactionStatus startTransaction(Boolean readOnly);

    public void commitTransaction(TransactionStatus tx);

    public void rollbackTransaction(TransactionStatus txStatus);

	public Object getBean(String string);


    public PlatformTransactionManager getTransactionManager();

    public ConversationHolder NewConversation();

}
