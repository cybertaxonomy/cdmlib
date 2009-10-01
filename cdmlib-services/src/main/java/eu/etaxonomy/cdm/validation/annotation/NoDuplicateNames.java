package eu.etaxonomy.cdm.validation.annotation;

import eu.etaxonomy.cdm.validation.Level3;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.NoDuplicateNamesValidator;

@Target( { TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NoDuplicateNamesValidator.class)
@Documented
public @interface NoDuplicateNames {
String message() default "{eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames.message}";
Class<? extends Payload>[] payload() default {};
Class<?>[] groups() default {Level3.class};
}
