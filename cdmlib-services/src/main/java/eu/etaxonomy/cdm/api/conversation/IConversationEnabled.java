/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.conversation;

import eu.etaxonomy.cdm.persistence.hibernate.ICdmPostDataChangeObserver;


/**
 * Objects implementing this class should hold a conversation. 
 * 
 * @author n.hoffmann
 * @since 17.03.2009
 * @version 1.0
 */
public interface IConversationEnabled extends ICdmPostDataChangeObserver {
	
	/** 
	 * @return the conversation holder
	 */
	public ConversationHolder getConversationHolder();
}
