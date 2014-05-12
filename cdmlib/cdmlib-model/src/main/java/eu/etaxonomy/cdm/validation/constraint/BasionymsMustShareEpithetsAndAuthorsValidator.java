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
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors;


public class BasionymsMustShareEpithetsAndAuthorsValidator implements
		ConstraintValidator<BasionymsMustShareEpithetsAndAuthors, NameRelationship> {

	public void initialize(BasionymsMustShareEpithetsAndAuthors basionymsMustShareEpithetsAndAuthors) { }

	public boolean isValid(NameRelationship nameRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(nameRelationship.getType().equals(NameRelationshipType.BASIONYM())) {
			TaxonNameBase<?,?> from = CdmBase.deproxy(nameRelationship.getFromName(), TaxonNameBase.class);
			TaxonNameBase<?,?> to = CdmBase.deproxy(nameRelationship.getToName(), TaxonNameBase.class);
			
			if(from instanceof NonViralName && to instanceof NonViralName) {
				NonViralName<?> fromName = (NonViralName<?>) from;
				NonViralName<?> toName = (NonViralName<?>) to;
				if(fromName.getBasionymAuthorTeam() == null || !fromName.getBasionymAuthorTeam().equals(toName.getBasionymAuthorTeam())) {
					valid = false;
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentAuthors.message}").addNode("fromName").addNode("basionymAuthorTeam").addConstraintViolation();				
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentAuthors.message}").addNode("toName").addNode("basionymAuthorTeam").addConstraintViolation();
				}

				String fromNameLastEpithet = fromName.getInfraSpecificEpithet() == null ? fromName.getInfraSpecificEpithet() : fromName.getSpecificEpithet();
				String toNameLastEpithet = toName.getInfraSpecificEpithet() == null ? toName.getInfraSpecificEpithet() : toName.getSpecificEpithet();
				if(!fromNameLastEpithet.equals(toNameLastEpithet)) {
					valid = false;
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentEpithets.message}").addNode("fromName").addNode("nameCache").addConstraintViolation();				
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentEpithets.message}").addNode("toName").addNode("nameCache").addConstraintViolation();
				}
				
				if(fromName instanceof ZoologicalName && toName instanceof ZoologicalName) {
					if(!fromName.getNomenclaturalReference().equals(toName.getNomenclaturalReference())) {
						valid = false;
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("fromName").addNode("nomenclaturalReference").addConstraintViolation();				
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("toName").addNode("nomenclaturalReference").addConstraintViolation();
					}
					
					if(!fromName.getNomenclaturalMicroReference().equals(toName.getNomenclaturalMicroReference())) {
						valid = false;
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("fromName").addNode("nomenclaturalMicroReference").addConstraintViolation();				
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("toName").addNode("nomenclaturalMicroReference").addConstraintViolation();
					}
				}
			}
				
		}
		
		return valid;		
	}
}
