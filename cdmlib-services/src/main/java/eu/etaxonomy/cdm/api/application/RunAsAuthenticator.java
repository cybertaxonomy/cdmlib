/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * Helper class to work around the apparently broken @RunAs("ROLE_ADMIN")
 * in spring see: https://jira.springsource.org/browse/SEC-1671
 *
 * @author a.kohlbecker
 * @since Jul 24, 2017
 *
 */
public class RunAsAuthenticator {

    public static final Logger logger = Logger.getLogger(FirstDataInserter.class);

    /**
     * must match the key in eu/etaxonomy/cdm/services_security.xml
     */
    private static final String RUN_AS_KEY = "TtlCx3pgKC4l";

    // not to be autowired, since the FirstdataInserter must be usable without security
    private AuthenticationProvider runAsAuthenticationProvider = null;


    private Authentication authentication;


    /**
    * needed to work around the broken @RunAs("ROLE_ADMIN") which seems to be
    * broken in spring see: https://jira.springsource.org/browse/SEC-1671
     * @param ga
    */
   public void runAsAuthentication(GrantedAuthority ga) {
       if(runAsAuthenticationProvider == null){
           logger.debug("no RunAsAuthenticationProvider set, skipping run-as authentication");
           return;
       }

       SecurityContext securityContext = SecurityContextHolder.getContext();
       authentication = securityContext.getAuthentication();


       Collection<GrantedAuthority> rules = new ArrayList<GrantedAuthority>();
       rules.add(ga);
       RunAsUserToken adminToken = new RunAsUserToken(
               RUN_AS_KEY,
               "system-admin",
               null,
               rules,
               (authentication != null ? authentication.getClass() : AnonymousAuthenticationToken.class));

       Authentication runAsAuthentication = runAsAuthenticationProvider.authenticate(adminToken);
       SecurityContextHolder.getContext().setAuthentication(runAsAuthentication);

       logger.debug("switched to run-as authentication: " + runAsAuthentication);
   }

   /**
    * needed to work around the broken @RunAs("ROLE_ADMIN") which
    * seems to be broken in spring see: https://jira.springsource.org/browse/SEC-1671
    */
   public void restoreAuthentication() {
       if(runAsAuthenticationProvider == null){
           logger.debug("no RunAsAuthenticationProvider set, thus nothing to restore");
       }
       SecurityContext securityContext = SecurityContextHolder.getContext();
       securityContext.setAuthentication(authentication);
       logger.debug("last authentication restored: " + (authentication != null ? authentication : "NULL"));
   }

   /**
    * @return the runAsAuthenticationProvider
    */
   public AuthenticationProvider getRunAsAuthenticationProvider() {
       return runAsAuthenticationProvider;
   }

   /**
    * @param runAsAuthenticationProvider the runAsAuthenticationProvider to set
    */
   public void setRunAsAuthenticationProvider(AuthenticationProvider runAsAuthenticationProvider) {
       this.runAsAuthenticationProvider = runAsAuthenticationProvider;
   }

}
