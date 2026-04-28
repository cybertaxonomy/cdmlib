/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringBeanByName;

import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Nov 5, 2021
 */
@Transactional(TransactionMode.DISABLED)
public class PasswordResetTokenStoreTest extends CdmTransactionalIntegrationTest {

    private static final String USER_EMAIL = "dummy@cybertaxonomy.test";
    private static final String USER_PWD = "dummy123";
    private static final String USER_NAME = "dummy";

    @SpringBeanByName
    private IAbstractRequestTokenStore<PasswordResetRequest, User> passwordResetTokenStore;

    private User testUser;

    @Before
    public void reset() {
        passwordResetTokenStore.setTokenLifetimeMinutes(IAbstractRequestTokenStore.TOKEN_LIFETIME_MINUTES_DEFAULT);
        testUser = User.NewInstance(USER_NAME, USER_PWD);
        testUser.setEmailAddress(USER_EMAIL);
    }

    @Test
    public void testTokenStillValid() {
        String token = passwordResetTokenStore.create(USER_EMAIL, testUser).getToken();
        assertTrue(passwordResetTokenStore.isEligibleToken(token));
        Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findRequest(token);
        assertTrue(resetRequest.isPresent());
        assertEquals(USER_NAME, resetRequest.get().getUserName());
        assertEquals(USER_EMAIL, resetRequest.get().getUserEmail());
        assertEquals(token, resetRequest.get().getToken());
    }

    @Test
    public void testTokenExpired() {
        passwordResetTokenStore.setTokenLifetimeMinutes(-10);
        String token = passwordResetTokenStore.create(USER_EMAIL, testUser).getToken();
        assertFalse(passwordResetTokenStore.isEligibleToken(token));
        Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findRequest(token);
        assertTrue(!resetRequest.isPresent());
    }

    @Test
    public void testTokenUnknown() {
        String unknownToken = "un-known-token";
        assertFalse(passwordResetTokenStore.isEligibleToken(unknownToken));
        Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findRequest(unknownToken);
        assertTrue(!resetRequest.isPresent());
    }

    @Test
    public void testTokenNull() {
        String nullToken = null;
        assertFalse(passwordResetTokenStore.isEligibleToken(nullToken));
        Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findRequest(nullToken);
        assertTrue(!resetRequest.isPresent());
    }


    @Test
    public void testTokenRemove() {
        String token = passwordResetTokenStore.create(USER_EMAIL, testUser).getToken();
        assertTrue(passwordResetTokenStore.isEligibleToken(token));
        Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findRequest(token);
        assertTrue(resetRequest.isPresent());
        passwordResetTokenStore.remove(token);
        resetRequest = passwordResetTokenStore.findRequest(token);
        assertFalse("Expecing false since the token has been removed", resetRequest.isPresent());
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // NO DATA NEEDED
    }



}
