/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import java.util.concurrent.ThreadFactory;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

/**
 * {@code ThreadFactory} implementation used by a {@link ValidationExecutor}.
 *
 * @author ayco_holleman
 *
 */
class ValidationThreadFactory implements ThreadFactory {

    private static final String THREAD_GROUP_NAME = "VALIDATION";
    private static final String DEFAULT_THREAD_NAME = "DEFAUL_VALIDATION";

    // TODO: Autowire this?
    private final ValidatorFactory factory;

    private final ThreadGroup threadGroup;

    public ValidationThreadFactory() {
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        factory = config.buildValidatorFactory();
        threadGroup = new ThreadGroup(THREAD_GROUP_NAME);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new EntityValidationThread(threadGroup, runnable, DEFAULT_THREAD_NAME, factory.getValidator());
    }

}
