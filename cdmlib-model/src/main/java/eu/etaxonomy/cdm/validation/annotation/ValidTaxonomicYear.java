/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.ValidTaxonomicYearValidator;

@Target( { METHOD })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidTaxonomicYearValidator.class)
@Documented
public @interface ValidTaxonomicYear {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.ValidTaxonomicYear.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {};  //Level3
}
