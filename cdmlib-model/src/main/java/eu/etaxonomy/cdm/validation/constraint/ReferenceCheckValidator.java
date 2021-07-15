/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.validation.annotation.ReferenceCheck;

/**
 * @author k.luther
 * @since 2011
 */
public class ReferenceCheckValidator implements ConstraintValidator<ReferenceCheck, Reference>{

    @Override
    public void initialize(ReferenceCheck constraintAnnotation) {}

	@Override
    public boolean isValid(Reference value, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;

		isValid &= validIsbn(value, constraintValidatorContext);
		if (value.getType() == ReferenceType.Journal) {
			if (value.getDatePublished() != null){
			    isValid &= false;
			    constraintValidatorContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.JournalShouldNotHaveDatePublished.message}").addConstraintViolation();
			}
	         if (value.getAuthorship() != null){
	             isValid &= false;
	             constraintValidatorContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.InReference.JournalShouldNotHaveAnAuthor.message}").addConstraintViolation();
	         }
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
