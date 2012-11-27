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

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks;

public class ChildTaxaMustNotSkipRanksValidator implements
		ConstraintValidator<ChildTaxaMustNotSkipRanks, TaxonRelationship> {

	public void initialize(ChildTaxaMustNotSkipRanks childTaxaMustNotSkipRanks) { }

	public boolean isValid(TaxonRelationship taxonRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(taxonRelationship.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			Taxon parent = taxonRelationship.getToTaxon();
			Taxon child = taxonRelationship.getFromTaxon();
			
			
			if(parent.getName().getRank().isSupraGeneric() && child.getName().getRank().isLower(Rank.GENUS())) {
				valid = false;
				constraintContext.buildErrorWithMessageTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks.cannotSkipGenus.message}").addSubNode("fromTaxon").addSubNode("name").addSubNode("rank").addError();				
			} else if(parent.getName().getRank().isHigher(Rank.SPECIES()) && child.getName().getRank().isLower(Rank.SPECIES())) {
				valid = false;
				constraintContext.buildErrorWithMessageTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks.cannotSkipSpecies.message}").addSubNode("fromTaxon").addSubNode("name").addSubNode("rank").addError();
			}
		}
		
		return valid;		
	}
}
