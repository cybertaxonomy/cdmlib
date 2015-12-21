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

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup;

public class HomotypicSynonymsShouldBelongToGroupValidator implements
		ConstraintValidator<HomotypicSynonymsShouldBelongToGroup, SynonymRelationship> {

	@Override
    public void initialize(HomotypicSynonymsShouldBelongToGroup homotypicSynonymsShouldBelongToGroup) { }

	@Override
    public boolean isValid(SynonymRelationship synonymRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if (synonymRelationship.getType() == null){
		    return valid;
		}
		if(synonymRelationship.getType().equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			Taxon accepted = synonymRelationship.getAcceptedTaxon();
			Synonym synonym = synonymRelationship.getSynonym();

			if (accepted != null && synonym != null && accepted.getName() != null && synonym.getName() != null){
    			if(!accepted.getName().getHomotypicalGroup().equals(synonym.getName().getHomotypicalGroup())) {
    				valid = false;
    				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup.message}").addNode("tyoe").addConstraintViolation();
    			}
			}
		}

		return valid;
	}
}
