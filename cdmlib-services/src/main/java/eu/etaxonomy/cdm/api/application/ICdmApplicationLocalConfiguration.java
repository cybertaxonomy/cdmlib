/**
 * 
 */
package eu.etaxonomy.cdm.api.application;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;

/**
 * @author j.koch
 *
 */
public interface ICdmApplicationLocalConfiguration extends
		ICdmApplicationConfiguration {

	/**
	 * @return
	 */
	public PlatformTransactionManager getTransactionManager();
	
	/**
	 * 
	 * @return
	 */
	public ProviderManager getAuthenticationManager();
	
	/**
	 * @return
	 */
	public ConversationHolder NewConversation();
}
