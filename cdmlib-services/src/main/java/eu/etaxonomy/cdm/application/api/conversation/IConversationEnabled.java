// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.application.api.conversation;


/**
 * Objects implementing this class hold a conversation. 
 * 
 * @author n.hoffmann
 * @created 17.03.2009
 * @version 1.0
 */
public interface IConversationEnabled {
	
	/** 
	 * @return the conversation holder
	 */
	public ConversationHolder getConversationHolder();
	
	/**
	 * A mediation event might have changed the persistence context (the ConversationHolder) of this 
	 * IConversationEnabled instance. Any actions that have to be taken to cope with these changes 
	 * should take place here, as it gets called automatically by the mediator.
	 */
	public void updateAfterPropagation();
}
