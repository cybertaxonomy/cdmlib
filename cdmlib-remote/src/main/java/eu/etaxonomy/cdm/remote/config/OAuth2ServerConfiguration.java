/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import eu.etaxonomy.cdm.remote.oauth2.CdmUserApprovalHandler;

@Configuration
public class OAuth2ServerConfiguration {

    private static final String CDM_RESOURCE_ID = "cdm";

    private static final String ACCEXPR_MANAGE_CLIENT =
            "#oauth2.clientHasRole('ROLE_CLIENT') "
          + "or (!#oauth2.isOAuth() and ( "
          + "      hasRole('ROLE_ADMIN') or hasRole('" + MultiWebSecurityConfiguration.ROLE_MANAGE_CLIENT + "')"
          + "   )"
          + ")";

    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(CDM_RESOURCE_ID).stateless(false);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                // Since we want the protected resources to be accessible in the UI as well we need
                // session creation to be allowed (it's disabled by default in 2.0.6)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
                .authorizeRequests()
                    // see
                    // - http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#el-access
                    //      or
                    //   org.springframework.security.access.expression.SecurityExpressionRoot
                    // - org.springframework.security.oauth2.provider.expression.OAuth2SecurityExpressionMethods
                    .antMatchers(HttpMethod.OPTIONS, "/manage/**").permitAll() // see #6393
                    .antMatchers("/manage/**").access(ACCEXPR_MANAGE_CLIENT)
                    .antMatchers("/**description/accumulateDistributions").access(ACCEXPR_MANAGE_CLIENT)
                    .antMatchers("/user/me").access("isAuthenticated()")
                    .regexMatchers("/user/.*|/user\\..*").access("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")

                    // ------ DELVELOPER SNIPPETS ------
                    // experiments with classification controller
                    //.regexMatchers("/classification/.*|/classification\\..*")
                            //.access("#oauth2.hasScope('trust')")
                            //.access("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
                            //.access("#oauth2.clientHasRole('ROLE_CLIENT') or (!#oauth2.isOAuth() and hasAnyRole('ROLE_ADMIN', 'ROLE_USER'))")
                    //
                    // .regexMatchers(HttpMethod.DELETE, "/oauth/users/([^/].*?)/tokens/.*")
                    //     .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('write')")
                    // .regexMatchers(HttpMethod.GET, "/oauth/clients/([^/].*?)/users/.*")
                    //     .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('read')")
                    // .regexMatchers(HttpMethod.GET, "/oauth/clients/.*")
                    //     .access("#oauth2.clientHasRole('ROLE_CLIENT') and #oauth2.isClient() and #oauth2.hasScope('read')");
                    // ---------------------------
            .and().httpBasic();
            // @formatter:on
        }

    }

    /**
     * @author a.kohlbecker
     * @since Oct 6, 2016
     *
     */
    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        private static final String CLIENT_ID = "any-client";

        public static final Logger logger = Logger.getLogger(AuthorizationServerConfiguration.class);

        @Autowired
        private UserApprovalHandler userApprovalHandler;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Bean
        public TokenStore tokenStore() {
            return new InMemoryTokenStore();
        }


        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

            InMemoryClientDetailsServiceBuilder builder = clients.inMemory();

            // @formatter:off
            /*
             * Client for 'implicit grant'
             */
            builder.withClient(CLIENT_ID)
            //.resourceIds(RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "refresh_token", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write", "trust")
            .secret("secret") // secret for login of the client into /oauth/token
            .autoApprove("read");
            // @formatter:on

        }


        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(tokenStore()).userApprovalHandler(userApprovalHandler)
                    .authenticationManager(authenticationManager);
        }

    }


   protected static class CommonBeans {

        @Autowired
        private ClientDetailsService clientDetailsService;


        @Bean
        public ApprovalStore approvalStore() {
            return new InMemoryApprovalStore();
        }

        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        public CdmUserApprovalHandler userApprovalHandler() throws Exception {
            CdmUserApprovalHandler handler = new CdmUserApprovalHandler();
            handler.setApprovalStore(approvalStore());
            handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
            handler.setClientDetailsService(clientDetailsService);
            handler.setUseApprovalStore(false);
            return handler;
        }
    }
}
