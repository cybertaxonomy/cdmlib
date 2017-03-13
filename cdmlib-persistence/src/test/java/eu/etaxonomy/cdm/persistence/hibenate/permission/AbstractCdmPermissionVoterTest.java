/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;

/**
 * @author a.kohlbecker
 * @date Feb 2, 2017
 *
 */
abstract public class AbstractCdmPermissionVoterTest extends Assert {

    /**
     * @param e
     * @return
     */
    protected Authentication authentication(CdmAuthority ... authorities) {

        List<GrantedAuthority> ga = new ArrayList<>();

        for(CdmAuthority a : authorities){
            ga.add(a);
        }

        Authentication auth = new TestingAuthenticationToken(
                User.NewInstance("Tester", "secret"),
                null,
                ga);
        return auth;
    }

}
