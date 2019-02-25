/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.validation.annotation.ValidLectotypeSource;

/**
 * @author a.mueller
 * @since 25.02.2019
 *
 */
public class LectotypeSourceValidator implements ConstraintValidator<ValidLectotypeSource, TypeDesignationBase<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ValidLectotypeSource constraintAnnotation) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(TypeDesignationBase<?> typeDesignation, ConstraintValidatorContext context) {
        boolean isValid;
        //no citation
        if(typeDesignation.getCitation() == null && StringUtils.isBlank(typeDesignation.getCitationMicroReference())){
            isValid = true;
        //no status
        }else if (typeDesignation.getTypeStatus() == null){
            isValid = false;
        }else{
            isValid = typeDesignation.isLectoType();
        }
        return isValid;
    }

}
