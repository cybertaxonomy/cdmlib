/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.common.CdmBase;

//$Id$
/**
 * The CrudEvent unifies all CRUD events into one interface.
 * Crud as in Create, Retrieve, Update, Delete. This event will only hold CdmBase objects.
 *  
 * 
 * @author n.hoffmann
 * @created 24.03.2009
 * @version 1.0
 */
public class CdmDataChangeEvent extends AbstractEvent{
	private static final long serialVersionUID = 9113025682352080372L;
	private static final Logger logger = Logger.getLogger(CdmDataChangeEvent.class);
	
	/**
	 * The event types currently implemented
	 * 
	 * @author n.hoffmann
	 * @created 25.03.2009
	 * @version 1.0
	 */
	public enum EventType {
		INSERT, LOAD, UPDATE, DELETE
	}
	
	private CdmBase entity;
	private EntityPersister persister;
	private Object[] state;
	// for update only
	private Object[] oldState;
	private Serializable id;
	private EventType eventType;
	
	/**
	 * @param source
	 */
	private CdmDataChangeEvent(
			CdmBase entity, 
			Serializable id,
			Object[] state,
			Object[] oldState,
			EntityPersister persister,
			EventSource source,
			EventType eventType
	) {
		super(source);
		this.entity = entity;
		this.id = id;
		this.state = state;
		this.oldState = oldState;
		this.persister = persister;
		this.eventType = eventType;
	}

	public CdmBase getEntity() {
		return entity;
	}
	public Serializable getId() {
		return id;
	}
	public EntityPersister getPersister() {
		return persister;
	}
	public Object[] getState() {
		return state;
	}
	public Object[] getOldState() {
		if(oldState == null){
			oldState = state;
		}
		return oldState;
	}
	
	public EventType getEventType(){
		return eventType;
	}

	public boolean isInsert(){
		return eventType == EventType.INSERT;
	}
	
	public boolean isLoad(){
		return eventType == EventType.LOAD;
	}
	
	public boolean isUpdate(){
		return eventType == EventType.UPDATE;
	}
	
	public boolean isDelete(){
		return eventType == EventType.DELETE;
	}

	/**
	 * @param event
	 * @return
	 */
	public static CdmDataChangeEvent NewInstance(AbstractEvent event) {

		CdmDataChangeEvent mediationEvent = null;
		try{
			if(event instanceof PostInsertEvent){
				PostInsertEvent postEvent = (PostInsertEvent) event;
				mediationEvent = new CdmDataChangeEvent(
																(CdmBase)postEvent.getEntity(), 
																postEvent.getId(), 
																postEvent.getState(), 
																null,
																postEvent.getPersister(), 
																postEvent.getSession(), 
																EventType.INSERT
																);
			}
			if(event instanceof PostLoadEvent){
				PostLoadEvent updateEvent = (PostLoadEvent) event;
				mediationEvent = new CdmDataChangeEvent(
																(CdmBase)updateEvent.getEntity(), 
																updateEvent.getId(), 
																null,
																null,
																updateEvent.getPersister(), 
																updateEvent.getSession(), 
																EventType.LOAD
																);
			}
			if(event instanceof PostUpdateEvent){
				PostUpdateEvent updateEvent = (PostUpdateEvent) event;
				mediationEvent = new CdmDataChangeEvent(
																(CdmBase)updateEvent.getEntity(), 
																updateEvent.getId(), 
																updateEvent.getState(), 
																updateEvent.getOldState(),
																updateEvent.getPersister(), 
																updateEvent.getSession(), 
																EventType.UPDATE
																);
			}
			if(event instanceof PostDeleteEvent){
				PostDeleteEvent deleteEvent = (PostDeleteEvent) event;
				mediationEvent = new CdmDataChangeEvent(
																(CdmBase)deleteEvent.getEntity(), 
																deleteEvent.getId(), 
																deleteEvent.getDeletedState(), 
																null,
																deleteEvent.getPersister(), 
																deleteEvent.getSession(), 
																EventType.DELETE
																);
			}	
		}catch(ClassCastException e){
			// we are only interested in CdmBase entities, we have the try/catch block in case another entity slips through
			logger.warn("tried to instantiate event for non CdmBase entity");
		}
		
		return mediationEvent;
	}
	
}
