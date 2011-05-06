/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.Iterator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author j.koch
 */
@Service
public class SecurityServiceImpl implements ISecurityService {

    public String[] getRoles() {
        Collection<GrantedAuthority> col = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String[] roles = new String[col.size()];
        int i = 0;
        for (Iterator<GrantedAuthority> it = col.iterator(); it.hasNext();) {
            GrantedAuthority grantedAuthority = it.next();
            roles[i] = grantedAuthority.getAuthority();
            i++;
        }
        return roles;
    }
}
