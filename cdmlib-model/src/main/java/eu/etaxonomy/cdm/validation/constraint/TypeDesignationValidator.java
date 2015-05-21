package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.validation.annotation.ValidTypeDesignation;

public class TypeDesignationValidator implements ConstraintValidator<ValidTypeDesignation, TypeDesignationBase<?>>{

    @Override
    public void initialize(ValidTypeDesignation constraintAnnotation) {}

	@Override
    public boolean isValid(TypeDesignationBase<?> value, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;

		if (value.getTypifiedNames().isEmpty()){
		    return false;
		}
		if (value.isNotDesignated()){
		    return true;
		} else if (value.isInstanceOf(SpecimenTypeDesignation.class)){
		    SpecimenTypeDesignation specDesig = CdmBase.deproxy(value, SpecimenTypeDesignation.class);
		    if (specDesig.getTypeSpecimen() == null){
		        return false;
		    }
		}else if (value.isInstanceOf(NameTypeDesignation.class)){
		    NameTypeDesignation nameDesig = CdmBase.deproxy(value, NameTypeDesignation.class);
            if (nameDesig.getTypeName() == null){
                return false;
            }
        }

		return isValid;
	}
}
