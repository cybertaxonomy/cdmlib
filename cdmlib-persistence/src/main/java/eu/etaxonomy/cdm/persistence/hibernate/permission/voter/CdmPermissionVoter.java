// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.AuthorityPermission;

/**
 * The <code>CdmPermissionVoter</code> provides access control votes for {@link CdmBase} objects.
 *
 * @author andreas kohlbecker
 * @date Sep 4, 2012
 *
 */
public abstract class CdmPermissionVoter implements AccessDecisionVoter {


    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(org.springframework.security.access.ConfigAttribute)
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        // all CdmPermissionVoter support AuthorityPermission
        return attribute instanceof AuthorityPermission;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        // all CdmPermissionVoters must support CdmBase.class
        return clazz.isInstance(CdmBase.class);
    }

}
