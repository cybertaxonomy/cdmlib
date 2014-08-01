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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.validation.annotation.MustHaveAuthority;

public class MustHaveAuthorityValidator implements
		ConstraintValidator<MustHaveAuthority, NonViralName> {

	public void initialize(MustHaveAuthority mustHaveAuthority) { }

	public boolean isValid(NonViralName nvn, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		
		NonViralName<?> name = CdmBase.deproxy(nvn, NonViralName.class);
		if(name.getBasionymAuthorTeam() == null && name.getAuthorshipCache() == null) {
		    valid = false;
		    if(name instanceof BotanicalName && name.isInfraSpecific()) {
			    if(name.isAutonym()) {
				    valid = true; // is AUTONYM
			    } 
		    } 
		    if(name.getRank().isSpeciesAggregate()) { // Species aggregates don't have authorities
		    	valid = true;
			}
		    
		} else {
			valid = true;
			if(name instanceof BotanicalName && name.isInfraSpecific()) {
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
