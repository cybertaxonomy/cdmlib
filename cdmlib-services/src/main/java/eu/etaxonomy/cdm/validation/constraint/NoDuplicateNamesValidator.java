/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation.constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

public class NoDuplicateNamesValidator implements
		ConstraintValidator<NoDuplicateNames,TaxonNameBase<?,?>> {

	private static Set<String> includeProperties;

	static {
		includeProperties = new HashSet<String>();
		includeProperties.add("genusOrUninomial");
		includeProperties.add("infraGenericEpithet");
		includeProperties.add("specificEpithet");
		includeProperties.add("infraSpecificEpithet");
		includeProperties.add("rank");
		includeProperties.add("nomenclaturalReference");
		includeProperties.add("nomenclaturalMicroReference");
		includeProperties.add("basionymAuthorship");
		includeProperties.add("exBasionymAuthorship");
		includeProperties.add("combinationAuthorship");
		includeProperties.add("exCombinationAuthorship");
	}

	private INameService nameService;

	@Autowired
	public void setNameService(INameService nameService) {
		this.nameService = nameService;
	}

	@Override
	public void initialize(NoDuplicateNames noDuplicateNames) { }

    @Override
	public boolean isValid(TaxonNameBase<?,?> name, ConstraintValidatorContext constraintContext) {
		if(name == null) {
			return true;
		} else {
			List<TaxonNameBase> matchingNonViralNames = nameService.list(name, includeProperties, null, null, null, null);
			if(matchingNonViralNames.size() > 0) {
				if(matchingNonViralNames.size() == 1 && matchingNonViralNames.get(0).equals(name)) {
					return true;
				} else {
			        return false;
				}
			} else {
				return true;
			}
		}
	}
}
