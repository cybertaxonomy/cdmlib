package eu.etaxonomy.cdm.event;

public interface ICdmEventListenerRegistration {
	public abstract void addCdmEventListener(ICdmEventListener listener);

	public abstract void removeCdmEventListener(ICdmEventListener listener);
	
	public abstract ICdmEventListener[] getCdmEventListener();

}
