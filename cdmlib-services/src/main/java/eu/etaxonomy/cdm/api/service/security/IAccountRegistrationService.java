/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import javax.mail.internet.AddressException;

import org.springframework.mail.MailException;
import org.springframework.util.concurrent.ListenableFuture;

import eu.etaxonomy.cdm.api.security.AccountCreationRequest;

/**
 * @author a.kohlbecker
 * @since Nov 18, 2021
 */
public interface IAccountRegistrationService extends IRateLimitedService {

    public static final int RATE_LIMTER_TIMEOUT_SECONDS = 2;

    public static final double PERMITS_PER_SECOND = 0.3;

    /**
     * Create a {@link AccountCreationRequest} token and send it to the user via
     * email.
     *
     * <ul>
     * <li>Hides internal processing time differences by sending the email
     * asynchronously</li>.
     * <li>Access to the method is rate limited, see {@link #RATE_LIMIT}</li>
     * </ul>
     *
     * @param emailAddress
     *            The email address to send the account creation request to
     * @param accountCreationRequestFormUrlTemplate
     *            A template string for {@code String.format()} for the URL to
     *            the form in which the user can create a new user account. The
     *            template string must contain one string placeholder {@code %s}
     *            for the request token string.
     * @return A <code>Future</code> for a <code>Boolean</code> flag. The
     *         boolean value will be <code>false</code> in case the max access
     *         rate for this method has been exceeded and a time out has
     *         occurred. Internal error states that may expose sensitive
     *         information are intentionally hidden this way (see above link to
     *         the Forgot_Password_Cheat_Sheet).
     * @throws MailException
     *             in case sending the email has failed
     * @throws AddressException
     *             in case the <code>emailAddress</code> in not valid
     * @throws AccountSelfManagementException
     *             in case the user name is already being used.
     */
    public ListenableFuture<Boolean> emailAccountRegistrationRequest(String emailAddress,
            String passwordRequestFormUrlTemplate)
            throws MailException, AddressException, AccountSelfManagementException;

    /**
     *
     * @param token
     *            the token string
     * @param userName
     *            The user name (login name) for the new account
     * @param password
     *            The password
     * @param givenName
     *            The new password to set - <b>required</b>
     * @param familyName
     *            The family name - optional, can be left empty
     * @param prefix
     *            The family name - optional, can be left empty
     * @return A <code>Future</code> for a <code>Boolean</code> flag. The
     *         boolean value will be <code>false</code> in case the max access
     *         rate for this method has been exceeded and a time out has
     *         occurred.
     * @throws AccountSelfManagementException
     *             in case an invalid token has been used
     * @throws MailException
     *             in case sending the email has failed
     * @throws AddressException
     *             in case the <code>emailAddress</code> stored in the
     *             {@link AccountCreationRequest} identified by the
     *             <code>token</code> not valid
     */
    ListenableFuture<Boolean> createUserAccount(String token, String userName, String password, String givenName,
            String familyName, String prefix) throws MailException, AccountSelfManagementException, AddressException;

    boolean userNameExists(String userName);

    boolean emailAddressExists(String emailAddress);

}
