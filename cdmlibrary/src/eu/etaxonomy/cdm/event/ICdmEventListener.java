package eu.etaxonomy.cdm.event;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/*
 * Listener to be implemented by a View class in a MVC environment
 * Registered listeners will receive modified CDM objects from the persistence layer 
 */
public interface ICdmEventListener {
	public abstract void onLoad(VersionableEntity cdmObj);
		
	public abstract void onUpdate(VersionableEntity cdmObj);

	public abstract void onInsert(VersionableEntity cdmObj);

	public abstract void onDelete(VersionableEntity cdmObj);
}
