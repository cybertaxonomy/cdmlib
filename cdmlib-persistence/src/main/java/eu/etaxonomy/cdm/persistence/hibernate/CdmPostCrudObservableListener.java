// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

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

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * ICdmPostCrudObserver implementors may register for this listener and their updatedAfterEvent will 
 * be called after any CRUD (Create, Retrieve, Update, Delete).
 * 
 * Only events whose entities are of type CdmBase will be propagated
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class CdmPostCrudObservableListener implements PostDeleteEventListener, PostInsertEventListener, PostLoadEventListener, PostUpdateEventListener{

	private static final long serialVersionUID = -8764348096490526927L;

	private static final Logger logger = Logger
			.getLogger(CdmPostCrudObservableListener.class);
	
	/**
	 * Observing objects 
	 */
	private Set<ICdmPostCrudObserver> observers = new HashSet<ICdmPostCrudObserver>();
	
	/**
	 * Singleton instance
	 */
	private static CdmPostCrudObservableListener instance;
	
	/**
	 * @return the singleton ConversationMediator
	 */
	public static CdmPostCrudObservableListener getDefault(){
		if(instance == null){
			instance = new CdmPostCrudObservableListener();
		}
		return instance;
	}
	
	/**
	 * Register for updates
	 * 
	 * @param observer
	 */
	public void register(ICdmPostCrudObserver observer){
		getDefault().observers.add(observer);
	}
	
	/**
	 * Propagates the event to all registered objects.
	 * 
	 * @param event
	 */
	private void notifyObservers(CdmCrudEvent event){
		for( ICdmPostCrudObserver observer : observers){
			// call update() on the ICdmPostCrudObserver implementor
			observer.update(event);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.hibernate.event.PostInsertEventListener#onPostInsert(org.hibernate.event.PostInsertEvent)
	 */
	public void onPostInsert(PostInsertEvent event) {
		logger.trace("post insert fired: " + event.getEntity());	
		if(event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmCrudEvent.NewInstance(event));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.hibernate.event.PostLoadEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)
	 */
	public void onPostLoad(PostLoadEvent event) {
		logger.trace("post load fired: " + event.getEntity());
		if(event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmCrudEvent.NewInstance(event));
		}
	}

	/* (non-Javadoc)
	 * @see org.hibernate.event.PostUpdateEventListener#onPostUpdate(org.hibernate.event.PostUpdateEvent)
	 */
	public void onPostUpdate(PostUpdateEvent event) {
		logger.trace("post update fired: " + event.getEntity());
		if(event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmCrudEvent.NewInstance(event));
		}
	}

	/* (non-Javadoc)
	 * @see org.hibernate.event.PostDeleteEventListener#onPostDelete(org.hibernate.event.PostDeleteEvent)
	 */
	public void onPostDelete(PostDeleteEvent event) {
		logger.trace("post delete fired: " + event.getEntity());
		if(event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmCrudEvent.NewInstance(event));
		}
	}
	
}
