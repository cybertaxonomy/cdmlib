package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.validation.annotation.InReference;

public class InReferenceValidator implements ConstraintValidator<InReference, Reference> {

	@Override
    public void initialize(InReference constraintAnnotation) {}

	@Override
    public boolean isValid(Reference value, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		try {
    		if (value.getInReference() != null){
    			if (value.getType() == ReferenceType.Article ){
					if (value.getInReference().getType() != ReferenceType.Journal) {
                        isValid = false;
                    }
    			}
    			if (value.getType() == ReferenceType.BookSection){
    				if (value.getInReference().getType() != ReferenceType.Book) {
                        isValid = false;
                    }
    			}
    			if (value.getType() == ReferenceType.InProceedings){
    				if (value.getInReference().getType() != ReferenceType.Proceedings) {
                        isValid = false;
                    }
    			}
    			if (value.getType() == ReferenceType.Book){
    				if (value.getInReference().getType() != ReferenceType.PrintSeries) {
                        isValid = false;
                    }
    			}

    		}
    		if (!isValid){
    			constraintValidatorContext.disableDefaultConstraintViolation();
    			constraintValidatorContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.wrongInReferenceForReferenceType.message}").addNode("inReference").addConstraintViolation();
    		}
		}catch(NullPointerException e){
			return isValid;
		}

		return isValid;
	}

}
