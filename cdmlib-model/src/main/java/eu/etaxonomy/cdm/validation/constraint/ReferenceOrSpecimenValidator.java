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

import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.validation.annotation.ReferenceOrSpecimen;

/**
 * @author a.mueller
 * @since 2022-12-19
 */
public class ReferenceOrSpecimenValidator
        implements ConstraintValidator<ReferenceOrSpecimen, DescriptionElementSource>{

    @Override
    public void initialize(ReferenceOrSpecimen constraintAnnotation) {}

	@Override
    public boolean isValid(DescriptionElementSource value, ConstraintValidatorContext constraintValidatorContext) {

	    boolean isValid = true;

	    if (value.getCitation() != null && value.getSpecimen() != null) {
	        isValid &= false;
	    }
		return isValid;
	}
}