package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.ChildTaxaMustNotSkipRanksValidator;

@Target( { TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ChildTaxaMustNotSkipRanksValidator.class)
@Documented
public @interface ChildTaxaMustNotSkipRanks {

	String message() default "{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks.message}";

	Class<? extends Payload>[] payload() default {};

	Class<?>[] groups() default {};  //Level3.class
}
