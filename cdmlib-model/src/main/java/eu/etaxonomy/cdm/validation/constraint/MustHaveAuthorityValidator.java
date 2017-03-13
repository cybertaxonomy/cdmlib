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
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.validation.annotation.NameMustHaveAuthority;

public class MustHaveAuthorityValidator implements
		ConstraintValidator<NameMustHaveAuthority, INonViralName> {

	@Override
    public void initialize(NameMustHaveAuthority mustHaveAuthority) { }

    @Override
    public boolean isValid(INonViralName name, ConstraintValidatorContext constraintContext) {
		boolean valid = true;

		if(name.getBasionymAuthorship() == null && name.getAuthorshipCache() == null) {
		    valid = false;
		    if(name.isInstanceOf(BotanicalName.class) && name.isInfraSpecific()) {
			    if(name.isAutonym() ) {
				    valid = true; // is AUTONYM
			    }
		    }
		    if(name.isSpeciesAggregate()) { // Species aggregates don't have authorities
		    	valid = true;
			}

		} else {
			valid = true;
			if(name.isInstanceOf(BotanicalName.class) && name.isInfraSpecific()) {
			    if(name.isAutonym()) {
				    valid = false; // is AUTONYM
			    }
		    }
		    if(name.isSpeciesAggregate()) { // Species aggregates don't have authorities
			    valid = false;
		    }
		}

		return valid;
	}
}
