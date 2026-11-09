package eu.etaxonomy.cdm.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.annotation.NoRecursiveInReference;

public class NoRecursiveInReferenceValidator implements ConstraintValidator<NoRecursiveInReference, Reference> {

	@Override
    public void initialize(NoRecursiveInReference constraintAnnotation) {}

	@Override
    public boolean isValid(Reference value, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		try {
		    if (value.getInReference() != null && value.equals(value.getInReference())){
		        isValid = false;
    		}
 		}catch(NullPointerException e){
			return isValid;
		}

		return isValid;
	}

}
