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
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author a.kohlbecker
 * @date Oct 6, 2016
 *
 */
@EnableWebSecurity
public class MultiWebSecurityConfiguration {

    /**
     * Check for full authentication for remoting services
     * @author a.kohlbecker
     * @date Oct 6, 2016
     *
     */
    @Configuration
    @Order(1)
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
