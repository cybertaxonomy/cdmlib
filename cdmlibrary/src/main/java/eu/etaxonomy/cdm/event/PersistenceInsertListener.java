/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.event;

import java.util.EventObject;

import org.apache.log4j.Logger;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.def.DefaultSaveEventListener;

import eu.etaxonomy.cdm.api.service.IEventRegistrationService;
import eu.etaxonomy.cdm.model.common.CdmEntity;



/**
 * @author Markus Döring
 * @version 0.1
 */
public class PersistenceInsertListener extends DefaultSaveEventListener implements PostInsertEventListener{
	static Logger logger = Logger.getLogger(PersistenceInsertListener.class);
    
	private IEventRegistrationService eventRegistrationService;

	public IEventRegistrationService getEventRegistrationService() {
		return eventRegistrationService;
	}
	public void setEventRegistrationService(
			IEventRegistrationService eventRegistrationService) {
		this.eventRegistrationService = eventRegistrationService;
	}		


	public void onPostInsert(PostInsertEvent event) {
		CdmEntity cdmObj = (CdmEntity) event.getEntity();
		// iterate through listeners for new CDM objects stored in the respective services
		// FIXME: hardcoded for name service. get name service via Spring!
		ICdmEventListener[] listeners = eventRegistrationService.getCdmEventListener(cdmObj.getClass());
		for (ICdmEventListener l: listeners){
			// send modified object as "event" to listener
			l.onInsert(createEvent(cdmObj));
	        logger.debug("Send cdm insert event to listener for CDM object " + cdmObj.toString());		
		}
        logger.debug("CDM object '" + cdmObj.toString() + "' of class " + cdmObj.getClass().getSimpleName() + " inserted");		
	}
	public EventObject createEvent(CdmEntity cdmObj){
		return new EventObject(cdmObj);
	}
}


