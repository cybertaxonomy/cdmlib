/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplication;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.hibernate.Level3ValidationEventListener;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;

@SuppressWarnings("serial")
class Level3TransactionalValidationEventListener extends Level3ValidationEventListener{

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private final ICdmApplication repository;

	public Level3TransactionalValidationEventListener(ICdmApplication repository, IEntityValidationCrud dao){
        super(dao);
        this.repository = repository;
    }


    @Override
    protected EntityValidationTaskBase createValidationTask(ICdmBase entity, CRUDEventType trigger) {
        return new Level3TransactionalValidationTask(entity, trigger, getDao(), repository);
    }

}
