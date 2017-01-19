/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 *
 * <b>NOTE</b> on nested @Configuration classes:
 *
 * When bootstrapping such an arrangement, only the outer class need be registered against the application context.
 * By virtue of being a nested @Configuration class, DatabaseConfig will be registered automatically. This avoids
 * the need to use an @Import annotation when the relationship between AppConfig DatabaseConfig is already implicitly
 * clear.
 *
 * @author a.kohlbecker
 * @date Oct 6, 2016
 *
 */
@EnableWebSecurity
@Import(OAuth2ServerConfiguration.class)
public class MultiWebSecurityConfiguration {

    public static final String MANAGE_CLIENT = "MANAGE_CLIENT";

    public static final String ROLE_MANAGE_CLIENT = "ROLE_" + MANAGE_CLIENT;

    private static final String MANAGING_USERS_PROPERTIES = "managing-users.properties";

    /**
     * Check for full authentication for remoting services
     * @author a.kohlbecker
     * @date Oct 6, 2016
     *
     */
    @Configuration
    @Order(2)
    public static class RemotingWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
              // @formatter:off
                http
             .anonymous().disable()
             .antMatcher("/remoting/**")
                 .authorizeRequests().anyRequest().fullyAuthenticated()
                 .and()
             .csrf().disable()
             .httpBasic();
             // @formatter:on
        }
    }

    /**
     * Require full authentication on the OAuth2 authorization service
     * so that the user is requested to provide his credentials.
     *
     * @author a.kohlbecker
     * @date Jan 16, 2017
     *
     */
    @Configuration
    @Order(1)
    public static class LoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
              // @formatter:off
                http
             .anonymous().disable()
             .antMatcher("/oauth/authorize")
                 .authorizeRequests().anyRequest().fullyAuthenticated()
                 .and()
             .csrf().disable()
             .httpBasic();
             // @formatter:on
        }
    }

    /**
     * Allow anonymous authentication for all other services.
     *
     * <b>NOTE:</b> Further access restrictions are defined
     * in the OAuth2ServerConfiguration.
     *
     * @author a.kohlbecker
     * @date Oct 6, 2016
     *
     */
    @Configuration
    public static class DefaultWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
              // @formatter:off
                http
             .anonymous().and()
             .antMatcher("/**")
             .csrf().disable()
             .httpBasic();
             // @formatter:on
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {

        // add the DaoAuthenticationProvider which is defined in
        // /cdmlib-services/src/main/resources/eu/etaxonomy/cdm/services_security.xml
        // if not added here it will not be added to the context as long as we are doing the
        // configuration explicitly here.
        auth.authenticationProvider(daoAuthenticationProvider);

        // Add an inMemoryUserManager to  enable access to the global ROLE_MANAGE_CLIENTs.
        // This is the casue for the need to do the configuration explicitly.
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthConf = auth.inMemoryAuthentication();
        File managingUsersFile = new File(CdmUtils.getCdmHomeDir(), MANAGING_USERS_PROPERTIES);
        if(!managingUsersFile.exists()){
            makeManagingUsersPropertiesFile(managingUsersFile);
        }
        Properties users = new Properties();
        users.load(new FileInputStream(managingUsersFile));
        for(Object userName : users.keySet()){
            inMemoryAuthConf.withUser(userName.toString()).password(users.get(userName).toString()).roles(MANAGE_CLIENT);
        }
    }

    /**
     * @param globalManagementClients
     * @throws IOException
     */
    private void makeManagingUsersPropertiesFile(File propertiesFile) throws IOException {
        propertiesFile.createNewFile();
        FileUtils.write(
                propertiesFile,
                "# Managing users properties file\n"
                + "#\n"
                + "# This file has been autogenerated by the cdmlib.\n"
                + "# In case the file is deleted the cdmlib will re-create it during the next start up.\n"
                + "#\n"
                + "# This is a java properties file to populate the InMemoryUserDetailsManager in any of \n"
                + "# the cdm-remote instances with special global management users which are granted to \n"
                + "# access special web services. Among these are the /manage/ web services and those\n"
                + "# triggering long running tasks. For more details please refer to\n"
                + "# https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/CdmAuthorisationAndAccessControl\n"
                + "# \n"
                + "# Global management users have the role " + ROLE_MANAGE_CLIENT + ".\n"
                + "# and will be available in each of the cdm-remote instances.\n"
                + "# Changes made to this file are applied after restarting a cdm instance.\n"
                + "#\n"
                + "# This properties file should contain entries in the form\n"
                + "#    username=password\n"
                + "# -------------------------------------------------------------------------------------------\n"
                + "#\n"
                );
        }
}
