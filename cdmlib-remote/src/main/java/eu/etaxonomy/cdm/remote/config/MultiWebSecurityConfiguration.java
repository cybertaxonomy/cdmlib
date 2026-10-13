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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import eu.etaxonomy.cdm.config.ConfigFileUtil;

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
 * @since Oct 6, 2016
 */
@EnableWebSecurity
@Import(OAuth2ServerConfiguration.class)
@Configuration
public class MultiWebSecurityConfiguration {

    public static final String MANAGE_CLIENT = "MANAGE_CLIENT";

    public static final String ROLE_MANAGE_CLIENT = "ROLE_" + MANAGE_CLIENT;

    private static final String MANAGING_USERS_PROPERTIES = "managing-users.properties";

    @Autowired
    private ConfigFileUtil configFileUtil;

    /**
     * Require full authentication on the OAuth2 authorization service
     * so that the user is requested to provide his credentials.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) throws Exception {
        // AntPathRequestMatcher: avoid MvcRequestMatcher (needs HandlerMappingIntrospector in servlet context)
        http
            .securityMatcher(AntPathRequestMatcher.antMatcher("/oauth/authorize"))
            .authorizeHttpRequests(auth -> auth.anyRequest().fullyAuthenticated())
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .anonymous(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Check for full authentication for remoting services
     */
    @Bean
    @Order(2)
    public SecurityFilterChain remotingSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(AntPathRequestMatcher.antMatcher("/remoting/**"))
            .authorizeHttpRequests(auth -> auth.anyRequest()
                    .hasAnyRole("ADMIN", "PROJECT_MANAGER", "REMOTING"))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .anonymous(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Allow anonymous authentication for all other services.
     *
     * <b>NOTE:</b> Further access restrictions are defined
     * in the OAuth2ServerConfiguration.
     */
    @Bean
    @Order(4)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(AntPathRequestMatcher.antMatcher("/**"))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
            DaoAuthenticationProvider daoAuthenticationProvider,
            PasswordEncoder passwordEncoder) throws Exception {

        // add the DaoAuthenticationProvider which is defined in
        // /cdmlib-services/src/main/resources/eu/etaxonomy/cdm/services_security.xml
        auth.authenticationProvider(daoAuthenticationProvider);

        File managingUsersFile = new File(configFileUtil.perUserCdmFolder(), MANAGING_USERS_PROPERTIES);
        if (!managingUsersFile.exists()) {
            makeManagingUsersPropertiesFile(managingUsersFile);
        }
        Properties users = new Properties();
        users.load(new FileInputStream(managingUsersFile));
        List<UserDetails> userDetails = new ArrayList<>();
        for (Object userName : users.keySet()) {
            String password = users.get(userName).toString();
            if (!password.startsWith("{")) {
                password = "{noop}" + password;
            }
            userDetails.add(User.withUsername(userName.toString())
                    .password(password)
                    .roles(MANAGE_CLIENT)
                    .build());
        }
        DaoAuthenticationProvider inMemoryProvider =
                new DaoAuthenticationProvider(new InMemoryUserDetailsManager(userDetails));
        inMemoryProvider.setPasswordEncoder(passwordEncoder);
        auth.authenticationProvider(inMemoryProvider);
    }

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
                + "#\n",
                Charset.defaultCharset()
                );
        }
}
