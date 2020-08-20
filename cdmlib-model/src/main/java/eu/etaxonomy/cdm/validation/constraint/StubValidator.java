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

import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

/**
 * Stub validatior for use when a constraint uses cdmlib-services component
 * (and therfore the implementation requires components that are not visible
 * in the cdmlib-model package)
 *
 * To resolve this circular dependency, use this stub as the validator in the
 * annotation, then substitute an implementation using an XML config file.
 *
 * @author ben.clark
 */
public class StubValidator implements
		ConstraintValidator<NoDuplicateNames,Object> {   //TODO: NoDuplicateNames was java.lang.annotation.Annotation before upgrading to hibernate-validation 6.1.5 (#9204), it did throw an exception in test.
                                                         //The current solution is probably not correct but solved running tests. Needs fixed once this is really used.
                                                         //see also https://stackoverflow.com/questions/50979808/using-hibernate-6-0-10-validation-results-in-constraintdefinitionexception-from/51754361 and https://stackoverflow.com/questions/55833885/spring-migrate-2-0-2-to-2-1-4-hibernate-validator-error

	@Override
    public void initialize(NoDuplicateNames annotation) { }   //see above for use of NoDuplicateNames

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
