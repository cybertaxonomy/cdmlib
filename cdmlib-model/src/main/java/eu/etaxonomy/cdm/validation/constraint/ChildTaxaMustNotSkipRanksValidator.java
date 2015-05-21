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
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks;

public class ChildTaxaMustNotSkipRanksValidator implements
		ConstraintValidator<ChildTaxaMustNotSkipRanks, TaxonNode> {

	@Override
    public void initialize(ChildTaxaMustNotSkipRanks childTaxaMustNotSkipRanks) { }

	@Override
    public boolean isValid(TaxonNode taxonNode, ConstraintValidatorContext constraintContext) {
		boolean valid = true;

	    try{
	        Taxon parent = taxonNode.getParent() == null ? null : taxonNode.getParent().getTaxon();
	        Rank parentRank = parent == null ? null : parent.getNullSafeRank();
	        Rank childRank = taxonNode.getNullSafeRank();

    	    if (parent != null  && parent.getName() != null && childRank != null ) {
                if(parent.getName().isSupraGeneric() && childRank.isLower(Rank.GENUS())) {
        			valid = false;
        			constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks.cannotSkipGenus.message}").addNode("fromTaxon").addNode("name").addNode("rank").addConstraintViolation();
        		} else if(parentRank != null && parentRank.isHigher(Rank.SPECIES())
        		        && childRank.isLower(Rank.SPECIES())) {
        			valid = false;
        			constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks.cannotSkipSpecies.message}").addNode("fromTaxon").addNode("name").addNode("rank").addConstraintViolation();
        		}
                if (!valid){
                    constraintContext.disableDefaultConstraintViolation();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		return valid;
	}
}
