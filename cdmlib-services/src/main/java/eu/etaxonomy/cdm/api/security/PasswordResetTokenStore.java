/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.kohlbecker
 * @since Nov 3, 2021
 */
@Component
public class PasswordResetTokenStore extends AbstractRequestTokenStore<PasswordResetRequest, User> {

    @Override
    public PasswordResetRequest createNewToken(String userEmailAddress, User user, String randomToken, int tokenLifetimeMinutes) {
        PasswordResetRequest token = new PasswordResetRequest(user.getUsername(), userEmailAddress, randomToken, tokenLifetimeMinutes);
        return token;
    }
}
