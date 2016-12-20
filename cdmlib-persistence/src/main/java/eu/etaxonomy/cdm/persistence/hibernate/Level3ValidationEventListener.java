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
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;
import eu.etaxonomy.cdm.persistence.validation.Level3ValidationTask;

@SuppressWarnings("serial")
public class Level3ValidationEventListener extends ValidationEventListenerBase
            implements PostDeleteEventListener {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Level3ValidationEventListener.class);

    public Level3ValidationEventListener(IEntityValidationCrud dao) {
        super(dao);
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        validate(event.getEntity(), CRUDEventType.DELETE);
    }

    @Override
    protected EntityValidationTaskBase createValidationTask(ICdmBase entity, CRUDEventType trigger) {
        return new Level3ValidationTask(entity, trigger, getDao());
    }

    @Override
    protected final String levelString() {
        return "Level-3";
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
