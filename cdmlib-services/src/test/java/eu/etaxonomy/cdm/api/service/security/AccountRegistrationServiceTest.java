/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.concurrent.ListenableFuture;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.security.AbstractRequestTokenStore;
import eu.etaxonomy.cdm.api.security.AccountCreationRequest;
import eu.etaxonomy.cdm.api.security.IAbstractRequestTokenStore;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;


public class AccountRegistrationServiceTest extends eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest {

    private static final double maxRequestRate = 4.0;

    Logger logger = Logger.getLogger(AccountRegistrationServiceTest.class);

    private static final int rateLimiterTimeout = 200;
    private static final String userName = "pwdResetTestUser";
    private static final String userPWD = "super_SECURE_123";
    private static final String userEmail = "pwdResetTestUser@cybertaxonomy.test";


    private static String base64UrlSaveCharClass = "[a-zA-Z0-9\\-_]";


    private static final String requestFormUrlTemplate = "http://cybertaxonomy.test/passwordReset?userName={%s}&sessID=f8d8sf8dsf";

    @SpringBeanByType
    private IUserService userService;

    @SpringBeanByType
    private IAccountRegistrationService accountRegistrationService;

    @SpringBeanByName
    private IAbstractRequestTokenStore<AccountCreationRequest> accountCreationRequestTokenStore;

    @SpringBeanByType
    private JavaMailSender emailSender;

    @Autowired
    private Environment env;

    private Wiser wiser = null;

    private CountDownLatch createRequestTokenSendSignal;
    private CountDownLatch accountCreatedSignal;
    Throwable assyncError = null;

    @Before
    public void startEmailServer() {
        // Integer smtpPort = env.getProperty(SendEmailConfigurer.PORT, Integer.class);
        wiser = new Wiser();
        wiser.setPort(2500); // must be the same as configured for SendEmailConfigurer.PORT
        wiser.start();
        logger.debug("Wiser email server started");
    }


    @Before
    public void accountRegistrationService() throws InterruptedException {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(PasswordResetRequest.class).setLevel(Level.TRACE);
        // speed up testing
        accountRegistrationService.setRateLimiterTimeout(Duration.ofMillis(rateLimiterTimeout));
        accountRegistrationService.setRate(maxRequestRate);
        // pause long enough to avoid conflicts
        long sleepTime = Math.round(1000 / maxRequestRate) + rateLimiterTimeout;
        Thread.sleep(sleepTime);
    }

    @Before
    public void resetAsyncVars() {
        assyncError = null;
        createRequestTokenSendSignal = null;
        accountCreatedSignal = null;
    }

