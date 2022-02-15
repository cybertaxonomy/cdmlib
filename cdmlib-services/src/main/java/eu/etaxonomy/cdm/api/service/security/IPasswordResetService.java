/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import org.springframework.mail.MailException;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author a.kohlbecker
 * @since Nov 8, 2021
 */
public interface IPasswordResetService extends IRateLimitedService {

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
     *         occurred. Internal error states that may expose sensitive
     *         information are intentionally hidden this way (see above link to
     *         the Forgot_Password_Cheat_Sheet).
     * @throws MailException
     *             in case sending the email has failed
     * @throws EmailAddressNotFoundException
     *             in case the provided email address is unknown
     */
    ListenableFuture<Boolean> emailResetToken(String userNameOrEmail, String passwordRequestFormUrlTemplate)
            throws MailException, EmailAddressNotFoundException;

    /**
     *
     * @param token
     *            the token string
     * @param newPassword
     *            The new password to set
     * @return A <code>Future</code> for a <code>Boolean</code> flag. The
     *         boolean value will be <code>false</code> in case the max access
     *         rate for this method has been exceeded and a time out has
     *         occurred.
     * @throws AccountSelfManagementException
     *             in case an invalid token has been used
     * @throws MailException
     *             in case sending the email has failed
     */
    ListenableFuture<Boolean> resetPassword(String token, String newPassword) throws AccountSelfManagementException;

}