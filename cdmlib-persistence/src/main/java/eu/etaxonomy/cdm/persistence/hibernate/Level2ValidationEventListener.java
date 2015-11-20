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

import org.apache.log4j.Logger;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;
import eu.etaxonomy.cdm.persistence.validation.Level2ValidationTask;

@SuppressWarnings("serial")
public class Level2ValidationEventListener extends ValidationEventListenerBase {

	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Level2ValidationEventListener.class);

	public Level2ValidationEventListener(IEntityValidationCrud dao){
	    super(dao);
	}

    @Override
    protected EntityValidationTaskBase createValidationTask(ICdmBase entity, CRUDEventType trigger) {
        return new Level2ValidationTask(entity, trigger, getDao());
    }

    @Override
    protected final String levelString() {
        return "Level-2";
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

}
