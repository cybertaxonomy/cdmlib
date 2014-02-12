/**
 * 
 */
package eu.etaxonomy.cdm.api.application;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDatabaseService;

/**
 * @author j.koch
 *
 */
public interface ICdmApplicationDefaultConfiguration extends ICdmApplicationConfiguration {

    
    public IDatabaseService getDatabaseService();
	
    public TransactionStatus startTransaction();

    public TransactionStatus startTransaction(Boolean readOnly);

    public void commitTransaction(TransactionStatus tx);
    
	public PlatformTransactionManager getTransactionManager();

	public ConversationHolder NewConversation();
}
