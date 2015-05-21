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

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.validation.annotation.ValidTaxonomicYear;

public class ValidTaxonomicYearValidator implements ConstraintValidator<ValidTaxonomicYear, String> {

	@Override
    public void initialize(ValidTaxonomicYear correctEpithetsForRank) { }

	@Override
	public boolean isValid(String year, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		TimePeriod tp = TimePeriodParser.parseString(year);
		if (tp.getStartYear() != null &&  tp.getStartYear() < 1753){
		    valid = false;
		}else if (tp.getEndYear() != null &&  tp.getStartYear() < 1753){
		    valid = false;
		}
		return valid;
	}


}
