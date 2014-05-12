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
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent;

public class ChildTaxaMustDeriveNameFromParentValidator implements
		ConstraintValidator<ChildTaxaMustDeriveNameFromParent, TaxonRelationship> {

	public void initialize(ChildTaxaMustDeriveNameFromParent childTaxaMustDeriveNameFromParent) { }

	public boolean isValid(TaxonRelationship taxonRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(taxonRelationship.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			
			Taxon parent = taxonRelationship.getToTaxon();
			Taxon child = taxonRelationship.getFromTaxon();
			TaxonNameBase<?,?> parentName = CdmBase.deproxy(parent.getName(), TaxonNameBase.class);
			TaxonNameBase<?,?> childName = CdmBase.deproxy(child.getName(), TaxonNameBase.class);
			if(parentName instanceof NonViralName && childName instanceof NonViralName) {
				if(((NonViralName<?>)childName).getRank().isSpecies() || ((NonViralName<?>)childName).getRank().isInfraSpecific()) {
				    if(!((NonViralName<?>)parentName).getGenusOrUninomial().equals(((NonViralName<?>)childName).getGenusOrUninomial())) {
					valid = false;
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent.message}").addNode("fromTaxon").addNode("name").addNode("genusOrUninomial").addConstraintViolation();
				}
				if(((NonViralName<?>)parentName).getRank().isSpecies() || ((NonViralName<?>)parentName).getRank().isInfraSpecific()) {
					if(!((NonViralName<?>)parentName).getSpecificEpithet().equals(((NonViralName<?>)childName).getSpecificEpithet())) {
						valid = false;
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent.message}").addNode("fromTaxon").addNode("name").addNode("specificEpithet").addConstraintViolation();
					}	
				}
				}
			}
		}
		
		return valid;		
	}
}
