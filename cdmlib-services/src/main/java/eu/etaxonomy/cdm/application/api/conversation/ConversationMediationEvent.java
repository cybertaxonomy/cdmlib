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
 * The conversation mediation event is a container that holds all objects that were changed (are dirty 
 * in terms of a hibernate session). 
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class ConversationMediationEvent {
	private static final Logger logger = Logger
			.getLogger(ConversationMediationEvent.class);
	
	private Set<Object> objects = new HashSet<Object>();
	
	public Set<Object> getObjects(){
		return objects;
	}
	
	public void addObject(Object object){
		objects.add(object);
	}
	
}
