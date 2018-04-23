/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationDao;

/**
 *
 * @author ayco_holleman
 \* @since 15 jan. 2015
 *
 */
@Service
@Transactional(readOnly = true)
public class EntityValidationServiceImpl extends ServiceBase<EntityValidation, IEntityValidationDao>
        implements IEntityValidationService {

    @Autowired
    IEntityValidationDao dao;

    @Override
    protected void setDao(IEntityValidationDao dao) {
        this.dao = dao;
    }

    @Override
    public EntityValidation getValidationResult(String validatedEntityClass, int validatedEntityId) {
        return dao.getEntityValidation(validatedEntityClass, validatedEntityId);
    }

    @Override
    public List<EntityValidation> getValidationResults() {
        return dao.getEntityValidations();
    }

    @Override
    public List<EntityValidation> getEntityValidations(String validatedEntityClass) {
        return dao.getEntityValidations(validatedEntityClass);
    }

    @Override
    public List<EntityValidation> getEntitiesViolatingConstraint(String validatorClass) {
        return dao.getEntitiesViolatingConstraint(validatorClass);
    }

    @Override
    public List<EntityValidation> getValidationResults(String validatedEntityClass, Severity severity) {
        return dao.getEntityValidations(validatedEntityClass, severity);
    }

    @Override
    public List<EntityValidation> getValidationResults(Severity severity) {
        return dao.getEntityValidations(severity);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends ICdmBase> void saveEntityValidation(T validatedEntity, Set<ConstraintViolation<T>> errors,
            CRUDEventType crudEventType, Class<?>[] validationGroups) {
        dao.saveEntityValidation(validatedEntity, errors, crudEventType, validationGroups);

    }

    @Override
    @Transactional(readOnly = false)
    public void deleteEntityValidation(String validatedEntityClass, int validatedEntityId) {
        dao.deleteEntityValidation(validatedEntityClass, validatedEntityId);
    }

}
