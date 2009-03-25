/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.conversation;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * IConversationEnabled implementors may register for this mediator and their updatedAfterEvent will 
 * be called on insert, update and delete.
 * 
 * Only events whose entities are of type CdmBase will be mediated
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class ConversationMediator implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener{
	private static final Logger logger = Logger
			.getLogger(ConversationMediator.class);
	
	/**
	 * Observing objects 
	 */
	private Set<IConversationEnabled> conversationEnableds = new HashSet<IConversationEnabled>();
	
	/**
	 * Singleton instance
	 */
	private static ConversationMediator instance;
	
	/**
	 * @return the singleton ConversationMediator
	 */
	public static ConversationMediator getDefault(){
		if(instance == null){
			instance = new ConversationMediator();
		}
		return instance;
	}
	
	/**
	 * Register for mediation.
	 * 
	 * @param conversationEnabled
	 */
	public static void register(IConversationEnabled conversationEnabled){
		getDefault().conversationEnableds.add(conversationEnabled);
	}
	
	/**
	 * Propagates the event to all registered objects.
	 * 
	 * @param event
	 */
	private void mediate(ConversationMediationEvent event){
		for( IConversationEnabled conversationEnabled : conversationEnableds){
			// update the IConversationEnabled implementor
			conversationEnabled.updateAfterEvent(event);
		}
	}
	
	/**
	 * Listener hook for insert events
	 */
	public void onPostInsert(PostInsertEvent event) {
		logger.trace("post insert fired");	
		if(event.getEntity() instanceof CdmBase){
			getDefault().mediate(ConversationMediationEvent.NewInstance(event));
		}
	}

	/**
	 * Listener hook for update events
	 */
	public void onPostUpdate(PostUpdateEvent event) {
		logger.trace("post update fired");
		if(event.getEntity() instanceof CdmBase){
			getDefault().mediate(ConversationMediationEvent.NewInstance(event));
		}
	}

	/**
	 * Listener hook for delete events
	 */
	public void onPostDelete(PostDeleteEvent event) {
		logger.trace("post delete fired");
		if(event.getEntity() instanceof CdmBase){
			getDefault().mediate(ConversationMediationEvent.NewInstance(event));
		}
	}
	
}
