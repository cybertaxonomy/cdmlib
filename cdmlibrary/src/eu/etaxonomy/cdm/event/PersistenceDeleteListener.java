/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.event;

import org.apache.log4j.Logger;
import java.util.*;

import org.hibernate.event.*;
import org.hibernate.event.def.DefaultDeleteEventListener;


/**
 * @author Markus Döring
 * @version 0.1
 */
public class PersistenceDeleteListener extends DefaultDeleteEventListener implements PostDeleteEventListener{
	static Logger logger = Logger.getLogger(PersistenceDeleteListener.class);
    
	public void onPostDelete(PostDeleteEvent event) {
		
		ICdmEventListenerRegistration cdmObj = (ICdmEventListenerRegistration) event.getEntity();
		// iterate through listeners for this CDM object
		ICdmEventListener[] listeners = cdmObj.getCdmEventListener();
		for (ICdmEventListener l: listeners){
			// send modified object as "event" to listener
			l.onUpdate(cdmObj);
	        logger.info("Send cdm delete event to listener for CDM object " + cdmObj.toString());		
		}
        logger.info("CDM object " + cdmObj.toString() + " deleted");		
	}

}

