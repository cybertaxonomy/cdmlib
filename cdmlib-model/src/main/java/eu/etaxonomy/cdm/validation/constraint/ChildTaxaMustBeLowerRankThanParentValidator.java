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
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent;

public class ChildTaxaMustBeLowerRankThanParentValidator implements
		ConstraintValidator<ChildTaxaMustBeLowerRankThanParent, TaxonNode> {

	@Override
    public void initialize(ChildTaxaMustBeLowerRankThanParent childTaxaMustBeLowerRankThanParent) { }

	@Override
    public boolean isValid(TaxonNode taxonNode, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		Taxon parent = taxonNode.getParent().getTaxon();
		Taxon child = taxonNode.getTaxon();


		if(parent.getName().getRank().equals(child.getName().getRank()) || parent.getName().getRank().isLower(child.getName().getRank())) {
			valid = false;
			constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent.message}").addNode("fromTaxon").addNode("name").addNode("rank").addConstraintViolation();
		}

		return valid;
	}
}
