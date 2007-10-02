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



/**
 * @author Markus Döring
 * @version 0.1
 */
public class CdmChangeListener extends DefaultSaveOrUpdateEventListener implements SaveOrUpdateEventListener{
	static Logger logger = Logger.getLogger(CdmChangeListener.class);
    
	public CdmChangeListener() {
		logger.info("CdmChangeListener created");		
	}

	public void onSaveOrUpdate(SaveOrUpdateEvent event){
		super.onSaveOrUpdate(event);
		VersionableEntity cdmObj = (VersionableEntity) event.getEntity();
        logger.info("CDM object " + cdmObj.getUuid() + " saved or updated");		
	}
}
