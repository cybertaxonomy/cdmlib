/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.application.api.conversation;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * IConversationEnabled implementors may register for this mediator and get updated when 
 * the Mediator does it conversation mediation!
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class ConversationMediator {
	private static final Logger logger = Logger
			.getLogger(ConversationMediator.class);
	
	private Set<IConversationEnabled> conversationEnableds = new HashSet<IConversationEnabled>();
	
	/**
	 * Register for mediation.
	 * 
	 * @param conversationEnabled
	 */
	public void register(IConversationEnabled conversationEnabled){
		conversationEnableds.add(conversationEnabled);
	}
	
	/**
	 * Propagates the event to all registered objects.
	 * 
	 * @param event
	 */
	public void mediate(ConversationMediationEvent event){
		for( IConversationEnabled conversationEnabled : conversationEnableds){
			ConversationHolder conversation = conversationEnabled.getConversationHolder();
			// update the persistence context
			conversation.propagateEvent(event);
			// update the IConversationEnabled implementor
			conversationEnabled.updateAfterPropagation();
		}
	}
	
}
