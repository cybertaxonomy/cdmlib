package eu.etaxonomy.cdm.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

public abstract class CdmEventListenerRegistrationSupport implements ICdmEventListenerRegistration{
	static Logger logger = Logger.getLogger(CdmEventListenerRegistrationSupport.class);

	//****** CDM Event Support *****//	

	/**
	 * listener registry
	 */
	//transient: not serialized
	// use threadsafe arraylist, see http://www.ibm.com/developerworks/library/j-jtp07265/
	private transient List<ICdmEventListener> listener = new CopyOnWriteArrayList(); // ICdmChangeListener
	
	/**
     * Adds a cdm event listener.
     * @param listener the cdm event listener
     */
	public void addCdmEventListener(ICdmEventListener listener){
		logger.debug("Listener added to: "+this.toString());
		this.listener.add(listener);
	}

	public void removeCdmEventListener(ICdmEventListener listener){
		logger.debug("Listener removed from: "+this.toString());
		this.listener.remove(listener);
	}
	
	public ICdmEventListener[] getCdmEventListener(){
		return listener.toArray(new ICdmEventListener[0]);
	}
	
//******* END CDM Event Support  ******//
	
}
