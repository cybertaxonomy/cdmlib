/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A Mock class for testing entity validation tasks. DO NOT MODIFY UNLESS YOU
 * ALSO MODIFY THE UNIT TESTS MAKING USE OF THIS CLASS!
 *
 * @author ayco_holleman
 *
 */
public class CheckCaseValidator implements ConstraintValidator<CheckCase, String> {
	private CaseMode caseMode;


	@Override
    public void initialize(CheckCase constraintAnnotation){
		this.caseMode = constraintAnnotation.value();
	}


	@Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext){
		if (object == null) {
            return true;
        }
		if (caseMode == CaseMode.UPPER) {
            return object.equals(object.toUpperCase());
        } else {
            return object.equals(object.toLowerCase());
        }
	}
}
