package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.validation.annotation.ReferenceCheck;

public class ReferenceCheckValidator implements ConstraintValidator<ReferenceCheck, Reference>{

    @Override
    public void initialize(ReferenceCheck constraintAnnotation) {}

	@Override
    public boolean isValid(Reference value, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;

		isValid &= validIsbn(value, constraintValidatorContext);
		if (value.getType() == ReferenceType.Journal && value.getDatePublished() != null) {
			isValid &= false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.JournalShouldNotHaveDatePublished.message}").addConstraintViolation();
		}
		if (! isValid){
		    constraintValidatorContext.disableDefaultConstraintViolation();
		}

		return isValid;
	}



	private boolean validIsbn(Reference value, ConstraintValidatorContext constraintValidatorContext){
		boolean isValid = true;

		if ((value.getType() != ReferenceType.Book && value.getType() != ReferenceType.Proceedings && value.getType() != ReferenceType.Generic ) ) {
			if (value.getIsbn()!= null){
				isValid = false;
				constraintValidatorContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.ReferenceShouldNotHaveIsbn.message}").addConstraintViolation();
			}
		}
		return isValid;
	}

}
