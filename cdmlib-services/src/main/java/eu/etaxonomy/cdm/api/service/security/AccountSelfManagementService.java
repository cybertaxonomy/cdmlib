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
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.text.StringSubstitutor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.util.concurrent.RateLimiter;

import eu.etaxonomy.cdm.api.config.CdmConfigurationKeys;
import eu.etaxonomy.cdm.api.config.SendEmailConfigurer;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;

/**
 * @author a.kohlbecker
 * @since Nov 18, 2021
 */
public abstract class AccountSelfManagementService implements IRateLimitedService {

    protected static Logger logger = Logger.getLogger(PasswordResetRequest.class);

    public static final int RATE_LIMTER_TIMEOUT_SECONDS = 2;

    public static final double PERMITS_PER_SECOND = 0.3;

    @Autowired
    protected IUserDao userDao;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected JavaMailSender emailSender;

    @Autowired
    protected Environment env;

    private Duration rateLimiterTimeout = null;

    protected RateLimiter emailResetToken_rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);

    protected RateLimiter resetPassword_rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);

    /**
     * Uses the {@link StringSubstitutor} as simple template engine.
     * Below named values are automatically resolved, more can be added via the
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
    public void sendEmail(String userEmail, String userName, String subjectTemplate, String bodyTemplate, Map<String, String> additionalValuesMap) throws MailException {

        String from = env.getProperty(SendEmailConfigurer.FROM_ADDRESS);
        String dataSourceBeanId = env.getProperty(CdmConfigurationKeys.CDM_DATA_SOURCE_ID);
        String supportEmailAddress = env.getProperty(CdmConfigurationKeys.MAIL_ADDRESS_SUPPORT);
        if(additionalValuesMap == null) {
            additionalValuesMap = new HashedMap<>();
        }
        if(supportEmailAddress != null) {
            additionalValuesMap.put("supportEmailAddress", supportEmailAddress);
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

    @Override
    public Duration getRateLimiterTimeout() {
        if(rateLimiterTimeout == null) {
            rateLimiterTimeout = Duration.ofSeconds(RATE_LIMTER_TIMEOUT_SECONDS);
        }
        return rateLimiterTimeout;
    }


    @Override
    public void setRateLimiterTimeout(Duration timeout) {
        this.rateLimiterTimeout = timeout;
    }


    @Override
    public void setRate(double rate) {
        resetPassword_rateLimiter.setRate(rate);
        emailResetToken_rateLimiter.setRate(rate);
    }

}