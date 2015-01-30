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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author ayco_holleman
 * @date 27 jan. 2015
 *
 */
public class BatchValidator {

    private static final Logger logger = Logger.getLogger(BatchValidator.class);

    // change to VALIDATE to connect to an existing CDM database
    static DbSchemaValidation schemaValidation = DbSchemaValidation.VALIDATE;

    public static void main(String[] args) {
        new BatchValidator().run();
    }

    private void run() {

        logger.info("Starting batch validation");

        String server = "localhost";
        String database = "cdm_merged";
        String username = "root";
        ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username,
                AccountStore.readOrStorePassword(server, database, username, null));
        ICdmApplicationConfiguration app = CdmApplicationController.NewInstance(dataSource, schemaValidation);

//        List<IService<?>> services = BatchValidationUtil.getAvailableServices(app);


        //BatchValidationUtil.getAvailableServices(app);
        //BatchValidationUtil.getEntityClasses(null);

//        HibernateValidatorConfiguration validatorConfiguration = Validation.byProvider(HibernateValidator.class).configure();
//        ValidatorFactory validatorFactory = validatorConfiguration.buildValidatorFactory();
//        Validator validator = validatorFactory.getValidator();
//
//
//        IDescriptionService descriptionService = app.getDescriptionService();
//
//        logger.info("Validating taxon descriptions");
//        List<TaxonDescription> taxonDescriptions = descriptionService.list(TaxonDescription.class, 100, 1, null, null);
//
//        IEntityValidationResultService entityValidationResultService = app.getEntityValidationResultService();
//
//        for(TaxonDescription td : taxonDescriptions) {
//            Set<ConstraintViolation<TaxonDescription>> errors = validator.validate(td, Level2.class, Level3.class);
//            logger.info("Found validation errors for id=" + td.getId() + ": " + errors.size());
//            entityValidationResultService.saveValidationResult(errors, td, CRUDEventType.NONE);
//        }

        logger.info("Batch validation complete");
    }

}
