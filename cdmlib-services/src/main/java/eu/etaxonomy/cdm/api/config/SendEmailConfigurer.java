/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * This class replaces the {@code org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration} which can not
 * yet be used in this project.
 * <p>
 * The configurations keys contained in here explicitly are not placed in {@link CdmConfigurationKeys} since this class here
 * does only replace missing functionality which otherwise would be provided by the spring boot <code>MailSenderAutoConfiguration</code>.
 *
 * @author a.kohlbecker
 * @since Sep 14, 2021
 */
@Configuration
@AppConfigurationProperties
public class SendEmailConfigurer {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private Environment env;

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
    }

    @Bean
    public JavaMailSenderImpl mailSender() {

        reportMailConfiguration();
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

    private void reportMailConfiguration() {
        logger.info("+-------------------------------------------");
        logger.info("|          SendEmail Configuration            ");
        configKeys().stream().forEach(key -> logger.info("| " + key + " : " + env.getProperty(key, "[NULL]")));
        logger.info("+--------------------------------------------");

    }

    private List<String> configKeys() {
        List<String> configKeys = new ArrayList<>(Arrays.asList(HOST, PORT, USERNAME, PASSWORD, DISABLED, FROM_ADDRESS, INT_TEST_SERVER));
        configKeys.addAll(Arrays.asList(SMTP_PROPERTY_KEYS));
        return configKeys;
    }

}
