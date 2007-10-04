package eu.etaxonomy.cdm.event;

public interface ICdmEventListenerRegistration {
	public abstract void addCdmEventListener(ICdmChangeListener listener);

	public abstract void removeCdmEventListener(ICdmChangeListener listener);
	
	public abstract ICdmChangeListener[] getCdmEventListener();

}
