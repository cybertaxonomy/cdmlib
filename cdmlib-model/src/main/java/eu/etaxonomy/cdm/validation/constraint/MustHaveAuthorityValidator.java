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

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.validation.annotation.MustHaveAuthority;

public class MustHaveAuthorityValidator implements
		ConstraintValidator<MustHaveAuthority, NonViralName> {

	public void initialize(MustHaveAuthority mustHaveAuthority) { }

	public boolean isValid(NonViralName name, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		
		if(name.getBasionymAuthorTeam() == null && name.getAuthorshipCache() == null) {
		
		    if(name.getRank().isInfraSpecific()) {
			    if(name instanceof BotanicalName && name.getSpecificEpithet() != null && name.getInfraSpecificEpithet() != null && name.getInfraSpecificEpithet().equals(name.getSpecificEpithet())) {
				    valid = true;
			    } else {
				    valid = false;
			    }
		    } else {
			    valid = false;
		    }
		}
		
		return valid;		
	}
}
