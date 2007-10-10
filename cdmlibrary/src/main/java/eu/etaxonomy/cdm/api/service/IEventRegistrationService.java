package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.event.ICdmEventListenerRegistration;

public interface IEventRegistrationService {
	public abstract void addCdmEventListener(ICdmEventListener listener, Class clazz);

	public abstract void removeCdmEventListener(ICdmEventListener listener, Class clazz);
	
	public abstract ICdmEventListener[] getCdmEventListener(Class clazz);
}
