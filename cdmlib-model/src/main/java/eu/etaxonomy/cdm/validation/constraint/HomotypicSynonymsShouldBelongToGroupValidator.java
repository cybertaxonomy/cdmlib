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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup;

public class HomotypicSynonymsShouldBelongToGroupValidator implements
		ConstraintValidator<HomotypicSynonymsShouldBelongToGroup, SynonymRelationship> {

	private static final Logger logger = Logger.getLogger(HomotypicSynonymsShouldBelongToGroupValidator.class);
	public void initialize(HomotypicSynonymsShouldBelongToGroup homotypicSynonymsShouldBelongToGroup) { }

	public boolean isValid(SynonymRelationship synonymRelationship, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(synonymRelationship.getType().equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			Taxon accepted = synonymRelationship.getAcceptedTaxon();
			Synonym synonym = synonymRelationship.getSynonym();
			
			
			if(!accepted.getName().getHomotypicalGroup().equals(synonym.getName().getHomotypicalGroup())) {
				valid = false;
				if(logger.isDebugEnabled()) {
					logger.debug(accepted.getName().getHomotypicalGroup() + ", the homotypic group of " + accepted.getName() + " is not the same as " + synonym.getName().getHomotypicalGroup() + " the homotypical group of " + synonym.getName());
				}
				constraintContext.buildErrorWithMessageTemplate("{eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup.message}").addSubNode("type").addError();				
			}
		}
		
		return valid;		
	}
}
