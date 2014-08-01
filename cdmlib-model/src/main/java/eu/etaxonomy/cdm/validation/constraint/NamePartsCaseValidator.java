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

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.validation.annotation.NamePartsCase;

public class NamePartsCaseValidator implements
		ConstraintValidator<NamePartsCase, NonViralName> {

	public void initialize(NamePartsCase mustHaveAuthority) { }

	public boolean isValid(NonViralName nvn, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		
		NonViralName<?> name = CdmBase.deproxy(nvn,NonViralName.class);
		
		valid &= capitalOrBlank(name.getGenusOrUninomial());
		valid &= lowerOrBlank(name.getSpecificEpithet());
		valid &= lowerOrBlank(name.getInfraSpecificEpithet());
		
		
		return valid;		
	}

	
	private boolean capitalOrBlank(String epithet) {
		return isBlank(epithet) || CdmUtils.isCapital(epithet);
	}
	
	private boolean lowerOrBlank(String epithet) {
		return isBlank(epithet) || StringUtils.isAllLowerCase(epithet);
	}

	private boolean isBlank(String genusOrUninomial) {
		return StringUtils.isBlank(genusOrUninomial);
	}
}
