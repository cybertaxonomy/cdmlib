/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.validation.batch;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * @author ayco_holleman
 * @author a.mueller
 * @since 27 jan. 2015
 *
 */
@Component("batchValidator")
public class BatchValidator implements Runnable, ApplicationContextAware {

    static final Class<?>[] DEFAULT_VALIDATION_GROUPS = new Class<?>[] { Level2.class, Level3.class };

    private static final Logger logger = Logger.getLogger(BatchValidator.class);


    private ICdmRepository repository;

    private ApplicationContext appContext;

    private Validator validator;
    private Class<?>[] validationGroups;


    @Override
    public void setApplicationContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(1);
        initValidator();
        validate();
    }

    /**
     *
     */
    private void initValidator() {
        if (getValidator() == null){
            HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
            ValidatorFactory factory = config.buildValidatorFactory();
            setValidator(factory.getValidator());
        }
        if (validationGroups == null) {
            validationGroups = DEFAULT_VALIDATION_GROUPS;
        }
    }



    private <T extends ICdmBase, S extends T> void validate() {
        logger.info("Starting batch validation");

       // Get service for saving errors to database
//        IEntityValidationService validationResultService = context.getEntityValidationService();
        IEntityValidationService entityValidationService = appContext.getBean(IEntityValidationService.class);

        EntityValidationCrudJdbcImpl jdbcPersister = appContext.getBean(EntityValidationCrudJdbcImpl.class);

        // Get all services dealing with "real" entities
        List<Class<CdmBase>> classesToValidate = BatchValidationUtil.getClassesToValidate();

        for (Class<CdmBase> entityClass : classesToValidate) {
            //TODO currently this seems to work only on the exact class, we may move it down
            //to single entity validation again but cache the information for each class
            if (true || BatchValidationUtil.isConstrainedEntityClass(validator, entityClass)){

    //          ICommonService commonService = repository.getCommonService();
                ICommonService commonService = appContext.getBean(ICommonService.class);
                logger.info("Loading entities of type " + entityClass.getName());
                //false for saving validation results
                //TODO can we handle results in a different transaction?
                boolean readOnly = false;
                TransactionStatus txStatus =  startTransaction(readOnly);
                handleSingleClass(commonService, entityClass, entityValidationService, jdbcPersister);
                commitTransaction(txStatus);
            }
        }

        logger.info("Batch validation complete");
    }

    /**
     * @param txStatus
     */
    private void commitTransaction(TransactionStatus txStatus) {
        PlatformTransactionManager txManager = getTransactionManager();
        txManager.commit(txStatus);

    }

    /**
     * @param readOnly
     * @return
     *
     */
    private TransactionStatus startTransaction(boolean readOnly) {
        PlatformTransactionManager txManager = getTransactionManager();

        DefaultTransactionDefinition defaultTxDef = new DefaultTransactionDefinition();
        defaultTxDef.setReadOnly(readOnly);
        TransactionDefinition txDef = defaultTxDef;
        TransactionStatus txStatus = txManager.getTransaction(txDef);
        return txStatus;
    }

    /**
     * @return
     */
    private PlatformTransactionManager getTransactionManager() {
        PlatformTransactionManager txManager = appContext.getBean(HibernateTransactionManager.class);
        return txManager;
    }

    private void handleSingleClass(ICommonService commonService, Class<CdmBase> entityClass, IEntityValidationService entityValidationService, EntityValidationCrudJdbcImpl jdbcPersister) {
        int n = commonService.count(entityClass);
        int pageSize = 1000;
        for (int page = 0; page < n ; page = page + pageSize ){
            handlePage(commonService, entityClass, entityValidationService, jdbcPersister,
                    page/pageSize, pageSize);
        }
    }


    /**
     * @param commonService
     * @param entityClass
     * @param entityValidationService
     * @param jdbcPersister
     *
     */
    private void handlePage(ICommonService commonService, Class<CdmBase> entityClass, IEntityValidationService entityValidationService, EntityValidationCrudJdbcImpl jdbcPersister, int start, int pageSize) {

        List<CdmBase> entities;

        try {
//            commonService.count()
            entities = commonService.list(entityClass, pageSize, 0, null, null);
        } catch (Throwable t) {
            //TODO handle exception
            logger.error("Failed to load entities", t);
            return;
        }
        for (CdmBase entity : entities) {
            try {
                Set<ConstraintViolation<CdmBase>> errors = getValidator().validate(entity, getValidationGroups());
                if (errors.size() != 0) {
                    if (logger.isInfoEnabled()){logger.info(errors.size() + " constraint violation(s) detected in entity " + entity.toString());}
//                    entityValidationService.saveEntityValidation(entity, errors, CRUDEventType.NONE,
//                            getValidationGroups());

                    jdbcPersister.saveEntityValidation(entity, errors, CRUDEventType.NONE, getValidationGroups());
                }
            } catch (Exception e) {
                // TODO Exception handling
                e.printStackTrace();
            }
        }

    }

    private <T extends ICdmBase, S extends T> void validate_old() {
        logger.info("Starting batch validation");

        if (validationGroups == null) {
            validationGroups = DEFAULT_VALIDATION_GROUPS;
        }

        // Get service for saving errors to database
        IEntityValidationService validationResultService = repository.getEntityValidationService();

        // Get all services dealing with "real" entities
        List<EntityValidationUnit<T, S>> validationUnits = BatchValidationUtil.getAvailableServices(repository);

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
                if (BatchValidationUtil.isConstrainedEntityClass(getValidator(), entity.getClass())) {
                    Set<ConstraintViolation<S>> errors = getValidator().validate(entity, validationGroups);
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
    public ICdmRepository getAppController() {
        return repository;
    }

    /**
     * Set the application context.
     *
     * @param context
     *            The application context
     */
    public void setAppController(ICdmRepository context) {
        this.repository = context;
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
