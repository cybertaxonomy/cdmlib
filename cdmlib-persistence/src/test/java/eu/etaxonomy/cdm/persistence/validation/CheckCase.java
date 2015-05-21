/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * A Mock class for testing entity validation tasks. DO NOT MODIFY UNLESS YOU
 * ALSO MODIFY THE UNIT TESTS MAKING USE OF THIS CLASS!
 *
 * @author ayco_holleman
 *
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckCaseValidator.class)
@Documented
public @interface CheckCase {
    // Do not modify message. Unit tests depend on it
    String message() default "Casing is wrong";

    Class<?>[] groups() default {};

    // Do not modify severity. Unit tests depend on it
    Class<? extends Payload>[] payload() default {};

    CaseMode value();
}
