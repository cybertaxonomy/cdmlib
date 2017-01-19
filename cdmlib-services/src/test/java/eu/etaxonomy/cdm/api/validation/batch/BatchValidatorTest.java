/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.validation.batch;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.unitils.spring.annotation.SpringApplicationContext;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author admin.ayco.holleman
 * @date 11 feb. 2015
 *
 */
public class BatchValidatorTest extends CdmTransactionalIntegrationTest {

//    @SpringBeanByType
//    private BatchValidator batchValidator;

//    @SpringBeanByName  //does not exist as such
    private ICdmApplicationConfiguration app;

    @SpringApplicationContext
    private ApplicationContext applicationContext;

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.validation.batch.BatchValidator#run()}.
     *
     * @throws SQLException
     */
    @Test
    @Ignore
    public void testRun() throws SQLException {

        ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("PUBLIC", "sa", "");
        app = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.CREATE);

        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        ValidatorFactory validatorFactory = config.buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        BatchValidator batchValidator = new BatchValidator();
        batchValidator.setAppController(app);
        batchValidator.setValidator(validator);
        batchValidator.setValidationGroups(BatchValidator.DEFAULT_VALIDATION_GROUPS);

        // Create 10 References of type book and 10 of type Journal

        IReferenceService refService = app.getReferenceService();
        for (int i = 0; i < 10; ++i) {
            Reference ref0 = ReferenceFactory.newBook();
            ref0.setIsbn("bla bla");
            ref0.setIssn("foo foo");
            // Each book should violate 3 constraints
            if (i == 0) {
                Set<?> errors = validator.validate(ref0, BatchValidator.DEFAULT_VALIDATION_GROUPS);
                // We 're really not testing the BatchValidator here, but our
                // knowledge of the constraints on the Reference class, so we
                // do a plain java language assert here.
                assert (errors.size() == 3);
            }
            refService.save(ref0);
            Reference ref1 = ReferenceFactory.newJournal();
            ref1.setIsbn("bar bar");
            ref1.setIssn("baz baz");
            // Each journal should violate 4 constraints
            if (i == 0) {
                Set<?> errors = validator.validate(ref1, BatchValidator.DEFAULT_VALIDATION_GROUPS);
                assert (errors.size() == 4);
            }
            refService.save(ref1);
        }

        batchValidator.run();

        // So we should have 20 validation results (10 for books, 10 for
        // journals);
        IEntityValidationService validationResultService = app.getEntityValidationService();
        List<EntityValidation> results = validationResultService.getValidationResults();
        Assert.assertEquals("Expected 20 validation results", 20, results.size());

        // And we should have a total of 70 (10 * (3+4)) constraint violations
        int errors = 0;
        for(EntityValidation result: results) {
            errors += result.getEntityConstraintViolations().size();
        }
        Assert.assertEquals("Expected 70 errors", 70, errors);

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }

}
