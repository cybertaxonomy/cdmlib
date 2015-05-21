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

import eu.etaxonomy.cdm.validation.constraint.InReferenceValidator;


@Target( { TYPE, METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = InReferenceValidator.class)
@Documented
public @interface InReference {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.InReference.wrongInReferenceForReferenceType.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {};  //Level3.class
}
