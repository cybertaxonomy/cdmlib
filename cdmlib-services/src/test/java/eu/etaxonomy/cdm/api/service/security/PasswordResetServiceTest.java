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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.concurrent.ListenableFuture;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.security.IPasswordResetTokenStore;
import eu.etaxonomy.cdm.api.security.PasswordResetRequest;
import eu.etaxonomy.cdm.api.security.PasswordResetTokenStore;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Nov 8, 2021
 */
public class PasswordResetServiceTest extends eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest {

    /**
     *
     */
    private static final double maxRequestRate = 4.0;

    Logger logger = Logger.getLogger(PasswordResetServiceTest.class);

    private static final int rateLimiterTimeout = 200;
    private static final String userName = "pwdResetTestUser";
    private static final String userPWD = "super_SECURE_123";
    private static final String newPWD = "NEW_123_new_456";
    private static final String userEmail = "pwdResetTestUser@cybertaxonomy.test";


    private static String base64UrlSaveCharClass = "[a-zA-Z0-9\\-_]";


    private static final String requestFormUrlTemplate = "http://cybertaxonomy.test/passwordReset?userName={%s}&sessID=f8d8sf8dsf";

    @SpringBeanByType
    private IUserService userService;

    @SpringBeanByType
    private IPasswordResetService passwordResetService;

    @SpringBeanByType
    private IPasswordResetTokenStore passwordResetTokenStore;

    @SpringBeanByType
    private JavaMailSender emailSender;

    private Wiser wiser = null;

    private CountDownLatch resetTokenSendSignal;
    private CountDownLatch resetTokenSendSignal2;
    private CountDownLatch passwordChangedSignal;
    Throwable assyncError = null;

    @Before
    public void startEmailServer() {
        wiser = new Wiser();
        wiser.setPort(2500); // Default is 25
        wiser.start();
    }


    @Before
    public void createUser() {
        User user = User.NewInstance(userName, userPWD);
        user.setEmailAddress(userEmail);
        userService.save(user);
        commitAndStartNewTransaction();
        // printDataSet(System.err, "User");
    }

    @Before
    public void resetpasswordResetService() throws InterruptedException {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(PasswordResetRequest.class).setLevel(Level.TRACE);
        // speed up testing
        passwordResetService.setRateLimiterTimeout(Duration.ofMillis(rateLimiterTimeout));
        passwordResetService.setRate(maxRequestRate);
        // pause long enough to avoid conflicts
        long sleepTime = Math.round(1000 / maxRequestRate) + rateLimiterTimeout;
        Thread.sleep(sleepTime);
    }

    @Before
    public void resetAsyncVars() {
        assyncError = null;
        resetTokenSendSignal = null;
        resetTokenSendSignal2 = null;
        passwordChangedSignal = null;
    }

    @After
    public void removeUser() {
        userService.deleteUser(userName);
        userService.getSession().flush();
        commitAndStartNewTransaction();
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

        resetTokenSendSignal = new CountDownLatch(1);
        passwordChangedSignal = new CountDownLatch(1);

        ListenableFuture<Boolean> emailResetFuture = passwordResetService.emailResetToken(userName, requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    resetTokenSendSignal.countDown();
                }, futureException -> {
                    assyncError = futureException;
                    resetTokenSendSignal.countDown();
                });

        // -- wait for passwordResetService.emailResetToken() to complete
        resetTokenSendSignal.await();

        if(assyncError != null) {
            throw assyncError;
        }

        assertNotNull(emailResetFuture.get());
        assertEquals(1, wiser.getMessages().size());

        // -- read email message
        WiserMessage requestMessage = wiser.getMessages().get(0);
        MimeMessage requestMimeMessage = requestMessage.getMimeMessage();
        assertEquals(PasswordResetService.RESET_REQUEST_EMAIL_SUBJECT_TEMPLATE.replace("${userName}", userName), requestMimeMessage.getSubject());

        String messageContent = requestMimeMessage.getContent().toString();
        // -- extract token
        Pattern pattern = Pattern.compile("=\\{(" + base64UrlSaveCharClass + "+)\\}");
        Matcher m = pattern.matcher(messageContent);
        assertTrue(m.find());
        assertEquals(PasswordResetTokenStore.TOKEN_LENGTH + 17, m.group(1).length());

