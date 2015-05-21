package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.NamesWithHomotypicRelationshipsMustBelongToSameGroupValidator;

@Target( { TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NamesWithHomotypicRelationshipsMustBelongToSameGroupValidator.class)
@Documented
public @interface NamesWithHomotypicRelationshipsMustBelongToSameGroup {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.NamesWithHomotypicRelationshipsMustBelongToSameGroup.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {} ;  //Level3.class
}
