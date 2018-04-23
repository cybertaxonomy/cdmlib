/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This voter always returns {@link #ACCESS_GRANTED}.
 * It is needed as default voter when using the {@link UnanimousBased}
 * @author andreas kohlbecker
 \* @since Sep 4, 2012
 *
 */
public class GrantAlwaysVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#vote(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection)
     */
    @Override
    public int vote(Authentication authentication, CdmBase object, Collection<ConfigAttribute> attributes) {
        return ACCESS_GRANTED;
    }

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return CdmBase.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        return false;
    }

}