        // -- change password
        ListenableFuture<Boolean> resetPasswordFuture = passwordResetService.resetPassword( m.group(1), newPWD);
        resetPasswordFuture.addCallback(requestSuccessVal -> {
            passwordChangedSignal.countDown();
        }, futureException -> {
            assyncError =  futureException;
            passwordChangedSignal.countDown();
        });
        // -- wait for passwordResetService.resetPassword to complete
        passwordChangedSignal.await();

        assertTrue(resetPasswordFuture.get());
        assertEquals(2, wiser.getMessages().size());
        WiserMessage successMessage = wiser.getMessages().get(1);
        MimeMessage successMimeMessage = successMessage.getMimeMessage();
        assertEquals(PasswordResetService.RESET_SUCCESS_EMAIL_SUBJECT_TEMPLATE.replace("${userName}", userName), successMimeMessage.getSubject());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void emailResetTokenTimeoutTest() throws Throwable {

        // Logger.getLogger(PasswordResetRequest.class).setLevel(Level.TRACE);

        resetTokenSendSignal = new CountDownLatch(1);
        resetTokenSendSignal2 = new CountDownLatch(1);

        passwordResetService.setRate(0.1);
        passwordResetService.setRateLimiterTimeout(Duration.ofMillis(1)); // as should as possible to allow the fist call to be successful

        logger.debug("1. request");
        ListenableFuture<Boolean> emailResetFuture = passwordResetService.emailResetToken(userName, requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    logger.debug("success 1");
                    resetTokenSendSignal.countDown();
                }, futureException -> {
                    logger.debug("error 1");
                    assyncError = futureException;
                    resetTokenSendSignal.countDown();
                });

        logger.debug("2. request");
        ListenableFuture<Boolean> emailResetFuture2 = passwordResetService.emailResetToken(userName, requestFormUrlTemplate);
        emailResetFuture2.addCallback(
                requestSuccessVal -> {
                    logger.debug("success 2");
                    resetTokenSendSignal2.countDown();
                }, futureException -> {
                    logger.debug("error 2");
                    assyncError = futureException;
                    resetTokenSendSignal2.countDown();
                });

        // -- wait for passwordResetService.emailResetToken() to complete
        resetTokenSendSignal.await();
        resetTokenSendSignal2.await();

        logger.debug("all completed, testing assertions");

        if(assyncError != null) {
            throw assyncError; // an error should not have been thrown
        }
        assertTrue("First request should have been successful", emailResetFuture.get());
        assertFalse("Second request should have been rejecded", emailResetFuture2.get());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void emailResetToken_ivalidUserNameTest() throws Throwable {

        logger.debug("emailResetToken_ivalidUserNameTest() ...");

        resetTokenSendSignal = new CountDownLatch(1);

        passwordResetService.setRateLimiterTimeout(Duration.ofMillis(1)); // as should as possible to allow the fist call to be successful (with 1ns the fist call fails!)
        ListenableFuture<Boolean> emailResetFuture = passwordResetService.emailResetToken("iDoNotExist", requestFormUrlTemplate);
        emailResetFuture.addCallback(
                requestSuccessVal -> {
                    resetTokenSendSignal.countDown();
                }, futureException -> {
                    assyncError = futureException;
                    resetTokenSendSignal.countDown();
                });


        // -- wait for passwordResetService.emailResetToken() to complete
        resetTokenSendSignal.await();

        if(assyncError != null) {
            throw assyncError; // emailResetToken must be agnostic of the existence of user names
        }
        assertTrue("The request should look like succesful even in the user does not exist.", emailResetFuture.get());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testInvalidToken() throws Throwable {

        logger.debug("testInvalidToken() ...");

        passwordChangedSignal = new CountDownLatch(1);

        // -- change password
        ListenableFuture<Boolean> resetPasswordFuture = passwordResetService.resetPassword( "IUER9843URIO--INVALID-TOKEN--UWEUR89EUWWEOIR", newPWD);
        resetPasswordFuture.addCallback(requestSuccessVal -> {
            passwordChangedSignal.countDown();
        }, futureException -> {
            assyncError =  futureException;
            passwordChangedSignal.countDown();
        });
        // -- wait for passwordResetService.resetPassword to complete
        passwordChangedSignal.await();

        assertNotNull(assyncError);
        assertEquals(PasswordResetException.class, assyncError.getClass());
        assertEquals(0, wiser.getMessages().size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // not needed
    }

}
