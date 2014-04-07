package eu.etaxonomy.cdm.persistence.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LongRunningCheckCaseValidator.class)
@Documented
public @interface LongRunningCheckCase {

	String message() default "Casing is wrong";


	Class<?>[] groups() default {};


	Class<? extends Payload>[] payload() default {};


	CaseMode value();

}
