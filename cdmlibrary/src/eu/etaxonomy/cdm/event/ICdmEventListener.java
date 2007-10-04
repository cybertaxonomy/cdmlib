package eu.etaxonomy.cdm.event;

/*
 * Listener to be implemented by a View class in a MVC environment
 * Registered listeners will receive modified CDM objects from the persistence layer 
 */
public interface ICdmEventListener {
	public abstract void onLoad(ICdmEventListenerRegistration cdmObj);
		
	public abstract void onUpdate(ICdmEventListenerRegistration cdmObj);

	public abstract void onInsert(ICdmEventListenerRegistration cdmObj);

	public abstract void onDelete(ICdmEventListenerRegistration cdmObj);
}
