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

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank;

public class CorrectEpithetsForRankValidator implements ConstraintValidator<CorrectEpithetsForRank, NonViralName> {

	public void initialize(CorrectEpithetsForRank correctEpithetsForRank) { }

	public boolean isValid(NonViralName name, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(name.getRank().isSupraGeneric() || name.getRank().isGenus()) {				
			if(name.getInfraGenericEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("infraGenericEpithet").addConstraintViolation();
			}
			
			if(name.getSpecificEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("specificEpithet").addConstraintViolation();
			} 
			if(name.getInfraSpecificEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.getRank().isInfraGeneric()) {
			if(name.getInfraGenericEpithet() == null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addNode("infraGenericEpithet").addConstraintViolation();
			}
				
			if(name.getSpecificEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("specificEpithet").addConstraintViolation();
			} 
			if(name.getInfraSpecificEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.getRank().isSpecies()) {
			if(name.getSpecificEpithet() == null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addNode("specificEpithet").addConstraintViolation();
			}
				
			if(name.getInfraSpecificEpithet() != null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNotNull}").addNode("infraSpecificEpithet").addConstraintViolation();
			}
		} else if(name.getRank().isInfraSpecific()) {
			if(name.getSpecificEpithet() == null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addNode("specificEpithet").addConstraintViolation();
			}
			if(name.getInfraSpecificEpithet() == null) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank.epithetNull}").addNode("infraSpecificEpithet").addConstraintViolation();
			}
		}
		
		return valid;		
	}
}
