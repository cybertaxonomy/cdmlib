/**
 * 
 */
package eu.etaxonomy.cdm.api.application;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;

/**
 * @author j.koch
 *
 */
public interface ICdmApplicationLocalConfiguration extends
		ICdmApplicationConfiguration {

    public TransactionStatus startTransaction();

    public TransactionStatus startTransaction(Boolean readOnly);

    public void commitTransaction(TransactionStatus tx);
    
	/**
	 * @return
	 */
	public PlatformTransactionManager getTransactionManager();


}
