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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.text.StringSubstitutor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.common.util.concurrent.RateLimiter;

import eu.etaxonomy.cdm.api.config.CdmConfigurationKeys;
import eu.etaxonomy.cdm.api.config.SendEmailConfigurer;
import eu.etaxonomy.cdm.api.security.IPasswordResetTokenStore;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
@Service
@Transactional(readOnly = true)
public class PasswordResetService implements IPasswordResetService {

    private static final int RATE_LIMTER_TIMEOUT_SECONDS = 2;

    private static final double PERMITS_PER_SECOND = 0.3;

    private static Logger logger = Logger.getLogger(PasswordResetRequest.class);

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IUserService userService;

    @Autowired
    private IPasswordResetTokenStore passwordResetTokenStore;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    Environment env;

    RateLimiter emailResetToken_rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);
    RateLimiter resetPassword_rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);

    public static final String RESET_REQUEST_EMAIL_SUBJECT_TEMPLATE = "Your password reset request for ${userName}";
    public static final String RESET_REQUEST_EMAIL_BODY_TEMPLATE = "You are receiving this email because a password reset was requested for your account at the ${dataBase}"
            + " data base. If this was not initiated by you, please ignore this message."
            + ".\n Please click ${linkUrl} to reset your password";

    public static final String RESET_SUCCESS_EMAIL_SUBJECT_TEMPLATE = "Your password for ${userName} has been changed";
    public static final String RESET_SUCCESS_EMAIL_BODY_TEMPLATE = "The password of your account at the ${dataBase} data base has just been changed."
            + "If this was not initiated by you, please contact the adminitrator as soon as possible.";

    public static final String RESET_FAILED_EMAIL_SUBJECT_TEMPLATE = "Changing your password for ${userName} has failed";
    public static final String RESET_FAILED_EMAIL_BODY_TEMPLATE = "The attempt to change the password of your account at the ${dataBase} data base has failed."
            + "If this was not initiated by you, please contact the adminitrator as soon as possible.";

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
    @Override
    @Async
    public ListenableFuture<Boolean> emailResetToken(String userNameOrEmail, String passwordRequestFormUrlTemplate) {

        if (emailResetToken_rateLimiter.tryAcquire(Duration.ofSeconds(RATE_LIMTER_TIMEOUT_SECONDS))) {

            try {
                User user = findUser(userNameOrEmail);
                PasswordResetRequest resetRequest = passwordResetTokenStore.create(user);

                String passwordRequestFormUrl = String.format(passwordRequestFormUrlTemplate, resetRequest.getToken());
                Map<String, String> additionalValues = new HashMap<>();
                additionalValues.put("linkUrl", passwordRequestFormUrl);
                sendEmail(user.getEmailAddress(), user.getUsername(), RESET_REQUEST_EMAIL_SUBJECT_TEMPLATE, RESET_REQUEST_EMAIL_BODY_TEMPLATE,
                        additionalValues);
                logger.info("Password reset request for  " + userNameOrEmail + " has been send");
            } catch (UsernameNotFoundException e) {
                logger.warn("Password reset request for unknown user, cause: " + e.getMessage());
            }
            return new AsyncResult<Boolean>(true);
        } else {
            return new AsyncResult<Boolean>(false);
        }
    }

    /**
     * Uses the {@link StringSubstitutor} as simple template engine.
     * Below named values are automatically resoved, more can be added via the
     * <code>valuesMap</code> parameter.
     *
     * @param userEmail
     *  The TO-address
     * @param userName
     *  Used to set the value for <code>${userName}</code>
     * @param subjectTemplate
     *  A {@link StringSubstitutor} template for the email subject
     * @param bodyTemplate
     *  A {@link StringSubstitutor} template for the email body
     * @param additionalValuesMap
     *  Additional named values for to be replaced in the template strings.
     */
    public void sendEmail(String userEmail, String userName, String subjectTemplate, String bodyTemplate, Map<String, String> additionalValuesMap) {

        String from = env.getProperty(SendEmailConfigurer.FROM_ADDRESS);
        String dataSourceBeanId = env.getProperty(CdmConfigurationKeys.CDM_DATA_SOURCE_ID);
        if(additionalValuesMap == null) {
            additionalValuesMap = new HashedMap<>();
        }
        additionalValuesMap.put("userName", userName);
        additionalValuesMap.put("dataBase", dataSourceBeanId);
        StringSubstitutor substitutor = new StringSubstitutor(additionalValuesMap);

        // TODO use MimeMessages for better email layout?
        // TODO user Thymeleaf instead for HTML support?
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(userEmail);

        message.setSubject(substitutor.replace(subjectTemplate));
        message.setText(substitutor.replace(bodyTemplate));

        emailSender.send(message);
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
    @Override
    @Async
    public ListenableFuture<Boolean> resetPassword(String token, String newPassword) throws PasswordResetException {

        if (resetPassword_rateLimiter.tryAcquire(Duration.ofSeconds(RATE_LIMTER_TIMEOUT_SECONDS))) {

            Optional<PasswordResetRequest> resetRequest = passwordResetTokenStore.findResetRequest(token);
            if (resetRequest.isPresent()) {
                try {
                    userService.changePasswordForUser(resetRequest.get().getUserName(), newPassword);
                    sendEmail(resetRequest.get().getUserEmail(), resetRequest.get().getUserName(), RESET_SUCCESS_EMAIL_SUBJECT_TEMPLATE, RESET_SUCCESS_EMAIL_BODY_TEMPLATE, null);
                    return new AsyncResult<Boolean>(true);
                } catch (DataAccessException | UsernameNotFoundException e) {
                    logger.error("Failed to change password of User " + resetRequest.get().getUserName(), e);
                    sendEmail(resetRequest.get().getUserEmail(), resetRequest.get().getUserName(), RESET_FAILED_EMAIL_SUBJECT_TEMPLATE, RESET_FAILED_EMAIL_BODY_TEMPLATE, null);
                }
            } else {
                throw new PasswordResetException("Invalid password reset token");
            }
        }
        return new AsyncResult<Boolean>(false);
    }

}