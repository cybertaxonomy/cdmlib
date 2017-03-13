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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent;

public class ChildTaxaMustDeriveNameFromParentValidator implements
		ConstraintValidator<ChildTaxaMustDeriveNameFromParent, TaxonNode> {

	@Override
    public void initialize(ChildTaxaMustDeriveNameFromParent childTaxaMustDeriveNameFromParent) { }

	@Override
    public boolean isValid(TaxonNode taxonNode, ConstraintValidatorContext constraintContext) {
		boolean valid = true;

   try{
		Taxon parent = taxonNode.getParent() == null ? null : taxonNode.getParent().getTaxon();
        Taxon child = taxonNode.getTaxon();

        if (parent != null && child != null && parent.getName() != null && child.getName() != null){
            TaxonNameBase<?,?> parentNameBase = CdmBase.deproxy(parent.getName(), TaxonNameBase.class);
            TaxonNameBase<?,?> childNameBase = CdmBase.deproxy(child.getName(), TaxonNameBase.class);
            if(parentNameBase instanceof NonViralName && childNameBase instanceof NonViralName) {
                INonViralName parentName = parentNameBase;
                INonViralName childName = childNameBase;

                if(childName.isSpecies() || childName.isInfraSpecific()) {
                    if(! CdmUtils.nullSafeEqual(parentName.getGenusOrUninomial(), childName.getGenusOrUninomial())) {
                        valid = false;
                        constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent.message}").addNode("fromTaxon").addNode("name").addNode("genusOrUninomial").addConstraintViolation();
                    }
                    if(parentName.isSpecies() || parentName.isInfraSpecific()) {
                        if(! CdmUtils.nullSafeEqual(parentName.getSpecificEpithet(), childName.getSpecificEpithet())) {
                            valid = false;
                            constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent.message}").addNode("fromTaxon").addNode("name").addNode("specificEpithet").addConstraintViolation();
                        }
                    }
                    if (! valid){
                        constraintContext.disableDefaultConstraintViolation();
                    }
                }
            }
		}
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

		return valid;
	}
}
