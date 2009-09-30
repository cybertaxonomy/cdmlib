package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

public class NullOrNotEmptyValidator implements
		ConstraintValidator<NullOrNotEmpty, String> {

	public void initialize(NullOrNotEmpty nullOrNotEmpty) { }

	public boolean isValid(String string, ConstraintValidatorContext constraintContext) {
		if(string == null) {
			return true;
		} else if(string.trim().length() > 0) {
			return true;
		} else {
			return false;
		}
	}
}
