// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.validation.Level2ValidationTask;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;

@SuppressWarnings("serial")
public class Level2ValidationEventListener implements PostInsertEventListener, PostUpdateEventListener {

	private static final Logger logger = Logger.getLogger(Level2ValidationEventListener.class);

	// We would like to have a singleton instance injected here
	private ValidationExecutor validationExecutor;


	public Level2ValidationEventListener(){
	}


	public ValidationExecutor getValidationExecutor(){
		return validationExecutor;
	}


	public void setValidationExecutor(ValidationExecutor validationExecutor){
		this.validationExecutor = validationExecutor;
	}


	@Override
	public void onPostUpdate(PostUpdateEvent event){
		validate(event.getEntity(), CRUDEventType.UPDATE);
	}


	@Override
	public void onPostInsert(PostInsertEvent event){
		validate(event.getEntity(), CRUDEventType.INSERT);
	}


	private void validate(Object object, CRUDEventType trigger){
		try {
			if (object == null) {
				logger.warn("Nothing to validate (entity is null)");
				return;
			}
			if (!(object instanceof CdmBase)) {
				if (object.getClass() != HashMap.class) {
					logger.warn("Level-2 validation bypassed for entities of type " + object.getClass().getName());
				}
				return;
			}
			CdmBase entity = (CdmBase) object;
			Level2ValidationTask task = new Level2ValidationTask(entity, trigger);
			validationExecutor.execute(task);
		}
		catch (Throwable t) {
			logger.error("Failed applying Level-2 validation to " + object.toString(), t);
		}
	}
}
