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

import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.validation.annotation.NamesWithHomotypicRelationshipsMustBelongToSameGroup;


public class NamesWithHomotypicRelationshipsMustBelongToSameGroupValidator implements
		ConstraintValidator<NamesWithHomotypicRelationshipsMustBelongToSameGroup, NameRelationship> {

	@Override
    public void initialize(NamesWithHomotypicRelationshipsMustBelongToSameGroup namesWithHomotypicRelationshipsMustBelongToSameGroup) { }

    @Override
    public boolean isValid(NameRelationship nameRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(nameRelationship.getType().equals(NameRelationshipType.ALTERNATIVE_NAME()) ||
		   nameRelationship.getType().equals(NameRelationshipType.BASIONYM()) ||
		   nameRelationship.getType().equals(NameRelationshipType.CONSERVED_AGAINST()) ||
		   nameRelationship.getType().equals(NameRelationshipType.EMENDATION()) ||
		   nameRelationship.getType().equals(NameRelationshipType.MISSPELLING()) ||
		   nameRelationship.getType().equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT()) ||
		   nameRelationship.getType().equals(NameRelationshipType.REPLACED_SYNONYM())) {
		   if (nameRelationship.getFromName() != null && nameRelationship.getToName() != null){
		       if(!nameRelationship.getFromName().getHomotypicalGroup().equals(nameRelationship.getToName().getHomotypicalGroup())) {
		           valid = false;
		           constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.NamesWithHomotypicRelationshipsMustBelongToSameGroup.message}").addNode("fromName").addNode("homotypicalGroup").addConstraintViolation();
                   //remove duplicate violation as it does not give more information
//		           constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.NamesWithHomotypicRelationshipsMustBelongToSameGroup.message}").addNode("toName").addNode("homotypicalGroup").addConstraintViolation();
		       }
		   }
		}

		return valid;
	}
}
