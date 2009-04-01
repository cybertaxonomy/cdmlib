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

import java.util.Observable;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.conversation.IConversationEnabled;
import eu.etaxonomy.cdm.persistence.hibernate.CdmCrudEvent;

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
	 * @see eu.etaxonomy.cdm.persistence.hibernate.ICdmPostCrudObserver#update(eu.etaxonomy.cdm.persistence.hibernate.CdmCrudEvent)
	 */
	public void update(CdmCrudEvent event) {
		logger.warn("CdmCrudEvent fired: " + event.getEventType() + " : " + event.getEntity());
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
