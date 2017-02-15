/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.validation.Level3ValidationTask;

/**
 * A {@link Runnable} performing Level-3 validation of a JPA entity
 *
 * @author ayco_holleman
 *
 */
class Level3TransactionalValidationTask extends Level3ValidationTask {

    private ICdmRepository repository;

    public Level3TransactionalValidationTask(CdmBase entity, IEntityValidationCrud dao) {
        super(entity, dao);
    }

    public Level3TransactionalValidationTask(ICdmBase entity, CRUDEventType crudEventType, IEntityValidationCrud dao, ICdmRepository repository) {
        super(entity, crudEventType, dao);
        this.repository = repository;
    }

    @Override
    protected Set<ConstraintViolation<ICdmBase>> validateWithErrorHandling() {
        if (repository != null){
            TransactionStatus tx = repository.startTransaction(true);
            //TODO what if getEntity() is not CdmBase?
            CdmBase cdmBase = CdmBase.deproxy(getEntity(), CdmBase.class);
            repository.getCommonService().find(cdmBase.getClass(), cdmBase.getId());
            //was "create Entity in 2 open sessions" error
            //not sure if the above works, should set the entity, but allowing to do so is critical the id is part of hash function, so we have to make sure that only entities with the same id can be replaced
//            repository.getCommonService().updateEntity(cdmBase);
            Set<ConstraintViolation<ICdmBase>> result = super.validateWithErrorHandling();
            repository.commitTransaction(tx);
            return result;
        }else{
            return super.validateWithErrorHandling();
        }
    }

}
