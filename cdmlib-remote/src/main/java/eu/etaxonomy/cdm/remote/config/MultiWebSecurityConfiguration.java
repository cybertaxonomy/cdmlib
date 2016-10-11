/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
     * Allow anonymous authentication for all other services
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
             .antMatcher("/**").authorizeRequests().anyRequest().hasAnyRole("ANONYMOUS", "USER", "ADMIN")
                .and()
             .csrf()
             .disable()
             .httpBasic();
             // @formatter:on
        }
    }

}
