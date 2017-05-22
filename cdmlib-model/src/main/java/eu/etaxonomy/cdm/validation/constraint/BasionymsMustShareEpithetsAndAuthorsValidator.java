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
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors;


public class BasionymsMustShareEpithetsAndAuthorsValidator implements
		ConstraintValidator<BasionymsMustShareEpithetsAndAuthors, NameRelationship> {

	@Override
    public void initialize(BasionymsMustShareEpithetsAndAuthors basionymsMustShareEpithetsAndAuthors) { }

    @Override
	public boolean isValid(NameRelationship nameRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(nameRelationship.getType() != null && nameRelationship.getType().equals(NameRelationshipType.BASIONYM())) {
			TaxonName from = CdmBase.deproxy(nameRelationship.getFromName(), TaxonName.class);
			TaxonName to = CdmBase.deproxy(nameRelationship.getToName(), TaxonName.class);

			if(from.isNonViral() && to.isNonViral()) {
				INonViralName fromName =  from;
				INonViralName toName = to;

				//compare author teams
				if(fromName.getCombinationAuthorship() == null || !fromName.getCombinationAuthorship().equals(toName.getBasionymAuthorship())) {
					valid = false;
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentAuthors.message}").addNode("fromName").addNode("basionymAuthorship").addConstraintViolation();
					//remove duplicate violation as it does not give more information
//					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentAuthors.message}").addNode("toName").addNode("basionymAuthorship").addConstraintViolation();
				}

				//compare last epithet
				String fromNameLastEpithet = fromName.getInfraSpecificEpithet() != null ? fromName.getInfraSpecificEpithet() : fromName.getSpecificEpithet();
				String toNameLastEpithet = toName.getInfraSpecificEpithet() != null ? toName.getInfraSpecificEpithet() : toName.getSpecificEpithet();
				if( fromNameLastEpithet != null && ! fromNameLastEpithet.equals(toNameLastEpithet)) {
					valid = false;
					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentEpithets.message}").addNode("fromName").addNode("nameCache").addConstraintViolation();
	                //remove duplicate violation as it does not give more information
//					constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentEpithets.message}").addNode("toName").addNode("nameCache").addConstraintViolation();
				}


				//compare nomRefs and details for zoological names
				//why only for zooNames?
				if(fromName.isZoological() && toName.isZoological()) {
					if(fromName.getNomenclaturalReference() != null && !fromName.getNomenclaturalReference().equals(toName.getNomenclaturalReference())) {
						valid = false;
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("fromName").addNode("nomenclaturalReference").addConstraintViolation();
		                //remove duplicate violation as it does not give more information
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("toName").addNode("nomenclaturalReference").addConstraintViolation();
					}

					if(fromName.getNomenclaturalMicroReference() != null && !fromName.getNomenclaturalMicroReference().equals(toName.getNomenclaturalMicroReference())) {
						valid = false;
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("fromName").addNode("nomenclaturalMicroReference").addConstraintViolation();
		                //remove duplicate violation as it does not give more information
						constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors.differentNomenclaturalReference.message}").addNode("toName").addNode("nomenclaturalMicroReference").addConstraintViolation();
					}
				}
			}

		}
		if(!valid){
		    constraintContext.disableDefaultConstraintViolation();
		}

		return valid;
	}
}
