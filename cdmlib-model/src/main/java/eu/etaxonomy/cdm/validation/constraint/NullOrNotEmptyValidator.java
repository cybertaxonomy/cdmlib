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

import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

public class NullOrNotEmptyValidator implements ConstraintValidator<NullOrNotEmpty, String> {

    @Override
    public void initialize(NullOrNotEmpty nullOrNotEmpty) { }

    @Override
	public boolean isValid(String string, ConstraintValidatorContext constraintContext) {
		boolean isValid = false;
		if(string == null) {
			isValid = true;
		} else if(string.trim().length() > 0) {
			isValid =  true;
		}

		return isValid;
	}
}
