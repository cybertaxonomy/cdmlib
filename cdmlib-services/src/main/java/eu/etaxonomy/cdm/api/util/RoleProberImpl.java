/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.persistence.permission.Role;

/**
 * @author a.kohlbecker
 * @since Jul 25, 2018
 */
public class RoleProberImpl implements IRoleProber {

    private Role role;

    public RoleProberImpl(Role role){
        this.role = role;
    }

    @Override
    public boolean checkForRole(Authentication authentication) {
        if(authentication != null) {
            return authentication.getAuthorities().stream().anyMatch(a -> {
                return a.equals(role);
            });
        }
        return false;
    }
}