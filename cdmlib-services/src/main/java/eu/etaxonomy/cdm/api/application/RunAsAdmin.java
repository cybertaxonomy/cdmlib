/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.persistence.permission.Role;

/**
 * @author a.kohlbecker
 * @since Oct 10, 2018
 *
 */
public class RunAsAdmin extends AbstractRunAs {

    /**
     * @param authProvider
     */
    public RunAsAdmin(AuthenticationProvider authProvider) {
        setRunAsAuthenticationProvider(authProvider);
    }

    @Override
    public GrantedAuthority runAsGrantedAuthority() {
        return Role.ROLE_ADMIN;
    }

}
