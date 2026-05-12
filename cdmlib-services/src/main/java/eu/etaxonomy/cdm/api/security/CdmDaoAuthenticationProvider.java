/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.util.DigestUtils;

/**
 * A specialized {@link DaoAuthenticationProvider} which handles existing
 * md5 passwords and transforms them to bCrypt passwords during first login.
 *
 * @author muellera
 * @since 07.05.2026
 */
public class CdmDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    private UserDetailsPasswordService passwordService;

    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        String storedPassword = userDetails.getPassword();

        if (storedPassword == null || !storedPassword.startsWith("{md5}")) {
            logger.warn("No md5 pwd");
            //no md5
            super.additionalAuthenticationChecks(userDetails, authentication);
            return;
        }else {
            logger.warn("Update MD5 encoded pwd: " + storedPassword);
            // Legacy MD5-validation
            if (authentication.getCredentials() == null) {
                this.logger.debug("Failed to authenticate since no credentials provided");
                throw new BadCredentialsException(this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }

            String presentedPassword = authentication.getCredentials().toString();
            String salt = userDetails.getUsername();

            String saltedPassword = presentedPassword + "{" + salt + "}";
            String md5Hash = DigestUtils.md5DigestAsHex(
                    saltedPassword.getBytes(StandardCharsets.UTF_8)
                    );

            if (!md5Hash.equalsIgnoreCase(userDetails.getPassword()
                    .replace("{md5}", ""))) {
                throw new BadCredentialsException("Bad credentials");
            }

            // password correct – now upgrade to bCrypt
            if (passwordService != null) {
                String newEncodedPassword = getPasswordEncoder().encode(presentedPassword);
                passwordService.updatePassword(userDetails, newEncodedPassword);
            }

            return;
        }
    }
}