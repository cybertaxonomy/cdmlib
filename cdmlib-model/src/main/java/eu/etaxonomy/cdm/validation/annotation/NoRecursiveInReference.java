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

import eu.etaxonomy.cdm.validation.constraint.NoRecursiveInReferenceValidator;


@Target( { TYPE, METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NoRecursiveInReferenceValidator.class)
@Documented
public @interface NoRecursiveInReference {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.NoRecursiveInReference.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {};  //Level3.class
}
