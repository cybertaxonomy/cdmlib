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

import javax.persistence.*;
import org.hibernate.event.*;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TaxonName;



/**
 * @author Markus Döring
 * @version 0.1
 */
public class PersistenceChangeListener extends DefaultSaveOrUpdateEventListener implements SaveOrUpdateEventListener{
	static Logger logger = Logger.getLogger(PersistenceChangeListener.class);
    
	public void onSaveOrUpdate(SaveOrUpdateEvent event){
		super.onSaveOrUpdate(event);
		ICdmEventListenerRegistration cdmObj = (ICdmEventListenerRegistration) event.getEntity();
		// iterate through listeners for this CDM object
		ICdmEventListener[] listeners = cdmObj.getCdmEventListener();
		for (ICdmEventListener l: listeners){
			// send modified object as "event" to listener
			l.onUpdate(cdmObj);
	        logger.info("Send cdm update event to listener for CDM object " + cdmObj.toString());		
		}
        logger.info("CDM object " + cdmObj.toString() + " saved or updated");		
	}
}
