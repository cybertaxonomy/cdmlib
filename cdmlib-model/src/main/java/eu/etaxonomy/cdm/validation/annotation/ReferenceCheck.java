package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.ReferenceCheckValidation;


@Target( { TYPE, METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ReferenceCheckValidation.class)
@Documented
public @interface ReferenceCheck {
	String message() default "{eu.etaxonomy.cdm.validation.annotation.ISBNe.wrongISBNForReferenceType.message}";
	Class<? extends Payload>[] payload() default {};
	Class<?>[] groups() default {};

}