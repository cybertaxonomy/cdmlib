/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.validation.constraint;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.validation.annotation.TaxonNameCannotBeAcceptedAndSynonym;

public class TaxonNameCannotBeAcceptedAndSynonymValidator implements
		ConstraintValidator<TaxonNameCannotBeAcceptedAndSynonym, TaxonBase> {

	public void initialize(TaxonNameCannotBeAcceptedAndSynonym taxonNameCannotBeAcceptedAndSynonym) { }

	public boolean isValid(TaxonBase taxon, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if(Hibernate.isInitialized(taxon.getName()) && Hibernate.isInitialized(taxon.getName().getTaxonBases())) {
			Set<TaxonBase> taxa = taxon.getName().getTaxonBases();

			for(TaxonBase t1 : taxa) {
				for(TaxonBase t2 : taxa) { // pairwise comparison of all taxa sharing the same name
					if(!t1.equals(t2)) { // exclude self comparison
						if(Hibernate.isInitialized(t1.getSec()) && Hibernate.isInitialized(t2.getSec())) { // Check that the sec property is initialized
							if(t1.getSec().equals(t2.getSec())) { // only compare concepts belonging to the same source
								TaxonBase taxonBase1 = TaxonBase.deproxy(t1, TaxonBase.class);
								TaxonBase taxonBase2 = TaxonBase.deproxy(t2, TaxonBase.class);
								
								if(taxonBase1 instanceof Taxon && taxonBase2 instanceof Taxon) {
									valid = false;
									constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.TaxonNameCannotBeAcceptedAndSynonym.twoAcceptedTaxaNotAllowed.message}").addNode("name").addConstraintViolation();
								}
								if((taxonBase1 instanceof Taxon && taxonBase2 instanceof Synonym) || (taxonBase1 instanceof Synonym && taxonBase2 instanceof Taxon)) {
									valid = false;
									constraintContext.buildConstraintViolationWithTemplate("{eu.etaxonomy.cdm.validation.annotation.TaxonNameCannotBeAcceptedAndSynonym.synonymAndTaxonNotAllowed.message}").addNode("name").addConstraintViolation();
								}								
							}
						}
					}
				}
			}
		}
		return valid;		
	}
}