    @After
    public void stopEmailServer() {
        wiser.stop();
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testSuccessfulEmailReset() throws Throwable {

        logger.debug("testSuccessfulEmailReset() ...");

        // printDataSet(System.err, "UserAccount");

        createRequestTokenSendSignal = new CountDownLatch(1);
        accountCreatedSignal = new CountDownLatch(1);

        ListenableFuture<Boolean> emailResetFuture = accountRegistrationService.emailAccountRegistrationRequest(userEmail, userName, userPWD, requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    createRequestTokenSendSignal.countDown();
                }, futureException -> {
                    assyncError = futureException;
                    createRequestTokenSendSignal.countDown();
                });

        // -- wait for passwordResetService.emailResetToken() to complete
        createRequestTokenSendSignal.await();

        if(assyncError != null) {
            throw assyncError;
        }

        assertNotNull(emailResetFuture.get());
        assertEquals(1, wiser.getMessages().size());

        // -- read email message
        WiserMessage requestMessage = wiser.getMessages().get(0);
        MimeMessage requestMimeMessage = requestMessage.getMimeMessage();

        assertTrue(requestMimeMessage.getSubject()
                .matches(UserAccountEmailTemplates.REGISTRATION_REQUEST_EMAIL_SUBJECT_TEMPLATE.replace("${dataBase}", ".*"))
                );

        String messageContent = requestMimeMessage.getContent().toString();
        // -- extract token
        Pattern pattern = Pattern.compile("=\\{(" + base64UrlSaveCharClass + "+)\\}");
        Matcher m = pattern.matcher(messageContent);
        assertTrue(m.find());
        assertEquals(AbstractRequestTokenStore.TOKEN_LENGTH + 17, m.group(1).length());

        // -- change password
        ListenableFuture<Boolean> createAccountFuture = accountRegistrationService.createUserAccount(m.group(1), "Testor", "Nutzer", "Dr.");
        createAccountFuture.addCallback(requestSuccessVal -> {
            accountCreatedSignal.countDown();
        }, futureException -> {
            assyncError =  futureException;
            accountCreatedSignal.countDown();
        });
        // -- wait for passwordResetService.resetPassword to complete
        accountCreatedSignal.await();

        assertTrue(createAccountFuture.get());
        assertEquals(2, wiser.getMessages().size());
        WiserMessage successMessage = wiser.getMessages().get(1);
        MimeMessage successMimeMessage = successMessage.getMimeMessage();
        assertEquals(UserAccountEmailTemplates.REGISTRATION_SUCCESS_EMAIL_SUBJECT_TEMPLATE.replace("${userName}", userName), successMimeMessage.getSubject());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void emailResetToken_ivalidEmailAddress() throws Throwable {

        logger.debug("emailResetToken_ivalidEmailAddress() ...");

        createRequestTokenSendSignal = new CountDownLatch(1);

        accountRegistrationService.setRateLimiterTimeout(Duration.ofMillis(1)); // as should as possible to allow the fist call to be successful (with 1ns the fist call fails!)
        ListenableFuture<Boolean> emailResetFuture = accountRegistrationService.emailAccountRegistrationRequest("not-a-valid-email@#address#", userName, userPWD, requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    createRequestTokenSendSignal.countDown();
                }, futureException -> {
                    assyncError = futureException;
                    createRequestTokenSendSignal.countDown();
                });


        // -- wait for passwordResetService.emailResetToken() to complete
        createRequestTokenSendSignal.await();

        assertNotNull(assyncError);
        assertEquals(AddressException.class, assyncError.getClass());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testInvalidToken() throws Throwable {

        logger.debug("testInvalidToken() ...");

        accountCreatedSignal = new CountDownLatch(1);

        // -- change password
        ListenableFuture<Boolean> resetPasswordFuture = accountRegistrationService.createUserAccount("IUER9843URIO--INVALID-TOKEN--UWEUR89EUWWEOIR", userName, null, null);
        resetPasswordFuture.addCallback(requestSuccessVal -> {
            accountCreatedSignal.countDown();
        }, futureException -> {
            assyncError =  futureException;
            accountCreatedSignal.countDown();
        });
        // -- wait for passwordResetService.resetPassword to complete
        accountCreatedSignal.await();

        assertNotNull(assyncError);
        assertEquals(AccountSelfManagementException.class, assyncError.getClass());
        assertEquals(0, wiser.getMessages().size());
    }

    // @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testUserNameExists() throws Throwable {

        logger.debug("testUserNameExists() ...");

        createRequestTokenSendSignal = new CountDownLatch(1);

        ListenableFuture<Boolean> emailResetFuture = accountRegistrationService.emailAccountRegistrationRequest(userEmail, "admin", userPWD, requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    createRequestTokenSendSignal.countDown();
                }, futureException -> {
                    assyncError = futureException;
                    createRequestTokenSendSignal.countDown();
                });

        // -- wait for passwordResetService.emailResetToken() to complete
        createRequestTokenSendSignal.await();

        assertNotNull(assyncError);
        assertEquals(AccountSelfManagementException.class, assyncError.getClass());
        assertEquals(AccountRegistrationService.USER_NAME_EXISTS_MSG, assyncError.getMessage());
        assertEquals(0, wiser.getMessages().size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // not needed
    }

}
