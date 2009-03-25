// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.conversation.ConversationMediationEvent;
import eu.etaxonomy.cdm.api.conversation.IConversationEnabled;

/**
 * @author nho
 * @created 25.03.2009
 * @version 1.0
 */
public class TestConversationEnabled implements IConversationEnabled {
	private static final Logger logger = Logger
			.getLogger(TestConversationEnabled.class);

	ConversationHolder conversationHolder;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.conversation.IConversationEnabled#getConversationHolder()
	 */
	public ConversationHolder getConversationHolder() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.conversation.IConversationEnabled#updateAfterEvent(eu.etaxonomy.cdm.api.conversation.ConversationMediationEvent)
	 */
	public void updateAfterEvent(ConversationMediationEvent event) {
		logger.warn("ConversationMediationEvent fired: " + event.getEventType());
	}
}
