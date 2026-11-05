/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.validation.constraint.ValidPasswordValidator;
import jakarta.validation.ConstraintViolation;

/**
 * See https://dev.e-taxonomy.eu/redmine/issues/9862
 *
 * @author a.mueller
 * @date 21.01.2022
 */
public class ValidPasswordTest  extends ValidationTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private User user;

    private static final String valid = "Aa345678";

    @BeforeEach
    public void setUp() {

    }

/****************** TESTS *****************************/

    @Test
    public final void testValidPassword() {

        user = User.NewInstance("testuser", valid);

        Set<ConstraintViolation<User>> constraintViolations  = validator.validate(user, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as the 'valid' password fullfils all requirements");

        user.setPassword(StringUtils.leftPad(valid, 256, 'a'));
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as up to 256 characters are allowed");

    }

    @Test
    public final void testNotValidPassword() {
        user = User.NewInstance("testuser", null);

        validateHasConstraint(user, ValidPasswordValidator.class, Level2.class);

        user.setPassword("");
        validateHasConstraint(user, ValidPasswordValidator.class, Level2.class);

        user.setPassword("A");
        validateHasConstraint(user, ValidPasswordValidator.class, Level2.class);
        user.setPassword(valid.substring(0, 7));
        validateHasConstraint(user, ValidPasswordValidator.class, Level2.class);

        user.setPassword(StringUtils.leftPad(valid, 257, 'a'));
        validateHasConstraint(user, ValidPasswordValidator.class, Level2.class);


   }

}
