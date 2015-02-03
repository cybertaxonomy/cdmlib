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
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.apache.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.IEntityValidationResultService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author ayco_holleman
 * @date 27 jan. 2015
 *
 */
public class BatchValidator implements Runnable {

    // Example of how to run the batch validator
    public static void main(String[] args) {

        String server = "localhost";
        String database = "cdm_merged";
        String username = "root";
        String password = AccountStore.readOrStorePassword(server, database, username, null);
        ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, password);

        BatchValidator batchValidator = new BatchValidator(dataSource);

        // Create some entities violating some constraint
        IReferenceService refService = batchValidator.appController.getReferenceService();
        @SuppressWarnings("rawtypes")
        List<Reference> refs = refService.list(Reference.class, 0, 0, null, null);
        for (@SuppressWarnings("rawtypes")
        Reference ref : refs) {
            refService.delete(ref);
        }
        for (int i = 0; i < 1000; ++i) {
            Reference<?> ref0 = ReferenceFactory.newBook();
            ref0.setIsbn("bla bla");
            ref0.setIssn("foo foo");
            refService.save(ref0);
            Reference<?> ref1 = ReferenceFactory.newJournal();
            ref1.setIsbn("bar bar");
            ref1.setIssn("+++++++");
            refService.save(ref1);
        }

        batchValidator.run();
    }

    private static final Logger logger = Logger.getLogger(BatchValidator.class);

    private ICdmApplicationConfiguration appController;

    public BatchValidator(ICdmApplicationConfiguration app) {
        this.appController = app;
    }

    public BatchValidator(ICdmDataSource dataSource) {
        this.appController = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.VALIDATE);
    }

    public ICdmApplicationConfiguration getAppController() {
        return appController;
    }

    public void setAppController(ICdmApplicationConfiguration appController) {
        this.appController = appController;
    }

    @Override
    public void run() {
        validate();
    }

    private <T extends ICdmBase, S extends T> void validate() {

        logger.info("Starting batch validation");

        // Configure validation framework
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        ValidatorFactory validatorFactory = config.buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        // Get service for saving errors to database
        IEntityValidationResultService entityValidationResultService = appController.getEntityValidationResultService();

        // Get all services dealing with "real" entities
        List<EntityValidationUnit<T, S>> validationUnits = BatchValidationUtil.getAvailableServices(appController);

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
                    Set<ConstraintViolation<S>> errors = validator.validate(entity, Level2.class, Default.class);
                    if (errors.size() != 0) {
                        logger.debug(errors.size() + " error(s) detected in entity " + entity.toString());
                        entityValidationResultService.saveValidationResult(errors, entity, CRUDEventType.NONE);
                    }
                }
            }
        }

        logger.info("Batch validation complete");
    }

}
