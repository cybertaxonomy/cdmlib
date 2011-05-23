package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.validation.annotation.InReference;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;
import eu.etaxonomy.cdm.validation.annotation.ReferenceCheck;

public class ReferenceCheckValidation implements
ConstraintValidator<ReferenceCheck, Reference>{
	
	@Override
	public boolean isValid(Reference value,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		// oder besser andersherum, bestimmte Referenzen dürfen keine ISBN haben?
		/*
		 * if (value.getType() == ReferenceType.Article || value.getType() == ReferenceType.Journal || value.getType() == ReferenceType.BookSection || value.getType() == ReferenceType.WebPage || value.getType() == ReferenceType.InProceedings ){
		 * 		if (!value.getIsbn().isEmpty()) isValid = false;
		 * }
		 */
		
		if ((value.getType() != ReferenceType.Book || value.getType() != ReferenceType.Proceedings) && !value.getIsbn().isEmpty()) isValid = false;
		
		return isValid;
	}

	@Override
	public void initialize(ReferenceCheck constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	
}
