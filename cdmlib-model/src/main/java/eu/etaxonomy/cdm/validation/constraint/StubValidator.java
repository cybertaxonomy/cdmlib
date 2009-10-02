package eu.etaxonomy.cdm.validation.constraint;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Stub validatior for use when a constraint uses cdmlib-services component
 * (and therfore the implementation requires components that are not visible 
 * in the cdmlib-model package)
 * 
 * To resolve this circular dependency, use this stub as the validator in the
 * annotation, then substitute an implementation using an XML config file.
 *  
 * @author ben.clark
 */
public class StubValidator implements
		ConstraintValidator<Annotation,Object> {
	
	public void initialize(Annotation annotation) { }

	public boolean isValid(Object obj, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
