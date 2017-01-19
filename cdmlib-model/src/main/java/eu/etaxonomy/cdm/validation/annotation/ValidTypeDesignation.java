package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.TypeDesignationValidator;


@Target( { TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = TypeDesignationValidator.class)
@Documented
public @interface ValidTypeDesignation {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.name.ValidTypeDesignation.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {};  //Level2.class
}
