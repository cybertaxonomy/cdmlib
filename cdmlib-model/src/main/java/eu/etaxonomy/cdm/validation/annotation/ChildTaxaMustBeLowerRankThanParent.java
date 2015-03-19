package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.ChildTaxaMustBeLowerRankThanParentValidator;

@Target( { TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ChildTaxaMustBeLowerRankThanParentValidator.class)
@Documented
public @interface ChildTaxaMustBeLowerRankThanParent {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {}; //Level3.class
}
