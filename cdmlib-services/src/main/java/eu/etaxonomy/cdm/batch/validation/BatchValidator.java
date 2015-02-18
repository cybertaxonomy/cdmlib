// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.batch.validation;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * @author ayco_holleman
 * @date 27 jan. 2015
 *
 */
public class BatchValidator implements Runnable {

    static final Class<?>[] DEFAULT_VALIDATION_GROUPS = new Class<?>[] { Level2.class, Level3.class };

    private static final Logger logger = Logger.getLogger(BatchValidator.class);

    private ICdmApplicationConfiguration context;
    private Validator validator;
    private Class<?>[] validationGroups;

    @Override
    public void run() {
        validate();
    }

    private <T extends ICdmBase, S extends T> void validate() {
        logger.info("Starting batch validation");

        if (validationGroups == null) {
            validationGroups = DEFAULT_VALIDATION_GROUPS;
        }

        // Get service for saving errors to database
        IEntityValidationService validationResultService = context.getEntityValidationResultService();

        // Get all services dealing with "real" entities
        List<EntityValidationUnit<T, S>> validationUnits = BatchValidationUtil.getAvailableServices(context);

        for (EntityValidationUnit<T, S> unit : validationUnits) {
            Class<S> entityClass = unit.getEntityClass();
            IService<T> entityLoader = unit.getEntityLoader();
            logger.info("Loading entities of type " + entityClass.getName());
            List<S> entities;
            try {
                entities = entityLoader.list(entityClass, 0, 0, null, null);
            } catch (Throwable t) {
                logger.error("Failed to load entities", t);
                continue;
            }
            for (S entity : entities) {
                if (BatchValidationUtil.isConstrainedEntityClass(validator, entity.getClass())) {
                    Set<ConstraintViolation<S>> errors = validator.validate(entity, validationGroups);
                    if (errors.size() != 0) {
                        logger.warn(errors.size() + " error(s) detected in entity " + entity.toString());
                        validationResultService.saveEntityValidation(entity, errors, CRUDEventType.NONE,
                                validationGroups);
                    }
                }
            }
        }

        logger.info("Batch validation complete");
    }

    /**
     * Get the application context that will provide the services that will, on
     * their turn, provide the entities to be validated.
     *
     * @return The application context
     */
    public ICdmApplicationConfiguration getAppController() {
        return context;
    }

    /**
     * Set the application context.
     *
     * @param context
     *            The application context
     */
    public void setAppController(ICdmApplicationConfiguration context) {
        this.context = context;
    }

    /**
     * Get the {@code Validator} instance that will carry out the validations.
     *
     * @return The {@code Validator}
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Set the {@code Validator} instance that will carry out the validations.
     *
     * @param validator
     *            The {@code Validator}
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Get the validation groups to be applied by the {@code Validator}.
     *
     * @return The validation groups
     */
    public Class<?>[] getValidationGroups() {
        return validationGroups;
    }

    /**
     * Set the validation groups to be applied by the {@code Validator}. By
     * default all Level2 and Level3 will be checked. So if that is what you
     * want, you do not need to call this method before calling {@link #run()}.
     *
     * @param validationGroups
     *            The validation groups
     */
    public void setValidationGroups(Class<?>... validationGroups) {
        this.validationGroups = validationGroups;
    }

}
