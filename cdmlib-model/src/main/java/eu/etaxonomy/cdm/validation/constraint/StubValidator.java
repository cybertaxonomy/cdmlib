package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

public class StubValidator implements
		ConstraintValidator<NoDuplicateNames,NonViralName> {
	
	public void initialize(NoDuplicateNames noDuplicateNames) { }

	public boolean isValid(NonViralName name, ConstraintValidatorContext constraintContext) {
		System.out.println("in isValid");
		return true;
	}
}
