package eu.etaxonomy.cdm.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.event.CdmEventListenerRegistrationSupport;
import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.event.ICdmEventListenerRegistration;

/**
 * @author markus
 *
 */
public class EventRegistrationServiceImpl extends ServiceBase implements IEventRegistrationService {
	static Logger logger = Logger.getLogger(CdmEventListenerRegistrationSupport.class);

	
	/**
	 * listener registration map.
	 * for every class, excluding interfaces, a listener list is registered
	 */
	private Map<Class, List<ICdmEventListener> > listenerMap = new WeakHashMap<Class, List<ICdmEventListener> >();
	
	/**
	 * mapping interfaces to array of classes that implement that interface.
	 * This map is used to cache reflection results for performance
	 */
	private Map<Class, Class[]> interfaceMap = new HashMap<Class, Class[]>();
	
	
	/* (non-Javadoc)
	 * A listener can register for a certain class.
	 * If the class is an interface, the listener will be registered for all classes that implement that interface
	 * @see eu.etaxonomy.cdm.api.service.IEventRegistrationService#addCdmEventListener(eu.etaxonomy.cdm.event.ICdmEventListener, java.lang.Class)
	 */
	public void addCdmEventListener(ICdmEventListener listener, Class clazz) {
		// TODO expand interface classes into implemented classes
		if (!listenerMap.containsKey(clazz)){
			// init the map key with a threadsafe array list if not yet existing
			// see http://www.ibm.com/developerworks/library/j-jtp07265/
			listenerMap.put(clazz, new CopyOnWriteArrayList<ICdmEventListener>());
		}
		// add listener to list
		listenerMap.get(clazz).add(listener);
		logger.debug(clazz.getSimpleName() + " listener added to "+this.toString());
	}
	public ICdmEventListener[] getCdmEventListener(Class clazz) {
		if (listenerMap.containsKey(clazz)){
			return listenerMap.get(clazz).toArray(new ICdmEventListener[0]);
		}else{
			return new ICdmEventListener[0];
		}
	}
	public void removeCdmEventListener(ICdmEventListener listener, Class clazz) {
		// remove listener from list
		listenerMap.get(clazz).remove(listener);		
		logger.debug(clazz.getSimpleName() + " listener removed from "+this.toString());
	}


}
