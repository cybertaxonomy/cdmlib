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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.permission.IGroupDao;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
@Service
@Transactional(readOnly = true)
public class AccountRegistrationService
        extends AccountSelfManagementService
        implements IAccountRegistrationService {

    private static final Logger logger = LogManager.getLogger();

    private static final String EMAIL_EXISTS = "An account for this email address already exits.";

    //not private as it is currently used in test
    static final String USER_NAME_EXISTS_MSG = "This user name is already being used by someone else.";

    @Autowired
    protected IGroupDao groupDao;

    @Autowired
    @Qualifier("accountCreationRequestTokenStore")
    private IAbstractRequestTokenStore<AccountCreationRequest, Object> accountRegistrationTokenStore;

    @Override
    @Async
    public ListenableFuture<Boolean> emailAccountRegistrationRequest(String emailAddress, String accountCreationRequestFormUrlTemplate) throws MailException, AddressException, AccountSelfManagementException {

        if(logger.isTraceEnabled()) {
            logger.trace("emailAccountRegistrationConfirmation() trying to aquire from rate limiter [rate: " + emailResetToken_rateLimiter.getRate() + ", timeout: " + getRateLimiterTimeout().toMillis() + "ms]");
        }
        if (emailResetToken_rateLimiter.tryAcquire(getRateLimiterTimeout())) {
            logger.trace("emailAccountRegistrationConfirmation() allowed by rate limiter");
            try {
                emailAddressValidAndUnused(emailAddress);
                AbstractRequestToken resetRequest = accountRegistrationTokenStore.create(emailAddress, null);
                String passwordRequestFormUrl = String.format(accountCreationRequestFormUrlTemplate, resetRequest.getToken());
                Map<String, String> additionalValues = new HashMap<>();
                additionalValues.put("linkUrl", passwordRequestFormUrl);
                sendEmail(emailAddress, null,
                        UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_SUBJECT_TEMPLATE,
                        UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_BODY_TEMPLATE, additionalValues);
                logger.info("An account creation request has been send to " + emailAddress);
                return new AsyncResult<>(true);
            } catch (MailException e) {
                throw e;
            }
        } else {
            logger.trace("blocked by rate limiter");
            return new AsyncResult<>(false);
        }
    }

    @Override
    @Async
    @Transactional(readOnly = false)
    public ListenableFuture<Boolean> createUserAccount(String token, String userName, String password,
            String givenName, String familyName, String prefix)
            throws MailException, AccountSelfManagementException, AddressException {

        if (resetPassword_rateLimiter.tryAcquire(getRateLimiterTimeout())) {

            Optional<AccountCreationRequest> creationRequest = accountRegistrationTokenStore.findRequest(token);
            if (creationRequest.isPresent()) {
                try {
                    User newUser = User.NewInstance(userName, password);
                    //email
                    String emailAddress = creationRequest.get().getUserEmail();
                    if (CdmUtils.isNotBlank(emailAddress)) {
                        // check again if the email address is still unused
                        emailAddressValidAndUnused(emailAddress);
                        newUser.setEmailAddress(emailAddress);
                    }
                    //username + pwd
                    if(userNameExists(userName)) {
                        throw new AccountSelfManagementException(USER_NAME_EXISTS_MSG);
                    }
                    userService.encodeUserPassword(newUser, password);

                    //person
                    //String givenName, String familyName, String prefix
                    if (! CdmUtils.areBlank(emailAddress, familyName, prefix)) {
                        Person person = Person.NewInstance(null, familyName, null, givenName);
                        person.setPrefix(CdmUtils.Nb(prefix));
                        newUser.setPerson(person);
                    }

                    //group, #10116
                    //for Phycobank only (preliminary, should be handled in Phycobank explicitly)
                    Group submitterGroup = groupDao.findGroupByName(Group.GROUP_SUBMITTER);
                    if (submitterGroup != null) {
                        submitterGroup.addMember(newUser);
                    }

                    //save
                    userDao.saveOrUpdate(newUser);

                    accountRegistrationTokenStore.remove(token);
                    sendEmail(creationRequest.get().getUserEmail(), userName,
                            UserAccountEmailTemplates.REGISTRATION_SUCCESS_EMAIL_SUBJECT_TEMPLATE,
                            UserAccountEmailTemplates.REGISTRATION_SUCCESS_EMAIL_BODY_TEMPLATE, null);

                    return new AsyncResult<Boolean>(true);

                } catch (DataAccessException e) {
                    String message = "Failed to create a new user [userName: " + userName + ", email: " + creationRequest.get().getUserEmail() + "]";
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