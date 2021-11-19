/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import eu.etaxonomy.cdm.api.security.AbstractRequestToken;
import eu.etaxonomy.cdm.api.security.AccountCreationRequest;
import eu.etaxonomy.cdm.api.security.IAbstractRequestTokenStore;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
@Service
@Transactional(readOnly = true)
public class AccountRegistrationService extends AccountSelfManagementService implements IAccountRegistrationService {


    protected static final String EMAIL_EXISTS = "An account for this email address already exits.";

    protected static final String USER_NAME_EXISTS_MSG = "This user name is already being used by someone else.";

    private static Logger logger = Logger.getLogger(PasswordResetRequest.class);

    @Autowired
    @Qualifier("accountCreationRequestTokenStore")
    private IAbstractRequestTokenStore<AccountCreationRequest> accountRegistrationTokenStore;

    @Override
    @Async
    public ListenableFuture<Boolean> emailAccountRegistrationRequest(String emailAddress,
            String userName, String password, String accountCreationRequestFormUrlTemplate) throws MailException, AddressException, AccountSelfManagementException {

        if(logger.isTraceEnabled()) {
            logger.trace("emailAccountRegistrationConfirmation() trying to aquire from rate limiter [rate: " + emailResetToken_rateLimiter.getRate() + ", timeout: " + getRateLimiterTimeout().toMillis() + "ms]");
        }
        if (emailResetToken_rateLimiter.tryAcquire(getRateLimiterTimeout())) {
            logger.trace("emailAccountRegistrationConfirmation() allowed by rate limiter");
            try {
                emailAddressValidAndUnused(emailAddress);
                if(userNameExists(userName)) {
                    throw new AccountSelfManagementException(USER_NAME_EXISTS_MSG);
                }
                User user = User.NewInstance(userName, password);
                user.setEmailAddress(emailAddress);
                AbstractRequestToken resetRequest = accountRegistrationTokenStore.create(user);
                String passwordRequestFormUrl = String.format(accountCreationRequestFormUrlTemplate, resetRequest.getToken());
                Map<String, String> additionalValues = new HashMap<>();
                additionalValues.put("linkUrl", passwordRequestFormUrl);
                sendEmail(user.getEmailAddress(), user.getUsername(),
                        UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_SUBJECT_TEMPLATE,
                        UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_BODY_TEMPLATE, additionalValues);
                logger.info("An account creartion request has been send to "
                        + user.getEmailAddress());
                return new AsyncResult<Boolean>(true);
            } catch (MailException e) {
                throw e;
            }
        } else {
            logger.trace("blocked by rate limiter");
            return new AsyncResult<Boolean>(false);
        }
    }

    @Override
    @Async
    @Transactional(readOnly = false)
    public ListenableFuture<Boolean> createUserAccount(String token, String givenName, String familyName, String prefix)
            throws MailException, AccountSelfManagementException, AddressException {

        if (resetPassword_rateLimiter.tryAcquire(getRateLimiterTimeout())) {

            Optional<AccountCreationRequest> creationRequest = accountRegistrationTokenStore.findResetRequest(token);
            if (creationRequest.isPresent()) {
                try {
                    // check again if the email address is still unused
                    emailAddressValidAndUnused(creationRequest.get().getUserEmail());
                    if(userNameExists(creationRequest.get().getUserName())) {
                        throw new AccountSelfManagementException(USER_NAME_EXISTS_MSG);
                    }
                    User newUser = User.NewInstance(creationRequest.get().getUserName(), creationRequest.get().getEncryptedPassword());
                    userDao.saveOrUpdate(newUser);
                    accountRegistrationTokenStore.remove(token);
                    sendEmail(creationRequest.get().getUserEmail(), creationRequest.get().getUserName(),
                            UserAccountEmailTemplates.REGISTRATION_SUCCESS_EMAIL_SUBJECT_TEMPLATE,
                            UserAccountEmailTemplates.REGISTRATION_SUCCESS_EMAIL_BODY_TEMPLATE, null);
                    return new AsyncResult<Boolean>(true);
                } catch (DataAccessException e) {
                    String message = "Failed to create a new user [userName: " + creationRequest.get().getUserName() + ", email: " + creationRequest.get().getUserEmail() + "]";
                    logger.error(message, e);
                    throw new AccountSelfManagementException(message);
                }
            } else {
                throw new AccountSelfManagementException("Invalid account creation token");
            }
        }
        return new AsyncResult<Boolean>(false);
    }

    /**
     * Throws exceptions in case of any problems, returns silently in case
     * everything is OK.
     *
     * @param userNameOrEmail
     * @throws AddressException
     *             in case the <code>emailAddress</code> is invalid
     * @throws EmailAddressAlreadyInUseException
     *             in case the <code>emailAddress</code> is in use
     */
    protected void emailAddressValidAndUnused(String emailAddress)
            throws AddressException, EmailAddressAlreadyInUseException {
        InternetAddress emailAddr = new InternetAddress(emailAddress);
        emailAddr.validate();
        if (emailAddressExists(emailAddr.toString())) {
            throw new EmailAddressAlreadyInUseException(EMAIL_EXISTS);
        }
    }

    @Override
    public boolean emailAddressExists(String emailAddress) {
        return userDao.emailAddressExists(emailAddress);
    }

    @Override
    public boolean userNameExists(String userName) {
        return userDao.userNameExists(userName);
    }

}