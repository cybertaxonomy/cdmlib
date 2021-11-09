/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import java.time.Duration;

import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author a.kohlbecker
 * @since Nov 8, 2021
 */
public interface IPasswordResetService {

    /**
     * Create a request token and send it to the user via email.
     *
     * Must conform to the recommendations of <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html">
     * https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html</a>
     *
     * <ul>
     * <li>Hides internal processing time differences by sending the email
     * asynchronously</li>
     * <li>Access to the method is rate limited, see {@link #RATE_LIMIT}</li>
     * </ul>
     *
     * @param userNameOrEmail
     *            The user name or email address of the user requesting for a
     *            password reset.
     * @param passwordRequestFormUrlTemplate
     *            A template string for {@code String.format()} for the URL to
     *            the request form in which the user can enter the new password.
     *            The template string must contain one string placeholder
     *            {@code %s} for the request token string.
     * @return A <code>Future</code> for a <code>Boolean</code> flag. The
     *         boolean value will be <code>false</code> in case the max access
     *         rate for this method has been exceeded and a time out has
     *         occurred. Other internal error states are intentionally hidden to
     *         avoid leaking of information on the existence of users (see above
     *         link to the Forgot_Password_Cheat_Sheet).
     */
    ListenableFuture<Boolean> emailResetToken(String userNameOrEmail, String passwordRequestFormUrlTemplate);

    /**
     *
     * @param token
     *            the token string
     * @param newPassword
     *            The new password to set
     * @return A <code>Future</code> for a <code>Boolean</code> flag. The
     *         boolean value will be <code>false</code> in case the max access
     *         rate for this method has been exceeded and a time out has
     *         occurred. Other internal error states are intentionally hidden to
     *         avoid leaking of information on the existence of users (see above
     *         link to the Forgot_Password_Cheat_Sheet).
     * @throws PasswordResetException
     */
    ListenableFuture<Boolean> resetPassword(String token, String newPassword) throws PasswordResetException;


    void setRateLimiterTimeout(Duration timeout);


    Duration getRateLimiterTimeout();

}