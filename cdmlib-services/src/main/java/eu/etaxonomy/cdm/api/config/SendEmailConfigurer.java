/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * This class replaces the {@code org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration} which can not
 * yet used in this project
 *
 * @author a.kohlbecker
 * @since Sep 14, 2021
 */
@Configuration
@AppConfigurationProperties
public class SendEmailConfigurer {

    private static final Logger logger = Logger.getLogger(SendEmailConfigurer.class);

    @Autowired
    Environment env;

    // conforms to spring boot config
    private static final String HOST = "mail.host";
    public static final String PORT = "mail.port";
    private static final String USERNAME = "mail.username";
    private static final String PASSWORD = "mail.password";
    public static final String FROM_ADDRESS = "mail.from-address";
    private static final String DEFAULT_ENCODING = "mail.default-encoding";

    private static final String[] SMTP_PROPERTY_KEYS = new String[] {"mail.smtp.auth", "mail.smtp.starttls.enable"};

    /**
     * Disables the configuration of the mail system
     * integration tests of the mail system are skipped if this property is set true
     */
    public static final String DISABLED = "mail.disabled";
    public static final String INT_TEST_SERVER = "mail.int-test-server";

    public SendEmailConfigurer() {
        System.out.print(1);
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        boolean disabled = false;
        try {
           disabled = Boolean.valueOf(env.getProperty(DISABLED));
        } catch (Exception e) {
            logger.error("Invalid  property '" + DISABLED + "=" + env.getProperty(DISABLED) + "' for JavaMailSenderImpl configuration");
        }
        if(disabled) {
            logger.warn("JavaMailSenderImpl configuration explictly disabled by property '" + DISABLED + "=true'");
            return null;
        } else {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            applyProperties(sender);
            return sender;
        }
    }

    private void applyProperties(JavaMailSenderImpl sender) {
        sender.setHost(env.getProperty(HOST));
        if (env.getProperty(PORT) != null) {
            sender.setPort(Integer.parseInt(env.getProperty(PORT)));
        }
        if (env.getProperty(USERNAME) != null) {
            sender.setUsername(env.getProperty(USERNAME));
        }
        if (env.getProperty(PASSWORD) != null) {
        sender.setPassword(env.getProperty(PASSWORD));
        }
        // sender.setProtocol(this.properties.getProtocol());
        if (env.getProperty(DEFAULT_ENCODING) != null) {
            sender.setDefaultEncoding(env.getProperty(DEFAULT_ENCODING));
        }

        Properties smtpProperties = new Properties();
        for(String key : SMTP_PROPERTY_KEYS) {
            String value = env.getProperty(key);
            if(value != null) {
                smtpProperties.put(key, value);
            }
        }
        sender.setJavaMailProperties(smtpProperties);
    }

}
