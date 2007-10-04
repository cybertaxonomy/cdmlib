package eu.etaxonomy.cdm.event;

import java.util.List;

public abstract class CdmEventListenerSupport implements ICdmEventListenerRegistration{

	//****** CDM Event Support *****//	

	/**
	 * listener registry
	 */
	//transient: not serialized
	private transient List<ICdmEventListener> listener; // ICdmChangeListener
	
	/**
     * Adds a cdm event listener.
     * @param listener the cdm event listener
     */
	public void addCdmEventListener(ICdmEventListener listener){
		this.listener.add(listener);
	}

	public void removeCdmEventListener(ICdmEventListener listener){
		this.listener.remove(listener);
	}
	
	public ICdmEventListener[] getCdmEventListener(){
		return (ICdmEventListener[]) listener.toArray();
	}
	
//******* END CDM Event Support  ******//
	
}
