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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * ICdmPostDataChangeObserver implementors may register for this listener and their update() will
 * be called after any CRUD (Create, Retrieve, Update, Delete).
 *
 * Only events whose entities are of type CdmBase will be propagated
 *
 * TODO Manage this class via Spring
 *
 * @author n.hoffmann
 * @since 24.03.2009
 */
public class CdmPostDataChangeObservableListener implements
		  PostDeleteEventListener
		, PostInsertEventListener
		, PostLoadEventListener
		, PostUpdateEventListener
{
	private static final long serialVersionUID = -8764348096490526927L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	/**
	 * if this is set to true, observers have to be notified manually by calling
	 * {@link #delayedNotify()}. All events until then will be stored in {@link #changeEvents}
	 */
	private boolean delayed;

	/**
	 * Define what events will be propagated
	 */
	private boolean propagateLoads = false,
					propagateInserts = true,
					propagateUpdates = true,
					propagateDeletes = true;

	/**
	 * DataChangeEvents get stored in this list for delayed propagation
	 */
	private CdmDataChangeMap changeEvents;

	/**
	 * Observing objects
	 */
	private final Set<ICdmPostDataChangeObserver> observers = new HashSet<>();


	/**
	 * Singleton instance
	 */
	private static CdmPostDataChangeObservableListener instance;

	/**
	 * @return the singleton CdmPostDataChangeObservableListener
	 */
	public static CdmPostDataChangeObservableListener getDefault(){
		if(instance == null){
			instance = new CdmPostDataChangeObservableListener();
			// TODO set these properties via Spring
			// get the delayed version by default
			instance.setDelayed(true);
			// omit load events from propagation
			instance.setPropagateLoads(false);
		}
		return instance;
	}

	/**
	 * Register for updates
	 *
	 * @param observer
	 */
	public void register(ICdmPostDataChangeObserver observer){
		getDefault().observers.add(observer);
	}

	/**
	 * Remove observer from notify queue
	 * @param observer
	 */
	public void unregister(ICdmPostDataChangeObserver observer){
		getDefault().observers.remove(observer);
	}

	public void delayedNotify(){
		if(delayed && changeEvents.size() > 0){
			Set<ICdmPostDataChangeObserver> modificationSaveObservers
						= new HashSet<>(observers);
			for( ICdmPostDataChangeObserver observer : modificationSaveObservers){
				observer.update(changeEvents);
			}
			changeEvents.clear();
		}
	}

	/**
	 * Propagates the event to all registered objects.
	 *
	 * @param event
	 */
	public void notifyObservers(CdmDataChangeEvent event){
		for( ICdmPostDataChangeObserver observer : observers){
			if(delayed){
				// store event for delayed propagation
				changeEvents.add(event.getEventType(), event);
			}else{
				// propagate event directly
				CdmDataChangeMap tmpMap = new CdmDataChangeMap();
				tmpMap.add(event.getEventType(), event);
				observer.update(tmpMap);
			}
		}
	}

	public void fireNotification(CdmDataChangeEvent event) {
	    for( ICdmPostDataChangeObserver observer : observers){
	        // propagate event directly
	        CdmDataChangeMap tmpMap = new CdmDataChangeMap();
	        tmpMap.add(event.getEventType(), event);
	        observer.update(tmpMap);
	    }
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if(propagateInserts && event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmDataChangeEvent.NewInstance(event));
		}
	}

	@Override
	public void onPostLoad(PostLoadEvent event) {
		if(propagateLoads && event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmDataChangeEvent.NewInstance(event));
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if(propagateUpdates && event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmDataChangeEvent.NewInstance(event));
		}
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if(propagateDeletes && event.getEntity() instanceof CdmBase){
			getDefault().notifyObservers(CdmDataChangeEvent.NewInstance(event));
		}
	}

	public boolean isDelayed() {
		return delayed;
	}
	public void setDelayed(boolean delayed) {
		if(delayed && changeEvents == null){
			changeEvents = new CdmDataChangeMap();
		}
		this.delayed = delayed;
	}

	public boolean isPropagateLoads() {
		return propagateLoads;
	}
	public void setPropagateLoads(boolean propagateLoads) {
		this.propagateLoads = propagateLoads;
	}

	public boolean isPropagateInserts() {
		return propagateInserts;
	}
	public void setPropagateInserts(boolean propagateInserts) {
		this.propagateInserts = propagateInserts;
	}

	public boolean isPropagateUpdates() {
		return propagateUpdates;
	}
	public void setPropagateUpdates(boolean propagateUpdates) {
		this.propagateUpdates = propagateUpdates;
	}


	public boolean isPropagateDeletes() {
		return propagateDeletes;
	}
	public void setPropagateDeletes(boolean propagateDeletes) {
		this.propagateDeletes = propagateDeletes;
	}

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}