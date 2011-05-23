package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.validation.annotation.InReference;

public class InReferenceValidation implements ConstraintValidator<InReference, Reference> {

	public void initialize(InReference constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	public boolean isValid(Reference value,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		try {
		if (value.getInReference() != null){
			if (value.getType() == ReferenceType.Article ){
					if (value.getInReference().getType() != ReferenceType.Journal)isValid = false;			
			}
			if (value.getType() == ReferenceType.BookSection){
				if (value.getInReference().getType() != ReferenceType.Book) isValid = false;
			}
			if (value.getType() == ReferenceType.InProceedings){
				if (value.getInReference().getType() != ReferenceType.Proceedings) isValid = false;
			}
			if (value.getType() == ReferenceType.Book){
				if (value.getInReference().getType() != ReferenceType.PrintSeries) isValid = false;
			}
		
		}
		if (!isValid){
			constraintValidatorContext.disableDefaultError();
			constraintValidatorContext.buildErrorWithMessageTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.wrongInReferenceForReferenceType.message}").addSubNode("inReference").addError();
		}
		}catch(NullPointerException e){
			return isValid;
		}
		
		return isValid;
	}

}
