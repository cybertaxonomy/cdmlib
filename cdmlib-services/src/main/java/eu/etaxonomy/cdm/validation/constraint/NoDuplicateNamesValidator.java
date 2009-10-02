package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

public class NoDuplicateNamesValidator implements
		ConstraintValidator<NoDuplicateNames,NonViralName> {
	
	private INameService nameService;
	
	@Autowired
	public void setNameService(INameService nameService) {
		this.nameService = nameService;
	}

	public void initialize(NoDuplicateNames noDuplicateNames) { }

	public boolean isValid(NonViralName name, ConstraintValidatorContext constraintContext) {
		if(name == null) {
			return true;
		} else {
			Pager<TaxonNameBase> matchingNonViralNames = nameService.searchNames(name.getGenusOrUninomial(),
					                                                             name.getInfraGenericEpithet(),
					                                                             name.getSpecificEpithet(),
					                                                             name.getInfraSpecificEpithet(),
					                                                             name.getRank(),
					                                                             null,null, null, null);
			if(matchingNonViralNames.getCount() > 0) {
				if(matchingNonViralNames.getCount() == 1 && matchingNonViralNames.getRecords().get(0).equals(name)) {
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
