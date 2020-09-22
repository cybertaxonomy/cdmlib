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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

public class NoDuplicateNamesValidator implements
		ConstraintValidator<NoDuplicateNames,TaxonName> {

	private static Set<String> includeProperties;

	static {
		includeProperties = new HashSet<>();
		includeProperties.add("genusOrUninomial");
		includeProperties.add("infraGenericEpithet");
		includeProperties.add("specificEpithet");
		includeProperties.add("infraSpecificEpithet");
		includeProperties.add("rank");
//		includeProperties.add("nomenclaturalSource.citation");   //handled in method now since #6581
//		includeProperties.add("nomenclaturalSource.citationMicroReference"); //handled in method now since #6581
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
	public boolean isValid(TaxonName name, ConstraintValidatorContext constraintContext) {
		if(name == null) {
			return true;
		} else {
			List<TaxonName> matchingNames = nameService.list(name, includeProperties, null, null, null, null);
			matchingNames = matchingNames.stream().filter(existing->
			           Objects.equals(existing.getNomenclaturalReference(),name.getNomenclaturalReference())
			        && Objects.equals(existing.getNomenclaturalMicroReference(), name.getNomenclaturalMicroReference())
			        && !existing.equals(name)
			    ).collect(Collectors.toList());

			if(matchingNames.size() > 0) {
			    if(matchingNames.size() == 1 && matchingNames.get(0).equals(name)) {
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
