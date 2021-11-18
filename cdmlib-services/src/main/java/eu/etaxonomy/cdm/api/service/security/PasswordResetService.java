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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;

import eu.etaxonomy.cdm.api.security.AbstractRequestToken;
import eu.etaxonomy.cdm.api.security.IAbstractRequestTokenStore;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
@Service
@Transactional(readOnly = false)
public class PasswordResetService extends AccountSelfManagementService implements IPasswordResetService {

    @Autowired
    @Qualifier("passwordResetTokenStore")
    IAbstractRequestTokenStore<PasswordResetRequest> passwordResetTokenStore;

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
     *         occurred. Internal error states that may
     *         expose sensitive information are intentionally hidden this way
     *         (see above link to the Forgot_Password_Cheat_Sheet).
     * @throws MailException
     *             in case sending the email has failed
     */
    @Override
    @Async
    public ListenableFuture<Boolean> emailResetToken(String userNameOrEmail, String passwordRequestFormUrlTemplate) throws MailException {

        if(logger.isTraceEnabled()) {
            logger.trace("emailResetToken trying to aquire from rate limiter [rate: " + emailResetToken_rateLimiter.getRate() + ", timeout: " + getRateLimiterTimeout().toMillis() + "ms]");
        }
        if (emailResetToken_rateLimiter.tryAcquire(getRateLimiterTimeout())) {
            logger.trace("emailResetToken allowed by rate limiter");
            try {
                User user = findUser(userNameOrEmail);
                AbstractRequestToken resetRequest = passwordResetTokenStore.create(user);
                String passwordRequestFormUrl = String.format(passwordRequestFormUrlTemplate, resetRequest.getToken());
                Map<String, String> additionalValues = new HashMap<>();
                additionalValues.put("linkUrl", passwordRequestFormUrl);
                sendEmail(user.getEmailAddress(), user.getUsername(),
                        UserAccountEmailTemplates.RESET_REQUEST_EMAIL_SUBJECT_TEMPLATE,
                        UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_BODY_TEMPLATE, additionalValues);
                logger.info("A password reset request for  " + user.getUsername() + " has been send to "
                        + user.getEmailAddress());
            } catch (UsernameNotFoundException e) {
                logger.warn("Password reset request for unknown user, cause: " + e.getMessage());
            } catch (MailException e) {
                throw e;
            }
            return new AsyncResult<Boolean>(true);
        } else {
            logger.trace("blocked by rate limiter");
            return new AsyncResult<Boolean>(false);
        }
    }

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
   @Override
   @Async
   public ListenableFuture<Boolean> resetPassword(String token, String newPassword) throws AccountSelfManagementException, MailException {

       if (resetPassword_rateLimiter.tryAcquire(getRateLimiterTimeout())) {

           Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findResetRequest(token);
           if (resetRequest.isPresent()) {
               try {
                   UserDetails user = userService.loadUserByUsername(resetRequest.get().getUserName());
                   Assert.isAssignable(user.getClass(), User.class);
                   userService.encodeUserPassword((User)user, newPassword);
                   userDao.saveOrUpdate((User)user);
                   passwordResetTokenStore.remove(token);
                   sendEmail(resetRequest.get().getUserEmail(), resetRequest.get().getUserName(),
                           UserAccountEmailTemplates.RESET_SUCCESS_EMAIL_SUBJECT_TEMPLATE,
                           UserAccountEmailTemplates.RESET_SUCCESS_EMAIL_BODY_TEMPLATE, null);
                   return new AsyncResult<Boolean>(true);
               } catch (DataAccessException | IllegalArgumentException | UsernameNotFoundException e) {
                   logger.error("Failed to change password of User " + resetRequest.get().getUserName(), e);
                   sendEmail(resetRequest.get().getUserEmail(), resetRequest.get().getUserName(),
                           UserAccountEmailTemplates.RESET_FAILED_EMAIL_SUBJECT_TEMPLATE,
                           UserAccountEmailTemplates.RESET_FAILED_EMAIL_BODY_TEMPLATE, null);
               }
           } else {
               throw new AccountSelfManagementException("Invalid password reset token");
           }
       }
       return new AsyncResult<Boolean>(false);
   }

    /**
     *
     * @param userNameOrEmail
     * @return
     */
    protected User findUser(String userNameOrEmail) throws UsernameNotFoundException, EmailAddressNotFoundException {

        User user;
        try {
            InternetAddress emailAddr = new InternetAddress(userNameOrEmail);
            emailAddr.validate();
            user = userDao.findByEmailAddress(userNameOrEmail);
            if (user == null) {
                throw new EmailAddressNotFoundException(
                        "No user with the email address'" + userNameOrEmail + "' found.");
            }
        } catch (AddressException ex) {
            user = userDao.findUserByUsername(userNameOrEmail);
            if (user == null) {
                throw new UsernameNotFoundException("No user with the user name: '" + userNameOrEmail + "' found.");
            }
        }
        return user;
    }
}