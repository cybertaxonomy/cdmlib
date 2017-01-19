/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;

/**
 * Class providing some base functionality for validator testing
 * @author a.mueller
 * @date 18.02.2015
 *
 */
public abstract class ValidationTestBase {

    protected Validator validator;

    {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }


    protected void validateHasConstraint(Object cdmBase, Class validatorClass, Class group) {
        Set<ConstraintViolation<Object>> constraintViolations  = validator.validate(cdmBase, group);
        assertHasConstraintOnValidator(constraintViolations, validatorClass);
    }

    protected void validateHasNoConstraint(Object cdmBase, Class validatorClass, Class group) {
        Set<ConstraintViolation<Object>> constraintViolations  = validator.validate(cdmBase, group);
        assertNoConstraintOnValidator(constraintViolations, validatorClass);
    }

    protected void assertNoConstraintOnValidator(Set<ConstraintViolation<Object>> constraintViolations, Class validatorClass) {
        assertHasConstraintOnValidator(constraintViolations, validatorClass, false);
    }

    protected void assertHasConstraintOnValidator(Set<ConstraintViolation<Object>> constraintViolations, Class validatorClass) {
        assertHasConstraintOnValidator(constraintViolations, validatorClass, true);
    }

    /**
     * @param constraintViolations
     * @return
     */
    private void assertHasConstraintOnValidator(Set<ConstraintViolation<Object>> constraintViolations, Class validatorClass, boolean requiresViolation) {
        boolean hasViolation = false;
        for (ConstraintViolation<?> violation : constraintViolations){
            Class<?> validatedValidatorClass = violation.getConstraintDescriptor().getConstraintValidatorClasses().iterator().next();
            if (validatedValidatorClass.equals(validatorClass)){
                hasViolation = true;
            }
        }
        if (! hasViolation  && requiresViolation){
            Assert.fail("constraint violations are missing an validator class " + validatorClass.getSimpleName());
        }else if (hasViolation  && ! requiresViolation){
            Assert.fail("constraint violations should not exist for validator class " + validatorClass.getSimpleName());
        }
        return;
    }
}
