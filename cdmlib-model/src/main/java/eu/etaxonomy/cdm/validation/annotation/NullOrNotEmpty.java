package eu.etaxonomy.cdm.validation.annotation;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.NullOrNotEmptyValidator;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NullOrNotEmptyValidator.class)
@Documented
public @interface NullOrNotEmpty {
String message() default "{eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty.message}";
Class<? extends Payload>[] payload() default {};
Class<?>[] groups() default {};
}
