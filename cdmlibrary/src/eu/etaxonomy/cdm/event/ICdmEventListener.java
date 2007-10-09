package eu.etaxonomy.cdm.event;

import java.util.EventObject;

/*
 * Listener to be implemented by a View class in a MVC environment
 * Registered listeners will receive modified CDM objects from the persistence layer 
 */
public interface ICdmEventListener {
	public abstract void onLoad(EventObject event);
		
	public abstract void onUpdate(EventObject event);

	public abstract void onInsert(EventObject event);

	public abstract void onDelete(EventObject event);
}
