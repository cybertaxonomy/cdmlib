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
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.def.DefaultSaveEventListener;



/**
 * @author Markus Döring
 * @version 0.1
 */
public class PersistenceInsertListener extends DefaultSaveEventListener implements PostInsertEventListener{
	static Logger logger = Logger.getLogger(PersistenceChangeListener.class);
    
	public void onPostInsert(PostInsertEvent event) {
		ICdmEventListenerRegistration cdmObj = (ICdmEventListenerRegistration) event.getEntity();
		// iterate through listeners for this CDM object
		ICdmEventListener[] listeners = cdmObj.getCdmEventListener();
		for (ICdmEventListener l: listeners){
			// send modified object as "event" to listener
			l.onInsert(cdmObj);
	        logger.info("Send cdm insert event to listener for CDM object " + cdmObj.toString());		
		}
        logger.info("CDM object " + cdmObj.toString() + " inserted");		
	}		

}


