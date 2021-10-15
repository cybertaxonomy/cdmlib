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

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank;

public class CorrectEpithetsForRankValidator implements ConstraintValidator<CorrectEpithetsForRank, INonViralName> {

	@Override
    public void initialize(CorrectEpithetsForRank correctEpithetsForRank) { }

	@Override
	public boolean isValid(INonViralName name, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if (name.isCultivar()){
		    return valid;  //there are no strict rules for cultivar ranks so far
		}
		if(name.isSupraGeneric() || name.isGenus()) {
			if(isNotBlank(name.getInfraGenericEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("infraGenericEpithet").addConstraintViolation();
			}

			if(isNotBlank(name.getSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("specificEpithet").addConstraintViolation();
			}
			if(isNotBlank(name.getInfraSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.isInfraGeneric()) {
			if(isBlank(name.getInfraGenericEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addPropertyNode("infraGenericEpithet").addConstraintViolation();
			}

			if(isNotBlank(name.getSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("specificEpithet").addConstraintViolation();
			}
			if(isNotBlank(name.getInfraSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.isSpecies()) {
			if(isBlank(name.getSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addPropertyNode("specificEpithet").addConstraintViolation();
			}

			if(isNotBlank(name.getInfraSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addPropertyNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.isInfraSpecific()) {
			if(isBlank(name.getSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addPropertyNode("specificEpithet").addConstraintViolation();
			}
			if(isBlank(name.getInfraSpecificEpithet())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addPropertyNode("infraSpecificEpithet").addConstraintViolation();
			}
		}
		if (!valid){
		    constraintContext.disableDefaultConstraintViolation();
		}
		return valid;
	}

    private boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    private boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
}
