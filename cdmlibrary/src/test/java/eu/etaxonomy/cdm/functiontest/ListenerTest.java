/**
 * 
 */
package eu.etaxonomy.cdm.functiontest;

import java.util.EventObject;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.event.ICdmEventListenerRegistration;

/**
 * @author m.doering
 *
 */
public class ListenerTest implements ICdmEventListener {
	private static final Logger logger = Logger.getLogger(ListenerTest.class);
	
	public void onUpdate(EventObject cdmObj){
		logger.info("Jippie, cdm object updated: " + cdmObj.toString());
	}

	public void onDelete(EventObject cdmObj){
		logger.info("Jippie, cdm object deleted: " + cdmObj.toString());
	}

	public void onInsert(EventObject cdmObj) {
		logger.info("Jippie, cdm object inserted: " + cdmObj.toString());
	}

	public void onLoad(EventObject cdmObj) {
		logger.info("Jippie, cdm object loaded: " + cdmObj.toString());
	}

}
