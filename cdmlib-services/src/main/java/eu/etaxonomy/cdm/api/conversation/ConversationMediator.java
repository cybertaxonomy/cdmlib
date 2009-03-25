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
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;

/**
 * IConversationEnabled implementors may register for this mediator and get updated when 
 * the Mediator does it conversation mediation!
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class ConversationMediator implements PostDeleteEventListener, PostInsertEventListener, PostLoadEventListener, PostUpdateEventListener{
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
	 * @deprecated we don't want to do this explicitly, we rely on the hibernate listeners to do the 
	 * mediation for us
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

	public void onPostDelete(PostDeleteEvent event) {
		logger.info("post delete fired");
	}

	public void onPostInsert(PostInsertEvent event) {
		logger.info("post insert fired");	
	}

	public void onPostLoad(PostLoadEvent event) {
		logger.info("post load fired");
	}

	public void onPostUpdate(PostUpdateEvent event) {
		logger.info("post update fired");
	}
	
}
