package eu.etaxonomy.cdm.event;

import java.util.List;

public abstract class CdmEventListenerSupport implements ICdmEventListenerRegistration{

	//****** CDM Event Support *****//	

	/**
	 * listener registry
	 */
	//transient: not serialized
	private transient List<ICdmChangeListener> listener; // ICdmChangeListener
	
	/**
     * Adds a cdm event listener.
     * @param listener the cdm event listener
     */
	public void addCdmEventListener(ICdmChangeListener listener){
		this.listener.add(listener);
	}

	public void removeCdmEventListener(ICdmChangeListener listener){
		this.listener.remove(listener);
	}
	
	public ICdmChangeListener[] getCdmEventListener(){
		return (ICdmChangeListener[]) listener.toArray();
	}
	
//******* END CDM Event Support  ******//
	
}
