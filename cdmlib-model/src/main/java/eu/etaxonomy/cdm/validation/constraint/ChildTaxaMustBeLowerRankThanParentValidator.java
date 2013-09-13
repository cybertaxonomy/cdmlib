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

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent;

public class ChildTaxaMustBeLowerRankThanParentValidator implements
		ConstraintValidator<ChildTaxaMustBeLowerRankThanParent, TaxonRelationship> {

	public void initialize(ChildTaxaMustBeLowerRankThanParent childTaxaMustBeLowerRankThanParent) { }

	public boolean isValid(TaxonRelationship taxonRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		//FIXME Replace by TaxonNode relationship
		if(taxonRelationship.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			Taxon parent = taxonRelationship.getToTaxon();
			Taxon child = taxonRelationship.getFromTaxon();
			
			
			if(parent.getName().getRank().equals(child.getName().getRank()) || parent.getName().getRank().isLower(child.getName().getRank())) {
				valid = false;
				constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent.message}").addNode("fromTaxon").addNode("name").addNode("rank").addConstraintViolation();				
			}
		}
		
		return valid;		
	}
}
