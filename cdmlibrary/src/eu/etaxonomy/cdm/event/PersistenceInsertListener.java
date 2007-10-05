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

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;



/**
 * @author Markus Döring
 * @version 0.1
 */
public class PersistenceInsertListener extends DefaultSaveEventListener implements PostInsertEventListener{
	static Logger logger = Logger.getLogger(PersistenceInsertListener.class);
    
	private INameService nameService;

	public void onPostInsert(PostInsertEvent event) {
		Object cdmObj = event.getEntity();
		// iterate through listeners for new CDM objects stored in the respective services
		// FIXME: hardcoded for name service. get name service via Spring!
		ICdmEventListener[] listeners = nameService.getCdmEventListener();
		for (ICdmEventListener l: listeners){
			// send modified object as "event" to listener
			l.onInsert(cdmObj);
	        logger.info("Send cdm insert event to listener for CDM object " + cdmObj.toString());		
		}
        logger.info("CDM object " + cdmObj.toString() + " inserted");		
	}		

}


