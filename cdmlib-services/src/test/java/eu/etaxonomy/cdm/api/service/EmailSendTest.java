/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.config.SendEmailConfigurer;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;


public class EmailSendTest extends CdmIntegrationTest {

    private static final Logger logger = Logger.getLogger(EmailSendTest.class);

    private static final String SUBJECT = "eu.etaxonomy.cdm.test.function.EmailSendTest";

    private static final String TO = "a.kohlbecker@bgbm.org";

    @SpringBeanByType
    private JavaMailSender emailSender;

    @SpringBeanByType
    private Environment env;

    @Test
    public void sendEmailTest() {

        boolean disabled = false;
        try {
            disabled = Boolean.valueOf(env.getProperty(SendEmailConfigurer.DISABLED));
        } catch (Exception e) {
            // ignore all
        }

        if(disabled) {
            logger.warn("sendEmailTest is disabled");
            return;
        }

        String from = env.getProperty(SendEmailConfigurer.FROM_ADDRESS);
        assertNotNull(from);
        assertTrue(from.contains("@"));

        boolean useWiser = Objects.equals(env.getProperty(SendEmailConfigurer.INT_TEST_SERVER), "wiser");
        Wiser wiser = null;
        if(useWiser) {
            // start test smtp server
            wiser = new Wiser();
            wiser.setPort(2500); // better use random port
            wiser.start();
        }

        if(!disabled) {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(TO);
            message.setSubject(SUBJECT);
            message.setText("This is a test email send from the cdmlib intergration test suite");
            emailSender.send(message);
        }

        if(useWiser) {
            for (WiserMessage message : wiser.getMessages())
            {
                String envelopeSender = message.getEnvelopeSender();
                String envelopeReceiver = message.getEnvelopeReceiver();
                String subject = null;

                assertEquals(from, envelopeSender);
                assertEquals(TO, envelopeReceiver);
                try {
                    MimeMessage mess = message.getMimeMessage();
                    subject  = mess.getSubject();
                } catch (MessagingException e) {
                    logger.error("MessagingException", e);
                }
                assertEquals(SUBJECT, subject);
            }
            wiser.stop();
        }

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
