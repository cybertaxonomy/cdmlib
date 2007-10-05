/**
 * 
 */
package eu.etaxonomy.cdm.functiontest;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.event.ICdmEventListenerRegistration;

/**
 * @author m.doering
 *
 */
public class ListenerTest implements ICdmEventListener {
	private static final Logger logger = Logger.getLogger(ListenerTest.class);
	
	public void onUpdate(ICdmEventListenerRegistration cdmObj){
		logger.info("Jippie, cdm object updated: " + cdmObj.toString());
	}

	public void onDelete(ICdmEventListenerRegistration cdmObj){
		logger.info("Jippie, cdm object deleted: " + cdmObj.toString());
	}

	public void onInsert(Object cdmObj) {
		logger.info("Jippie, cdm object inserted: " + cdmObj.toString());
	}

	public void onLoad(Object cdmObj) {
		logger.info("Jippie, cdm object loaded: " + cdmObj.toString());
	}

}
