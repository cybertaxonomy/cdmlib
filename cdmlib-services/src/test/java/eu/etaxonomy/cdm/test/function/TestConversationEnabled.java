/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.conversation.IConversationEnabled;
import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeEvent;
import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeMap;

/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 * @author n.hoffmann
 * @created 25.03.2009
 * @version 1.0
 */
@Ignore
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
	 * @see eu.etaxonomy.cdm.persistence.hibernate.ICdmPostDataChangeObserver#update(eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeMap)
	 */
	public void update(CdmDataChangeMap changeEvents) {
		Collection<CdmDataChangeEvent> events = changeEvents.getAllEvents();
		for(CdmDataChangeEvent event : events){
			logger.warn("CdmCrudEvent fired: " + event.getEventType() + " : " + event.getEntity());
		}
	}
}
